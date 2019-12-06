/*
 * Copyright (C) 2013-2019 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.intro;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;


public class IntroSlide6 extends IntroFragment {

  @Override
  public void onActivityCreated (Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    background.setBackgroundColor(Color.parseColor("#222222"));
    title.setText(R.string.tour_listactivity_final_title);
    image.setVisibility(View.GONE);
    image_small.setImageResource(R.drawable.facebook);
    image_small.setVisibility(View.VISIBLE);
    image_small.setOnClickListener(v -> {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(Constants.FACEBOOK_COMMUNITY));
      startActivity(intent);
    });
    description.setText(R.string.tour_community);
  }
}