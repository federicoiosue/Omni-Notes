/*******************************************************************************
 * Copyright 2013 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import android.content.res.AssetManager;

public class AssetUtils {

	public static boolean exists(String fileName, String path,
			AssetManager assetManager) throws IOException {
		for (String currentFileName : assetManager.list(path)) {
			if (currentFileName.equals(fileName)) {
				return true;
			}
		}
		return false;
	}

	public static String[] list(String path, AssetManager assetManager)
			throws IOException {
		String[] files = assetManager.list(path);
		Arrays.sort(files);
		return files;
	}
	
	
	/**
	 * Loads a file into string
	 * @param name
	 * @param assetManager
	 * @return
	 * @throws IOException 
	 */
	public static String readFile(String fileName, AssetManager assetManager) throws IOException {
		InputStream input;
		String text = null;
		input = assetManager.open(fileName);

		int size = input.available();
		byte[] buffer = new byte[size];
		input.read(buffer);
		input.close();

		// byte buffer into a string
		text = new String(buffer);
		return text;
	}
}
