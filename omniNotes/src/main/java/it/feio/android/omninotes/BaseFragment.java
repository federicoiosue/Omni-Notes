package it.feio.android.omninotes;

import android.support.v4.app.Fragment;
import com.squareup.leakcanary.RefWatcher;


/**
 * Created by fede on 10/05/15.
 */
public class BaseFragment extends Fragment {


	@Override
	public void onStart() {
		super.onStart();
		// Analytics tracking
		((OmniNotes) getActivity().getApplication()).getTracker().trackScreenView(getClass().getName());
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		RefWatcher refWatcher = OmniNotes.getRefWatcher();
		refWatcher.watch(this);
	}

}
