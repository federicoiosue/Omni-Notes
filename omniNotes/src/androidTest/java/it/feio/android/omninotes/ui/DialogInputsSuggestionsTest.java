/*
 * Copyright (C) 2013-2026 Federico Iosue (omninotes@iosue.it)
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

package it.feio.android.omninotes.ui;


import static org.junit.Assert.assertFalse;

import android.view.View;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import it.feio.android.omninotes.R;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DialogInputsSuggestionsTest extends BaseEspressoTest {

  @Test
  public void categoryTitleHasNoSuggestions() {
    assertNoSuggestions(R.layout.activity_category, R.id.category_title);
  }

  @Test
  public void passwordQuestionHasNoSuggestions() {
    assertNoSuggestions(R.layout.activity_password, R.id.question);
  }

  @Test
  public void exportFileNameHasNoSuggestions() {
    assertNoSuggestions(R.layout.dialog_backup_layout, R.id.export_file_name);
  }

  private void assertNoSuggestions(int layoutRes, int viewId) {
    var layout = new AtomicReference<View>();
    InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
        layout.set(activityRule.getActivity().getLayoutInflater().inflate(layoutRes, null)));

    assertFalse(((TextView) layout.get().findViewById(viewId)).isSuggestionsEnabled());
  }
}
