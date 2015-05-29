package it.feio.android.omninotes.async.bus;

import android.util.Log;
import it.feio.android.omninotes.utils.Constants;


public class PushbulletReplyEvent {

	public String message;

	public PushbulletReplyEvent(String message) {
		Log.d(Constants.TAG, this.getClass().getName());
		this.message = message;
	}
}
