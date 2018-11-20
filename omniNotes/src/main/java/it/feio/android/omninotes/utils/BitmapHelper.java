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
package it.feio.android.omninotes.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.apache.commons.io.FilenameUtils;

import java.util.concurrent.ExecutionException;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.helpers.AttachmentsHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.simplegallery.util.BitmapUtils;


public class BitmapHelper {

    /**
     * Retrieves a the bitmap relative to attachment based on mime type
     */
    public static Bitmap getBitmapFromAttachment(Context mContext, Attachment mAttachment, int width, int height) {
        Bitmap bmp = null;
        String path;
        mAttachment.getUri().getPath();

		// Video or image
		if (AttachmentsHelper.typeOf(mAttachment, Constants.MIME_TYPE_VIDEO, Constants.MIME_TYPE_IMAGE, Constants.MIME_TYPE_SKETCH)) {
			try {
				bmp = Glide.with(OmniNotes.getAppContext()).asBitmap()
						.apply(new RequestOptions()
								.centerCrop()
								.error(R.drawable.attachment_broken))
						.load(mAttachment.getUri())
						.submit(width, height).get();
			} catch (NullPointerException | InterruptedException | ExecutionException e) {
				bmp = null;
			}

			// Audio
        } else if (Constants.MIME_TYPE_AUDIO.equals(mAttachment.getMime_type())) {
            bmp = ThumbnailUtils.extractThumbnail(
                    BitmapUtils.decodeSampledBitmapFromResourceMemOpt(mContext.getResources().openRawResource(R
									.raw.play), width, height), width, height);

		// File
		} else if (Constants.MIME_TYPE_FILES.equals(mAttachment.getMime_type())) {

			// vCard
			if (Constants.MIME_TYPE_CONTACT_EXT.equals(FilenameUtils.getExtension(mAttachment.getName()))) {
				bmp = ThumbnailUtils.extractThumbnail(
						BitmapUtils.decodeSampledBitmapFromResourceMemOpt(mContext.getResources().openRawResource(R
										.raw.vcard), width, height), width, height);
			} else {
				bmp = ThumbnailUtils.extractThumbnail(
						BitmapUtils.decodeSampledBitmapFromResourceMemOpt(mContext.getResources().openRawResource(R
										.raw.files), width, height), width, height);
			}
		}

        return bmp;
    }


	public static Uri getThumbnailUri(Context mContext, Attachment mAttachment) {
		Uri uri = mAttachment.getUri();
		String mimeType = StorageHelper.getMimeType(uri.toString());
		if (!TextUtils.isEmpty(mimeType)) {
			String type = mimeType.split("/")[0];
			String subtype = mimeType.split("/")[1];
			switch (type) {
				case "image":
				case "video":
					// Nothing to do, bitmap will be retrieved from this
					break;
				case "audio":
					uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.play);
					break;
				default:
					int drawable = "x-vcard".equals(subtype) ? R.raw.vcard : R.raw.files;
					uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + drawable);
					break;
			}
		} else {
			uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.files);
		}
		return uri;
	}
}
