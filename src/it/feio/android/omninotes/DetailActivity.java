package it.feio.android.omninotes;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.neopixl.pixlui.components.textview.TextView;

import it.feio.android.omninotes.models.ImageAdapter;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ParcelableNote;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.utils.BitmapDecoder;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.date.DateHelper;
import it.feio.android.omninotes.utils.date.DatePickerFragment;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.utils.date.TimePickerFragment;
import it.feio.android.omninotes.R;
import android.R.anim;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * An activity representing a single Item detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link ItemListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ItemDetailFragment}.
 */
public class DetailActivity extends BaseActivity implements OnDateSetListener,
		OnTimeSetListener {

	private final int HEIGHT_FULLSCREEN_LIMIT = 350;
	private static final int TAKE_PHOTO = 1;
	private static final int GALLERY = 2;
	
	private SherlockFragmentActivity mActivity;
	
	private Note note;
	private ImageView reminder, reminder_delete;
	private TextView datetime;
	private long alarmDateTime = -1;
	private String alarmDate, alarmTime;
	public Uri imageUri;
	private Object bitmap;
	private int photoQuality;
	private ImageAdapter mImageAdapter;
	private GridView mGridView;
	private List<Uri> imageList = new ArrayList<Uri>();
	private AlertDialog attachmentDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		mActivity = this;

		if (getWindowManager().getDefaultDisplay().getHeight() < HEIGHT_FULLSCREEN_LIMIT) {
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		}

		// Show the Up button in the action bar.
		if (getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Views initialization
		initViews();
		
		// Note initialization
		initNote();
	}

	private void initViews() {
		
		// Initialzation of gridview for images
		mImageAdapter = new ImageAdapter(mActivity, imageList);
		mGridView = (GridView) findViewById(R.id.gridview);
	    mGridView.setAdapter(mImageAdapter);
	    // Click events for images in gridview (zooms image)
	    mGridView.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            Toast.makeText(mActivity, "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });
	    // Long click events for images in gridview	(removes image)
	    mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
				alertDialogBuilder.setMessage(R.string.confirm_image_deletion).setCancelable(false)
						.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								imageList.remove(position);
								mImageAdapter.notifyDataSetChanged();
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
//				imgSrcDialog.dismiss();
				return false;
			}
		});
		
		reminder = (ImageView) findViewById(R.id.reminder);
		reminder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Timepicker will be automatically called after date is
				// inserted by user
				showTimePickerDialog(v);
				showDatePickerDialog(v);
			}
		});

		reminder_delete = (ImageView) findViewById(R.id.reminder_delete);
		reminder_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alarmDate = "";
				alarmTime = "";
				alarmDateTime = -1;
				datetime.setText("");
				reminder_delete.setVisibility(View.INVISIBLE);
			}
		});
		
		datetime = (TextView) findViewById(R.id.datetime);
	}

	private void initNote() {
		ParcelableNote parcelableNote = (ParcelableNote) getIntent()
				.getParcelableExtra(Constants.INTENT_NOTE);
		note = parcelableNote.getNote();

		if (note.get_id() != 0) {
			((EditText) findViewById(R.id.title)).setText(note.getTitle());
			((EditText) findViewById(R.id.content)).setText(note.getContent());
			((TextView) findViewById(R.id.creation))
					.append(getString(R.string.creation) + " "
							+ note.getCreationShort());
			((TextView) findViewById(R.id.last_modification))
					.append(getString(R.string.last_update) + " "
							+ note.getLastModificationShort());
			if (note.getAlarm() != null){				
				datetime.setText(initAlarm( Long.parseLong(note.getAlarm()) ));
				reminder_delete.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_share).setVisible(true);
		menu.findItem(R.id.menu_attachment).setVisible(true);
		menu.findItem(R.id.menu_delete).setVisible(true);
		menu.findItem(R.id.menu_discard_changes).setVisible(true);

		boolean archived = note.isArchived();
		menu.findItem(R.id.menu_archive).setVisible(!archived);
		menu.findItem(R.id.menu_unarchive).setVisible(archived);

		return super.onPrepareOptionsMenu(menu);
	}

	private boolean goHome() {
		NavUtils.navigateUpFromSameTask(this);
		if (prefs.getBoolean("settings_enable_animations", true)) {
			overridePendingTransition(R.animator.slide_left,
					R.animator.slide_right);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		saveNote(null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			saveNote(null);
			break;
		case R.id.menu_share:
			shareNote();
			break;
		case R.id.menu_archive:
			saveNote(true);
			break;
		case R.id.menu_unarchive:
			saveNote(false);
			break;
		case R.id.menu_attachment:			
			this.attachmentDialog = showAttachmentDialog(); 
			break;
		case R.id.menu_delete:
			deleteNote();
			break;
		case R.id.menu_discard_changes:
			goHome();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private AlertDialog showAttachmentDialog() {
		AlertDialog.Builder attachmentDialog = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.attachment_dialog,
				(ViewGroup) findViewById(R.id.layout_root));
		attachmentDialog.setView(layout);
		android.widget.TextView cameraSelection = (android.widget.TextView) layout.findViewById(R.id.camera);
		cameraSelection.setOnClickListener(new AttachmentOnClickListener());
		android.widget.TextView gallerySelection = (android.widget.TextView) layout.findViewById(R.id.gallery);
		gallerySelection.setOnClickListener(new AttachmentOnClickListener());
		return attachmentDialog.show();
	}
	
	
	/**
	 * Manages clicks on attachment dialog
	 */
	private class AttachmentOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.camera:
				ContentValues values = new ContentValues();
				values.put(MediaStore.Images.Media.TITLE, "New Picture");
				values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
				imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent, TAKE_PHOTO);
				attachmentDialog.dismiss();
				break;
			case R.id.gallery:
				Intent galleryIntent = new Intent();
				galleryIntent.setType("image/*");
				galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
				galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
				startActivityForResult(galleryIntent, GALLERY);
				attachmentDialog.dismiss();
				break;
			}

		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		// Recupero immagini
		BufferedInputStream stream = null;
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case TAKE_PHOTO:
//					try {
//						bitmap = BitmapDecoder.decodeSampledFromUri(this, imageUri, photoQuality,
//								photoQuality);
//
//						fotoView.setImageBitmap(bitmap);
//						imageChanged = true;
//					} catch (FileNotFoundException e) {
//						e.printStackTrace();
//					}
					imageList.add(imageUri);
					mImageAdapter.notifyDataSetChanged();
					break;
				case GALLERY:
//						if (bitmap != null) {
//							bitmap.recycle();
//						}
//						bitmap = BitmapDecoder.decodeSampledFromUri(this, intent.getData(), photoQuality,
//								photoQuality);
//						fotoView.setImageBitmap(bitmap);
//						imageChanged = true;
//					} catch (FileNotFoundException e) {
//						Log.e(Constants.TAG, "Errore foto galleria");
					imageList.add(intent.getData());
					mImageAdapter.notifyDataSetChanged();
					break;
			}
		}
	}


	
	
	private void deleteNote() {

		// Confirm dialog creation
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setMessage(R.string.delete_note_confirmation)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								// Simply return to the previous
								// activity/fragment if it was a new note
								if (getIntent().getStringExtra(
										Constants.INTENT_KEY) == null) {
									goHome();
									return;
								}

								// Create note object
								int _id = Integer.parseInt(getIntent()
										.getStringExtra(Constants.INTENT_KEY));
								Note note = new Note();
								note.set_id(_id);

								// Deleting note using DbHelper
								DbHelper db = new DbHelper(
										getApplicationContext());
								db.deleteNote(note);

								// Informs the user about update
								Log.d(Constants.TAG, "Deleted note with id '"
										+ _id + "'");
								showToast(
										getResources().getText(
												R.string.note_deleted),
										Toast.LENGTH_SHORT);
								goHome();
								return;
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
							}
						});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	/**
	 * Save new notes, modify them or archive
	 * 
	 * @param archive
	 *            Boolean flag used to archive note
	 */
	private void saveNote(Boolean archive) {
		// Changed fields
		String title = ((EditText) findViewById(R.id.title)).getText()
				.toString();
		String content = ((EditText) findViewById(R.id.content)).getText()
				.toString();

		Note noteEdited = note;
		if (noteEdited != null) {
			note = noteEdited;
		} else {
			note = new Note();
		}

		// Check if some text has ben inserted or is an empty note
		if ((title + content).length() == 0) {
			Log.d(Constants.TAG, "Empty note not saved");
			showToast(getResources().getText(R.string.empty_note_not_saved),
					Toast.LENGTH_SHORT);
			goHome();
			return;
		}

		// Logging operation
		Log.d(Constants.TAG, "Saving new note titled: " + title
				+ " (archive var: " + archive + ")");

		note.set_id(note.get_id());
		note.setTitle(title);
		note.setContent(content);
		note.setArchived(archive != null ? archive : note.isArchived());
		note.setAlarm(alarmDateTime != -1 ? String.valueOf(alarmDateTime): null);

		// Saving changes to the note
		DbHelper db = new DbHelper(this);
		db.updateNote(note);
		
		// Save alarm
		if (alarmDateTime != -1)
			setAlarm();

		// Logs update
		Log.d(Constants.TAG, "New note saved with title '" + note.getTitle()
				+ "'");
		showToast(getResources().getText(R.string.note_updated),
				Toast.LENGTH_SHORT);

		// Go back on stack
		goHome();
	}

	private void setAlarm() {
		Intent intent = new Intent(this, AlarmReceiver.class);
		intent.putExtra(Constants.INTENT_NOTE, new ParcelableNote(note));
		PendingIntent sender = PendingIntent.getBroadcast(this, Constants.INTENT_ALARM_CODE, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, alarmDateTime, sender);
		
	}

	private void shareNote() {
		// Changed fields
		String title = ((EditText) findViewById(R.id.title)).getText()
				.toString();
		String content = ((EditText) findViewById(R.id.content)).getText()
				.toString();

		// Check if some text has ben inserted or is an empty note
		if ((title + content).length() == 0) {
			Log.d(Constants.TAG, "Empty note not shared");
			showToast(getResources().getText(R.string.empty_note_not_shared),
					Toast.LENGTH_SHORT);
			return;
		}

		// Definition of shared content
		String text = title + System.getProperty("line.separator") + content
				+ System.getProperty("line.separator")
				+ System.getProperty("line.separator")
				+ getResources().getString(R.string.shared_content_sign);

		// Prepare sharing intent
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		// shareIntent.setType("*/*");
		shareIntent.putExtra(Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(shareIntent, getResources()
				.getString(R.string.share_message_chooser)));
	}

	public void showDatePickerDialog(View v) {
		DatePickerFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
	}

	/**
	 * Shows time picker to set alarm
	 * 
	 * @param v
	 */
	private void showTimePickerDialog(View v) {
		TimePickerFragment newFragment = new TimePickerFragment();
		newFragment.show(getSupportFragmentManager(), Constants.TAG);
	}


	@Override
	public void onDateSet(DatePicker v, int year, int month, int day) {
		alarmDate = DateHelper.onDateSet(year, month, day,
				Constants.DATE_FORMAT_SHORT_DATE);
	}

	@Override
	public void onTimeSet(TimePicker v, int hour, int minute) {
		
		// Creation of string rapresenting alarm time		
		alarmTime = DateHelper.onTimeSet(hour, minute,
				Constants.DATE_FORMAT_SHORT_TIME);
		datetime.setText(getString(R.string.alarm_set_on) + " " + alarmDate
				+ " " + getString(R.string.at_time) + " " + alarmTime);

		// Setting alarm time in milliseconds
		alarmDateTime = DateHelper.getLongFromDateTime(alarmDate,
				Constants.DATE_FORMAT_SHORT_DATE, alarmTime,
				Constants.DATE_FORMAT_SHORT_TIME).getTimeInMillis();
		
		// Shows icon to remove alarm
		reminder_delete.setVisibility(View.VISIBLE);
	}
	
	
	/**
	 * Used to set acual alarm state when initializing a note to be edited
	 * @param alarmDateTime
	 * @return
	 */
	private String initAlarm(long alarmDateTime) {
		this.alarmDateTime = alarmDateTime;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(alarmDateTime);
		alarmDate = DateHelper.onDateSet(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
				Constants.DATE_FORMAT_SHORT_DATE);
		alarmTime = DateHelper.onTimeSet(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
				Constants.DATE_FORMAT_SHORT_TIME);
		String dateTimeText = getString(R.string.alarm_set_on) + " " + alarmDate
				+ " " + getString(R.string.at_time) + " " + alarmTime;
		return dateTimeText;
	}
	
	
	public String getAlarmDate(){
		return alarmDate;
	}
	
	public String getAlarmTime(){
		return alarmTime;
	}



}
