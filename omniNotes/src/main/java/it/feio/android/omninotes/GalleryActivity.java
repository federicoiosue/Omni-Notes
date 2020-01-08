/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes;

import static it.feio.android.omninotes.utils.ConstantsBase.GALLERY_CLICKED_IMAGE;
import static it.feio.android.omninotes.utils.ConstantsBase.GALLERY_IMAGES;
import static it.feio.android.omninotes.utils.ConstantsBase.GALLERY_TITLE;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_VIDEO;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.listeners.OnViewTouchedListener;
import it.feio.android.omninotes.models.views.InterceptorFrameLayout;
import it.feio.android.omninotes.utils.FileProviderHelper;
import it.feio.android.omninotes.utils.StorageHelper;
import it.feio.android.simplegallery.models.GalleryPagerAdapter;
import it.feio.android.simplegallery.views.GalleryViewPager;
import java.util.ArrayList;
import java.util.List;


/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and navigation/system bar) * with
 * user interaction.
 */
public class GalleryActivity extends AppCompatActivity {

  /**
   * Whether or not the system UI should be auto-hidden after {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
   */
  private static final boolean AUTO_HIDE = false;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction before hiding the * system
   * UI.
   */
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  /**
   * If set, will toggle the system UI visibility upon interaction. Otherwise, will show the system UI visibility * upon
   * interaction.
   */
  private static final boolean TOGGLE_ON_CLICK = true;

  @BindView(R.id.gallery_root)
  InterceptorFrameLayout galleryRootView;
  @BindView(R.id.fullscreen_content)
  GalleryViewPager mViewPager;

  private List<Attachment> images;
  OnViewTouchedListener screenTouches = new OnViewTouchedListener() {
    private final int MOVING_THRESHOLD = 30;
    float x;
    float y;
    private boolean status_pressed = false;


    @Override
    public void onViewTouchOccurred (MotionEvent ev) {
      if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
        x = ev.getX();
        y = ev.getY();
        status_pressed = true;
      }
      if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
        float dx = Math.abs(x - ev.getX());
        float dy = Math.abs(y - ev.getY());
        double dxy = Math.sqrt(dx * dx + dy * dy);
        LogDelegate.d("Moved of " + dxy);
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


    private void click () {
      Attachment attachment = images.get(mViewPager.getCurrentItem());
      if (attachment.getMime_type().equals(MIME_TYPE_VIDEO)) {
        viewMedia();
      }
    }
  };

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_gallery);
    ButterKnife.bind(this);

    initViews();
    initData();
  }

  @Override
  public void onStart () {
    ((OmniNotes) getApplication()).getAnalyticsHelper().trackScreenView(getClass().getName());
    super.onStart();
  }

  @Override
  public boolean onCreateOptionsMenu (Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_gallery, menu);
    return true;
  }

  private void initViews () {
    // Show the Up button in the action bar.
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowTitleEnabled(true);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    galleryRootView.setOnViewTouchedListener(screenTouches);

    mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
      @Override
      public void onPageSelected (int arg0) {
        getSupportActionBar().setSubtitle("(" + (arg0 + 1) + "/" + images.size() + ")");
      }


      @Override
      public void onPageScrolled (int arg0, float arg1, int arg2) {
      }


      @Override
      public void onPageScrollStateChanged (int arg0) {
      }
    });
  }

  /**
   * Initializes data received from note detail screen
   */
  private void initData () {
    String title = getIntent().getStringExtra(GALLERY_TITLE);
    images = getIntent().getParcelableArrayListExtra(GALLERY_IMAGES);
    int clickedImage = getIntent().getIntExtra(GALLERY_CLICKED_IMAGE, 0);

    ArrayList<Uri> imageUris = new ArrayList<>();
    for (Attachment mAttachment : images) {
      imageUris.add(mAttachment.getUri());
    }

    GalleryPagerAdapter pagerAdapter = new GalleryPagerAdapter(this, imageUris);
    mViewPager.setOffscreenPageLimit(3);
    mViewPager.setAdapter(pagerAdapter);
    mViewPager.setCurrentItem(clickedImage);

    getSupportActionBar().setTitle(title);
    getSupportActionBar().setSubtitle("(" + (clickedImage + 1) + "/" + images.size() + ")");

    // If selected attachment is a video it will be immediately played
    if (images.get(clickedImage).getMime_type().equals(MIME_TYPE_VIDEO)) {
      viewMedia();
    }
  }

  @Override
  public boolean onOptionsItemSelected (MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        break;
      case R.id.menu_gallery_share:
        shareMedia();
        break;
      case R.id.menu_gallery:
        viewMedia();
        break;
      default:
        LogDelegate.e("Wrong element choosen: " + item.getItemId());
    }
    return super.onOptionsItemSelected(item);
  }

  private void viewMedia () {
    Attachment attachment = images.get(mViewPager.getCurrentItem());
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.setDataAndType(FileProviderHelper.getShareableUri(attachment),
        StorageHelper.getMimeType(this, attachment.getUri()));
    startActivity(intent);
  }

  private void shareMedia () {
    Attachment attachment = images.get(mViewPager.getCurrentItem());
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType(StorageHelper.getMimeType(this, attachment.getUri()));
    intent.putExtra(Intent.EXTRA_STREAM, FileProviderHelper.getShareableUri(attachment));
    startActivity(intent);
  }
}
