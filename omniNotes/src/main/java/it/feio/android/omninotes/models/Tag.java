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

import android.os.Parcel;
import android.os.Parcelable;


public class Tag extends it.feio.android.omninotes.commons.models.Tag implements Parcelable {

    private Tag(Parcel in) {
        setText(in.readString());
        setCount(in.readInt());
    }


    public Tag() {
        super();
    }


    public Tag(String text, Integer count) {
        super(text, count);
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(getText());
        parcel.writeInt(getCount());
    }


    @Override
    public String toString() {
        return getText();
    }


    /*
     * Parcelable interface must also have a static field called CREATOR, which is an object implementing the
     * Parcelable.Creator interface. Used to un-marshal or de-serialize object from Parcel.
     */
    public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {

        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }


        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
}
