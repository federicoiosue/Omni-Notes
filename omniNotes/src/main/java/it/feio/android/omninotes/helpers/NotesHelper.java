package it.feio.android.omninotes.helpers;

import android.text.TextUtils;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class NotesHelper {

    public static boolean haveSameId(Note note, Note currentNote) {
            return currentNote != null
            && currentNote.get_id() != null
            && currentNote.get_id().equals(note.get_id());

    }

    public static StringBuilder appendContent(Note note, StringBuilder content, boolean includeTitle) {
        if (content.length() > 0
                && (!StringUtils.isEmpty(note.getTitle()) || !StringUtils.isEmpty(note.getContent()))) {
            content.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"))
                    .append(Constants.MERGED_NOTES_SEPARATOR).append(System.getProperty("line.separator"))
                    .append(System.getProperty("line.separator"));
        }
        if (includeTitle && !StringUtils.isEmpty(note.getTitle())) {
            content.append(note.getTitle());
        }
        if (!StringUtils.isEmpty(note.getTitle()) && !StringUtils.isEmpty(note.getContent())) {
            content.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (!StringUtils.isEmpty(note.getContent())) {
            content.append(note.getContent());
        }
        return content;
    }

    public static void addAttachments(boolean keepMergedNotes, Note note, ArrayList<Attachment> attachments) {
        if (keepMergedNotes) {
            for (Attachment attachment : note.getAttachmentsList()) {
                attachments.add(StorageHelper.createAttachmentFromUri(OmniNotes.getAppContext(), attachment.getUri
                        ()));
            }
        } else {
            attachments.addAll(note.getAttachmentsList());
        }
    }

	public static Note mergeNotes(List<Note> notes, boolean keepMergedNotes) {
		boolean locked = false;
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		Category category = null;
		String reminder = null;
		String reminderRecurrenceRule = null;
		Double latitude = null, longitude = null;

		Note mergedNote = new Note();
		mergedNote.setTitle(notes.get(0).getTitle());
		StringBuilder content = new StringBuilder();
		// Just first note title must not be included into the content
		boolean includeTitle = false;

		for (Note note : notes) {
			appendContent(note, content, includeTitle);
			locked = locked || note.isLocked();
			category = (Category) ObjectUtils.defaultIfNull(category, note.getCategory());
			String currentReminder = note.getAlarm();
			if (!StringUtils.isEmpty(currentReminder) && reminder == null) {
				reminder = currentReminder;
				reminderRecurrenceRule = note.getRecurrenceRule();
			}
			latitude = (Double) ObjectUtils.defaultIfNull(latitude, note.getLatitude());
			longitude = (Double) ObjectUtils.defaultIfNull(longitude, note.getLongitude());
			addAttachments(keepMergedNotes, note, attachments);
			includeTitle = true;
		}

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
