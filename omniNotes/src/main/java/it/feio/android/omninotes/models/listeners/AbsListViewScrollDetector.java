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

package it.feio.android.omninotes.models.listeners;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;


public abstract class AbsListViewScrollDetector implements RecyclerView.OnScrollChangeListener {

    private AbsListView mListView;
    private int mScrollThreshold;


    public abstract void onScrollUp();

    public abstract void onScrollDown();

    @Override
    public void onScrollChange(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        boolean isSignificantDelta = Math.abs(oldScrollY - scrollY) > mScrollThreshold;
        if (isSignificantDelta) {
            if (oldScrollY > scrollY) {
                onScrollUp();
            } else {
                onScrollDown();
            }
        }
    }

    public void setScrollThreshold(int scrollThreshold) {
        mScrollThreshold = scrollThreshold;
    }


    public void setListView(@NonNull AbsListView listView) {
        mListView = listView;
    }



    private int getTopItemScrollY() {
        if (mListView == null || mListView.getChildAt(0) == null) {
            return 0;
        }
        View topChild = mListView.getChildAt(0);
        return topChild.getTop();
    }
}
