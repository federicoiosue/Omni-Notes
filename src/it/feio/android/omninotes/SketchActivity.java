package it.feio.android.omninotes;

import it.feio.android.checklistview.utils.AlphaManager;
import it.feio.android.omninotes.models.SketchView;
import it.feio.android.omninotes.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.SVBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SketchActivity extends Activity {

	private Context mContext;
	private ImageView eraser;
	private SketchView drawingView;
	private ImageView undo;
	private ImageView redo;
	private PopupWindow stokePopup;
	private ImageView erase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sketch);
		
		mContext = this;
		drawingView = (SketchView) findViewById(R.id.drawing);

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
	}

	
	@Override
	public void onBackPressed() {
		save(drawingView.getBitmap());
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

			// Inflate the popup_layout.xml
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.popup_sketch_stroke, (ViewGroup) findViewById(R.id.layout_root));

			// Creating the PopupWindow
			stokePopup = new PopupWindow(this);
			stokePopup.setContentView(layout);
			stokePopup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
			stokePopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
			stokePopup.setFocusable(true);

			// Clear the default translucent background
			stokePopup.setBackgroundDrawable(new BitmapDrawable());
			
			// Actual stroke shape size is retrieved
			final ImageView circle = (ImageView) layout.findViewById(R.id.stroke_circle);
			final Drawable circleDrawable = circle.getDrawable();
			final int size = circleDrawable.getIntrinsicWidth();
			
			// Stroke size seekbar initialization and event managing
			SeekBar mSeekBar = (SeekBar) layout.findViewById(R.id.stroke_seekbar);
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
					int newSize = (int)Math.round((size/100f)*progress);
					int offset = (int)Math.round((size-newSize)/2);
					
					int left = offset;
					int top = offset;
					int right = offset + newSize;
					int bottom = offset + newSize;
					
					circleDrawable.setBounds(left, top, right, bottom);
					
					drawingView.setStrokeSize(newSize);
				}
			});
			mSeekBar.setProgress(drawingView.getStrokeSize());

			// Stroke color picker initialization and event managing
			ColorPicker mColorPicker = (ColorPicker) layout.findViewById(R.id.stroke_color_picker);	
			mColorPicker.addSVBar((SVBar) layout.findViewById(R.id.svbar));
			mColorPicker.setOnColorChangedListener(new OnColorChangedListener() {
				
				@Override
				public void onColorChanged(int color) {
					drawingView.setStrokeColor(color);
				}
			});
			mColorPicker.setColor(drawingView.getStrokeColor());			

			// Displaying the popup at the specified location, + offsets.
			stokePopup.showAsDropDown(anchor, 0, -630);
		}
		
		
}