package it.feio.android.omninotes.utils;

import android.graphics.Color;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ColorsUtilInstrumentalTest {

    @Test
    public void testCalculateColorLuminance_black() {
        int black = Color.BLACK;
        double luminance = ColorsUtil.calculateColorLuminance(black);
        assertEquals("Luminance of black should be 0", 0.0, luminance, 0.01);
    }

    @Test
    public void testCalculateColorLuminance_white() {
        int white = Color.WHITE;
        double luminance = ColorsUtil.calculateColorLuminance(white);
        assertEquals("Luminance of white should be 255", 255.0, luminance, 0.01);
    }

    @Test
    public void testGetContrastedColor_darkColor() {
        int darkColor = Color.BLACK;
        int contrastedColor = ColorsUtil.getContrastedColor(darkColor);
        assertEquals("Contrasted color for dark color should be light", 1, contrastedColor);
    }

    @Test
    public void testGetContrastedColor_lightColor() {
        int lightColor = Color.WHITE;
        int contrastedColor = ColorsUtil.getContrastedColor(lightColor);
        assertEquals("Contrasted color for light color should be dark", 0, contrastedColor);
    }
}