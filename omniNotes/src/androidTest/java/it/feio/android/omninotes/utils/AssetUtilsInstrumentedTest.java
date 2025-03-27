package it.feio.android.omninotes.utils;

  import android.content.Context;
  import android.content.res.AssetManager;
  import androidx.test.core.app.ApplicationProvider;
  import androidx.test.ext.junit.runners.AndroidJUnit4;
  import org.junit.Before;
  import org.junit.Test;
  import org.junit.runner.RunWith;
  import static org.junit.Assert.*;

  @RunWith(AndroidJUnit4.class)
  public class AssetUtilsInstrumentedTest {

      private Context context;
      private AssetManager assetManager;

      @Before
      public void setUp() {
          context = ApplicationProvider.getApplicationContext();
          assetManager = context.getAssets();
      }

      @Test
      public void testExists_invalidFile() throws Exception {
          boolean exists = AssetUtils.exists("invalid_file.txt", "path/to/assets", assetManager);
          assertFalse("File should not exist in the specified path", exists);
      }

      @Test
      public void testList_invalidPath() throws Exception {
          String[] files = AssetUtils.list("invalid/path", assetManager);
          assertNotNull("File list should not be null for an invalid path", files);
          assertEquals("File list should be empty for an invalid path", 0, files.length);
      }
  }