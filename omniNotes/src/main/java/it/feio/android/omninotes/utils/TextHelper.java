package it.feio.android.omninotes.utils;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Pair;
import it.feio.android.omninotes.models.Note;
import it.feio.android.pixlui.links.RegexPatternsConstants;

import java.util.*;
import java.util.regex.Matcher;


public class TextHelper {
	/**
	 * @param note
	 * @return
	 */
	public static Spanned[] parseTitleAndContent(Context mContext, Note note) {

		final int CONTENT_SUBSTRING_LENGTH = 300;
		final int TITLE_SUBSTRING_OF_CONTENT_LIMIT = 50;

		// Defining title and content texts
		String titleText, contentText;

		String content = note.getContent().trim();

		if (note.getTitle().length() > 0) {
			titleText = note.getTitle();
			contentText = limit(note.getContent().trim(), 0, CONTENT_SUBSTRING_LENGTH, false, true);
		} else {
			titleText = limit(content, 0, TITLE_SUBSTRING_OF_CONTENT_LIMIT, true, false);
			contentText = limit(content.replace(titleText, "").trim(), 0, CONTENT_SUBSTRING_LENGTH, false, false);
		}
		content = null;

		// Masking title and content string if note is locked
		if (note.isLocked()
				&& !mContext.getSharedPreferences(Constants.PREFS_NAME, mContext.MODE_MULTI_PROCESS).getBoolean(
						"settings_password_access", false)) {
			// This checks if a part of content is used as title and should be partially masked
			if (!note.getTitle().equals(titleText) && titleText.length() > 3) {
				titleText = limit(titleText, 0, 4, false, false);
			}
			contentText = "";
		}

		// Replacing checkmarks symbols with html entities
		Spanned titleSpanned, contentSpanned;
		if (note.isChecklist()) {
			titleText = titleText.replace(it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM,
					it.feio.android.checklistview.interfaces.Constants.CHECKED_ENTITY).replace(
					it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM,
					it.feio.android.checklistview.interfaces.Constants.UNCHECKED_ENTITY);
			titleSpanned = Html.fromHtml(titleText);
			contentText = contentText
					.replace(it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM,
							it.feio.android.checklistview.interfaces.Constants.CHECKED_ENTITY)
					.replace(it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM,
							it.feio.android.checklistview.interfaces.Constants.UNCHECKED_ENTITY)
					.replace(System.getProperty("line.separator"), "<br/>");
			contentSpanned = Html.fromHtml(contentText);
		} else {
			titleSpanned = new SpannedString(titleText);
			contentSpanned = new SpannedString(contentText);
		}

		return new Spanned[] { titleSpanned, contentSpanned };
	}


	public static String limit(String value, int start, int length, boolean singleLine, boolean elipsize) {
		if (start > value.length()) { return null; }
		StringBuilder buf = new StringBuilder(value.substring(start));
		int indexNewLine = buf.indexOf(System.getProperty("line.separator"));
		int endIndex = singleLine && indexNewLine < length ? indexNewLine : length < buf.length() ? length : -1;
		if (endIndex != -1) {
			buf.setLength(endIndex);
			if (elipsize) {
				buf.append("...");
			}
		}
		return buf.toString();
	}


	public static String capitalize(String string) {
		StringBuilder res = new StringBuilder();
		res.append(string.substring(0, 1).toUpperCase(Locale.getDefault())).append(
				string.substring(1, string.length()).toLowerCase(Locale.getDefault()));
		return res.toString();
	}


    public static HashMap<String, Boolean> retrieveTags(Note note) {
        HashMap<String, Boolean> tagsMap = new HashMap<String, Boolean>();
        Matcher matcher = RegexPatternsConstants.HASH_TAG.matcher(note.getTitle() + " " + note.getContent());
        while (matcher.find()) {
            tagsMap.put(matcher.group().trim(), false);
        }
        return tagsMap;
    }


    public static Pair<String, List<String>> addTagToNote(List<String> tags, Integer[] selectedTags, Note note) {
        StringBuilder sbTags = new StringBuilder();
        List<String> tagsToRemove = new ArrayList<String>();
        HashMap<String, Boolean> tagsMap = retrieveTags(note);

        for (int i = 0; i < selectedTags.length; i++) {
            if (tagsMap.containsKey(tags.get(selectedTags[i]))) {
                tagsMap.remove(tags.get(selectedTags[i]));
            } else {
                tagsMap.put(tags.get(selectedTags[i]), true);
            }
        }

        for (Map.Entry<String, Boolean> tagMapEntry : tagsMap.entrySet()) {
            if (tagMapEntry.getValue()) {
                if (sbTags.length() > 0) {
                    sbTags.append(" ");
                }
                sbTags.append(tagMapEntry.getKey());
            } else {
                tagsToRemove.add(tagMapEntry.getKey());
            }
        }
        return new Pair(sbTags.toString(), tagsToRemove);
    }



}
