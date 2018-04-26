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

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;


public class IntroSlide6 extends Fragment {

	@BindView(R.id.intro_background)
	View background;

	@BindView(R.id.intro_title)
	TextView title;

	@BindView(R.id.intro_image_community_googleplus)
	ImageView image_gplus;

	@BindView(R.id.intro_image_community_facebook)
	ImageView image_facebook;

	@BindView(R.id.intro_description)
	TextView description;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.intro_slide_communities, container, false);
		ButterKnife.bind(this, v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		background.setBackgroundColor(Color.parseColor("#222222"));
		title.setText(R.string.tour_listactivity_final_title);
		image_gplus.setOnClickListener(v -> {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(Constants.GOOGLE_PLUS_COMMUNITY));
			startActivity(intent);
		});
		image_facebook.setOnClickListener(v -> {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(Constants.FACEBOOK_COMMUNITY));
			startActivity(intent);
		});
		description.setText(R.string.tour_community);
	}
}
