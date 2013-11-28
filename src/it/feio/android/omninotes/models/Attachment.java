package it.feio.android.omninotes.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Attachment implements Parcelable {
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

	private Attachment(Parcel in) {
		setId(in.readInt());
		setUri(Uri.parse(in.readString()));
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(id);
		parcel.writeString(uri.toString());		
	}

	/*
	 * Parcelable interface must also have a static field called CREATOR, which
	 * is an object implementing the Parcelable.Creator interface. Used to
	 * un-marshal or de-serialize object from Parcel.
	 */
	public static final Parcelable.Creator<Attachment> CREATOR = new Parcelable.Creator<Attachment>() {

		public Attachment createFromParcel(Parcel in) {
			return new Attachment(in);
		}

		public Attachment[] newArray(int size) {
			return new Attachment[size];
		}
	};
}
