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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;
import it.feio.android.omninotes.models.Note;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;


public class BaseUnitTest {

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

  protected Note getNote(Long id, String title, String content) {
    Note note = new Note();
    note.set_id(id);
    note.setTitle(title);
    note.setContent(content);
    return note;
  }

  protected Context getContextMock() {
    Context context = mock(Context.class);
    Resources res = getResourcesMock();
    when(context.getResources()).thenReturn(res);
    return context;
  }

  protected Resources getResourcesMock() {
    Resources resources = mock(Resources.class);
    Configuration conf = getConfigurationMock();
    when(resources.getConfiguration()).thenReturn(conf);
    return resources;
  }

  protected Configuration getConfigurationMock() {
    Configuration configuration = mock(Configuration.class);
    LocaleList localeList = mock(LocaleList.class);
    when(configuration.getLocales()).thenReturn(localeList);
    when(configuration.getLocales().get(0)).thenReturn(Locale.ITALY);
//    doReturn(Locale.ITALY).when(configuration.locale);
    return configuration;
  }

}
