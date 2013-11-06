package it.feio.android.omninotes.models;


public class Note {

	private int _id;
	private String title;
	private String content;
	private String lastModification;


	public Note() {}
	

	public Note(String lastModification, String title, String content) {
		super();
		this.title = title;
		this.content = content;
		this.lastModification = lastModification;
	}
	

	public Note(int _id, String lastModification, String title, String content) {
		super();
		this._id = _id;
		this.title = title;
		this.content = content;
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
