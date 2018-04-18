/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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

import android.support.annotation.NonNull;

import java.io.*;
import java.util.Arrays;


public class ResourceAwareTest {

  private static final String MODULE_NAME = "omniNotes";
  private static final String BASE_PATH = resolveBasePath(MODULE_NAME);


  private static String resolveBasePath(String moduleName) {
    final String path = "./" + moduleName + "/src/test/res/";
    if (Arrays.asList(new File("./").list()).contains(moduleName)) {
      return path; // version for call unit tests from Android Studio
    }
    return "../" + path; // version for call unit tests from terminal './gradlew test'
  }


  /**
   * Retrieves test resouce InputStream
   *
   * @throws IOException
   */
  public static FileInputStream getResourceAsStream(@NonNull final String path) throws IOException {
    return new FileInputStream(BASE_PATH + path);
  }


  /**
   * Retrieves test resouce as {@link File} object
   *
   * @throws IOException
   */
  public static File getResourceAsFile(@NonNull final String path) throws IOException {
    return new File(BASE_PATH + path);
  }


  /**
   * Reads file content and returns string.
   *
   * @throws IOException
   */
  public static String readFile(@NonNull final String path) throws IOException {
    final StringBuilder sb = new StringBuilder();
    String strLine;
    try (final BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(BASE_PATH + path), "UTF-8"))) {
      while ((strLine = reader.readLine()) != null) {
        sb.append(strLine);
      }
    }
    return sb.toString();
  }
}
