package it.feio.android.omninotes.models;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DrawingPanel extends View implements OnTouchListener {

	private Canvas mCanvas;
	private Path mPath;
	private Paint mPaint;//, circlePaint, outercirclePaint;

	private ArrayList<Path> undonePaths = new ArrayList<Path>();
	private ArrayList<Path> paths = new ArrayList<Path>();

	// private ArrayList<Path> undonePaths = new ArrayList<Path>();
	private float xleft, xright, xtop, xbottom;
	private int width;
	private int height;
	private Bitmap bitmap;

	public DrawingPanel(Context context) {
		super(context);
		setFocusable(true);
		setFocusableInTouchMode(true);

		this.setOnTouchListener(this);

		
	}
	
	private void init() {
//		circlePaint = new Paint();
//		circlePaint.setAntiAlias(true);
//		circlePaint.setColor(0xAADD5522);
//		circlePaint.setStyle(Paint.Style.FILL);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(0xFF000000);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(6);		
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		
//		outercirclePaint = new Paint();
//		outercirclePaint.setAntiAlias(true);
//		outercirclePaint.setColor(0x44FFF000);
//		outercirclePaint.setStyle(Paint.Style.STROKE);
//		outercirclePaint.setStrokeWidth(6);
		
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(bitmap);
		
		mPath = new Path();
		paths.clear();
		paths.add(mPath);
	}

	public void colorChanged(int color) {
		mPaint.setColor(color);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = View.MeasureSpec.getSize(widthMeasureSpec);
	    height = View.MeasureSpec.getSize(heightMeasureSpec);
	    
		init();
	    
	    setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		for (Path p : paths) {
			canvas.drawPath(p, mPaint);
		}

	}

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 0;

	private void touch_start(float x, float y) {
		mPath.reset();
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
		mPath.lineTo(mX, mY);
		// commit the path to our offscreen
		mCanvas.drawPath(mPath, mPaint);
		// kill this so we don't double draw
		mPath = new Path();
		paths.add(mPath);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// if (x <= cx+circleRadius+5 && x>= cx-circleRadius-5) {
			// if (y<= cy+circleRadius+5 && cy>= cy-circleRadius-5){
			// paths.clear();
			// return true;
			// }
			// }
			touch_start(x, y);
			invalidate();
			// Undone paths are deleted 
			undonePaths.clear();
			break;
		case MotionEvent.ACTION_MOVE:
			touch_move(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touch_up();
			invalidate();
			break;
		}
		return true;
	}

	public void undo() {
		if (paths.size() > 0) {
			undonePaths.add(paths.remove(paths.size() - 1));
			invalidate();
		}
	}

	public void redo() {
		if (undonePaths.size() > 0) {
			paths.add(undonePaths.remove(undonePaths.size() - 1));
			invalidate();
		}
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

}