package it.feio.android.omninotes;

import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.listeners.OnViewTouchedListener;
import it.feio.android.omninotes.models.views.InterceptorFrameLayout;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.FileHelper;
import it.feio.android.omninotes.utils.StorageManager;
import it.feio.android.omninotes.utils.systemui.SystemUiHider;

import java.util.ArrayList;

import ru.truba.touchgallery.GalleryWidget.FilePagerAdapter;
import ru.truba.touchgallery.GalleryWidget.GalleryViewPager;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class GalleryActivity extends ActionBarActivity {

	/**
	 * Whether or not the system UI should be auto-hidden after {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise, will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private GalleryViewPager mViewPager;

	private ArrayList<Attachment> images;

	private GalleryActivity mActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_gallery);
		mActivity = this;

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {

			// Cached values.
			int mControlsHeight;
			int mShortAnimTime;

			@Override
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
			public void onVisibilityChange(boolean visible) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
					// If the ViewPropertyAnimator API is available
					// (Honeycomb MR2 and later), use it to animate the
					// in-layout UI controls at the bottom of the
					// screen.
					if (mControlsHeight == 0) {
						mControlsHeight = controlsView.getHeight();
					}
					if (mShortAnimTime == 0) {
						mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
					}
					controlsView.animate().translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
				} else {
					// If the ViewPropertyAnimator APIs aren't
					// available, simply show or hide the in-layout UI
					// controls.
					controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
				}

//				if (visible && AUTO_HIDE) {
//					// Schedule a hide().
//					delayedHide(AUTO_HIDE_DELAY_MILLIS);
//				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
//		findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
		
		initViews();
		initData();
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_gallery, menu);
	    return true;
	}
	
	
	private void initViews() {
		
		// Show the Up button in the action bar.
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayShowTitleEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		((InterceptorFrameLayout) findViewById(R.id.gallery_root)).setOnViewTouchedListener(screenTouches);
		
		mViewPager = (GalleryViewPager)findViewById(R.id.fullscreen_content);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {			
			@Override
			public void onPageSelected(int arg0) {
				getSupportActionBar().setSubtitle("(" + (arg0+1) + "/" + images.size() + ")");
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});
	}

	
	/**
	 * Initializes data received from note detail screen
	 */
	private void initData() {
		String title = getIntent().getStringExtra(Constants.GALLERY_TITLE);
		images = getIntent().getParcelableArrayListExtra(Constants.GALLERY_IMAGES);
		int clickedImage = getIntent().getIntExtra(Constants.GALLERY_CLICKED_IMAGE, 0);
		
		ArrayList<String> imagesPaths = new ArrayList<String>();
		for (Attachment mAttachment : images) {
			Uri uri = mAttachment.getUri();
			imagesPaths.add(FileHelper.getPath(this, uri));
		}
		
		FilePagerAdapter  pagerAdapter = new FilePagerAdapter (this, imagesPaths);  
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			mViewPager.setPageTransformer(false, new DepthPageTransformer());
//		}
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(clickedImage);
		
		getSupportActionBar().setTitle(title);
		getSupportActionBar().setSubtitle("(" + (clickedImage+1) + "/" + images.size() + ")");
		
		// If selected attachment is a video it will be immediately played
		if (images.get(clickedImage).getMime_type().equals(Constants.MIME_TYPE_VIDEO)) {
			viewMedia();
		}
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				break;
			case R.id.menu_gallery_share: {
				shareMedia();
				break;
			}
			case R.id.menu_gallery: {
				viewMedia();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	

	
	private void viewMedia() {
		Attachment attachment = images.get(mViewPager.getCurrentItem());
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(attachment.getUri(),
				StorageManager.getMimeType(this, attachment.getUri()));
		startActivity(intent);
	}



	private void shareMedia() {
		Attachment attachment = images.get(mViewPager.getCurrentItem());
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType(StorageManager.getMimeType(this, attachment.getUri()));
		intent.putExtra(Intent.EXTRA_STREAM, attachment.getUri());
		startActivity(intent);
	}


//	@Override
//	protected void onPostCreate(Bundle savedInstanceState) {
//		super.onPostCreate(savedInstanceState);
//		// Trigger the initial hide() shortly after the activity has been
//		// created, to briefly hint to the user that UI controls
//		// are available.
//		delayedHide(100);
//	}


	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the system UI. This is to prevent the jarring behavior of controls going away while interacting with activity UI.
	 */
//	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//		@Override
//		public boolean onTouch(View view, MotionEvent motionEvent) {
//			if (AUTO_HIDE) {
//				delayedHide(AUTO_HIDE_DELAY_MILLIS);
//			}
//			return false;
//		}
//	};

//	Handler mHideHandler = new Handler();
//	Runnable mHideRunnable = new Runnable() {
//		@Override
//		public void run() {
//			mSystemUiHider.hide();
//		}
//	};

	
	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
	 */
//	private void delayedHide(int delayMillis) {
//		mHideHandler.removeCallbacks(mHideRunnable);
//		mHideHandler.postDelayed(mHideRunnable, delayMillis);
//	}
	


	OnViewTouchedListener screenTouches = new OnViewTouchedListener() {
		
		private final int MOVING_THRESHOLD = 30;

		float x;
		float y;
		private boolean status_pressed = false;

		@Override
		public void onViewTouchOccurred(MotionEvent ev) {
			if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
				x = ev.getX();
				y = ev.getY();
				status_pressed = true;
			}
			if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
				float dx = Math.abs(x - ev.getX());
				float dy = Math.abs(y - ev.getY());
				double dxy = Math.sqrt(dx*dx + dy*dy);
				Log.d(Constants.TAG, "Moved of " + dxy);
				if (dxy >= MOVING_THRESHOLD) {
					status_pressed = false;
				}
			}
			if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
				if (status_pressed) {
					click();
					status_pressed = false;
				}
			}
		}

		private void click() {
//			if (TOGGLE_ON_CLICK) {
//				mSystemUiHider.toggle();
//			} else {
//				mSystemUiHider.show();
//			}
			Attachment attachment = images.get(mViewPager.getCurrentItem());
			if (attachment.getMime_type().equals(Constants.MIME_TYPE_VIDEO)) {
				viewMedia();
			}
		}
	};
}
