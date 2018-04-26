/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import it.feio.android.omninotes.R;


public class IntroSlide1 extends IntroFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		background.setBackgroundColor(Color.parseColor("#222222"));
		title.setText(R.string.tour_listactivity_intro_title);
		image.setVisibility(View.GONE);
		image_small.setImageResource(R.drawable.logo);
		image_small.setVisibility(View.VISIBLE);
		description.setText(R.string.tour_listactivity_final_detail);
	}
}
