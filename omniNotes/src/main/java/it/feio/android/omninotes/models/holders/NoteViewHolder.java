/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import com.neopixl.pixlui.components.textview.TextView;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.views.SquareImageView;


public class NoteViewHolder {

	public NoteViewHolder(View view) {
		ButterKnife.inject(this, view);
	}

	@InjectView(R.id.root) public View root;
	@InjectView(R.id.card_layout) public View cardLayout;
	@InjectView(R.id.category_marker) public View categoryMarker;

	@InjectView(R.id.note_title) public TextView title;
	@InjectView(R.id.note_content) public TextView content;
	@InjectView(R.id.note_date) public TextView date;

	@InjectView(R.id.archivedIcon) public ImageView archiveIcon;
	@InjectView(R.id.locationIcon) public ImageView locationIcon;
	@InjectView(R.id.alarmIcon) public ImageView alarmIcon;
	@InjectView(R.id.lockedIcon) public ImageView lockedIcon;
	@Optional @InjectView(R.id.attachmentIcon) public ImageView attachmentIcon;

	@Optional @InjectView(R.id.attachmentThumbnail) public SquareImageView attachmentThumbnail;
}