package it.feio.android.omninotes.intro;

import android.graphics.Color;
import android.os.Bundle;
import it.feio.android.omninotes.R;


public class IntroSlide3 extends IntroFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		background.setBackgroundColor(Color.parseColor("#8bc34a"));
		title.setText(R.string.categories);
		image.setImageResource(R.drawable.slide3);
		description.setText(R.string.tour_listactivity_tag_detail);
	}
}