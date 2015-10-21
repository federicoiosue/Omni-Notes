package it.feio.android.omninotes.intro;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import it.feio.android.omninotes.R;


public class IntroSlide6 extends IntroFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		background.setBackgroundColor(Color.parseColor("#222222"));
		title.setText(R.string.tour_listactivity_final_title);
		image.setVisibility(View.GONE);
		image_small.setImageResource(R.drawable.gplus);
		image_small.setVisibility(View.VISIBLE);
		image_small.setOnClickListener(v -> {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(getString(R.string.gplus_community)));
			startActivity(intent);
		});
		description.setText(R.string.tour_community);
	}
}