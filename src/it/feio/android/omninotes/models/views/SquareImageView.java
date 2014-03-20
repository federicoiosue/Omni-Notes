package it.feio.android.omninotes.models.views;

import java.lang.ref.WeakReference;

import it.feio.android.omninotes.async.BitmapWorkerTask;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImageView extends ImageView {
	
	private WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
	
	public SquareImageView(Context context) {
		super(context);
		setScaleType(ScaleType.CENTER_CROP);
	}

	public SquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); 
	}

    public void setBitmapWorkerTask(BitmapWorkerTask mBitmapWorkerTask) {
        this.bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(mBitmapWorkerTask);
    }

    public BitmapWorkerTask getBitmapWorkerTask() {
    	if (bitmapWorkerTaskReference != null) {
    		return bitmapWorkerTaskReference.get();
    	} else {
    		return null;
    	}
    }
}