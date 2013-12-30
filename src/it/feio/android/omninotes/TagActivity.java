package it.feio.android.omninotes;

import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.utils.Constants;
import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag);

		// Retrieving intent
		tag = getIntent().getParcelableExtra(Constants.INTENT_TAG);

		// Getting Views from layout
		initViews();

		if (tag == null) {
			Log.d(Constants.TAG, "Adding new tag");
			tag = new Tag();
		} else {
			Log.d(Constants.TAG, "Editing tag " + tag.getTitle());
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
		title.setText(tag.getTitle());
		description.setText(tag.getDescription());
		color.setText(tag.getColor());
		deleteBtn.setVisibility(View.VISIBLE);
	}

	private void saveTag() {
		tag.setTitle(title.getText().toString());
		tag.setDescription(description.getText().toString());
		tag.setColor(color.getText().toString());
		db.updateTag(tag);
		showToast(getString(R.string.tag_saved), Toast.LENGTH_SHORT);
		onBackPressed();
	}

	private void deleteTag() {
		db.deleteTag(tag);
		onBackPressed();
	}

	private void discard() {
		onBackPressed();
	}
}
