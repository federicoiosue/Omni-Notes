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
