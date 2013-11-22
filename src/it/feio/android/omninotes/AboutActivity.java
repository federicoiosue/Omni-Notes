package it.feio.android.omninotes;

import com.actionbarsherlock.view.MenuItem;
import it.feio.android.omninotes.R;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AboutActivity extends BaseActivity {

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		// Shape creation for the layout
		RoundRectShape rect = new RoundRectShape(new float[] { 30, 30, 30, 30, 30, 30, 30, 30 }, null, null);
		ShapeDrawable bg = new ShapeDrawable(rect);
		bg.getPaint().setColor(Color.parseColor(getString(R.color.about_bg)));
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			getWindow().getDecorView().getRootView().setBackgroundDrawable(bg);
		} else {
			getWindow().getDecorView().getRootView().setBackground(bg);
		}
		
		// Version printing
		TextView copyleft = (TextView) findViewById(R.id.copyleft);
		try {
			copyleft.append("   " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {}
				
		// Site click management
		TextView site = (TextView) findViewById(R.id.site);
		site.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW, 
					       Uri.parse(getString(R.string.dev_site)));
					startActivity(i);				
			}
		});
		
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
//				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
