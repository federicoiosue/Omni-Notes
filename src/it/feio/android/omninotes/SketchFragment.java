package it.feio.android.omninotes;

import it.feio.android.checklistview.utils.AlphaManager;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.listeners.OnDrawChangedListener;
import it.feio.android.omninotes.models.views.SketchView;
import it.feio.android.omninotes.utils.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class SketchFragment extends Fragment implements OnDrawChangedListener{

	private MainActivity mActivity;
	private ImageView stroke;
	private ImageView eraser;
	private SketchView mSketchView;
	private ImageView undo;
	private ImageView redo;
	private ImageView erase;
	private int seekBarStrokeProgress, seekBarEraserProgress;
	private View popupLayout, popupEraserLayout;
	private ImageView strokeImageView, eraserImageView;
	private int size;
	private ColorPicker mColorPicker;
	private int oldColor;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mActivity = (MainActivity) getActivity();	
		
		setHasOptionsMenu(true);
		setRetainInstance(false);
	}
	
	
	@Override
	public void onStart() {
		// GA tracking
		OmniNotes.getGaTracker().set(Fields.SCREEN_NAME, getClass().getName());
		OmniNotes.getGaTracker().send(MapBuilder.createAppView().build());	
		super.onStart();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		if (savedInstanceState != null) {
//			if (savedInstanceState.containsKey("listViewPosition")) {
//				mSketchView.setPaths((ArrayList<Pair<Path, Paint>>) savedInstanceState.getSerializable("paths"));
//				mSketchView.setUndonePaths((ArrayList<Pair<Path, Paint>>) savedInstanceState.getSerializable("undonePaths"));
//			}			
//		}		
		return inflater.inflate(R.layout.fragment_sketch, container, false);
	}
	

	@Override
	public void onAttach(android.app.Activity activity) {
		super.onAttach(activity);
		
		mActivity = (MainActivity) getActivity();	
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mSketchView = (SketchView) mActivity.findViewById(R.id.drawing);
		mSketchView.setOnDrawChangedListener(this);
		
		Uri baseUri = getArguments().getParcelable("base");
		if (baseUri != null) {
			Bitmap bmp = null;
			try {
				bmp = BitmapFactory.decodeStream(mActivity.getContentResolver().openInputStream(baseUri));
				mSketchView.setBackgroundBitmap(mActivity, bmp);
			} catch (FileNotFoundException e) {
				Log.e(Constants.TAG, "Error replacing sketch bitmap background");
			}
		}

		// Show the Up button in the action bar.
		if (mActivity.getSupportActionBar() != null) {
			mActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);			
			mActivity.getSupportActionBar().setTitle(R.string.title_activity_sketch);
			mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		stroke = (ImageView) mActivity.findViewById(R.id.sketch_stroke);
		stroke.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mSketchView.getMode() == SketchView.STROKE) {			
					showPopup(v, SketchView.STROKE);
				} else {
					mSketchView.setMode(SketchView.STROKE);	
					AlphaManager.setAlpha(eraser, 0.4f);			
					AlphaManager.setAlpha(stroke, 1f);		
				}
			}
		});

		eraser = (ImageView) mActivity.findViewById(R.id.sketch_eraser);	
		AlphaManager.setAlpha(eraser, 0.4f);			
		eraser.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mSketchView.getMode() == SketchView.ERASER) {			
					showPopup(v, SketchView.ERASER);
				} else {
					mSketchView.setMode(SketchView.ERASER);		
					AlphaManager.setAlpha(stroke, 0.4f);			
					AlphaManager.setAlpha(eraser, 1f);					
				}
			}
		});
		
		undo = (ImageView) mActivity.findViewById(R.id.sketch_undo);
		undo.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mSketchView.undo();			
			}
		});
		
		redo = (ImageView) mActivity.findViewById(R.id.sketch_redo);
		redo.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mSketchView.redo();
				
			}
		});

		erase = (ImageView) mActivity.findViewById(R.id.sketch_erase);
		erase.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				askForErase();
			}

			private void askForErase() {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
				alertDialogBuilder.setMessage(R.string.erase_sketch)
						.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								mSketchView.erase();
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
		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(ActionBarActivity.LAYOUT_INFLATER_SERVICE);
//		popupLayout = inflater.inflate(R.layout.popup_sketch_stroke, (ViewGroup) mActivity.findViewById(R.id.layout_root));
		popupLayout = inflater.inflate(R.layout.popup_sketch_stroke, null);
		// And the one for eraser
		LayoutInflater inflaterEraser = (LayoutInflater) mActivity.getSystemService(ActionBarActivity.LAYOUT_INFLATER_SERVICE);
//		popupEraserLayout = inflaterEraser.inflate(R.layout.popup_sketch_eraser, (ViewGroup) mActivity.findViewById(R.id.layout_root));
		popupEraserLayout = inflaterEraser.inflate(R.layout.popup_sketch_eraser, null);

		// Actual stroke shape size is retrieved
		strokeImageView = (ImageView) popupLayout.findViewById(R.id.stroke_circle);
		final Drawable circleDrawable = getResources().getDrawable(R.drawable.circle);
		size = circleDrawable.getIntrinsicWidth();
		// Actual eraser shape size is retrieved
		eraserImageView = (ImageView) popupEraserLayout.findViewById(R.id.stroke_circle);
//		final Drawable circleEraserDrawable = getResources().getDrawable(R.drawable.circle);
		size = circleDrawable.getIntrinsicWidth();

		setSeekbarProgress(SketchView.DEFAULT_STROKE_SIZE, SketchView.STROKE);
		setSeekbarProgress(SketchView.DEFAULT_ERASER_SIZE, SketchView.ERASER);
		
		// Stroke color picker initialization and event managing
		mColorPicker = (ColorPicker) popupLayout.findViewById(R.id.stroke_color_picker);	
		mColorPicker.addSVBar((SVBar) popupLayout.findViewById(R.id.svbar));	
		mColorPicker.addOpacityBar((OpacityBar) popupLayout.findViewById(R.id.opacitybar));
		mColorPicker.setOnColorChangedListener(new OnColorChangedListener() {				
			@Override
			public void onColorChanged(int color) {
				mSketchView.setStrokeColor(color);
			}
		});
		mColorPicker.setColor(mSketchView.getStrokeColor());
		mColorPicker.setOldCenterColor(mSketchView.getStrokeColor());
	}

	
//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		outState.putSerializable("paths", mSketchView.getPaths());
//		outState.putSerializable("undonePaths", mSketchView.getUndonePaths());
//		super.onSaveInstanceState(outState);
//	}
	
	
	@Override
	public void onPause() {
		super.onPause();
//		save();
		
		// Removes forced portrait orientation for this fragment
//		getActivity().setRequestedOrientation(
//                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mActivity.onBackPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	

	public void save() {
		
		Bitmap bitmap = mSketchView.getBitmap();		
		if (bitmap != null) {
		
			try {			
				Uri uri = getArguments().getParcelable(MediaStore.EXTRA_OUTPUT);
				File bitmapFile = new File(uri.getPath());
				FileOutputStream out = new FileOutputStream(bitmapFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.close();
				if (bitmapFile.exists()) {
//					if (mOnSketchSavedListener != null) {
//						mOnSketchSavedListener.onSketchSaved(uri);
//					}					
					mActivity.sketchUri = uri;
				} else {
					Crouton.makeText(mActivity, R.string.error, ONStyle.ALERT).show();
				}
	
			} catch (Exception e) {
				Log.d(Constants.TAG, "Error writing sketch image data", e);
			}
		}
//		mActivity.getSupportFragmentManager().popBackStack(); 
	}
	
	
	
	// The method that displays the popup.
	private void showPopup(View anchor, final int eraserOrStroke) {
		
		boolean isErasing = eraserOrStroke == SketchView.ERASER;
		
		oldColor = mColorPicker.getColor();
		
		DisplayMetrics metrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		// Creating the PopupWindow
		PopupWindow popup = new PopupWindow(mActivity);
		popup.setContentView(isErasing ? popupEraserLayout : popupLayout);
		popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popup.setFocusable(true);
		popup.setOnDismissListener(new OnDismissListener() {			
			@Override
			public void onDismiss() {
				if (mColorPicker.getColor() != oldColor)
					mColorPicker.setOldCenterColor(oldColor);
			}
		});

		// Clear the default translucent background
		popup.setBackgroundDrawable(new BitmapDrawable());		

		// Displaying the popup at the specified location, + offsets (transformed 
		// dp to pixel to support multiple screen sizes)
//		popup.showAsDropDown(anchor, 0, DensityUtil.convertDpToPixel(isErasing ? -120 : -390, mActivity));
		popup.showAsDropDown(anchor);
		
		// Stroke size seekbar initialization and event managing
		SeekBar mSeekBar;
		mSeekBar = (SeekBar) (isErasing ? popupEraserLayout
				.findViewById(R.id.stroke_seekbar) : popupLayout
				.findViewById(R.id.stroke_seekbar));
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
				setSeekbarProgress(progress, eraserOrStroke);
			}
		});			
		int progress = isErasing ? seekBarEraserProgress : seekBarStrokeProgress;
		mSeekBar.setProgress(progress);			
	}


	protected void setSeekbarProgress(int progress, int eraserOrStroke) {			
		// Avoid 
		int calcProgress = progress > 1 ? progress : 1;
		
		int newSize = (int) Math.round( (size/100f) * calcProgress);
		int offset = (int) Math.round( (size-newSize) / 2);					
		Log.v(Constants.TAG, "Stroke size " + newSize + " (" + calcProgress + "%)");
		
		LayoutParams lp = new LayoutParams(newSize, newSize);
		lp.setMargins(offset, offset, offset, offset);	
		if (eraserOrStroke == SketchView.STROKE) {	
			strokeImageView.setLayoutParams(lp);
			seekBarStrokeProgress = progress;			
		} else {	
			eraserImageView.setLayoutParams(lp);
			seekBarEraserProgress = progress;
		}
		
		mSketchView.setSize(newSize, eraserOrStroke);
	}


	@Override
	public void onDrawChanged() {
		// Undo
		if (mSketchView.getPaths().size() > 0)
			AlphaManager.setAlpha(undo, 1f);
		else
			AlphaManager.setAlpha(undo, 0.4f);
		// Redo
		if (mSketchView.getUndoneCount() > 0)
			AlphaManager.setAlpha(redo, 1f);
		else
			AlphaManager.setAlpha(redo, 0.4f);
	}
		
		
}