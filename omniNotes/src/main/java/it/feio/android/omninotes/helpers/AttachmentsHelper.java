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

package it.feio.android.omninotes.helpers;


import it.feio.android.omninotes.models.Attachment;
import org.apache.commons.io.FileUtils;

import java.io.File;


public class AttachmentsHelper {

	/**
	 * Retrieves attachment file size
	 *
	 * @param attachment Attachment to evaluate
	 * @return Human readable file size string
	 */
	public static String getSize(Attachment attachment) {
		long sizeInKb = attachment.getSize();
		if (attachment.getSize() == 0) {
			sizeInKb = new File(attachment.getUri().getPath()).length();
		}
		return FileUtils.byteCountToDisplaySize(sizeInKb);
	}
}
