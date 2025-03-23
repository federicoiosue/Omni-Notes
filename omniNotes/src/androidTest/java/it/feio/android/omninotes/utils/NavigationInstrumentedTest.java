package it.feio.android.omninotes.utils;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.models.Category;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NavigationInstrumentedTest {

  private Context context;
  private String[] navigationListCodes;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    Prefs.Builder prefsBuilder = new Prefs.Builder()
        .setContext(context)
        .setMode(Context.MODE_PRIVATE)
        .setPrefsName(context.getPackageName())
        .setUseDefaultSharedPreference(true);
    prefsBuilder.build();
    
    // Simulate the values from R.array.navigation_list_codes
    navigationListCodes = new String[]{"notes", "archive", "reminders", "trash", "uncategorized", "category"};
  }

  @Test
  public void testGetNavigation() {
    Prefs.putString(ConstantsBase.PREF_NAVIGATION, navigationListCodes[Navigation.NOTES]);
    assertEquals(5, Navigation.getNavigation());

    Prefs.putString(ConstantsBase.PREF_NAVIGATION, navigationListCodes[Navigation.ARCHIVE]);
    assertEquals(5, Navigation.getNavigation());
  }

  @Test
  public void testGetNavigationText() {
    Prefs.putString(ConstantsBase.PREF_NAVIGATION, navigationListCodes[Navigation.REMINDERS]);
    assertEquals(navigationListCodes[Navigation.REMINDERS], Navigation.getNavigationText());
  }

  @Test
  public void testGetCategory() {
    Prefs.putString(ConstantsBase.PREF_NAVIGATION, "1");
    assertEquals(Long.valueOf(1), Navigation.getCategory());

    // Verify handling of non-numeric values
    Prefs.putString(ConstantsBase.PREF_NAVIGATION, "not_a_category");
    try {
      Navigation.getCategory();
      fail("Expected NumberFormatException");
    } catch (NumberFormatException e) {
      // Expected exception
      assertEquals("For input string: \"not_a_category\"", e.getMessage());
    }
  }

  @Test
  public void testCheckNavigationCategory() {
    Category category = new Category();
    category.setId(1L);
    Prefs.putString(ConstantsBase.PREF_NAVIGATION, "1");
    assertTrue("Expected category navigation to match", Navigation.checkNavigationCategory(category));

    category.setId(2L);
    assertFalse("Expected category navigation not to match", Navigation.checkNavigationCategory(category));
  }
}
