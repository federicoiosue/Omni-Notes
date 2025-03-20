/*
 * Copyright (C) 2013-2024 Federico Iosue (federico@iosue.it)
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

  public static boolean exists(String fileName, String path,
      AssetManager assetManager) throws IOException {
    if (fileName == null || path == null || assetManager == null) {
      throw new IllegalArgumentException("Arguments cannot be null.");
    }

    String[] fileList = assetManager.list(path);
    if (fileList != null) {
      for (String currentFileName : fileList) {
        if (fileName.equals(currentFileName)) {
          return true;
        }
      }
    }
    return false;
  }

  public static String[] list(String path, AssetManager assetManager)
      throws IOException {
    if (path == null || assetManager == null) {
      throw new IllegalArgumentException("Arguments cannot be null.");
    }

    String[] files = assetManager.list(path);
    if (files == null) {
      return new String[0]; // Retorna um array vazio se o diretório não existir ou estiver vazio
    }
    Arrays.sort(files);
    return files;
  }
}
