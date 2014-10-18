package it.feio.android.omninotes;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.adapters.PaletteColorPickerAdapter;
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
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.SVBar;

public class CategoryActivity extends Activity {
	private final String colors[][] = {
			{ "0044AA", "660099", "337700", "DD4400", "990000", "404040", },
			{ "0099CC", "9933CC", "669900", "FF8800", "CC0000", "606060", },
			{ "44B5E5", "AA66CC", "99CC00", "FFBB33", "FF4444", "909090", },
			{ "77F5FF", "CC99FF", "CCFF33", "FFFF66", "FF7777", "E0E0E0", },
		};

	Category category;
	EditText title;
	EditText description;
	ColorPicker picker;
	Button deleteBtn;
	Button saveBtn;
	Button discardBtn;
	Button moreColorsBtn;
	GridView palette;
	private Integer chosenColor;
	private CategoryActivity mActivity;
	private AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category);
		
		mActivity = this;

		// Retrieving intent
		category = getIntent().getParcelableExtra(Constants.INTENT_TAG);

		// Getting Views from layout
		initViews();

		if (category == null) {
			Log.d(Constants.TAG, "Adding new category");
			category = new Category();
			category.setColor(String.valueOf(Color.parseColor("#" + colors[1][2])));
			populateViews(true);
		} else {
			Log.d(Constants.TAG, "Editing category " + category.getName());
			populateViews(false);
		}
	}
	
	
	
	@Override
	public void onBackPressed() {
		discard();
	}
	

	private void initViews() {
		title = (EditText) findViewById(R.id.category_title);
		description = (EditText) findViewById(R.id.category_description);
		picker = (ColorPicker) findViewById(R.id.colorpicker_category);
		picker.setOnColorChangedListener(new OnColorChangedListener() {			
			@Override
			public void onColorChanged(int color) {
				title.setBackgroundColor(color);
				chosenColor = picker.getColor();
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

		palette = (GridView) findViewById(R.id.colorPalette);
		palette.setAdapter(new PaletteColorPickerAdapter(this, colors));
		palette.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				chosenColor = (Integer)parent.getItemAtPosition(position);
				picker.setColor(chosenColor);
				title.setBackgroundColor(chosenColor);
			}
		});
		
		// Added invisible saturation and value bars to achieve pastel colors
		SVBar SVBar = (SVBar) findViewById(R.id.svbar_category);
		picker.addSVBar(SVBar);

		moreColorsBtn = (Button) findViewById(R.id.moreColors);
		deleteBtn = (Button) findViewById(R.id.delete);
		saveBtn = (Button) findViewById(R.id.save);
		discardBtn = (Button) findViewById(R.id.discard);

		// Buttons events
		moreColorsBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				((ViewSwitcher)findViewById(R.id.colorChooserSwitcher)).showNext();
			}
		});
		deleteBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				deleteCategory();
			}
		});
		saveBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// In case category name is not compiled a message will be shown
                if (title.getText().toString().length() > 0) {
    				saveCategory();
                } else {
                	title.setError(getString(R.string.category_missing_title));
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

	private void populateViews(boolean newCategory) {
		// Reset picker to saved color
		String color = category.getColor();
		if (color != null && color.length() > 0) {
			chosenColor = Integer.parseInt(color);
			title.setBackgroundColor(chosenColor);
			picker.setColor(chosenColor);
			picker.setOldCenterColor(chosenColor);
		}
		if (!newCategory) {
			title.setText(category.getName());
			description.setText(category.getDescription());
			deleteBtn.setVisibility(View.VISIBLE);
		}
	}

	
	/**
	 * Category saving
	 */
	private void saveCategory() {
		category.setName(title.getText().toString());
		category.setDescription(description.getText().toString());
		
		category.setColor(String.valueOf(chosenColor));
		
		// Saved to DB and new id or update result catched
		DbHelper db = DbHelper.getInstance(this);
		category = db.updateCategory(category);
		
		// If category has no its an insertion and id is filled from db
//		if (category.getId() == null) {
//			category.setId((int)n);
//		}		
		
		// Sets result to show proper message
		getIntent().putExtra(Constants.INTENT_TAG, category);
		setResult(RESULT_OK, getIntent());
		finish();
	}

	private void deleteCategory() {
		
		// Retrieving how many notes are categorized with category to be deleted
		DbHelper db = DbHelper.getInstance(this);
		int count = db.getCategorizedCount(category);
		String msg;
		if (count > 0)
			msg = getString(R.string.delete_category_confirmation).replace("$1$", String.valueOf(count));
		else
			msg = getString(R.string.delete_unused_category_confirmation);
	
		// Showing dialog
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(msg)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						// Changes navigation if actually are shown notes associated with this category
						SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
						String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
						String navigation = prefs.getString(Constants.PREF_NAVIGATION, navNotes);
						if (String.valueOf(category.getId()).equals(navigation))
							prefs.edit().putString(Constants.PREF_NAVIGATION, navNotes).commit();
						// Removes category and edit notes associated with it
						DbHelper db = DbHelper.getInstance(mActivity);
						db.deleteCategory(category);
						
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
