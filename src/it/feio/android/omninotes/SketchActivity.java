package it.feio.android.omninotes;

import it.feio.android.checklistview.utils.AlphaManager;
import it.feio.android.omninotes.models.SketchView;
import it.feio.android.omninotes.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class SketchActivity extends Activity {

	private Context mContext;
	private ImageView eraser;
	private SketchView drawingView;
	private ImageView undo;
	private ImageView redo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sketch);
		
		mContext = this;
		drawingView = (SketchView) findViewById(R.id.drawing);

		eraser = (ImageView) findViewById(R.id.sketch_eraser);
		eraser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (drawingView.isEraserActive) {
					drawingView.isEraserActive = false;
					eraser.setImageResource(R.drawable.ic_action_discard);
				} else {
					drawingView.isEraserActive = true;
					eraser.setImageResource(R.drawable.ic_action_edit);
				}

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

	}

	@Override
	public void onBackPressed() {
		back(drawingView.getBitmap());
		// super.onBackPressed();
	}

	public void back(Bitmap bitmap) {
		try {
			// File folder = new
			// File(getIntent().getStringExtra(MediaStore.EXTRA_OUTPUT));
			// if (!folder.exists()) {
			// folder.mkdirs();
			// }
			// File nomediaFile = new File(folder, ".nomedia");
			// if (!nomediaFile.exists()) {
			// nomediaFile.createNewFile();
			// }
			
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
}