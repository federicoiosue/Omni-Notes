/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes.receiver;

import it.feio.android.omninotes.async.AlarmRestoreOnRebootService;
import it.feio.android.omninotes.utils.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		Log.i(Constants.TAG, "System rebooted: refreshing reminders");
		
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Intent service = new Intent(ctx, AlarmRestoreOnRebootService.class);
			ctx.startService(service);
		}	

	}

	

}
