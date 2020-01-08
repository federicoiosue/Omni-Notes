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
package it.feio.android.omninotes.utils;

import android.content.res.AssetManager;
import java.io.IOException;
import java.util.Arrays;


public class AssetUtils {

  private AssetUtils() {
    // hides public constructor
  }

  public static boolean exists (String fileName, String path,
      AssetManager assetManager) throws IOException {
    for (String currentFileName : assetManager.list(path)) {
      if (currentFileName.equals(fileName)) {
        return true;
      }
    }
    return false;
  }

  public static String[] list (String path, AssetManager assetManager)
      throws IOException {
    String[] files = assetManager.list(path);
    Arrays.sort(files);
    return files;
  }

}
