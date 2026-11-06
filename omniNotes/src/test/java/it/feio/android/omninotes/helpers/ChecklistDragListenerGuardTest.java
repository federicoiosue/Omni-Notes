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

package it.feio.android.omninotes.helpers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import it.feio.android.checklistview.models.CheckListView;
import it.feio.android.checklistview.models.CheckListViewItem;
import it.feio.android.omninotes.BaseUnitTest;
import org.junit.Test;

public class ChecklistDragListenerGuardTest extends BaseUnitTest {

  private final View view = mock(View.class);
  private final OnDragListener library = mock(OnDragListener.class);

  @Test
  public void isChecklistDragRejectsLocalStatesThatAreNotViews() {
    assertFalse(ChecklistDragListenerGuard.isChecklistDrag(null));
    assertFalse(ChecklistDragListenerGuard.isChecklistDrag("dragged text"));
    assertFalse(ChecklistDragListenerGuard.isChecklistDrag(new Object()));
  }

  @Test
  public void isChecklistDragAcceptsChecklistItems() {
    assertTrue(ChecklistDragListenerGuard.isChecklistDrag(mock(View.class)));
  }

  @Test
  public void guardedListenerIgnoresDragsStartedOutsideTheChecklist() {
    DragEvent event = dragEventWithLocalState("dragged text");

    assertFalse(ChecklistDragListenerGuard.guarded(library).onDrag(view, event));
    verify(library, never()).onDrag(view, event);
  }

  @Test
  public void guardedListenerForwardsChecklistDrags() {
    DragEvent event = dragEventWithLocalState(mock(View.class));
    when(library.onDrag(view, event)).thenReturn(true);

    assertTrue(ChecklistDragListenerGuard.guarded(library).onDrag(view, event));
    verify(library).onDrag(view, event);
  }

  @Test
  public void dropGuardedListenerForwardsOnlyDrops() {
    DragEvent drop = dragEventWithAction(DragEvent.ACTION_DROP);
    when(drop.getLocalState()).thenReturn(mock(View.class));
    OnDragListener listener = ChecklistDragListenerGuard.dropOnly(
        ChecklistDragListenerGuard.guarded(library));

    assertTrue(listener.onDrag(view, drop));
    listener.onDrag(view, dragEventWithAction(DragEvent.ACTION_DRAG_LOCATION));

    verify(library, times(1)).onDrag(view, drop);
  }

  @Test
  public void installWrapsContainerAndExistingItems() {
    CheckListView listView = mock(CheckListView.class);
    CheckListViewItem item = mock(CheckListViewItem.class);
    when(listView.getChildCount()).thenReturn(1);
    when(listView.getChildAt(0)).thenReturn(item);
    when(item.isHintItem()).thenReturn(false);

    ChecklistDragListenerGuard.install(listView);

    verify(listView).setOnDragListener(any(OnDragListener.class));
    verify(item).setOnDragListener(any(OnDragListener.class));
    verify(listView).setOnHierarchyChangeListener(any(ViewGroup.OnHierarchyChangeListener.class));
  }

  private DragEvent dragEventWithLocalState(Object localState) {
    DragEvent event = dragEventWithAction(DragEvent.ACTION_DRAG_STARTED);
    when(event.getLocalState()).thenReturn(localState);
    return event;
  }

  private DragEvent dragEventWithAction(int action) {
    DragEvent event = mock(DragEvent.class);
    when(event.getAction()).thenReturn(action);
    return event;
  }

}
