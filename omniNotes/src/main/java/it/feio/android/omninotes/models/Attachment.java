/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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


public class Attachment extends it.feio.android.omninotes.commons.models.Attachment implements Parcelable {

    private Uri uri;


    public Attachment(Uri uri, String mime_type) {
        super();
        setUri(uri);
        setMime_type(mime_type);
    }


    public Attachment(int id, Uri uri, String name, int size, long length, String mime_type) {
        super();
        setId(id);
        setUri(uri);
        setName(name);
        setSize(size);
        setLength(length);
        setMime_type(mime_type);
    }


    private Attachment(Parcel in) {
        setId(in.readInt());
        setUri(Uri.parse(in.readString()));
        setMime_type(in.readString());
    }


    public Uri getUri() {
        return uri;
    }


    public void setUri(Uri uri) {
        this.uri = uri;
        setUriPath(uri.getPath());
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(getId());
        parcel.writeString(getUri().toString());
        parcel.writeString(getMime_type());
    }


    /*
     * Parcelable interface must also have a static field called CREATOR, which is an object implementing the
     * Parcelable.Creator interface. Used to un-marshal or de-serialize object from Parcel.
     */
    public static final Parcelable.Creator<Attachment> CREATOR = new Parcelable.Creator<Attachment>() {

        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }


        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
}
