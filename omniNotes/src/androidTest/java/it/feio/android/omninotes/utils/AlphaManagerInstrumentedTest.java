package it.feio.android.omninotes.utils;

import android.content.Context;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AlphaManagerInstrumentedTest {

    private Context context;
    private View testView;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        testView = new View(context);
    }

    @Test
    public void testSetAlpha_validAlpha() {
        float alphaValue = 0.5f;
        AlphaManager.setAlpha(testView, alphaValue);
        assertEquals("Alpha value should be set to 0.5", alphaValue, testView.getAlpha(), 0.0f);
    }

    @Test
    public void testSetAlpha_nullView() {
        try {
            AlphaManager.setAlpha(null, 0.5f);
        } catch (Exception e) {
            fail("Setting alpha on a null view should not throw an exception");
        }
    }
}