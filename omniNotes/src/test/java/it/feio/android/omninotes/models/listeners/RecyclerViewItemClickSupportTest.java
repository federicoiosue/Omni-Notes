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

package it.feio.android.omninotes.models.listeners;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RecyclerViewItemClickSupportTest {

  private RecyclerView recyclerView;
  private View itemView;
  private RecyclerView.ViewHolder holder;
  private RecyclerViewItemClickSupport.OnItemClickListener itemClickListener;
  private RecyclerViewItemClickSupport.OnItemLongClickListener itemLongClickListener;
  private View.OnClickListener clickListener;
  private View.OnLongClickListener longClickListener;

  @Before
  public void setUp() {
    recyclerView = mock(RecyclerView.class);
    itemView = mock(View.class);
    holder = mock(RecyclerView.ViewHolder.class);
    itemClickListener = mock(RecyclerViewItemClickSupport.OnItemClickListener.class);
    itemLongClickListener = mock(RecyclerViewItemClickSupport.OnItemLongClickListener.class);

    RecyclerViewItemClickSupport.addTo(recyclerView)
        .setOnItemClickListener(itemClickListener)
        .setOnItemLongClickListener(itemLongClickListener);

    var attachCaptor = ArgumentCaptor.forClass(RecyclerView.OnChildAttachStateChangeListener.class);
    verify(recyclerView).addOnChildAttachStateChangeListener(attachCaptor.capture());
    attachCaptor.getValue().onChildViewAttachedToWindow(itemView);

    var clickCaptor = ArgumentCaptor.forClass(View.OnClickListener.class);
    verify(itemView).setOnClickListener(clickCaptor.capture());
    clickListener = clickCaptor.getValue();

    var longClickCaptor = ArgumentCaptor.forClass(View.OnLongClickListener.class);
    verify(itemView).setOnLongClickListener(longClickCaptor.capture());
    longClickListener = longClickCaptor.getValue();

    when(recyclerView.findContainingViewHolder(itemView)).thenReturn(holder);
  }

  @Test
  public void clickOnItemWithoutPositionIsIgnored() {
    setHolderPosition(RecyclerView.NO_POSITION);

    clickListener.onClick(itemView);

    verify(itemClickListener, never()).onItemClicked(any(), anyInt(), any());
  }

  @Test
  public void clickOnItemWithPositionIsForwarded() {
    setHolderPosition(3);

    clickListener.onClick(itemView);

    verify(itemClickListener).onItemClicked(recyclerView, 3, itemView);
  }

  @Test
  public void longClickOnItemWithoutPositionIsIgnored() {
    setHolderPosition(RecyclerView.NO_POSITION);

    boolean handled = longClickListener.onLongClick(itemView);

    assertFalse(handled);
    verify(itemLongClickListener, never()).onItemLongClicked(any(), anyInt(), any());
  }

  @Test
  public void longClickOnItemWithPositionIsForwarded() {
    setHolderPosition(3);
    when(itemLongClickListener.onItemLongClicked(recyclerView, 3, itemView)).thenReturn(true);

    boolean handled = longClickListener.onLongClick(itemView);

    assertTrue(handled);
    verify(itemLongClickListener).onItemLongClicked(recyclerView, 3, itemView);
  }

  @SuppressWarnings("deprecation")
  private void setHolderPosition(int position) {
    when(holder.getBindingAdapterPosition()).thenReturn(position);
    when(holder.getAdapterPosition()).thenReturn(position);
  }
}
