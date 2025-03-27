package it.feio.android.omninotes.utils;

import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AnimationsHelperInstrumentalTest {

    private Context context;
    private View view;
    private ImageView expandedImageView;
    private View targetView;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        view = new View(context);
        expandedImageView = new ImageView(context);
        targetView = new View(context);
    }

    @Test
    public void testExpandOrCollapse_expand() {
        // Initially set the view to GONE
        view.setVisibility(View.GONE);

        // Call the method to expand
        AnimationsHelper.expandOrCollapse(view, true);

        // Verify the view is visible after animation
        assertEquals(View.VISIBLE, view.getVisibility());
    }

    @Test
    public void testExpandOrCollapse_collapse() {
        // Initially set the view to VISIBLE
        view.setVisibility(View.GONE);

        // Call the method to collapse
        AnimationsHelper.expandOrCollapse(view, false);

        // Verify the view is gone after animation
        assertEquals(View.GONE, view.getVisibility());
    }
}