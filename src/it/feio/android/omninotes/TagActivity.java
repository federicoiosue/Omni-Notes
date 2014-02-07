package it.feio.android.omninotes;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class TagActivity extends Activity {

	private final float SATURATION = 0.4f;
	private final float VALUE = 0.9f;

	Tag tag;
	EditText title;
	EditText description;
	ColorPicker picker;
	Button deleteBtn;
	Button saveBtn;
	Button discardBtn;
	private TagActivity mActivity;
	private AlertDialog dialog;
	private boolean colorChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag);
		
		mActivity = this;

		// Retrieving intent
		tag = getIntent().getParcelableExtra(Constants.INTENT_TAG);

		// Getting Views from layout
		initViews();

		if (tag == null) {
			Log.d(Constants.TAG, "Adding new tag");
			tag = new Tag();
		} else {
			Log.d(Constants.TAG, "Editing tag " + tag.getName());
			populateViews();
		}
	}
	
	
	
	@Override
	public void onBackPressed() {
		discard();
	}
	

	private void initViews() {
		title = (EditText) findViewById(R.id.tag_title);
		description = (EditText) findViewById(R.id.tag_description);
		picker = (ColorPicker) findViewById(R.id.colorpicker_tag);
		picker.setOnColorChangedListener(new OnColorChangedListener() {			
			@Override
			public void onColorChanged(int color) {
				picker.setOldCenterColor(picker.getColor());
				colorChanged = true;
			}
		});
		// Long click on color picker to remove color
		picker.setOnLongClickListener(new OnLongClickListener() {			
			@Override
			public boolean onLongClick(View v) {
				picker.setColor(Color.WHITE);
				return true;
			}
		});
		picker.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				picker.setColor(Color.WHITE);
			}
		});

		// Added invisible saturation and value bars to get achieve pastel colors
		SaturationBar saturationbar = (SaturationBar) findViewById(R.id.saturationbar_tag);
		saturationbar.setSaturation(SATURATION);
		picker.addSaturationBar(saturationbar);
		ValueBar valuebar = (ValueBar) findViewById(R.id.valuebar_tag);
		valuebar.setValue(VALUE);
		picker.addValueBar(valuebar);

		deleteBtn = (Button) findViewById(R.id.delete);
		saveBtn = (Button) findViewById(R.id.save);
		discardBtn = (Button) findViewById(R.id.discard);
		
		// Buttons events
		deleteBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				deleteTag();
			}
		});
		saveBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// In case tag name is not compiled a message will be shown
                if (title.getText().toString().length() > 0) {
    				saveTag();
                } else {
                	title.setError(getString(R.string.tag_title));
                }
			}
		});
		discardBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				discard();
			}
		});
	}

	private void populateViews() {
		title.setText(tag.getName());
		description.setText(tag.getDescription());
		// Reset picker to saved color
		String color = tag.getColor();
		if (color != null && color.length() > 0) {
			picker.setColor(Integer.parseInt(color));
			picker.setOldCenterColor(Integer.parseInt(color));
		}
		deleteBtn.setVisibility(View.VISIBLE);
	}

	
	/**
	 * Tag saving
	 */
	private void saveTag() {
		tag.setName(title.getText().toString());
		tag.setDescription(description.getText().toString());
		if (colorChanged || tag.getColor() == null)
			tag.setColor(String.valueOf(picker.getColor()));
		
		// Saved to DB and new id or update result catched
		DbHelper db = new DbHelper(this);
		long n = db.updateTag(tag);
		
		// If tag has no its an insertion and id is filled from db
		if (tag.getId() == null) {
			tag.setId((int)n);
		}		
		
		// Sets result to show proper message
		getIntent().putExtra(Constants.INTENT_TAG, tag);
		setResult(RESULT_OK, getIntent());
		finish();
	}

	private void deleteTag() {
		
		// Retrieving how many notes are tagged with tag to be deleted
		DbHelper db = new DbHelper(this);
		int count = db.getTaggedCount(tag);
		String msg;
		if (count > 0)
			msg = getString(R.string.delete_tag_confirmation).replace("$1$", String.valueOf(count));
		else
			msg = getString(R.string.delete_unused_tag_confirmation);
	
		// Showing dialog
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(msg)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						// Changes navigation if actually are shown notes associated with this tag
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
						String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
						String navigation = prefs.getString(Constants.PREF_NAVIGATION, navNotes);
						if (String.valueOf(tag.getId()).equals(navigation))
							prefs.edit().putString(Constants.PREF_NAVIGATION, navNotes).commit();
						// Removes tag and edit notes associated with it
						DbHelper db = new DbHelper(mActivity);
						db.deleteTag(tag);
						
						// Sets result to show proper message
						setResult(RESULT_CANCELED);
						finish();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		dialog = alertDialogBuilder.create();
		dialog.show();
	}

	private void discard() {		
		// Sets result to show proper message
		setResult(RESULT_FIRST_USER);
		finish();
	}
	

	public boolean goHome() {
		
		// In this case the caller activity is DetailActivity
		if (getIntent().getBooleanExtra("noHome", false)) {
			setResult(RESULT_OK);
			super.finish();
			return true;
		}
		
		NavUtils.navigateUpFromSameTask(this);
		return true;
	}
	
	
public void save(Bitmap bitmap) {
		
		if (bitmap == null) {
			setResult(RESULT_CANCELED);
			super.finish();
		}
		
		try {			
			Uri uri = getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);
			File bitmapFile = new File(uri.getPath());
			FileOutputStream out = new FileOutputStream(bitmapFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

			if (bitmapFile.exists()) {
				Intent localIntent = new Intent().setData(Uri
						.fromFile(bitmapFile));
				setResult(RESULT_OK, localIntent);
			} else {
				setResult(RESULT_CANCELED);
			}
			super.finish();

		} catch (Exception e) {
			e.printStackTrace();
			Log.d(Constants.TAG, "Error writing sketch image data");
		}
	}
}
