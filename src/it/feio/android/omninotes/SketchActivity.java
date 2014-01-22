package it.feio.android.omninotes;

import it.feio.android.omninotes.models.SketchView;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class SketchActivity extends Activity {

	private ImageView eraser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sketch);

		final SketchView drawingView = (SketchView) findViewById(R.id.drawing);

		eraser = (ImageView) findViewById(R.id.eraser);
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

	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.activity_main, menu);
	// return true;
	// }

}