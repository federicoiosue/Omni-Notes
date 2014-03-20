package it.feio.android.omninotes.models.views;

import it.feio.android.omninotes.models.listeners.OnViewTouchedListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class InterceptorLinearLayout extends LinearLayout {

	private OnViewTouchedListener mOnViewTouchedListener;

	public InterceptorLinearLayout(Context context) {
		super(context);
	}

	public InterceptorLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		mOnViewTouchedListener.onViewTouchOccurred();
		return super.onInterceptTouchEvent(ev);
	}

	public void setOnViewTouchedListener(OnViewTouchedListener mOnViewTouchedListener) {
		this.mOnViewTouchedListener = mOnViewTouchedListener;
	}

}
