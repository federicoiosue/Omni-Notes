package it.feio.android.omninotes;

import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.utils.Constants;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TagActivity extends BaseActivity {

	Tag tag;
	EditText title;
	EditText description;
	EditText color;
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
		color = (EditText) findViewById(R.id.tag_color);

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
		color.setText(tag.getColor());
		deleteBtn.setVisibility(View.VISIBLE);
	}

	private void saveTag() {
		tag.setName(title.getText().toString());
		tag.setDescription(description.getText().toString());
		tag.setColor(color.getText().toString());
		db.updateTag(tag);
		showToast(getString(R.string.tag_saved), Toast.LENGTH_SHORT);
		goHome();
	}

	private void deleteTag() {
		
		// Retrieving how many notes are tagged with tag to be deleted
		int count = db.getTaggedCount(tag);
		String msg = getString(R.string.delete_tag_confirmation).replace("$1$", String.valueOf(count));
				
		// Showing dialog
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(msg)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						// Changes navigation if actually are shown notes associated with this tag
						String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
						String navigation = prefs.getString(Constants.PREF_NAVIGATION, navNotes);
						if (String.valueOf(tag.getId()).equals(navigation))
							prefs.edit().putString(Constants.PREF_NAVIGATION, navNotes).commit();
						// Removes tag and edit notes associated with it
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
