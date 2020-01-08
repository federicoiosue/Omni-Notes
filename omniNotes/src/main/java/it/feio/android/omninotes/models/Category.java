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

import android.os.Parcel;
import android.os.Parcelable;
import it.feio.android.omninotes.commons.models.BaseCategory;


public class Category extends BaseCategory implements Parcelable {

  private Category (Parcel in) {
    setId(in.readLong());
    setName(in.readString());
    setDescription(in.readString());
    setColor(in.readString());
  }


  public Category () {
    super();
  }


  public Category (BaseCategory category) {
    super(category.getId(), category.getName(), category.getDescription(), category.getColor());
  }


  public Category (Long id, String title, String description, String color) {
    super(id, title, description, color);
  }


  public Category (Long id, String title, String description, String color, int count) {
    super(id, title, description, color, count);
  }


  @Override
  public int describeContents () {
    return 0;
  }


  @Override
  public void writeToParcel (Parcel parcel, int flags) {
    parcel.writeLong(getId());
    parcel.writeString(getName());
    parcel.writeString(getDescription());
    parcel.writeString(getColor());
  }


  @Override
  public String toString () {
    return getName();
  }


  /*
   * Parcelable interface must also have a static field called CREATOR, which is an object implementing the
   * Parcelable.Creator interface. Used to un-marshal or de-serialize object from Parcel.
   */
  public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {

    public Category createFromParcel (Parcel in) {
      return new Category(in);
    }


    public Category[] newArray (int size) {
      return new Category[size];
    }
  };
}
