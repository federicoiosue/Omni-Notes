package it.feio.android.omninotes.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableNote implements Parcelable {

	private Note note;

	public ParcelableNote(Note note) {
		super();
		this.note = note;
	}

	public Note getNote() {
		return note;
	}

	private ParcelableNote(Parcel in) {
		note = new Note();
		note.set_id(in.readInt());
		note.setCreation(in.readString());
		note.setLastModification(in.readString());
		note.setTitle(in.readString());
		note.setContent(in.readString());
		note.setArchived(in.readInt());
	}

	/*
	 * you can use hashCode() here.
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * Actual object Serialization/flattening happens here. You need to individually Parcel each property of your object.
	 */
	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(note.get_id());
		parcel.writeString(String.valueOf(note.getCreation()));
		parcel.writeString(String.valueOf(note.getLastModification()));
		parcel.writeString(note.getTitle());
		parcel.writeString(note.getContent());
		parcel.writeInt(note.isArchived() ? 1 : 0);
	}

	/*
	 * Parcelable interface must also have a static field called CREATOR, which is an object implementing the Parcelable.Creator interface. Used to un-marshal or de-serialize object from Parcel.
	 */
	public static final Parcelable.Creator<ParcelableNote> CREATOR = new Parcelable.Creator<ParcelableNote>() {

		public ParcelableNote createFromParcel(Parcel in) {
			return new ParcelableNote(in);
		}

		public ParcelableNote[] newArray(int size) {
			return new ParcelableNote[size];
		}
	};
}