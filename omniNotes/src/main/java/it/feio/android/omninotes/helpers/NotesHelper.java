/*
 * Copyright (C) 2013-2019 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.helpers;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.helpers.count.CountFactory;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.StatsSingleNote;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;
import it.feio.android.omninotes.utils.TagsHelper;


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
        ArrayList<Attachment> attachments = new ArrayList<>();
        String reminder = null;
        String reminderRecurrenceRule = null;
        Double latitude = null;
        Double longitude = null;

        Note mergedNote = new Note();
        mergedNote.setTitle(notes.get(0).getTitle());
        mergedNote.setArchived(notes.get(0).isArchived());
        mergedNote.setCategory(notes.get(0).getCategory());
        StringBuilder content = new StringBuilder();
        // Just first note title must not be included into the content
        boolean includeTitle = false;

        for (Note note : notes) {
            appendContent(note, content, includeTitle);
            locked = locked || note.isLocked();
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
        mergedNote.setAlarm(reminder);
        mergedNote.setRecurrenceRule(reminderRecurrenceRule);
        mergedNote.setLatitude(latitude);
        mergedNote.setLongitude(longitude);
        mergedNote.setAttachmentsList(attachments);

        return mergedNote;
    }

    /**
     * Retrieves statistics data for a single note
     */
    public static StatsSingleNote getNoteInfos(Note note) {
        StatsSingleNote infos = new StatsSingleNote();

        int words, chars;
        if (note.isChecklist()) {
            infos.setChecklistCompletedItemsNumber(StringUtils.countMatches(note.getContent(), it.feio.android.checklistview
                    .interfaces.Constants.CHECKED_SYM));
            infos.setChecklistItemsNumber(infos.getChecklistCompletedItemsNumber() +
                    StringUtils.countMatches(note.getContent(), it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM));
        }
        infos.setTags(TagsHelper.retrieveTags(note).size());
        words = getWords(note);
        chars = getChars(note);
        infos.setWords(words);
        infos.setChars(chars);

        int attachmentsAll = 0, images = 0, videos = 0, audioRecordings = 0, sketches = 0, files = 0;
        for (Attachment attachment : note.getAttachmentsList()) {
            if (Constants.MIME_TYPE_IMAGE.equals(attachment.getMime_type())) {
                images++;
            } else if (Constants.MIME_TYPE_VIDEO.equals(attachment.getMime_type())) {
                videos++;
            } else if (Constants.MIME_TYPE_AUDIO.equals(attachment.getMime_type())) {
                audioRecordings++;
            } else if (Constants.MIME_TYPE_SKETCH.equals(attachment.getMime_type())) {
                sketches++;
            } else if (Constants.MIME_TYPE_FILES.equals(attachment.getMime_type())) {
                files++;
            }
        }
        infos.setAttachments(attachmentsAll);
        infos.setImages(images);
        infos.setVideos(videos);
        infos.setAudioRecordings(audioRecordings);
        infos.setSketches(sketches);
        infos.setFiles(files);

        if (note.getCategory() != null) {
            infos.setCategoryName(note.getCategory().getName());
        }

        return infos;
    }

    /**
     * Counts words in a note
     */
    public static int getWords(Note note) {
        return CountFactory.getWordCounter().countWords(note);
    }

    /**
     * Counts chars in a note
     */
    public static int getChars(Note note) {
        return CountFactory.getWordCounter().countChars(note);
    }

}
