package it.feio.android.omninotes.helpers;

import android.text.TextUtils;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;
import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.List;


public class NotesHelper {

	public static Note mergeNotes(List<Note> notes, boolean keepMergedNotes) {

		Note mergedNote = null;
		boolean locked = false;
		StringBuilder content = new StringBuilder();
		ArrayList<Attachment> attachments = new ArrayList<>();
		Category category = null;
		Long reminder = null;
		String reminderRecurrenceRule = null;
		Double latitude = null, longitude = null;

		for (Note note : notes) {

			if (mergedNote == null) {
				mergedNote = new Note();
				mergedNote.setTitle(note.getTitle());
				content.append(note.getContent());

			} else {
				if (content.length() > 0
						&& (!TextUtils.isEmpty(note.getTitle()) || !TextUtils.isEmpty(note.getContent()))) {
					content.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"))
							.append(Constants.MERGED_NOTES_SEPARATOR).append(System.getProperty("line.separator"))
							.append(System.getProperty("line.separator"));
				}
				if (!TextUtils.isEmpty(note.getTitle())) {
					content.append(note.getTitle());
				}
				if (!TextUtils.isEmpty(note.getTitle()) && !TextUtils.isEmpty(note.getContent())) {
					content.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
				}
				if (!TextUtils.isEmpty(note.getContent())) {
					content.append(note.getContent());
				}
			}

			locked = locked || note.isLocked();

			category = (Category) ObjectUtils.defaultIfNull(category, note.getCategory());

			String currentReminder = note.getAlarm();
			if (!TextUtils.isEmpty(currentReminder) && reminder == null) {
				reminder = Long.parseLong(currentReminder);
				reminderRecurrenceRule = note.getRecurrenceRule();
			}

			latitude = (Double) ObjectUtils.defaultIfNull(latitude, note.getLatitude());
			longitude = (Double) ObjectUtils.defaultIfNull(longitude, note.getLongitude());

			if (keepMergedNotes) {
				for (Attachment attachment : note.getAttachmentsList()) {
					attachments.add(StorageHelper.createAttachmentFromUri(OmniNotes.getAppContext(), attachment.getUri
							()));
				}
			} else {
				attachments.addAll(note.getAttachmentsList());
			}
		}

		// Resets all the attachments id to force their note re-assign when saved
		for (Attachment attachment : attachments) {
			attachment.setId(null);
		}

		// Sets merged values
		mergedNote.setContent(content.toString());
		mergedNote.setLocked(locked);
		mergedNote.setCategory(category);
		mergedNote.setAlarm(reminder);
		mergedNote.setRecurrenceRule(reminderRecurrenceRule);
		mergedNote.setLatitude(latitude);
		mergedNote.setLongitude(longitude);
		mergedNote.setAttachmentsList(attachments);

		return mergedNote;
	}
}
