/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.extensions;

import com.pushbullet.android.extension.MessagingExtension;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.bus.PushbulletReplyEvent;
import it.feio.android.omninotes.helpers.LogDelegate;


public class PushBulletExtension extends MessagingExtension {

  private static final String TAG = "PushBulletExtension";


  @Override
  protected void onMessageReceived (final String conversationIden, final String message) {
    LogDelegate.i("Pushbullet MessagingExtension: onMessageReceived(" + conversationIden + ", " + message
        + ")");
    EventBus.getDefault().post(new PushbulletReplyEvent(message));
//        MainActivity runningMainActivity = MainActivity.getInstance();
//        if (runningMainActivity != null && !runningMainActivity.isFinishing()) {
//            runningMainActivity.onPushBulletReply(message);
//        }
  }


  @Override
  protected void onConversationDismissed (final String conversationIden) {
    LogDelegate.i("Pushbullet MessagingExtension: onConversationDismissed(" + conversationIden + ")");
  }
}
