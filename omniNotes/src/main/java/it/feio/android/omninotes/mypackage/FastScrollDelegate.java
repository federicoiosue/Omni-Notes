package it.feio.android.omninotes.mypackage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Interpolator;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 *
 * https://github.com/Mixiaoxiao/FastScroll-Everywhere FastScrollDelegate
 *
 * @author Mixiaoxiao 2016-08-28
 */
public class FastScrollDelegate {

    @SuppressWarnings("unused")
    private void log(String msg) {
        Log.d("FastScrollDelegate", msg);
    }

    public static interface FastScrollable {

        public void superOnTouchEvent(MotionEvent event);

        public int superComputeVerticalScrollExtent();

        public int superComputeVerticalScrollOffset();

        public int superComputeVerticalScrollRange();

        public View getFastScrollableView();

        public FastScrollDelegate getFastScrollDelegate();

        public void setNewFastScrollDelegate(FastScrollDelegate newDelegate);
    }

    public static interface OnFastScrollListener {
        public void onFastScrollStart(View view, FastScrollDelegate delegate);

        public void onFastScrolled(View view, FastScrollDelegate delegate, int touchDeltaY, int viewScrollDeltaY,
                                   float scrollPercent);

        public void onFastScrollEnd(View view, FastScrollDelegate delegate);
    }

    // codes from FastScroller (for AbsListView)
    /** Duration of fade-out animation. */
    public static int FASTSCROLLER_DURATION_FADE_OUT = 300;
    /** Duration of fade-in animation. */
    public static int FASTSCROLLER_DURATION_FADE_IN = 150;
    /** Inactivity timeout before fading controls. */
    public static long FASTSCROLLER_FADE_TIMEOUT = 1500;

    private static final int[] DRAWABLE_STATE_PRESSED = new int[] { android.R.attr.state_pressed };
    private static final int[] DRAWABLE_STATE_DEFAULT = new int[] {};

    private final View mView;
    private final float mDensity;
    private float mDownY;
    private final Rect mThumbRect;
    private Drawable mThumbDrawable;
    private final FastScrollable mFastScrollable;
    private int mThumbMinHeight;
    private final ScrollabilityCache mScrollCache;
    private IndicatorPopup mIndicatorPopup;
    private boolean mThumbDynamicHeight;

    private OnFastScrollListener mFastScrollListener;
    private boolean mIsHanlingTouchEvent = false;

    private FastScrollDelegate(final FastScrollable fastScrollable, int width, int height, Drawable thumbDrawable,
                               boolean isDynamicHeight) {
        super();
        this.mView = fastScrollable.getFastScrollableView();
        mView.setVerticalScrollBarEnabled(false);
        Context context = mView.getContext();
        this.mDensity = context.getResources().getDisplayMetrics().density;
        this.mThumbMinHeight = dp2px(FASTSCROLLER_MIN_HEIGHT_DP);
        this.mThumbRect = new Rect(0, 0, width, height);
        this.mThumbDrawable = thumbDrawable;
        this.mFastScrollable = fastScrollable;
        this.mScrollCache = new ScrollabilityCache(ViewConfiguration.get(context), mView);
        this.mThumbDynamicHeight = isDynamicHeight;
    }

    // ===========================================================
    // Useful methods
    // ===========================================================
    public void setThumbDrawable(Drawable drawable) {
        if (drawable == null) {
            throw new IllegalArgumentException("setThumbDrawable must NOT be NULL");
        }
        mThumbDrawable = drawable;
        updateThumbRect(0);
    }

    public void setThumbSize(int widthDp, int heightDp) {
        mThumbRect.left = mThumbRect.right - dp2px(widthDp);
        mThumbMinHeight = dp2px(heightDp);
        updateThumbRect(0);
    }

    public void setThumbDynamicHeight(boolean isDynamicHeight) {
        if (mThumbDynamicHeight != isDynamicHeight) {
            mThumbDynamicHeight = isDynamicHeight;
            updateThumbRect(0);
        }
    }

    public void setOnFastScrollListener(OnFastScrollListener l) {
        mFastScrollListener = l;
    }

    // ===========================================================
    // Delegate
    // ===========================================================

    // See View.class
    public boolean awakenScrollBars() {
        return awakenScrollBars(FASTSCROLLER_FADE_TIMEOUT);// Cache.scrollBarDefaultDelayBeforeFade
    }

    // See View.class
    private boolean initialAwakenScrollBars() {
        return awakenScrollBars(mScrollCache.scrollBarDefaultDelayBeforeFade * 4);
    }

    // See View.class
    public boolean awakenScrollBars(long startDelay) {
        ViewCompat.postInvalidateOnAnimation(mView);
        // log("awakenScrollBars call startDelay->" + startDelay);
        if (!mIsHanlingTouchEvent) {
            if (mScrollCache.state == ScrollabilityCache.OFF) {
                // FIXME: this is copied from WindowManagerService.
                // We should get this value from the system when it
                // is possible to do so.
                final int KEY_REPEAT_FIRST_DELAY = 750;
                startDelay = Math.max(KEY_REPEAT_FIRST_DELAY, startDelay);
            }
            // Tell mScrollCache when we should start fading. This may
            // extend the fade start time if one was already scheduled
            long fadeStartTime = AnimationUtils.currentAnimationTimeMillis() + startDelay;
            mScrollCache.fadeStartTime = fadeStartTime;
            mScrollCache.state = ScrollabilityCache.ON;
            // Schedule our fader to run, unscheduling any old ones first
            // if (mAttachInfo != null) {
            // mAttachInfo.mHandler.removeCallbacks(scrollCache);
            // mAttachInfo.mHandler.postAtTime(scrollCache, fadeStartTime);
            // }
            mView.removeCallbacks(mScrollCache);
            mView.postDelayed(mScrollCache, fadeStartTime - AnimationUtils.currentAnimationTimeMillis());
        }
        return false;
    }

    // ===========================================================
    // TouchEvent Delegate
    // ===========================================================
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return onInterceptTouchEventInternal(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return onTouchEventInternal(event);
    }

    // ===========================================================
    // TouchEvent Internal
    // ===========================================================
    private boolean onInterceptTouchEventInternal(MotionEvent ev) {
        final int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            // Just check if hit the thumb
            return onTouchEventInternal(ev);
        }
        return false;
    }

    private boolean onTouchEventInternal(MotionEvent event) {
        final int action = event.getActionMasked();
        final float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // log("onTouchEvent ACTION_DOWN");
                if (mScrollCache.state == ScrollabilityCache.OFF) {
                    mIsHanlingTouchEvent = false;
                    return false;
                }
                if (!mIsHanlingTouchEvent) {
                    updateThumbRect(0);
                    final float x = event.getX();
                    // Check if hit the thumb, Rect.contains(int x ,int y) is NOT
                    // exact
                    if (y >= mThumbRect.top && y <= mThumbRect.bottom && x >= mThumbRect.left && x <= mThumbRect.right) {
                        mIsHanlingTouchEvent = true;
                        mDownY = y;
                        // try to stop scroll
                        // step 0: call super ACTION_DOWN
                        mFastScrollable.superOnTouchEvent(event);
                        // step 1: call super ACTION_CANCEL
                        MotionEvent fakeCancelMotionEvent = MotionEvent.obtain(event);
                        fakeCancelMotionEvent.setAction(MotionEvent.ACTION_CANCEL);
                        mFastScrollable.superOnTouchEvent(fakeCancelMotionEvent);
                        fakeCancelMotionEvent.recycle();
                        // update ThumbDrawable state and report
                        // OnFastScrollListener
                        setPressedThumb(true);
                        // Call updateThumbRect to report
                        // OnFastScrollListener.onFastScrolled
                        updateThumbRect(0, true);
                        // Do NOT fade Thumb
                        mView.removeCallbacks(mScrollCache);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mIsHanlingTouchEvent) {
                    final int touchDeltaY = Math.round(y - mDownY);
                    if (touchDeltaY != 0) {
                        updateThumbRect(touchDeltaY);
                        // only touchDeltaY != 0, we save the touchY, to Avoid
                        // accuracy error
                        mDownY = y;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mIsHanlingTouchEvent) {
                    setPressedThumb(false);
                    mIsHanlingTouchEvent = false;
                    awakenScrollBars();
                }
                break;
            }
        }// End switch
        if (mIsHanlingTouchEvent) {
            mView.invalidate();
            mView.getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
        return false;
    }

    // ===========================================================
    // Delegate
    // ===========================================================
    /** Call after View.dispatchDraw() **/
    public void dispatchDrawOver(Canvas canvas) {
        onDrawScrollBars(canvas);
    }

    public void onAttachedToWindow() {
        initialAwakenScrollBars();
    }

    @SuppressLint("MissingSuperCall")
    // fuck this lint warning
    public void onDetachedFromWindow() {
        if (mIndicatorPopup != null) {
            mIndicatorPopup.dismiss();
        }
    }

    /**
     * Please check if the delegate is NULL before call this method If your view
     * has the android:visibility attr in xml, this method in view is called
     * before your delegate is created
     */
    public void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == View.VISIBLE) {
            // This compat method is interesting, KK has method
            // isAttachedToWindow
            // < KK is view.getWindowToken() != null
            if (ViewCompat.isAttachedToWindow(mView)) {
                // Same as mAttachInfo != null
                initialAwakenScrollBars();
            }

        }
    }

    public void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {
            initialAwakenScrollBars();
        }
    }

    // ===========================================================
    // Internal
    // ===========================================================

    private void onDrawScrollBars(Canvas canvas) {
        boolean invalidate = false;
        if (mIsHanlingTouchEvent) {
            mThumbDrawable.setAlpha(255);
        } else {
            // Copy from View.class
            final ScrollabilityCache cache = mScrollCache;
            // cache.scrollBar = mThumbDrawable;
            final int state = cache.state;
            if (state == ScrollabilityCache.OFF) {
                return;
            }
            if (state == ScrollabilityCache.FADING) {
                // We're fading -- get our fade interpolation
                if (cache.interpolatorValues == null) {
                    cache.interpolatorValues = new float[1];
                }
                float[] values = cache.interpolatorValues;
                // Stops the animation if we're done
                if (cache.scrollBarInterpolator.timeToValues(values) == Interpolator.Result.FREEZE_END) {
                    cache.state = ScrollabilityCache.OFF;
                } else {
                    // in View.class is "cache.scrollBar.mutate()"
                    mThumbDrawable.setAlpha(Math.round(values[0]));
                }
                invalidate = true;
            } else {
                // reset alpha, in View.class is "cache.scrollBar.mutate()"
                mThumbDrawable.setAlpha(255);
            }
        }

        // Draw the thumb
        if (updateThumbRect(0)) {
            final int scrollY = mView.getScrollY();
            final int scrollX = mView.getScrollX();
            mThumbDrawable.setBounds(mThumbRect.left + scrollX, mThumbRect.top + scrollY, mThumbRect.right + scrollX,
                    mThumbRect.bottom + scrollY);
            mThumbDrawable.draw(canvas);
        }
        if (invalidate) {
            mView.invalidate();
        }

    }

    private void setPressedThumb(boolean pressed) {
        mThumbDrawable.setState(pressed ? DRAWABLE_STATE_PRESSED : DRAWABLE_STATE_DEFAULT);
        mView.invalidate();
        if (mIndicatorPopup != null) {
            if (pressed) {
                mIndicatorPopup.show();
            } else {
                mIndicatorPopup.dismiss();
            }
        }
        if (mFastScrollListener != null) {
            if (pressed) {
                mFastScrollListener.onFastScrollStart(mView, this);
            } else {
                mFastScrollListener.onFastScrollEnd(mView, this);
            }

        }
    }

    private boolean updateThumbRect(int touchDeltaY) {
        return updateThumbRect(touchDeltaY, false);
    }

    /**
     * updateThumbRect
     *
     * @param touchDeltaY
     *            ,if touchDeltaY != 0, will report
     *            FastScrollListener.onFastScrolled
     * @param forceReportFastScrolled
     *            , if true, will force report FastScrollListener.onFastScrolled
     * @return false:Thumb return false means no need to draw thumb
     */
    private boolean updateThumbRect(int touchDeltaY, boolean forceReportFastScrolled) {
        final int thumbWidth = mThumbRect.width();
        mThumbRect.right = mView.getWidth();
        mThumbRect.left = mThumbRect.right - thumbWidth;
        final int scrollRange = mFastScrollable.superComputeVerticalScrollRange();// 整体的全部高度
        if (scrollRange <= 0) {// no content, 仅在有内容的时候绘制thumb
            return false;
        }
        final int scrollOffset = mFastScrollable.superComputeVerticalScrollOffset();// 上方已经滑动出本身范围的高度
        final int scrollExtent = mFastScrollable.superComputeVerticalScrollExtent();// 当前显示区域的高度
        final int scrollMaxOffset = scrollRange - scrollExtent;
        if (scrollMaxOffset <= 0) {// can not scroll, 内容部分不够或刚好充满
            return false;
        }
        final float scrollPercent = scrollOffset * 1f / (scrollMaxOffset);
        final float visiblePercent = scrollExtent * 1f / scrollRange;
        // log("scrollPercent->" + scrollPercent + " visiblePercent->" +
        // visiblePercent);
        final int viewHeight = mView.getHeight();
        final int thumbHeight = mThumbDynamicHeight ? Math
                .max(mThumbMinHeight, Math.round(visiblePercent * viewHeight)) : mThumbMinHeight;
        mThumbRect.bottom = mThumbRect.top + thumbHeight;
        final int thumbTop = Math.round((viewHeight - thumbHeight) * scrollPercent);
        mThumbRect.offsetTo(mThumbRect.left, thumbTop);

        if (mIndicatorPopup != null) {
            mIndicatorPopup.setOffset(mView.getWidth() - mIndicatorPopup.getPopupSize() - mThumbRect.width(),
                    -viewHeight + mThumbRect.centerY() - mIndicatorPopup.getPopupSize());
        }
        if (touchDeltaY != 0) {// compute the ScrollOffset, 按touchDeltaY计算滚动
            int newThumbTop = thumbTop + touchDeltaY;
            final int minThumbTop = 0;
            final int maxThumbTop = viewHeight - thumbHeight;
            if (newThumbTop > maxThumbTop) {
                newThumbTop = maxThumbTop;
            } else if (newThumbTop < minThumbTop) {
                newThumbTop = minThumbTop;
            }

            final float newScrollPercent = newThumbTop * 1f / maxThumbTop;// 百分比
            final int newScrollOffset = Math.round((scrollRange - scrollExtent) * newScrollPercent);
            final int viewScrollDeltaY = newScrollOffset - scrollOffset;
            if (mView instanceof AbsListView) {
                // Call scrollBy to AbsListView , not work correctly
                ((AbsListView) mView).smoothScrollBy(viewScrollDeltaY, 0);
            } else {
                mView.scrollBy(0, viewScrollDeltaY);

            }
            if (mFastScrollListener != null) {
                mFastScrollListener.onFastScrolled(mView, this, touchDeltaY, viewScrollDeltaY, newScrollPercent);
            }
        } else {
            if (forceReportFastScrolled) {
                if (mFastScrollListener != null) {
                    mFastScrollListener.onFastScrolled(mView, this, 0, 0, scrollPercent);
                }
            }
        }
        return true;
    }

    /** Copy from View.class **/
    private static class ScrollabilityCache implements Runnable {
        /*** Scrollbars are not visible */
        public static final int OFF = 0;
        /** * Scrollbars are visible */
        public static final int ON = 1;
        /** * Scrollbars are fading away */
        public static final int FADING = 2;
        public final int scrollBarDefaultDelayBeforeFade;
        public final int scrollBarFadeDuration;

        // public ScrollBarDrawable scrollBar;
        // public Drawable scrollBar;
        public float[] interpolatorValues;
        public View host;

        public final Interpolator scrollBarInterpolator = new Interpolator(1, 2);

        private static final float[] OPAQUE = { 255 };
        private static final float[] TRANSPARENT = { 0.0f };

        /**
         * When fading should start. This time moves into the future every time
         * a new scroll happens. Measured based on SystemClock.uptimeMillis()
         */
        public long fadeStartTime;

        /** * The current state of the scrollbars: ON, OFF, or FADING */
        public int state = OFF;

        public ScrollabilityCache(ViewConfiguration configuration, View host) {
            // scrollBarSize = configuration.getScaledScrollBarSize();
            scrollBarDefaultDelayBeforeFade = ViewConfiguration.getScrollDefaultDelay();
            scrollBarFadeDuration = ViewConfiguration.getScrollBarFadeDuration();
            this.host = host;
        }

        public void run() {
            long now = AnimationUtils.currentAnimationTimeMillis();
            if (now >= fadeStartTime) {

                // the animation fades the scrollbars out by changing
                // the opacity (alpha) from fully opaque to fully
                // transparent
                int nextFrame = (int) now;
                int framesCount = 0;

                Interpolator interpolator = scrollBarInterpolator;

                // Start opaque
                interpolator.setKeyFrame(framesCount++, nextFrame, OPAQUE);

                // End transparent
                nextFrame += scrollBarFadeDuration;
                interpolator.setKeyFrame(framesCount, nextFrame, TRANSPARENT);

                state = FADING;

                // Kick off the fade animation
                // host.invalidate(true);
                host.invalidate();
            }
        }
    }

    public View getView() {
        return mView;
    }

    private int dp2px(float dp) {
        return (int) (mDensity * dp + 0.5f);
    }

    public void setIndicatorText(String indicator) {
        if (mIndicatorPopup != null) {
            mIndicatorPopup.setIndicatorText(indicator);
        }
    }

    public void initIndicatorPopup(IndicatorPopup indicatorPopup) {
        mIndicatorPopup = indicatorPopup;
    }

    // ===========================================================
    // IndicatorPopup
    // ===========================================================
    public static class IndicatorPopup {

        public static class Builder {

            private final float density;
            private final View anchor;
            private int indicatorPopupColor = COLOR_THUMB_PRESSED;
            private int indicatorPopupSize;
            private int indicatorTextSize;
            private int indicatorMarginRight;
            private int indicatorPopupAnimationStyle = FASTSCROLLER_INDICATOR_POPUPANIMATIONSTYLE;

            public Builder(FastScrollDelegate delegate) {
                this.anchor = delegate.getView();
                this.density = anchor.getContext().getResources().getDisplayMetrics().density;
                indicatorPopupSize = dp2px(FASTSCROLLER_INDICATOR_SIZE_DP);
                indicatorTextSize = dp2px(FASTSCROLLER_INDICATOR_TEXTSIZE_DP);
                indicatorMarginRight = dp2px(FASTSCROLLER_INDICATOR_MARINRIGHT_DP);
            }

            public Builder indicatorPopupColor(int popupColor) {
                indicatorPopupColor = popupColor;
                return this;
            }

            public Builder indicatorPopupSize(int popupSizeDp) {
                indicatorPopupSize = dp2px(popupSizeDp);
                return this;
            }

            public Builder indicatorTextSize(int textSizeDp) {
                indicatorTextSize = dp2px(textSizeDp);
                return this;
            }

            public Builder indicatorMarginRight(int marginRightDp) {
                indicatorMarginRight = dp2px(marginRightDp);
                return this;
            }

            public Builder indicatorPopupAnimationStyle(int animationStyle) {
                indicatorPopupAnimationStyle = animationStyle;
                return this;
            }

            private int dp2px(float dp) {
                return (int) (density * dp + 0.5f);
            }

            public IndicatorPopup build() {
                return new IndicatorPopup(anchor, indicatorPopupColor, indicatorPopupSize, indicatorTextSize,
                        indicatorMarginRight, indicatorPopupAnimationStyle);
            }

        }

        final View anchor;
        final int popupSize;
        final int marginRight;
        final TextView bubbleView;
        int xOffset, yOffset;
        final PopupWindow popupWindow;

        @SuppressWarnings("deprecation")
        private IndicatorPopup(View anchor, int popupColor, int popupSize, int textSize, int marginRight,
                               int popupAnimationStyle) {
            super();
            this.anchor = anchor;
            this.popupSize = popupSize;
            this.marginRight = marginRight;
            this.bubbleView = new TextView(anchor.getContext());
            bubbleView.setGravity(Gravity.CENTER);
            bubbleView.setTextColor(Color.WHITE);
            bubbleView.setSingleLine();
            bubbleView.setBackgroundDrawable(new BubbleDrawable(popupColor));
            bubbleView.setEllipsize(TruncateAt.END);
            bubbleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            this.popupWindow = new PopupWindow(bubbleView, popupSize, popupSize, false);
            popupWindow.setAnimationStyle(popupAnimationStyle);
        }

        public int getPopupSize() {
            return popupSize;
        }

        public void setOffset(int xoff, int yoff) {
            this.xOffset = xoff;
            this.yOffset = yoff;
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.update(anchor, xoff - marginRight, yoff, popupSize, popupSize);
            }

        }

        public void show() {
            if (popupWindow != null && !popupWindow.isShowing()) {
                popupWindow.showAsDropDown(anchor, xOffset - marginRight, yOffset);
            }
        }

        public void dismiss() {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }

        public void setIndicatorText(String indicator) {
            bubbleView.setText(indicator);
        }
    }

    private static class BubbleDrawable extends Drawable {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF rectF = new RectF();

        public BubbleDrawable(int color) {
            super();
            paint.setColor(color);
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            final int w = bounds.width();
            final int h = bounds.height();
            path.reset();
            final float radius = Math.min(w, h) / 2f - 1f;
            final float cx = w / 2f;
            final float cy = h / 2f;
            rectF.set(cx - radius, cy - radius, cx + radius, cy + radius);
            // path.addArc()//Not work
            path.arcTo(rectF, 0, -270, true);
            path.lineTo(cx + radius, cy + radius);
            path.close();
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            paint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

    }

    // ==================
    // Builder
    // ==================

    public static int FASTSCROLLER_WIDTH_DP = 20;// 12;
    public static int FASTSCROLLER_MIN_HEIGHT_DP = 32;
    public static int FASTSCROLLER_THUMB_WIDTH = 4;
    public static int FASTSCROLLER_THUMB_INSET_TOP_BOTTOM_RIGHT = 4;

    public static int COLOR_THUMB_NORMAL = 0x80808080;
    public static int COLOR_THUMB_PRESSED = 0xff03a9f4;// 0xff45c01a;

    private static int FASTSCROLLER_INDICATOR_SIZE_DP = 72;
    private static int FASTSCROLLER_INDICATOR_MARINRIGHT_DP = 24;
    private static int FASTSCROLLER_INDICATOR_TEXTSIZE_DP = 36;
    private static int FASTSCROLLER_INDICATOR_POPUPANIMATIONSTYLE = android.R.style.Animation_Dialog;

    public static class Builder {
        private final float density;
        private final FastScrollable fastScrollable;
        private int width;
        private int height;
        private boolean isDynamicHeight = true;
        private Drawable thumbDrawable;
        private int thumbNormalColor = COLOR_THUMB_NORMAL;
        private int thumbPressedColor = COLOR_THUMB_PRESSED;

        public Builder(FastScrollable fastScrollable) {
            super();
            this.fastScrollable = fastScrollable;
            this.density = fastScrollable.getFastScrollableView().getContext().getResources().getDisplayMetrics().density;
            width = dp2px(FASTSCROLLER_WIDTH_DP);
            height = dp2px(FASTSCROLLER_MIN_HEIGHT_DP);

        }

        public Builder width(float widthDp) {
            width = dp2px(widthDp);
            return this;
        }

        public Builder height(float heightDp) {
            height = dp2px(heightDp);
            return this;
        }

        public Builder thumbNormalColor(int normalColor) {
            thumbNormalColor = normalColor;
            return this;
        }

        public Builder thumbPressedColor(int pressedColor) {
            thumbPressedColor = pressedColor;
            return this;
        }

        public Builder thumbDrawable(Drawable thumb) {
            thumbDrawable = thumb;
            return this;
        }

        public Builder dynamicHeight(boolean isDynamic) {
            isDynamicHeight = isDynamic;
            return this;
        }

        public FastScrollDelegate build() {
            if (this.thumbDrawable == null) {
                this.thumbDrawable = makeDefaultThumbDrawable();
            }
            return new FastScrollDelegate(fastScrollable, width, height, thumbDrawable, isDynamicHeight);
        }

        private Drawable makeDefaultThumbDrawable() {
            StateListDrawable stateListDrawable = new StateListDrawable();
            GradientDrawable pressedDrawable = new GradientDrawable();
            pressedDrawable.setColor(thumbPressedColor);
            final float radius = width / 2f;
            final int inset = dp2px(FASTSCROLLER_THUMB_INSET_TOP_BOTTOM_RIGHT);// inset
            final int insetLeft = width - inset - dp2px(FASTSCROLLER_THUMB_WIDTH);
            pressedDrawable.setCornerRadius(radius);
            stateListDrawable.addState(DRAWABLE_STATE_PRESSED, new InsetDrawable(pressedDrawable, insetLeft, inset,
                    inset, inset));
            GradientDrawable normalDrawable = new GradientDrawable();
            normalDrawable.setColor(thumbNormalColor);
            normalDrawable.setCornerRadius(radius);
            stateListDrawable.addState(DRAWABLE_STATE_DEFAULT, new InsetDrawable(normalDrawable, insetLeft, inset,
                    inset, inset));
            return stateListDrawable;
        }

        public int dp2px(float dp) {
            return (int) (dp * density + 0.5f);
        }
    }
}