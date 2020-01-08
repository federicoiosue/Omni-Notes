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

package it.feio.android.omninotes.models;


import static androidx.core.view.ViewCompat.animate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import it.feio.android.checklistview.utils.AlphaManager;
import it.feio.android.omninotes.R;
import java.util.Locale;


public class UndoBarController {

  private View mBarView;
  private TextView mMessageView;
  private ViewPropertyAnimatorCompat mBarAnimator;
//    private Handler mHideHandler = new Handler();

  private UndoListener mUndoListener;

  // State objects
  private Parcelable mUndoToken;
  private CharSequence mUndoMessage;
  private Button mButtonView;
  private boolean isVisible;


  public interface UndoListener {

    void onUndo (Parcelable token);
  }


  public UndoBarController (View undoBarView, UndoListener undoListener) {
    mBarView = undoBarView;
//        mBarAnimator = mBarView.animate();
    mBarAnimator = animate(mBarView);
    mUndoListener = undoListener;

    mMessageView = mBarView.findViewById(R.id.undobar_message);

    mButtonView = mBarView.findViewById(R.id.undobar_button);
    mButtonView.setText(mButtonView.getText().toString().toUpperCase(Locale.getDefault()));
    mButtonView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick (View view) {
        hideUndoBar(false);
        mUndoListener.onUndo(mUndoToken);
      }
    });

    hideUndoBar(false);
  }


  public void showUndoBar (boolean immediate, CharSequence message, Parcelable undoToken) {
    mUndoToken = undoToken;
    mUndoMessage = message;
    mMessageView.setText(mUndoMessage);

//        mHideHandler.removeCallbacks(mHideRunnable);
//        mHideHandler.postDelayed(mHideRunnable,
//                mBarView.getResources().getInteger(R.integer.undobar_hide_delay));

    mBarView.setVisibility(View.VISIBLE);
    if (immediate) {
//            mBarView.setAlpha(1);
      AlphaManager.setAlpha(mBarView, 1);
    } else {
      mBarAnimator.cancel();
      mBarAnimator
          .alpha(1)
          .setDuration(
              mBarView.getResources()
                      .getInteger(android.R.integer.config_shortAnimTime))
          .setListener(null);
    }
    isVisible = true;
  }


  public void hideUndoBar (boolean immediate) {
//        mHideHandler.removeCallbacks(mHideRunnable);
    if (immediate) {
      mBarView.setVisibility(View.GONE);
      AlphaManager.setAlpha(mBarView, 0);
      mUndoMessage = null;
      mUndoToken = null;

    } else {
      mBarAnimator.cancel();
      mBarAnimator
          .alpha(0)
          .setDuration(mBarView.getResources()
                               .getInteger(android.R.integer.config_shortAnimTime))
          .setListener(new ViewPropertyAnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd (View view) {
              super.onAnimationEnd(view);
              mBarView.setVisibility(View.GONE);
              mUndoMessage = null;
              mUndoToken = null;
            }
          });
    }
    isVisible = false;
  }


  public void onSaveInstanceState (Bundle outState) {
    outState.putCharSequence("undo_message", mUndoMessage);
    outState.putParcelable("undo_token", mUndoToken);
  }


  public void onRestoreInstanceState (Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      mUndoMessage = savedInstanceState.getCharSequence("undo_message");
      mUndoToken = savedInstanceState.getParcelable("undo_token");

      if (mUndoToken != null || !TextUtils.isEmpty(mUndoMessage)) {
        showUndoBar(true, mUndoMessage, mUndoToken);
      }
    }
  }


  public boolean isVisible () {
    return isVisible;
  }

//    private Runnable mHideRunnable = new Runnable() {
//        @Override
//        public void run() {
//            hideUndoBar(false);
//        }
//    };
}
