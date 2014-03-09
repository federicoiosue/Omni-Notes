package it.feio.android.omninotes.widget;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.NavDrawerTagAdapter;
import it.feio.android.omninotes.models.Tag;

import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;

public class WidgetConfigurationActivity extends Activity {

	private Activity mActivity;
	private Button configOkButton;
	private Spinner tagSpinner;
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private ArrayList<Tag> tags;
	private String sqlCondition;
	private RadioGroup mRadioGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;

		setResult(RESULT_CANCELED);

		setContentView(R.layout.activity_widget_configuration);

		mRadioGroup = (RadioGroup) findViewById(R.id.widget_config_radiogroup);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.widget_config_notes:
					tagSpinner.setEnabled(false);
					break;

				case R.id.widget_config_tags:
					tagSpinner.setEnabled(true);
					break;
				}
			}
		});

		tagSpinner = (Spinner) findViewById(R.id.widget_config_spinner);
		tagSpinner.setEnabled(false);
		DbHelper db = new DbHelper(mActivity);
		tags = db.getTags();
		tagSpinner.setAdapter(new NavDrawerTagAdapter(mActivity, tags));

		configOkButton = (Button) findViewById(R.id.widget_config_confirm);
		configOkButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mRadioGroup.getCheckedRadioButtonId() == R.id.widget_config_notes) {
					sqlCondition = " WHERE " + DbHelper.KEY_ARCHIVED + " != 1 ";
				} else {
					Tag tag = (Tag) tagSpinner.getSelectedItem();
					sqlCondition = " WHERE " + DbHelper.TABLE_NOTES + "."
							+ DbHelper.KEY_TAG + " = " + tag.getId();
				}

				CheckBox showThumbnailsCheckBox = (CheckBox) findViewById(R.id.show_thumbnails);

				// Updating the ListRemoteViewsFactory parameter to get the list
				// of notes
				ListRemoteViewsFactory.updateConfiguration(mActivity, mAppWidgetId,
						sqlCondition, showThumbnailsCheckBox.isChecked());

				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						mAppWidgetId);
				setResult(RESULT_OK, resultValue);

				finish();
			}
		});

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}
	}

}
