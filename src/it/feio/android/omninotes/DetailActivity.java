/*******************************************************************************
 * Copyright 2013 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.joda.time.DateTime;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.neopixl.pixlui.components.edittext.EditText;
import com.neopixl.pixlui.components.textview.TextView;

import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.AttachmentAdapter;
import it.feio.android.omninotes.models.ExpandableHeightGridView;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.date.DateHelper;
import it.feio.android.omninotes.async.SaveNoteTask;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * An activity representing a single Item detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link ItemListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ItemDetailFragment}.
 */
public class DetailActivity extends BaseActivity {

	private static final int TAKE_PHOTO = 1;
	private static final int GALLERY = 2;
	
	private SherlockFragmentActivity mActivity;
	
	private Note note;
	private LinearLayout reminder_layout;
	private TextView datetime;
	private String alarmDate = "", alarmTime = "";
	private String dateTimeText = "";
	private long alarmDateTime = -1;
	public Uri imageUri;
	private AttachmentAdapter mAttachmentAdapter;
	private ExpandableHeightGridView mGridView;
	private ArrayList<Attachment> attachmentsList = new ArrayList<Attachment>();
	private AlertDialog attachmentDialog;
	private EditText title, content;
	private TextView location;
	
	// Location variables
	Location currentLocation;
    double currentLatitude;
    double currentLongitude;
    double noteLatitude;
    double noteLongitude;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		mActivity = this;

		// Show the Up button in the action bar.
		if (getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Note initialization
		initNote();
		
		// Views initialization
		initViews();
		
		setLocationManager();
	}


	private void initViews() {

		// Sets links clickable in title and content Views
		title = (EditText)findViewById(R.id.title);
		content = (EditText)findViewById(R.id.content);
		// Automatic links parsing if enabled
		if (prefs.getBoolean("settings_enable_editor_links", false)) {
			title.setLinksClickable(true);
			Linkify.addLinks(title, Linkify.ALL);
			title.setMovementMethod(LinkMovementMethod.getInstance());
			content.setLinksClickable(true);
			Linkify.addLinks(content, Linkify.ALL);
			content.setMovementMethod(LinkMovementMethod.getInstance());
		}
		
		// Initialization of location TextView
		location = (TextView) findViewById(R.id.location);
		if (currentLatitude != 0 && currentLongitude != 0) {
			location.setVisibility(View.VISIBLE);
			location.setText(getAddress());
		}
			
		location.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String urlTag = Constants.TAG  
								+ (note.getTitle() != null ? System.getProperty("line.separator") + note.getTitle() : "")
								+ (note.getContent() != null ? System.getProperty("line.separator") + note.getContent() : "");
				final String uriString = "http://maps.google.com/maps?q=" + noteLatitude + ',' + noteLongitude + "("+ urlTag +")&z=15";
				Intent locationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
				startActivity(locationIntent);
			}
	    });
		location.setOnLongClickListener(new OnLongClickListener() {			
			@Override
			public boolean onLongClick(View v) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
				alertDialogBuilder.setMessage(R.string.remove_location)
						.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								noteLatitude = 0;
								noteLongitude = 0;
								location.setText("");
								location.setVisibility(View.GONE);
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				return true;
			}
		});
		
		// Initialzation of gridview for images
		mGridView = (ExpandableHeightGridView) findViewById(R.id.gridview);
	    mGridView.setAdapter(mAttachmentAdapter);
//	    mGridView.setExpanded(true);
	    mGridView.autoresize();
	    
	    // Click events for images in gridview (zooms image)
	    mGridView.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        	Uri uri = ((Attachment)parent.getAdapter().getItem(position)).getUri();
	            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	            intent.setType("image/*");
	            startActivity(intent);
	        }
	    });
	    // Long click events for images in gridview	(removes image)
	    mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
				alertDialogBuilder.setMessage(R.string.delete_selected_image)
						.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								attachmentsList.remove(position);
								mAttachmentAdapter.notifyDataSetChanged();
							    mGridView.autoresize();
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();				
				return true;
			}
		});
		
	   
	    // Preparation for reminder icon
		reminder_layout = (LinearLayout) findViewById(R.id.reminder_layout);
		reminder_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDateTimeSelectors();				
			}
		});		
		reminder_layout.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
				alertDialogBuilder.setMessage(R.string.remove_reminder)
						.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								alarmDate = "";
								alarmTime = "";
								alarmDateTime = -1;
								datetime.setText("");
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				return true;
			}
		});
		
		datetime = (TextView) findViewById(R.id.datetime);
		datetime.setText(dateTimeText);
	}

	
	/**
	 *  Show date and time pickers
	 */
	protected void showDateTimeSelectors() {
		
		// Sets actual time or previously saved in note		
		final DateTime now = note.getAlarm() != null ? new DateTime(Long.parseLong(note.getAlarm())) : DateTime.now();
		
		CalendarDatePickerDialog mCalendarDatePickerDialog = CalendarDatePickerDialog.newInstance(new CalendarDatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(CalendarDatePickerDialog dialog, int year,
					int monthOfYear, int dayOfMonth) {
//				now.withYear(year);
//				now.withMonthOfYear(monthOfYear);
//				now.withDayOfMonth(dayOfMonth);
				alarmDate = DateHelper.onDateSet(year, monthOfYear, dayOfMonth, Constants.DATE_FORMAT_SHORT_DATE);
				Log.d(Constants.TAG, "Date set");
				RadialTimePickerDialog mRadialTimePickerDialog = RadialTimePickerDialog.newInstance(new RadialTimePickerDialog.OnTimeSetListener() {
					
					@Override
					public void onTimeSet(RadialPickerLayout view,
							int hourOfDay, int minute) {
//						now.withHourOfDay(hourOfDay);
//						now.withMinuteOfHour(minute);
						
						// Creation of string rapresenting alarm time		
						alarmTime = DateHelper.onTimeSet(hourOfDay, minute,
								Constants.DATE_FORMAT_SHORT_TIME);
						datetime.setText(getString(R.string.alarm_set_on) + " " + alarmDate
								+ " " + getString(R.string.at_time) + " " + alarmTime);
				
						// Setting alarm time in milliseconds
						alarmDateTime = DateHelper.getLongFromDateTime(alarmDate,
								Constants.DATE_FORMAT_SHORT_DATE, alarmTime,
								Constants.DATE_FORMAT_SHORT_TIME).getTimeInMillis();
						
						Log.d(Constants.TAG, "Time set");						
					}
				}, now.getHourOfDay(), now.getMinuteOfHour(), true);
				mRadialTimePickerDialog.show(getSupportFragmentManager(), Constants.TAG);
			}

		}, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth());
		mCalendarDatePickerDialog.show(getSupportFragmentManager(), Constants.TAG);
		
	}

	
	private void initNote() {
		note = (Note) getIntent().getParcelableExtra(Constants.INTENT_NOTE);

		if (note.get_id() != 0) {
			((TextView) findViewById(R.id.creation))
					.append(getString(R.string.creation) + " "
							+ note.getCreationShort());
			((TextView) findViewById(R.id.last_modification))
					.append(getString(R.string.last_update) + " "
							+ note.getLastModificationShort());
			if (note.getAlarm() != null) {
				alarmDateTime = Long.parseLong(note.getAlarm());
				dateTimeText = initAlarm(alarmDateTime);
			}
			if (note.getLatitude() != 0 && note.getLongitude() != 0) {
				noteLatitude = note.getLatitude();
				noteLongitude = note.getLongitude();
				currentLatitude = note.getLatitude();
				currentLongitude = note.getLongitude();
			}
			
			// If a new note is being edited the keyboard will not be shown on activity start
//			getWindow().setSoftInputMode(
//					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		}
		
		// Backup of actual attachments list to check if some of them will be deleted
		note.backupAttachmentsList();
		
		// Some fields can be filled by third party application and are always shown
		((EditText) findViewById(R.id.title)).setText(note.getTitle());
		((EditText) findViewById(R.id.content)).setText(note.getContent());
		attachmentsList = note.getAttachmentsList();
		mAttachmentAdapter = new AttachmentAdapter(mActivity, attachmentsList);
	}
	


	private void setLocationManager() {
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				updateLocation(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}
	
	String getAddress() {
		String addressString = "";
        try{
//        	Geocoder gcd = new Geocoder(this, Locale.getDefault());
//        	List<Address> addresses = 
//        			gcd.getFromLocation(currentLatitude, currentLongitude,100);
//        	if (addresses.size() > 0) {
//        		StringBuilder result = new StringBuilder();
//        		for(int i = 0; i < addresses.size(); i++){
//        			Address address =  addresses.get(i);
//        			int maxIndex = address.getMaxAddressLineIndex();
//        			for (int x = 0; x <= maxIndex; x++ ){
//        				result.append(address.getAddressLine(x));
//        				result.append(",");
//        			}               
//        			result.append(address.getLocality());
//        			result.append(",");
//        			result.append(address.getPostalCode());
//        			result.append("\n\n");
//        		}
//        		addressString = result.toString();
//        	}
//        }
        	noteLatitude = currentLatitude;
        	noteLongitude = currentLongitude;
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            Address address = gcd.getFromLocation(currentLatitude, currentLongitude,1).get(0);
            
            if (address != null) {
            	addressString = address.getThoroughfare() + ", " + address.getLocality();
            }
        }
        catch(IOException ex){
        	addressString = ex.getMessage().toString();
        }
        return addressString;
    }
    
    void updateLocation(Location location){
        currentLocation = location;
        currentLatitude = currentLocation.getLatitude();
        currentLongitude = currentLocation.getLongitude();
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
		android.widget.TextView locationSelection = (android.widget.TextView) layout.findViewById(R.id.location);
		locationSelection.setOnClickListener(new AttachmentOnClickListener());
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
				Intent galleryIntent;
				if (Build.VERSION.SDK_INT >= 19){
					galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				} else {
					galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
					galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
				}
				galleryIntent.setType("image/*");
				startActivityForResult(galleryIntent, GALLERY);
				attachmentDialog.dismiss();
				break;
			case R.id.location:
				String address = getAddress();
				location.setVisibility(View.VISIBLE);
				location.setText(address);
				attachmentDialog.dismiss();
				break;
				
			}

		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Fetch uri from activities, store into adapter and refresh adapter
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case TAKE_PHOTO:
					attachmentsList.add(new Attachment(imageUri));
					mAttachmentAdapter.notifyDataSetChanged();
				    mGridView.autoresize();
					break;
				case GALLERY:
					attachmentsList.add(new Attachment(intent.getData()));
					mAttachmentAdapter.notifyDataSetChanged();
				    mGridView.autoresize();
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
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void saveNote(Boolean archive) {
		
		// Get old reminder to check later if is changed
		String oldAlarm = note.getAlarm();
		
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

		// Check if some text or attachments of any type have been inserted or is an empty note
		if ((title + content).length() == 0 
				&& attachmentsList.size() == 0
				&& (noteLatitude == 0 && noteLongitude == 0) 
				&& alarmDateTime == -1) {
			
			Log.d(Constants.TAG, "Empty note not saved");
			showToast(getResources().getText(R.string.empty_note_not_saved),
					Toast.LENGTH_SHORT);
			goHome();
			return;
		}

		// Logging operation
		Log.d(Constants.TAG, "Saving new note titled: " + title + " (archive var: " + archive + ")");

		note.set_id(note.get_id());
		note.setTitle(title);
		note.setContent(content);
		note.setArchived(archive != null ? archive : note.isArchived());
		note.setAlarm(alarmDateTime != -1 ? String.valueOf(alarmDateTime) : null);
		note.setLatitude(noteLatitude);
		note.setLongitude(noteLongitude);
		note.setAttachmentsList(attachmentsList);

		// Saving changes to the note
//		DbHelper db = new DbHelper(this);
//		note = db.updateNote(note);
		SaveNoteTask saveNoteTask = new SaveNoteTask(this);
		// Forceing parallel execution disabled by default
		if (Build.VERSION.SDK_INT >= 11) {
			saveNoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
		} else {
			saveNoteTask.execute(note);
		}
		
		// Advice of update
		showToast(getResources().getText(R.string.note_updated),
				Toast.LENGTH_SHORT);

		// Saves reminder if is not in actual 
		if (note.getAlarm() != null && !note.getAlarm().equals(oldAlarm)) {				
				setAlarm();
		}
		
		// Go back on stack
		goHome();
	}

	
	private void setAlarm() {
		Intent intent = new Intent(this, AlarmReceiver.class);
		intent.putExtra(Constants.INTENT_NOTE, note);
		PendingIntent sender = PendingIntent.getBroadcast(this, Constants.INTENT_ALARM_CODE, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, alarmDateTime, sender);		
	}
	

	
	/**
	 * Notes sharing
	 */
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

		Intent shareIntent = new Intent();
		// Prepare sharing intent with only text
		if (attachmentsList.size() == 0) {
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, text);
			
		// Intent with single image attachment
		} else if (attachmentsList.size() == 1) {
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.setType("image/jpeg");
			shareIntent.putExtra(Intent.EXTRA_STREAM, attachmentsList.get(0).getUri());
			shareIntent.putExtra(Intent.EXTRA_TEXT, text);
			
		// Intent with multiple images
		} else if (attachmentsList.size() > 1) {
			shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
			shareIntent.setType("image/jpeg");
			ArrayList<Uri> uris = new ArrayList<Uri>();
			for (Attachment attachment : attachmentsList) {
				uris.add(attachment.getUri());
			}
			shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			shareIntent.putExtra(Intent.EXTRA_TEXT, text);
		}
		
		startActivity(Intent.createChooser(shareIntent, getResources()
				.getString(R.string.share_message_chooser)));
	}

	
//	public void showDatePickerDialog(View v) {
//		DatePickerFragment newFragment = new DatePickerFragment();
//		newFragment.show(getSupportFragmentManager(), "datePicker");
//	}
//
//	/**
//	 * Shows time picker to set alarm
//	 * 
//	 * @param v
//	 */
//	private void showTimePickerDialog(View v) {
//		TimePickerFragment newFragment = new TimePickerFragment();
//		newFragment.show(getSupportFragmentManager(), Constants.TAG);
//	}
//
//
//	@Override
//	public void onDateSet(DatePicker v, int year, int month, int day) {
//		alarmDate = DateHelper.onDateSet(year, month, day,
//				Constants.DATE_FORMAT_SHORT_DATE);
//		showTimePickerDialog(v);
//	}
//
//	@Override
//	public void onTimeSet(TimePicker v, int hour, int minute) {
//		
//		// Creation of string rapresenting alarm time		
//		alarmTime = DateHelper.onTimeSet(hour, minute,
//				Constants.DATE_FORMAT_SHORT_TIME);
//		datetime.setText(getString(R.string.alarm_set_on) + " " + alarmDate
//				+ " " + getString(R.string.at_time) + " " + alarmTime);
//
//		// Setting alarm time in milliseconds
//		alarmDateTime = DateHelper.getLongFromDateTime(alarmDate,
//				Constants.DATE_FORMAT_SHORT_DATE, alarmTime,
//				Constants.DATE_FORMAT_SHORT_TIME).getTimeInMillis();
//		
//		// Shows icon to remove alarm
//		reminder_delete.setVisibility(View.VISIBLE);
//	}
	
	
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
