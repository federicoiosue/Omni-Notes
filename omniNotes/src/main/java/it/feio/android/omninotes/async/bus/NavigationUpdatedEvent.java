package it.feio.android.omninotes.async.bus;

import it.feio.android.omninotes.models.NavigationItem;
import roboguice.util.Ln;


/**
 * Created by fede on 18/04/15.
 */
public class NavigationUpdatedEvent {

	public final Object navigationItem;


	public NavigationUpdatedEvent(Object navigationItem) {
		Ln.d(this.getClass().getName());
		this.navigationItem = navigationItem;
	}
}
