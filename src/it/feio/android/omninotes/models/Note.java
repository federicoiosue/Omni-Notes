package it.feio.android.omninotes.models;


public class Note {

	private int _id;
	private String title;
	private String content;
	private String creation;
	private String lastModification;


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

	public String getlastModification() {
		return lastModification;
	}

	public void setlastModification(String lastModification) {
		this.lastModification = lastModification;
	}
	
	
	public String toString(){
		return getTitle();
	}
}
