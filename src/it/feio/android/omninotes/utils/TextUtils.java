package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.models.Note;
import android.text.Html;
import android.text.Spanned;

public class TextUtils {
	/**
	 * @param note
	 * @return
	 */
	public static Spanned[] parseTitleAndContent(Note note) {
		// Defining title and content texts	
		String titleText, contentText;
		if (note.getTitle().length() > 0) {
			titleText = note.getTitle();
			contentText = note.getContent();
		} else {
//			String[] arr = note.getContent().split(System.getProperty("line.separator"));
//			titleText = arr.length > 0 ? arr[0] : "";
//			contentText = arr.length > 1 ? arr[1] : "";
			int index = note.getContent() != null ? note.getContent().indexOf(System.getProperty("line.separator")) : -1;
			int length = note.getContent().length();
			titleText = index == -1 ? note.getContent() : note.getContent().substring(0, index);
//			contentText = index == -1 ? "" : note.getContent().substring(index, note.getContent().length());
			contentText = index == -1 ? "" : note.getContent().substring(index, length < index + 50 ? length : index + 50);
		}
		
		// Masking title and content string if note is locked
		if (note.isLocked()) {
			// This checks if a part of content is used as title and should be partially masked 
			if (!note.getTitle().equals(titleText) && titleText.length() > 2) {	
				titleText = titleText.substring(0, 2) + titleText.substring(2).replaceAll(".", Constants.MASK_CHAR);
			}
			contentText = contentText.replaceAll(".", Constants.MASK_CHAR);
		}
		
		// Replacing checkmarks symbols with html entities
		titleText = titleText
				.replace(it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM,
				it.feio.android.checklistview.interfaces.Constants.CHECKED_ENTITY)
				.replace(it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM,
				it.feio.android.checklistview.interfaces.Constants.UNCHECKED_ENTITY);
		contentText = contentText
				.replace(it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM,
				it.feio.android.checklistview.interfaces.Constants.CHECKED_ENTITY)
				.replace(it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM,
				it.feio.android.checklistview.interfaces.Constants.UNCHECKED_ENTITY)
				.replace(System.getProperty("line.separator"), "<br/>");

		

		return new Spanned[] { Html.fromHtml(titleText), Html.fromHtml(contentText) };	
	}
}
