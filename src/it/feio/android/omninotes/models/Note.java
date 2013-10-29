package it.feio.android.omninotes.models;


public class Note {

	private int _id;
	private String title;
	private String content;
	private String timestamp;


	public Note() {}
	

	public Note(String timestamp, String title, String content) {
		super();
		this.title = title;
		this.content = content;
		this.timestamp = timestamp;
	}
	

	public Note(int _id, String timestamp, String title, String content) {
		super();
		this._id = _id;
		this.title = title;
		this.content = content;
		this.timestamp = timestamp;
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

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	
	public String toString(){
		return getTitle();
	}
}
