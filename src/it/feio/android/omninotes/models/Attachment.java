package it.feio.android.omninotes.models;

import android.net.Uri;

public class Attachment {
	private int id;
	private Uri uri;

	public Attachment(Uri uri) {
		super();
		this.uri = uri;
	}

	public Attachment(int id, Uri uri) {
		super();
		this.id = id;
		this.uri = uri;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}
}
