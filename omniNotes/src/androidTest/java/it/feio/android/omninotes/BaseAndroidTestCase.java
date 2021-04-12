/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;
import com.pixplicity.easyprefs.library.Prefs;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.bus.CategoriesUpdatedEvent;
import it.feio.android.omninotes.async.bus.NotesDeletedEvent;
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.exceptions.TestException;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;


public class BaseAndroidTestCase {

  protected static final Locale PRESET_LOCALE = new Locale(ENGLISH.toString());
  protected static DbHelper dbHelper;
  protected static Context testContext;
  protected static SharedPreferences prefs;

  @Rule
  public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
      ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE,
      RECORD_AUDIO
  );

  @BeforeClass
  public static void setUpBeforeClass() {
    testContext = ApplicationProvider.getApplicationContext();
    prefs = Prefs.getPreferences();
    dbHelper = DbHelper.getInstance(testContext);
  }

  @Before
  public void setUpBase() {
    prepareDatabase();
    prepareLocale();
    preparePreferences();
  }

  private void preparePreferences() {
    prefs.edit().clear().commit();
  }

  private static void prepareDatabase() {
    dbHelper.getDatabase(true).delete(DbHelper.TABLE_NOTES, null, null);
    dbHelper.getDatabase(true).delete(DbHelper.TABLE_CATEGORY, null, null);
    dbHelper.getDatabase(true).delete(DbHelper.TABLE_ATTACHMENTS, null, null);
    assertFalse("Database MUST be writable", dbHelper.getDatabase(true).isReadOnly());
  }

  private void prepareLocale() {
    Locale.setDefault(PRESET_LOCALE);
    Configuration config = testContext.getResources().getConfiguration();
    config.locale = PRESET_LOCALE;
  }

  protected Note createTestNote(String title, String content, int attachmentsNumber) {
    Note note = new Note();
    note.setTitle(title);
    note.setContent(content);
    long now = Calendar.getInstance().getTimeInMillis();
    note.setCreation(now);
    note.setLastModification(now);

    for (int i = 0; i < attachmentsNumber; i++) {
      Attachment attachment = createTestAttachment("testAttachment" + i);
      note.addAttachment(attachment);
    }

    dbHelper.updateNote(note, false);

    EventBus.getDefault().post(new NotesUpdatedEvent(Collections.singletonList(note)));
    EventBus.getDefault().post(new CategoriesUpdatedEvent());

    return note;
  }

  private Attachment createTestAttachment(String attachmentName) {
    try {
      File testAttachment = File.createTempFile(attachmentName, ".txt");
      IOUtils.write(
          String.format("some test content for attachment named %s", attachmentName).toCharArray(),
          new FileOutputStream(testAttachment));
      return new Attachment(Uri.fromFile(testAttachment), attachmentName);
    } catch (IOException e) {
      throw new TestException(e);
    }
  }

  protected void archiveNotes(List<Note> notes, boolean archive) {
    notes.forEach(n -> dbHelper.archiveNote(n, archive));

    EventBus.getDefault().post(new NotesUpdatedEvent(notes));
    EventBus.getDefault().post(new CategoriesUpdatedEvent());
  }

  protected void trashNotes(List<Note> notes, boolean trash) {
    notes.forEach(n -> dbHelper.trashNote(n, trash));

    EventBus.getDefault().post(new NotesUpdatedEvent(notes));
    EventBus.getDefault().post(new CategoriesUpdatedEvent());
  }

  protected void createCategory(String categoryName) {
    Category category = new Category();
    category.setName(categoryName);
    category.setColor(
        String.valueOf(testContext.getResources().getIntArray(R.array.material_colors)[0]));
    category.setDescription("testing category");

    dbHelper.updateCategory(category);

    EventBus.getDefault().post(new CategoriesUpdatedEvent());
  }

  /**
   * Verifies that a utility class is well defined.
   *
   * @param clazz utility class to verify.
   */
  protected static void assertUtilityClassWellDefined(final Class<?> clazz)
      throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
    assertUtilityClassWellDefined(clazz, false, false);
  }

  protected static void assertUtilityClassWellDefined(final Class<?> clazz,
      boolean weakClassModifier,
      boolean weakConstructorModifier)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    if (!weakClassModifier) {
      assertTrue("class must be final", Modifier.isFinal(clazz.getModifiers()));
    }

    assertEquals("There must be only one constructor", 1, clazz.getDeclaredConstructors().length);
    final Constructor<?> constructor = clazz.getDeclaredConstructor();
    if (!weakConstructorModifier && (constructor.isAccessible() || !Modifier
        .isPrivate(constructor.getModifiers()))) {
      fail("constructor is not private");
    }

    try {
      constructor.setAccessible(true);
      constructor.newInstance();
      constructor.setAccessible(false);
    } catch (InvocationTargetException e) {
      // Using @UtilityClass from Lombok is ok to get this
      assertTrue(e.getTargetException() instanceof UnsupportedOperationException);
    }

    for (final Method method : clazz.getMethods()) {
      if (!Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass().equals(clazz)) {
        fail("there exists a non-static method:" + method);
      }
    }
  }

}
