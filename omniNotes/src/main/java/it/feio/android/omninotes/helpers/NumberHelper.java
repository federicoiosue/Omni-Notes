/*
 * Copyright (C) 2013-2026 Federico Iosue (omninotes@iosue.it)
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
package it.feio.android.omninotes.helpers;

import lombok.experimental.UtilityClass;

/**
 * Numbers coming from user input or preferences are not guaranteed to be valid: these helpers never
 * throw, they fall back to a default value instead.
 */
@UtilityClass
public class NumberHelper {

  private static final long BYTES_PER_MEGABYTE = 1024L * 1024L;


  public static long megabytesToBytes(String megabytes) {
    var value = parseLong(megabytes);
    if (value <= 0) {
      return 0L;
    }
    if (value > Long.MAX_VALUE / BYTES_PER_MEGABYTE) {
      return Long.MAX_VALUE;
    }
    return value * BYTES_PER_MEGABYTE;
  }

  private static long parseLong(String value) {
    if (value == null) {
      return 0L;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return 0L;
    }
  }

}
