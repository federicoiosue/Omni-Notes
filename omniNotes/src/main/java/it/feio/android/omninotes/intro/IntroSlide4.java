package it.feio.android.omninotes.intro;

import android.graphics.Color;
import android.os.Bundle;
import it.feio.android.omninotes.R;


public class IntroSlide4 extends IntroFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		background.setBackgroundColor(Color.parseColor("#2196f3"));
		title.setText(R.string.tour_detailactivity_attachment_title);
		image.setImageResource(R.drawable.slide4);
		description.setText(R.string.tour_detailactivity_attachment_detail);
	}
}