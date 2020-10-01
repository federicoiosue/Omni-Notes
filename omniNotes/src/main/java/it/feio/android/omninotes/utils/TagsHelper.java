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

package it.feio.android.omninotes.utils;

import androidx.core.util.Pair;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.pixlui.links.UrlCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


public class TagsHelper {


  public static List<Tag> getAllTags () {
    return DbHelper.getInstance().getTags();
  }


  public static HashMap<String, Integer> retrieveTags (Note note) {
    HashMap<String, Integer> tagsMap = new HashMap<>();
    String[] words = (note.getTitle() + " " + note.getContent()).replaceAll("\n", " ").trim().split(" ");
    for (String word : words) {
      String parsedHashtag = UrlCompleter.parseHashtag(word);
      if (StringUtils.isNotEmpty(parsedHashtag)) {
        int count = tagsMap.get(parsedHashtag) == null ? 0 : tagsMap.get(parsedHashtag);
        tagsMap.put(parsedHashtag, ++count);
      }
    }
    return tagsMap;
  }


  public static Pair<String, List<Tag>> addTagToNote (List<Tag> tags, Integer[] selectedTags, Note note) {
    StringBuilder sbTags = new StringBuilder();
    List<Tag> tagsToRemove = new ArrayList<>();
    HashMap<String, Integer> tagsMap = retrieveTags(note);

    List<Integer> selectedTagsList = Arrays.asList(selectedTags);
    for (int i = 0; i < tags.size(); i++) {
      if (mapContainsTag(tagsMap, tags.get(i))) {
        if (!selectedTagsList.contains(i)) {
          tagsToRemove.add(tags.get(i));
        }
      } else {
        if (selectedTagsList.contains(i)) {
          if (sbTags.length() > 0) {
            sbTags.append(" ");
          }
          sbTags.append(tags.get(i));
        }
      }
    }
    return Pair.create(sbTags.toString(), tagsToRemove);
  }


  private static boolean mapContainsTag (HashMap<String, Integer> tagsMap, Tag tag) {
    for (String tagsMapItem : tagsMap.keySet()) {
      if (tagsMapItem.equals(tag.getText())) {
        return true;
      }
    }
    return false;
  }


  public static Pair<String, String> removeTag (String noteTitle, String noteContent, List<Tag> tagsToRemove) {
    String title = noteTitle, content = noteContent;
    for (Tag tagToRemove : tagsToRemove) {
      if (StringUtils.isNotEmpty(title)) {
        title = removeTagFromText(title, tagToRemove);
      }
      if (StringUtils.isNotEmpty(content)) {
        content = removeTagFromText(content, tagToRemove);
      }
    }
    return new Pair<>(title, content);
  }


  private static String removeTagFromText (String text, Tag tag) {
    final String tagSeparator = "[ <>,-.()\\[\\]{}!?]";
    final String tagReplacement = "";
    String regex =
            "(?<=" + tagSeparator + "|^)" +
            tag.getText() +
            "(?=" + tagSeparator + "|$|\\v)";
    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(text);
    return matcher.replaceAll(tagReplacement).trim();
  }


  public static String[] getTagsArray (List<Tag> tags) {
    String[] tagsArray = new String[tags.size()];
    for (int i = 0; i < tags.size(); i++) {
      tagsArray[i] = tags.get(i).getText().substring(1) + " (" + tags.get(i).getCount() + ")";
    }
    return tagsArray;
  }


  public static Integer[] getPreselectedTagsArray (Note note, List<Tag> tags) {
    List<Integer> t = new ArrayList<>();
    for (String noteTag : TagsHelper.retrieveTags(note).keySet()) {
      for (Tag tag : tags) {
        if (tag.getText().equals(noteTag)) {
          t.add(tags.indexOf(tag));
          break;
        }
      }
    }
    return t.toArray(new Integer[]{});
  }


  public static Integer[] getPreselectedTagsArray (List<Note> notes, List<Tag> tags) {
    HashSet<Integer> set = new HashSet<>();
    for (Note note : notes) {
      set.addAll(Arrays.asList(getPreselectedTagsArray(note, tags)));
    }
    return set.toArray(new Integer[]{});
  }
}
