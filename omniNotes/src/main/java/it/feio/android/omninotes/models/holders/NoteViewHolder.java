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

package it.feio.android.omninotes.models.holders;

import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.neopixl.pixlui.components.textview.TextView;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.views.SquareImageView;


public class NoteViewHolder {

  public NoteViewHolder (View view) {
    ButterKnife.bind(this, view);
  }

  @BindView(R.id.root)
  public View root;
  @BindView(R.id.card_layout)
  public View cardLayout;
  @BindView(R.id.category_marker)
  public View categoryMarker;

  @BindView(R.id.note_title)
  public TextView title;
  @BindView(R.id.note_content)
  public TextView content;
  @BindView(R.id.note_date)
  public TextView date;

  @BindView(R.id.archivedIcon)
  public ImageView archiveIcon;
  @BindView(R.id.locationIcon)
  public ImageView locationIcon;
  @BindView(R.id.alarmIcon)
  public ImageView alarmIcon;
  @BindView(R.id.lockedIcon)
  public ImageView lockedIcon;
  @Nullable
  @BindView(R.id.attachmentIcon)
  public ImageView attachmentIcon;
  @Nullable
  @BindView(R.id.attachmentThumbnail)
  public SquareImageView attachmentThumbnail;
}
