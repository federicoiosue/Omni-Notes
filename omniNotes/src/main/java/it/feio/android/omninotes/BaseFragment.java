package it.feio.android.omninotes;

import android.support.v4.app.Fragment;
import com.squareup.leakcanary.RefWatcher;


public class BaseFragment extends Fragment {


	@Override
	public void onStart() {
		super.onStart();
		((OmniNotes)getActivity().getApplication()).getAnalyticsHelper().trackScreenView(getClass().getName());
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		RefWatcher refWatcher = OmniNotes.getRefWatcher();
		refWatcher.watch(this);
	}

}
