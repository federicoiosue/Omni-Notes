package it.feio.android.omninotes;

import it.feio.android.checklistview.utils.AlphaManager;
import it.feio.android.checklistview.utils.DensityUtil;
import it.feio.android.omninotes.models.SketchView;
import it.feio.android.omninotes.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.SVBar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SketchActivity extends BaseActivity {

	private Context mContext;
	private ImageView eraser;
	private SketchView drawingView;
	private ImageView undo;
	private ImageView redo;
	private PopupWindow stokePopup;
	private ImageView erase;
	private int seekBarProgress;
	private View popupLayout;
	private ImageView strokeImageView;
	private int size;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sketch);
		
		mContext = this;
		drawingView = (SketchView) findViewById(R.id.drawing);

		// Show the Up button in the action bar.
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayShowTitleEnabled(false);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		eraser = (ImageView) findViewById(R.id.sketch_stroke);
		eraser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				showPopup(v);
			}
		});
		
		undo = (ImageView) findViewById(R.id.sketch_undo);
		undo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				drawingView.undo();			
			}
		});
		
		redo = (ImageView) findViewById(R.id.sketch_redo);
		redo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				drawingView.redo();
				
			}
		});

		erase = (ImageView) findViewById(R.id.sketch_erase);
		erase.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				askForErase();
			}

			private void askForErase() {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
				alertDialogBuilder.setMessage(R.string.erase_sketch)
						.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								drawingView.erase();
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
		});


		// Inflate the popup_layout.xml
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		popupLayout = inflater.inflate(R.layout.popup_sketch_stroke, (ViewGroup) findViewById(R.id.layout_root));
		
		// Actual stroke shape size is retrieved
		strokeImageView = (ImageView) popupLayout.findViewById(R.id.stroke_circle);
		final Drawable circleDrawable = getResources().getDrawable(R.drawable.circle);
		size = circleDrawable.getIntrinsicWidth();
		
		setStrokeSeekbarProgress(5);
	}

	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putSerializable("paths", drawingView.getPaths());
		outState.putSerializable("undonePaths", drawingView.getUndonePaths());
		super.onSaveInstanceState(outState);
	}
	
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		drawingView.setPaths((ArrayList<Pair<Path, Paint>>) savedInstanceState.getSerializable("paths"));
		drawingView.setUndonePaths((ArrayList<Pair<Path, Paint>>) savedInstanceState.getSerializable("undonePaths"));
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	
	@Override
	public void onBackPressed() {
		save(drawingView.getBitmap());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
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
	
	
	public void updateRedoAlpha() {
		if (drawingView.getUndoneCount() > 0)
			AlphaManager.setAlpha(redo, 1f);
		else
			AlphaManager.setAlpha(redo, 0.4f);
	}
	
	
	// The method that displays the popup.
		private void showPopup(View anchor) {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			// Creating the PopupWindow
			stokePopup = new PopupWindow(this);
			stokePopup.setContentView(popupLayout);
			stokePopup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
			stokePopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
			stokePopup.setFocusable(true);

			// Clear the default translucent background
			stokePopup.setBackgroundDrawable(new BitmapDrawable());		

			// Displaying the popup at the specified location, + offsets (transformed 
			// dp to pixel to support multiple screen sizes)
//			stokePopup.showAsDropDown(anchor, 0, -680);
			stokePopup.showAsDropDown(anchor, 0, DensityUtil.convertDpToPixel(-390, mContext));
			
			// Stroke size seekbar initialization and event managing
			SeekBar mSeekBar = (SeekBar) popupLayout.findViewById(R.id.stroke_seekbar);
			mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {					
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}					
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					
					// When the seekbar is moved a new size is calculated and the new shape
					// is positioned centrally into the ImageView
					setStrokeSeekbarProgress(progress);
				}
			});			
			mSeekBar.setProgress(seekBarProgress);			

			// Stroke color picker initialization and event managing
			ColorPicker mColorPicker = (ColorPicker) popupLayout.findViewById(R.id.stroke_color_picker);	
			mColorPicker.addSVBar((SVBar) popupLayout.findViewById(R.id.svbar));
			mColorPicker.addOpacityBar((OpacityBar) popupLayout.findViewById(R.id.opacitybar));
			mColorPicker.setOnColorChangedListener(new OnColorChangedListener() {
				
				@Override
				public void onColorChanged(int color) {
					drawingView.setStrokeColor(color);
				}
			});
			mColorPicker.setColor(drawingView.getStrokeColor());	
		}


		protected void setStrokeSeekbarProgress(int progress) {
			
			// Avoid 
			int calcProgress = progress > 1 ? progress : 1;
			
			int newSize = (int) Math.round( (size/100f) * calcProgress);
			int offset = (int) Math.round( (size-newSize) / 2);					
			Log.v(Constants.TAG, "Stroke size " + newSize + " (" + calcProgress + "%)");
			
			LayoutParams lp = new LayoutParams(newSize, newSize);
			lp.setMargins(offset, offset, offset, offset);	
			strokeImageView.setLayoutParams(lp);
			
			drawingView.setStrokeSize(newSize);
			seekBarProgress = progress;
		}
		
		
}