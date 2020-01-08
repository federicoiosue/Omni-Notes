/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.helpers.count;

import it.feio.android.omninotes.models.Note;
import rx.Observable;

public class DefaultWordCounter implements WordCounter {

  @Override
  public int countWords (Note note) {
    int count = 0;
    String[] fields = {note.getTitle(), note.getContent()};
    for (String field : fields) {
      field = sanitizeTextForWordsAndCharsCount(note, field);
      boolean word = false;
      int endOfLine = field.length() - 1;
      for (int i = 0; i < field.length(); i++) {
        // if the char is a letter, word = true.
        if (Character.isLetter(field.charAt(i)) && i != endOfLine) {
          word = true;
          // if char isn't a letter and there have been letters before, counter goes up.
        } else if (!Character.isLetter(field.charAt(i)) && word) {
          count++;
          word = false;
          // last word of String; if it doesn't end with a non letter, it  wouldn't count without this.
        } else if (Character.isLetter(field.charAt(i)) && i == endOfLine) {
          count++;
        }
      }
    }
    return count;
  }

  @Override
  public int countChars (Note note) {
    String titleAndContent = note.getTitle() + "\n" + note.getContent();
    return Observable
        .from(sanitizeTextForWordsAndCharsCount(note, titleAndContent).split(""))
        .filter(s -> !s.matches("\\s"))
        .count().toBlocking().single();
  }

}
