package it.feio.android.omninotes.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.MainActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DisplayInstrumentedTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testGetRootView() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                View rootView = Display.getRootView(activity);
                assertNotNull("Root view should not be null", rootView);
            });
        }
    }

    @Test
    public void testGetUsableSize() {
        Point size = Display.getUsableSize(context);
        assertTrue("Usable width should be greater than 0", size.x > 0);
        assertTrue("Usable height should be greater than 0", size.y > 0);
    }

    @Test
    public void testGetVisibleSize() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                Point size = Display.getVisibleSize(activity);
                assertTrue("Visible width should be greater than 0", size.x > 0);
                assertTrue("Visible height should be greater than 0", size.y > 0);
            });
        }
    }

    @Test
    public void testGetFullSize() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                View rootView = Display.getRootView(activity);
                Point size = Display.getFullSize(rootView);
                assertTrue("Full width should be greater than 0", size.x > 0);
                assertTrue("Full height should be greater than 0", size.y > 0);
            });
        }
    }

    @Test
    public void testGetStatusBarHeight() {
        int height = Display.getStatusBarHeight(context);
        assertTrue("Status bar height should be greater than 0", height > 0);
    }

    @Test
    public void testGetNavigationBarHeightStandard() {
        int height = Display.getNavigationBarHeightStandard(context);
        assertTrue("Navigation bar height should be greater than or equal to 0", height >= 0);
    }

    @Test
    public void testGetScreenDimensions() {
        Point size = Display.getScreenDimensions(context);
        assertTrue("Screen width should be greater than 0", size.x > 0);
        assertTrue("Screen height should be greater than 0", size.y > 0);
    }

    @Test
    public void testGetNavigationBarHeightKitkat() {
        int height = Display.getNavigationBarHeightKitkat(context);
        assertTrue("Navigation bar height should be greater than or equal to 0", height >= 0);
    }

    @Test
    public void testOrientationLandscape() {
        boolean isLandscape = Display.orientationLandscape(context);
        // Assuming the test device is in portrait mode
        assertFalse("Orientation should not be landscape", isLandscape);
    }

    @Test
    public void testGetSoftButtonsBarHeight() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                int height = Display.getSoftButtonsBarHeight(activity);
                assertTrue("Soft buttons bar height should be greater than or equal to 0", height >= 0);
            });
        }
    }
}
