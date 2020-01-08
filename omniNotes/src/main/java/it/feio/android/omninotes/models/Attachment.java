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
package it.feio.android.omninotes.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import it.feio.android.omninotes.commons.models.BaseAttachment;
import java.util.Calendar;


public class Attachment extends BaseAttachment implements Parcelable {

  private Uri uri;


  public Attachment (Uri uri, String mimeType) {
    this(Calendar.getInstance().getTimeInMillis(), uri, null, 0, 0, mimeType);
  }


  public Attachment (long id, Uri uri, String name, long size, long length, String mimeType) {
    super(id, uri != null ? uri.getPath() : null, name, size, length, mimeType);
    setUri(uri);
  }


  public Attachment (BaseAttachment attachment) {
    super(attachment.getId(), attachment.getUriPath(), attachment.getName(), attachment.getSize(), attachment
        .getLength(), attachment.getMime_type());
    this.uri = Uri.parse(attachment.getUriPath());
  }


  private Attachment (Parcel in) {
    setId(in.readLong());
    setUri(Uri.parse(in.readString()));
    setMime_type(in.readString());
  }


  public Uri getUri () {
    return uri;
  }


  public void setUri (Uri uri) {
    this.uri = uri;
    setUriPath(uri != null ? uri.toString() : "");
  }


  @Override
  public int describeContents () {
    return 0;
  }


  @Override
  public void writeToParcel (Parcel parcel, int flags) {
    parcel.writeLong(getId());
    parcel.writeString(getUri().toString());
    parcel.writeString(getMime_type());
  }


  /*
   * Parcelable interface must also have a static field called CREATOR, which is an object implementing the
   * Parcelable.Creator interface. Used to un-marshal or de-serialize object from Parcel.
   */
  public static final Parcelable.Creator<Attachment> CREATOR = new Parcelable.Creator<Attachment>() {

    public Attachment createFromParcel (Parcel in) {
      return new Attachment(in);
    }


    public Attachment[] newArray (int size) {
      return new Attachment[size];
    }
  };
}
