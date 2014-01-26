package it.feio.android.omninotes;

import it.feio.android.omninotes.models.DrawingPanel;
import it.feio.android.omninotes.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class SketchActivity_alt extends Activity {
    /** Called when the activity is first created. */
   
    
    FrameLayout frmLayout;
	private DrawingPanel dp;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_undo_redo);
       
        frmLayout=(FrameLayout)findViewById(R.id.frameLayout);     
        dp = new DrawingPanel(this);
        frmLayout.addView(dp);
        ((Button) findViewById(R.id.Undo))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	dp.undo();
                        
                    }
                });
        ((Button) findViewById(R.id.Redo))
        .setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dp.redo();
                 
            }
        });
    }

    
    
    
    
    @Override
	public void onBackPressed() {
		back(dp.getBitmap());
		// super.onBackPressed();
	}

	public void back(Bitmap bitmap) {
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