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

package it.feio.android.omninotes;

import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import it.feio.android.omninotes.databinding.ActivityNoteInfosBinding;
import it.feio.android.omninotes.helpers.NotesHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.StatsSingleNote;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;


public class NoteInfosActivity extends Activity {

  private ActivityNoteInfosBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityNoteInfosBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    Note note = Objects.requireNonNull(getIntent().getExtras()).getParcelable(INTENT_NOTE);
    populateViews(note);
  }

  private void populateViews(Note note) {
    StatsSingleNote infos = NotesHelper.getNoteInfos(note);

    populateView(binding.noteInfosCategory, infos.getCategoryName());
    populateView(binding.noteInfosTags, infos.getTags());
    populateView(binding.noteInfosChars, infos.getChars());
    populateView(binding.noteInfosWords, infos.getWords());
    populateView(binding.noteInfosChecklistItems, infos.getChecklistItemsNumber());
    populateView(binding.noteInfosCompletedChecklistItems, getChecklistCompletionState(infos),
        !note.isChecklist());
    populateView(binding.noteInfosImages, infos.getImages());
    populateView(binding.noteInfosVideos, infos.getVideos());
    populateView(binding.noteInfosAudiorecordings, infos.getAudioRecordings());
    populateView(binding.noteInfosSketches, infos.getSketches());
    populateView(binding.noteInfosFiles, infos.getFiles());
  }

  static String getChecklistCompletionState(StatsSingleNote infos) {
    int percentage = Math.round(
        (float) infos.getChecklistCompletedItemsNumber() / infos.getChecklistItemsNumber() * 100);
    return infos.getChecklistCompletedItemsNumber() + " (" + percentage + "%)";
  }

  private void populateView(TextView textView, int numberValue) {
    String stringValue = numberValue > 0 ? String.valueOf(numberValue) : "";
    populateView(textView, stringValue);
  }

  private void populateView(TextView textView, String value) {
    populateView(textView, value, false);
  }

  private void populateView(TextView textView, String value, boolean forceHide) {
    if (StringUtils.isNotEmpty(value) && !forceHide) {
      textView.setText(value);
    } else {
      ((View) textView.getParent()).setVisibility(View.GONE);
    }
  }

}
