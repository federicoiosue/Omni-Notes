package it.feio.android.omninotes.async.bus;

import android.util.Log;
import it.feio.android.omninotes.utils.Constants;


public class NavigationUpdatedNavDrawerClosedEvent {

	public final Object navigationItem;


	public NavigationUpdatedNavDrawerClosedEvent(Object navigationItem) {
		Log.d(Constants.TAG, this.getClass().getName());
		this.navigationItem = navigationItem;
	}
}
