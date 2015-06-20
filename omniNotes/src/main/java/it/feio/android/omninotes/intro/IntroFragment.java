package it.feio.android.omninotes.intro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import it.feio.android.omninotes.R;


public class IntroFragment extends Fragment {

	@InjectView(R.id.intro_background)
	View background;

	@InjectView(R.id.intro_title)
	TextView title;

	@InjectView(R.id.intro_image)
	ImageView image;

	@InjectView(R.id.intro_image_small)
	ImageView image_small;

	@InjectView(R.id.intro_description)
	TextView description;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.intro_slide, container, false);
		ButterKnife.inject(this, v);
		return v;
	}
}