/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
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
package it.feio.android.omninotes.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Attachment implements Parcelable {
	private int id;
	private Uri uri;
	private String name;
	private long size;
	private long length;
	private String mime_type;
	
	// Not stored in db
//	private boolean moveWhenNoteSaved = true;

	public Attachment(Uri uri, String mime_type) {
		super();
		this.uri = uri;
		this.setMime_type(mime_type);		
	}

	public Attachment(int id, Uri uri, String name, int size, long length, String mime_type) {
		super();
		this.id = id;
		this.uri = uri;
		this.name = name;
		this.size = size;
		this.length = length;
		this.setMime_type(mime_type);
	}

	private Attachment(Parcel in) {
		setId(in.readInt());
		setUri(Uri.parse(in.readString()));
		setMime_type(in.readString());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getMime_type() {
		return mime_type;
	}

	public void setMime_type(String mime_type) {
		this.mime_type = mime_type;
	}

//	public boolean getMoveWhenNoteSaved() {
//		return moveWhenNoteSaved;
//	}
//
//	public void setMoveWhenNoteSaved(boolean moveWhenNoteSaved) {
//		this.moveWhenNoteSaved = moveWhenNoteSaved;
//	}
	
	

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(id);
		parcel.writeString(uri.toString());		
		parcel.writeString(mime_type);
	}

	/*
	 * Parcelable interface must also have a static field called CREATOR, which
	 * is an object implementing the Parcelable.Creator interface. Used to
	 * un-marshal or de-serialize object from Parcel.
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
