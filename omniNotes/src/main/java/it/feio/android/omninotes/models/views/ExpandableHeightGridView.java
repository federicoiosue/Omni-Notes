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
package it.feio.android.omninotes.models.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;


public class ExpandableHeightGridView extends GridView {

    //	private boolean expanded = false;
    private int itemHeight;


    public ExpandableHeightGridView(Context context) {
        super(context);
    }


    public ExpandableHeightGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public ExpandableHeightGridView(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
    }
//
//	public boolean isExpanded() {
//		return expanded;
//	}


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		if (isExpanded()) {
        // Calculate entire height by providing a very large height hint.
        // View.MEASURED_SIZE_MASK represents the largest height possible.
        int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
//		} else {
//			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		}
    }


//	public void setExpanded(boolean expanded) {
//		this.expanded = expanded;
//	}


    public void autoresize() {
        // Set gridview height
//	    ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int items = getAdapter().getCount();
        int columns = items == 1 ? 1 : 2;

        setNumColumns(columns);
//    	itemHeight = Constants.THUMBNAIL_SIZE * 2 / columns;
//    	layoutParams.height = ( (items / columns) + (items % columns) ) * itemHeight; //this is in pixels
//	    
//	    setLayoutParams(layoutParams);
    }


    public int getItemHeight() {
        return itemHeight;
    }
}
