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

import android.os.Parcel;
import android.os.Parcelable;


public class ParcelableUtil {

	public static byte[] marshall(Parcelable parceable) {
		Parcel parcel = Parcel.obtain();
		parceable.writeToParcel(parcel, 0);
		byte[] bytes = parcel.marshall();
		parcel.recycle();
		return bytes;
	}


	public static Parcel unmarshall(byte[] bytes) {
		Parcel parcel = Parcel.obtain();
		parcel.unmarshall(bytes, 0, bytes.length);
		parcel.setDataPosition(0); // This is extremely important!
		return parcel;
	}


	public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
		Parcel parcel = unmarshall(bytes);
		T result = creator.createFromParcel(parcel);
		parcel.recycle();
		return result;
	}



}
