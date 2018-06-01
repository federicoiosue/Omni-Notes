/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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

package it.feio.android.omninotes.async;

import android.content.Context;
import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.BaseActivity;
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;

public class UpdateWidgetsTask extends AsyncTask<Void, Void, Void> {

    private Context context;

    public UpdateWidgetsTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        WidgetUpdateSubscriber widgetUpdateSubscriber = new WidgetUpdateSubscriber();
        return null;
    }

    class WidgetUpdateSubscriber {

        WidgetUpdateSubscriber() {
            EventBus.getDefault().register(this);
        }

        public void onEvent(NotesUpdatedEvent event) {
            BaseActivity.notifyAppWidgets(context);
            EventBus.getDefault().unregister(this);
        }
    }
}
