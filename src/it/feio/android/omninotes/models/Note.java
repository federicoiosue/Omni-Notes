package it.feio.android.omninotes.models;

import it.feio.android.omninotes.utils.Constants;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Note {

	private int _id;
	private String title;
	private String content;
	private Long creation;
	private Long lastModification;
	private Boolean archived;
	private String alarm;


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


	public String toString(){
		return getTitle();
	}
}
