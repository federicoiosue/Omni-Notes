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