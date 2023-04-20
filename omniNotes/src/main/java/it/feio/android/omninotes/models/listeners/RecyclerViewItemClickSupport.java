/*
 * Copyright (C) 2013-2023 Federico Iosue (federico@iosue.it)
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

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import it.feio.android.omninotes.R;

public class RecyclerViewItemClickSupport {

  private final RecyclerView mRecyclerView;
  private OnItemClickListener mOnItemClickListener;
  private OnItemLongClickListener mOnItemLongClickListener;
  private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if (mOnItemClickListener != null) {
        var holder = mRecyclerView.getChildViewHolder(v);
        mOnItemClickListener.onItemClicked(mRecyclerView, holder.getAdapterPosition(), v);
      }
    }
  };
  private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View v) {
      if (mOnItemLongClickListener != null) {
        var holder = mRecyclerView.getChildViewHolder(v);
        return mOnItemLongClickListener
            .onItemLongClicked(mRecyclerView, holder.getAdapterPosition(), v);
      }
      return false;
    }
  };
  private final RecyclerView.OnChildAttachStateChangeListener mAttachListener
      = new RecyclerView.OnChildAttachStateChangeListener() {
    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
      if (mOnItemClickListener != null) {
        view.setOnClickListener(mOnClickListener);
      }
      if (mOnItemLongClickListener != null) {
        view.setOnLongClickListener(mOnLongClickListener);
      }
    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {
      // Nothing to do
    }

  };

  private RecyclerViewItemClickSupport(RecyclerView recyclerView) {
    mRecyclerView = recyclerView;
    mRecyclerView.setTag(R.id.item_click_support, this);
    mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
  }

  public static RecyclerViewItemClickSupport addTo(RecyclerView view) {
    var item = (RecyclerViewItemClickSupport) view.getTag(R.id.item_click_support);
    if (item == null) {
      item = new RecyclerViewItemClickSupport(view);
    }
    return item;
  }

  public RecyclerViewItemClickSupport setOnItemClickListener(OnItemClickListener listener) {
    mOnItemClickListener = listener;
    return this;
  }

  public RecyclerViewItemClickSupport setOnItemLongClickListener(OnItemLongClickListener listener) {
    mOnItemLongClickListener = listener;
    return this;
  }

  public interface OnItemClickListener {

    void onItemClicked(RecyclerView recyclerView, int position, View v);
  }

  public interface OnItemLongClickListener {

    boolean onItemLongClicked(RecyclerView recyclerView, int position, View v);
  }

}