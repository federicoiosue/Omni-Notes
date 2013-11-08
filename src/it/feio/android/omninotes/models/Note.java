package it.feio.android.omninotes.models;

import it.feio.android.omninotes.utils.Constants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.analytics.tracking.android.Log;


public class Note {

	private int _id;
	private String title;
	private String content;
	private String creation;
	private String lastModification;
	private Boolean archived;


	public Note() {}
	

	public Note(String creation, String lastModification, String title, String content) {
		super();
		this.title = title;
		this.content = content;
		this.creation = creation;
		this.lastModification = lastModification;
	}
	

	public Note(int _id, String creation, String lastModification, String title, String content) {
		super();
		this._id = _id;
		this.title = title;
		this.content = content;
		this.creation = creation;
		this.lastModification = lastModification;
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

	public String getCreation() {
		return creation;
	}

	public void setCreation(String creation) {
		this.creation = creation;
	}

	public String getCreationShort() {
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_EU);
		Date d;
		try {
			d = sdf.parse(creation);
		} catch (ParseException e) {
			Log.e(e);
			return creation;
		}
		sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SHORT);
		return sdf.format(d);
	}

	public String getlastModification() {
		return lastModification;
	}

	public String getlastModificationShort() {
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_EU);
		Date d;
		try {
			d = sdf.parse(lastModification);
		} catch (ParseException e) {
			Log.e(e);
			return lastModification;
		}
		sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SHORT);
		return sdf.format(d);
	}

	public void setlastModification(String lastModification) {
		this.lastModification = lastModification;
	}
	
	
	public Boolean isArchived() {
		return archived;
	}


	public void setArchived(Boolean archived) {
		this.archived = archived;
	}


	public String toString(){
		return getTitle();
	}
}
