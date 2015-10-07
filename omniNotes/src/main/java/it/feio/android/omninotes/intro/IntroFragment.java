package it.feio.android.omninotes.intro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import it.feio.android.omninotes.R;


public class IntroFragment extends Fragment {

	@Bind(R.id.intro_background)
	View background;

	@Bind(R.id.intro_title)
	TextView title;

	@Bind(R.id.intro_image)
	ImageView image;

	@Bind(R.id.intro_image_small)
	ImageView image_small;

	@Bind(R.id.intro_description)
	TextView description;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.intro_slide, container, false);
		ButterKnife.bind(this, v);
		return v;
	}
}