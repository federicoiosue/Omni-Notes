/*
 * Copyright (C) 2013-2021 Federico Iosue (federico@iosue.it)
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

import static rx.Observable.from;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.Assert;

/**
 * Retry assertions with ease. By using composition and reflection assertions can be retried until a
 * certain quantum of time is passed, useful when some async processing must complete for example.
 */
public class RetryableAssert {

  /**
   * @param timeoutInMillisec milliseconds to retry within
   * @param o                 object to call method on
   * @param methodName        name of the method to call on the passed object
   * @param args              eventual args
   */
  public static void assertTrue(long timeoutInMillisec, @Nonnull Object o,
      @Nonnull String methodName,
      @Nullable Object... args)
      throws InvocationTargetException, IllegalAccessException {

    Method method = getMethod(o, methodName);
    long endTime = System.currentTimeMillis() + timeoutInMillisec;

    do {
      try {
        Assert.assertTrue((boolean) method.invoke(o, args));
      } catch (AssertionError e) {
        // Retry
      }
    }
    while (System.currentTimeMillis() == endTime);

    Assert.assertTrue((boolean) method.invoke(o, args));
  }

  private static Method getMethod(@Nonnull Object o, @Nonnull String methodName) {
    return from(o.getClass().getMethods()).filter(m -> methodName.equals(m.getName()))
        .toBlocking().first();
  }

}
