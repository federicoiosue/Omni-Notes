package it.feio.android.omninotes.async;

import it.feio.android.omninotes.utils.Constants;
import android.app.IntentService;
import android.content.Intent;

public class NotificationService extends IntentService{

	public NotificationService() {
		super(NotificationService.class.getName());
	}
	

	@Override
	protected void onHandleIntent(Intent intent) {
		// If an alarm has been fired a notification must be generated
		if (Constants.ACTION_DISMISS.equals(intent.getAction())) {
			dismiss(intent);
		} else if (Constants.ACTION_SNOOZE.equals(intent.getAction())) {
			snooze(intent);		
		}		
	}

	private void snooze(Intent intent) {
		// TODO Auto-generated method stub
		
	}

	private void dismiss(Intent intent) {
		// TODO Auto-generated method stub
		
	}

}
