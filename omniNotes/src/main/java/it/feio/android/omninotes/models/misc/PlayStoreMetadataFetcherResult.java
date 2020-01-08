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

package it.feio.android.omninotes.models.misc;

public class PlayStoreMetadataFetcherResult {

  private String datePublished;
  private String fileSize;
  private String numDownloads;
  private String softwareVersion;
  private String operatingSystems;
  private String contentRating;

  public String getDatePublished () {
    return datePublished;
  }

  public void setDatePublished (String datePublished) {
    this.datePublished = datePublished;
  }

  public String getFileSize () {
    return fileSize;
  }

  public void setFileSize (String fileSize) {
    this.fileSize = fileSize;
  }

  public String getNumDownloads () {
    return numDownloads;
  }

  public void setNumDownloads (String numDownloads) {
    this.numDownloads = numDownloads;
  }

  public String getSoftwareVersion () {
    return softwareVersion;
  }

  public void setSoftwareVersion (String softwareVersion) {
    this.softwareVersion = softwareVersion;
  }

  public String getOperatingSystems () {
    return operatingSystems;
  }

  public void setOperatingSystems (String operatingSystems) {
    this.operatingSystems = operatingSystems;
  }

  public String getContentRating () {
    return contentRating;
  }

  public void setContentRating (String contentRating) {
    this.contentRating = contentRating;
  }

}
