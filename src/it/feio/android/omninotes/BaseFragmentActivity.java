package it.feio.android.omninotes;

import it.feio.android.omninotes.R;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;


public class BaseFragmentActivity extends FragmentActivity {


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
}
