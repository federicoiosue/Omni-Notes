package it.feio.android.omninotes.intro;

import android.graphics.Color;
import android.os.Bundle;
import it.feio.android.omninotes.R;


public class IntroSlide5 extends IntroFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		background.setBackgroundColor(Color.parseColor("#9c27b0"));
		title.setText(R.string.tour_detailactivity_links_title);
		image.setImageResource(R.drawable.slide5);
		description.setText(R.string.tour_detailactivity_links_detail);
	}
}