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

import it.feio.android.omninotes.utils.EqualityChecker;
import it.feio.android.omninotes.utils.date.DateHelper;

import java.util.ArrayList;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class Note implements Parcelable {

	private int _id;
	private String title;
	private String content;
	private Long creation;
	private Long lastModification;
	private Boolean archived;
	private String alarm;
	private Double latitude;
	private Double longitude;
	private Category category;
	private Boolean locked;
	private Boolean checklist;
	private ArrayList<Attachment> attachmentsList = new ArrayList<Attachment>();
	private ArrayList<Attachment> attachmentsListOld = new ArrayList<Attachment>();
	
	// Not saved in DB
	private String address;
	private boolean passwordChecked = false;

	public Note() {
		super();
		this.title = "";
		this.content = "";
		this.archived = false;
		this.locked = false;
		this.checklist = false;
	}
	
	public Note(int _id, Long creation, Long lastModification, String title, String content, Integer archived,
			String alarm, String latitude, String longitude, Category category, Integer locked, Integer checklist) {
		super();
		this._id = _id;
		this.title = title;
		this.content = content;
		this.creation = creation;
		this.lastModification = lastModification;
		this.archived = archived == 1 ? true : false;
		this.alarm = alarm;
		setLatitude(latitude);
		setLongitude(longitude);
		setCategory(category);
		setLocked(locked == 1 ? true : false);
		setChecklist(checklist == 1 ? true : false);
	}

	public Note(Note note) {
		super();
		set_id(note.get_id());
		setTitle(note.getTitle());
		setContent(note.getContent());
		setCreation(note.getCreation());
		setLastModification(note.getLastModification());
		setArchived(note.isArchived());
		setAlarm(note.getAlarm());
		setLatitude(note.getLatitude());
		setLongitude(note.getLongitude());
		setCategory(note.getCategory());
		setLocked(note.isLocked());
		setChecklist(note.isChecklist());
		ArrayList<Attachment> list = new ArrayList<Attachment>();
		for (Attachment mAttachment : note.getAttachmentsList()) {
			list.add(mAttachment);
		}
		setAttachmentsList(list);
	}

	private Note(Parcel in) {
		set_id(in.readInt());
		setCreation(in.readString());
		setLastModification(in.readString());
		setTitle(in.readString());
		setContent(in.readString());
		setArchived(in.readInt());
		setAlarm(in.readString());
		setLatitude(in.readString());
		setLongitude(in.readString());
		setCategory((Category)in.readParcelable(Category.class.getClassLoader()));
		setLocked(in.readInt());
		setChecklist(in.readInt());
		in.readList(attachmentsList, Attachment.class.getClassLoader());
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int get_id() {
		return _id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getCreation() {
		return creation;
	}

	public void setCreation(Long creation) {
		this.creation = creation;
	}

	public void setCreation(String creation) {
		Long creationLong;
		try {
			creationLong = Long.parseLong(creation);
		} catch (NumberFormatException e) {
			creationLong = null;
		}
		this.creation = creationLong;
	}

	public String getCreationShort(Context mContext) {
		return DateHelper.getDateTimeShort(mContext, creation);
	}

	public Long getLastModification() {
		return lastModification;
	}

	public String getLastModificationShort(Context mContext) {
		return DateHelper.getDateTimeShort(mContext, lastModification);
	}

	public void setLastModification(Long lastModification) {
		this.lastModification = lastModification;
	}

	public void setLastModification(String lastModification) {
		Long lastModificationLong;
		try {
			lastModificationLong = Long.parseLong(lastModification);
		} catch (NumberFormatException e) {
			lastModificationLong = null;
		}
		this.lastModification = lastModificationLong;
	}

	public Boolean isArchived() {
		return archived == null || archived == false ? false : true;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public void setArchived(int archived) {
		this.archived = archived == 1 ? true : false;
	}

	public String getAlarm() {
		return alarm;
	}

	public String getAlarmShort(Context mContext) {
		if (getAlarm() == null) 
			return "";
		return DateHelper.getDateTimeShort(mContext, Long.parseLong(getAlarm()));
	}

	public void setAlarm(String alarm) {
		this.alarm = alarm;
	}

	public void setAlarm(long alarm) {
		this.alarm = String.valueOf(alarm);
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public void setLatitude(String latitude) {
		try {
			setLatitude(Double.parseDouble(latitude));
		} catch (NumberFormatException e) {
			latitude = null;
		} catch (NullPointerException e) {
			latitude = null;
		}
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public void setLongitude(String longitude) {
		try {
			setLongitude(Double.parseDouble(longitude));
		} catch (NumberFormatException e) {
			longitude = null;
		} catch (NullPointerException e) {
			latitude = null;
		}
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Boolean isLocked() {
		return locked == null || locked == false ? false : true;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public void setLocked(int locked) {
		this.locked = locked == 1 ? true : false;
	}

	public Boolean isChecklist() {
		return checklist == null || checklist == false ? false : true;
	}

	public void setChecklist(Boolean checklist) {
		this.checklist = checklist;
	}

	public void setChecklist(int checklist) {
		this.checklist = checklist == 1 ? true : false;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isPasswordChecked() {
		return passwordChecked;
	}

	public void setPasswordChecked(boolean passwordChecked) {
		this.passwordChecked = passwordChecked;
	}

	public ArrayList<Attachment> getAttachmentsList() {
		return attachmentsList;
	}

	public void setAttachmentsList(ArrayList<Attachment> attachmentsList) {
		this.attachmentsList = attachmentsList;
	}

	public void addAttachment(Attachment attachment) {
		this.attachmentsList.add(attachment);
	}

	public void backupAttachmentsList() {
		ArrayList<Attachment> attachmentsListOld = new ArrayList<Attachment>();
		for (Attachment mAttachment : getAttachmentsList()) {
			attachmentsListOld.add(mAttachment);
		}
		this.attachmentsListOld = attachmentsListOld;
	}

	public ArrayList<Attachment> getAttachmentsListOld() {
		return attachmentsListOld;
	}

	public void setAttachmentsListOld(ArrayList<Attachment> attachmentsListOld) {
		this.attachmentsListOld = attachmentsListOld;
	}

	public boolean equals(Object o) {
		boolean res = false;
		Note note;
		try {
			note = (Note) o;
		} catch (Exception e) {
			return res;
		}

		Object[] a = {get_id(), getTitle(), getContent(), getCreation(), getLastModification(), isArchived(), getAlarm(), getLatitude(), getLongitude(), getCategory(), isLocked(), isChecklist()};
		Object[] b = {note.get_id(), note.getTitle(), note.getContent(), note.getCreation(), note.getLastModification(), note.isArchived(), note.getAlarm(), note.getLatitude(), note.getLongitude(), note.getCategory(), note.isLocked(), note.isChecklist()};
		if (EqualityChecker.check(a, b)) {
			res = true;		
		}
		
		return res;
	}
	
	
	public boolean isChanged(Note note) {
		boolean res = false;
		res = !equals(note) || !getAttachmentsList().equals(note.getAttachmentsList());
		return res;
	}
	
	
	public boolean isEmpty() {
		Note emptyNote = new Note();
		// Field to exclude for comparison
		emptyNote.setCreation(getCreation());
		emptyNote.setCategory(getCategory());
		// Check
		if (isChanged(emptyNote))
			return false;
		else 
			return true;
	}
	

	public String toString() {
		return getTitle();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(get_id());
		parcel.writeString(String.valueOf(getCreation()));
		parcel.writeString(String.valueOf(getLastModification()));
		parcel.writeString(getTitle());
		parcel.writeString(getContent());
		parcel.writeInt(isArchived() ? 1 : 0);
		parcel.writeString(getAlarm());
		parcel.writeString(String.valueOf(getLatitude()));
		parcel.writeString(String.valueOf(getLongitude()));
		parcel.writeParcelable(getCategory(), 0);
		parcel.writeInt(isLocked() ? 1 : 0);
		parcel.writeInt(isChecklist() ? 1 : 0);
		parcel.writeList(getAttachmentsList());
	}

	/*
	 * Parcelable interface must also have a static field called CREATOR, which
	 * is an object implementing the Parcelable.Creator interface. Used to
	 * un-marshal or de-serialize object from Parcel.
	 */
	public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {

		public Note createFromParcel(Parcel in) {
			return new Note(in);
		}

		public Note[] newArray(int size) {
			return new Note[size];
		}
	};
}
