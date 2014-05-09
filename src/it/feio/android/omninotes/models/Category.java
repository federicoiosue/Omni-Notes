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

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable {
	private Integer id;
	private String name;
	private String description;
	private String color;

	private Category(Parcel in) {
		setId(in.readInt());
		setName(in.readString());
		setDescription(in.readString());
		setColor(in.readString());
	}
	
	public Category(){
		super();
	}
	
	
	public Category(Integer id, String title, String description, String color) {
		super();
		this.id = id;
		this.name = title;
		this.description = description;
		this.color = color;
	}




	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String title) {
		this.name = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(getId());
		parcel.writeString(getName());		
		parcel.writeString(getDescription());
		parcel.writeString(getColor());
	}
	
	@Override
	public String toString() {
		return getName();
	}

	/*
	 * Parcelable interface must also have a static field called CREATOR, which
	 * is an object implementing the Parcelable.Creator interface. Used to
	 * un-marshal or de-serialize object from Parcel.
	 */
	public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {

		public Category createFromParcel(Parcel in) {
			return new Category(in);
		}

		public Category[] newArray(int size) {
			return new Category[size];
		}
	};
}
