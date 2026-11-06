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

import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import it.feio.android.checklistview.dragging.ChecklistViewItemOnDragListener;
import it.feio.android.checklistview.models.CheckListView;
import it.feio.android.checklistview.models.CheckListViewItem;

/**
 * Drags started outside the checklist (text selection, other applications) carry a local state that
 * is not a {@link View}, while the checklist library casts it to a {@link View} without checking it.
 * The listeners installed here hand an event over to the library only when its local state is a
 * {@link View}, which is always the case for the drags the checklist starts itself.
 */
public final class ChecklistDragListenerGuard {

  private ChecklistDragListenerGuard() {
  }

  static boolean isChecklistDrag(Object localState) {
    return localState instanceof View;
  }

  static OnDragListener guarded(OnDragListener library) {
    return (view, event) -> isChecklistDrag(event.getLocalState()) && library.onDrag(view, event);
  }

  static OnDragListener dropOnly(OnDragListener delegate) {
    return (view, event) -> {
      if (event.getAction() == DragEvent.ACTION_DROP) {
        delegate.onDrag(view, event);
      }
      return true;
    };
  }

  /**
   * Replaces the drag listeners the checklist library wired on a converted checklist and on its
   * items with guarded equivalents, so that drags started outside the checklist never reach the
   * library. Items added later are guarded once the library has finished wiring them.
   *
   * @param checkListView checklist produced by a conversion, with its items already added
   */
  public static void install(CheckListView checkListView) {
    Guard guard = new Guard(new ChecklistViewItemOnDragListener());
    checkListView.setOnDragListener(guard.guarded);
    for (int i = 0; i < checkListView.getChildCount(); i++) {
      guard.applyTo(checkListView.getChildAt(i));
    }
    checkListView.setOnHierarchyChangeListener(guard);
  }

  private static final class Guard implements ViewGroup.OnHierarchyChangeListener {

    private final OnDragListener guarded;
    private final OnDragListener onDrop;

    private Guard(OnDragListener library) {
      this.guarded = guarded(library);
      this.onDrop = dropOnly(this.guarded);
    }

    private void applyTo(View child) {
      if (child instanceof CheckListViewItem) {
        CheckListViewItem item = (CheckListViewItem) child;
        item.setOnDragListener(item.isHintItem() ? onDrop : guarded);
      }
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
      child.post(() -> applyTo(child));
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
    }
  }
}
