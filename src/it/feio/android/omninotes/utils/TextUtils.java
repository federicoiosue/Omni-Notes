package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.models.Note;

import java.util.Locale;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;

public class TextUtils {
	/**
	 * @param note
	 * @return
	 */
	public static Spanned[] parseTitleAndContent(Context mContext, Note note) {
		
		final int CONTENT_SUBSTRING_LENGTH = 300; 
		
		// Defining title and content texts	
		String titleText, contentText;
		
		String content = limit(note.getContent().trim(), CONTENT_SUBSTRING_LENGTH);
		
		if (note.getTitle().length() > 0) {
			titleText = note.getTitle();
			contentText = content;
		} else {
			int index = content != null ? content.indexOf(System.getProperty("line.separator")) : -1;
			titleText = index == -1 ? content : content.substring(0, index);
			contentText = index == -1 ? "" : content.substring(index);
		}
		content = null;
		
		// Masking title and content string if note is locked
		if (note.isLocked()
				&& !mContext.getSharedPreferences(Constants.PREFS_NAME, mContext.MODE_MULTI_PROCESS).getBoolean("settings_password_access", false)) {
			// This checks if a part of content is used as title and should be partially masked 
			if (!note.getTitle().equals(titleText) && titleText.length() > 2) {	
				titleText = titleText.substring(0, 2) + titleText.substring(2).replaceAll(".", Constants.MASK_CHAR);
			}
			contentText = contentText.replaceAll(".", Constants.MASK_CHAR);
		}
		
		// Replacing checkmarks symbols with html entities
		Spanned titleSpanned, contentSpanned;
		if (note.isChecklist()) {
			titleText = titleText
					.replace(it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM,
					it.feio.android.checklistview.interfaces.Constants.CHECKED_ENTITY)
					.replace(it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM,
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
		

		return new Spanned[] {titleSpanned, contentSpanned};	
	}
	
	
	
	
	public static String limit(String value, int length) {
		StringBuilder buf = new StringBuilder(value);
		if (buf.length() > length) {
			buf.setLength(length);
		      buf.append("...");
		}
		return buf.toString();
	}
	
	
	
	public static String capitalize(String string) {
		StringBuilder res = new StringBuilder();
		res
			.append(string.substring(0, 1).toUpperCase(Locale.getDefault()))
			.append(string.substring(1, string.length()).toLowerCase(Locale.getDefault()));		
		return res.toString();
	}
}
