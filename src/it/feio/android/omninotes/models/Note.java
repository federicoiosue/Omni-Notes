package it.feio.android.omninotes.models;

import it.feio.android.omninotes.utils.Constants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
	private List<Attachment> attachmentsList = new ArrayList<Attachment>();


	public Note() {
		super();
		this.archived = false;
	}
	

	public Note(Long creation, Long lastModification, String title, String content, Boolean archived, String alarm) {
		super();
		this.title = title;
		this.content = content;
		this.creation = creation;
		this.lastModification = lastModification;
		this.archived = archived;
		this.alarm = alarm;
	}
	

	public Note(int _id, Long creation, Long lastModification, String title, String content, Boolean archived, String alarm) {
		super();
		this._id = _id;
		this.title = title;
		this.content = content;
		this.creation = creation;
		this.lastModification = lastModification;
		this.archived = archived;
		this.alarm = alarm;
	}
	

	public Note(Long creation, Long lastModification, String title, String content, Integer archived, String alarm) {
		super();
		this.title = title;
		this.content = content;
		this.creation = creation;
		this.lastModification = lastModification;
		this.archived = archived == 1 ? true : false;
		this.alarm = alarm;
	}
	

	public Note(int _id, Long creation, Long lastModification, String title, String content, Integer archived, String alarm) {
		super();
		this._id = _id;
		this.title = title;
		this.content = content;
		this.creation = creation;
		this.lastModification = lastModification;
		this.archived = archived == 1 ? true : false;
		this.alarm = alarm;
	}

	private Note(Parcel in) {
		set_id(in.readInt());
		setCreation(in.readString());
		setLastModification(in.readString());
		setTitle(in.readString());
		setContent(in.readString());
		setArchived(in.readInt());
		setAlarm(in.readString());
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

	public String getCreationShort() {
		Calendar.getInstance().setTimeInMillis(creation);
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SHORT);
		return sdf.format(Calendar.getInstance().getTimeInMillis());
	}

	public Long getLastModification() {
		return lastModification;
	}

	public String getLastModificationShort() {
		Calendar.getInstance().setTimeInMillis(lastModification);
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SHORT);
		return sdf.format(Calendar.getInstance().getTimeInMillis());
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
		return archived;
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


	public void setAlarm(String alarm) {
		this.alarm = alarm;
	}


	public List<Attachment> getAttachmentsList() {
		return attachmentsList;
	}


	public void setAttachmentsList(List<Attachment> attachmentsList) {
		this.attachmentsList = attachmentsList;
	}


	public String toString(){
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
