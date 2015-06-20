package it.feio.android.omninotes.intro;

import android.graphics.Color;
import android.os.Bundle;
import it.feio.android.omninotes.R;


public class IntroSlide1 extends IntroFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		background.setBackgroundColor(Color.parseColor("#222222"));
		title.setText(R.string.tour_listactivity_intro_title);
		image.setImageResource(R.drawable.logo);
	}
}