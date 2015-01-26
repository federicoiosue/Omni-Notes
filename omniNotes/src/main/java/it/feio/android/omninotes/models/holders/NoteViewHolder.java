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

import com.neopixl.pixlui.components.textview.TextView;

import it.feio.android.omninotes.models.views.SquareImageView;


public class NoteViewHolder {

    public View root;
    public View cardLayout;
    public View categoryMarker;

    public TextView title;
    public TextView content;
    public TextView date;

    public ImageView archiveIcon;
    public ImageView locationIcon;
    public ImageView alarmIcon;
    public ImageView lockedIcon;
    public ImageView attachmentIcon;

    public SquareImageView attachmentThumbnail;
}