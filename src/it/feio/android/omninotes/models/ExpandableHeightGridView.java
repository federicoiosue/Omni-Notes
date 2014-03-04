/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes.models;

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

//	@Override
//	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
////		if (isExpanded()) {
//			// Calculate entire height by providing a very large height hint.
//			// View.MEASURED_SIZE_MASK represents the largest height possible.
//			int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
//					MeasureSpec.AT_MOST);
//			super.onMeasure(widthMeasureSpec, expandSpec);
//
//			ViewGroup.LayoutParams params = getLayoutParams();
//			params.height = getMeasuredHeight();
////		} else {
////			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
////		}
//	}

	
	
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
