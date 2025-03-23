package it.feio.android.omninotes.utils;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ResourcesUtilsInstrumentalTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testGetXmlId_validResource() {
        int resourceId = ResourcesUtils.getXmlId(context, ResourcesUtils.ResourceIdentifiers.XML, "settings");
        assertTrue("Resource ID should not be 0 for valid XML resource", resourceId != 0);
    }

    @Test
    public void testGetXmlId_invalidResource() {
        int resourceId = ResourcesUtils.getXmlId(context, ResourcesUtils.ResourceIdentifiers.XML, "invalid_resource_name");
        assertEquals("Resource ID should be 0 for invalid XML resource", 0, resourceId);
    }

    @Test
    public void testGetXmlId_validIdResource() {
        int resourceId = ResourcesUtils.getXmlId(context, ResourcesUtils.ResourceIdentifiers.ID, "drawer_layout");
        assertTrue("Resource ID should not be 0 for valid ID resource", resourceId != 0);
    }

    @Test
    public void testGetXmlId_invalidIdResource() {
        int resourceId = ResourcesUtils.getXmlId(context, ResourcesUtils.ResourceIdentifiers.ID, "invalid_id_name");
        assertEquals("Resource ID should be 0 for invalid ID resource", 0, resourceId);
    }

    @Test
    public void testGetXmlId_validArrayResource() {
        int resourceId = ResourcesUtils.getXmlId(context, ResourcesUtils.ResourceIdentifiers.ARRAY, "navigation_list");
        assertTrue("Resource ID should not be 0 for valid array resource", resourceId != 0);
    }

    @Test
    public void testGetXmlId_invalidArrayResource() {
        int resourceId = ResourcesUtils.getXmlId(context, ResourcesUtils.ResourceIdentifiers.ARRAY, "invalid_array_name");
        assertEquals("Resource ID should be 0 for invalid array resource", 0, resourceId);
    }
}
