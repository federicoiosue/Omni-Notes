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

public class NavigationItem {


  private int arrayIndex;
  private String text;
  private int icon;
  private int iconSelected;


  public NavigationItem (int arrayIndex, String text, int icon, int iconSelected) {
    this.arrayIndex = arrayIndex;
    this.text = text;
    this.icon = icon;
    this.iconSelected = iconSelected;
  }


  public int getArrayIndex () {
    return arrayIndex;
  }


  public void setArrayIndex (int arrayIndex) {
    this.arrayIndex = arrayIndex;
  }


  public String getText () {
    return text;
  }


  public void setText (String text) {
    this.text = text;
  }


  public int getIcon () {
    return icon;
  }


  public void setIcon (int icon) {
    this.icon = icon;
  }


  public int getIconSelected () {
    return iconSelected;
  }


  public void setIconSelected (int iconSelected) {
    this.iconSelected = iconSelected;
  }
}
