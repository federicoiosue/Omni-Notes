package it.feio.android.omninotes.widgets;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.widget.ListRemoteViewsFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ListRemoteViewsFactoryTest {

    private ListRemoteViewsFactory factory;
    private Context context;
    private int appWidgetId;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        factory = new ListRemoteViewsFactory((Application) context, intent);
    }

    @Test
    public void testOnCreate() {
        factory.onCreate();
        assertNotNull(factory);
        assertTrue(factory.getCount() >= 0);
    }

    @Test
    public void testGetViewAt() {
        factory.onCreate();
        if (factory.getCount() > 0) {
            RemoteViews view = factory.getViewAt(0);
            assertNotNull(view);
        }
    }

    @Test
    public void testOnDataSetChanged() {
        factory.onCreate();
        int initialCount = factory.getCount();
        factory.onDataSetChanged();
        assertEquals(initialCount, factory.getCount());
    }

    @Test
    public void testGetLoadingView() {
        assertNull(factory.getLoadingView());
    }

    @Test
    public void testGetViewTypeCount() {
        assertEquals(1, factory.getViewTypeCount());
    }

    @Test
    public void testGetItemId() {
        factory.onCreate();
        if (factory.getCount() > 0) {
            assertEquals(0, factory.getItemId(0));
        }
    }

    @Test
    public void testHasStableIds() {
        assertFalse(factory.hasStableIds());
    }
}