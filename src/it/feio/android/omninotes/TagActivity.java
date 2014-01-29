package it.feio.android.omninotes;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.utils.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TagActivity extends Activity {

	Tag tag;
	EditText title;
	EditText description;
	ColorPicker picker;
	Button deleteBtn;
	Button saveBtn;
	Button discardBtn;
	private TagActivity mActivity;
	private AlertDialog dialog;

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

	private void initViews() {
		title = (EditText) findViewById(R.id.tag_title);
		description = (EditText) findViewById(R.id.tag_description);
		picker = (ColorPicker) findViewById(R.id.color_picker);
		picker.setOnColorChangedListener(new OnColorChangedListener() {			
			@Override
			public void onColorChanged(int color) {
				picker.setOldCenterColor(picker.getColor());
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
				saveTag();
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
		if (color.length() > 0) {
			picker.setColor(Integer.parseInt(color));
			picker.setOldCenterColor(Integer.parseInt(color));
		}
		deleteBtn.setVisibility(View.VISIBLE);
	}

	private void saveTag() {
		tag.setName(title.getText().toString());
		tag.setDescription(description.getText().toString());
		tag.setColor(String.valueOf(picker.getColor()));
		DbHelper db = new DbHelper(this);
		db.updateTag(tag);
		Toast.makeText(this, getString(R.string.tag_saved), Toast.LENGTH_SHORT).show();
		goHome();
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
						// Navigate back to notes list
						goHome();
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
		onBackPressed();
	}
	

	public boolean goHome() {
		NavUtils.navigateUpFromSameTask(this);
		return true;
	}
}
