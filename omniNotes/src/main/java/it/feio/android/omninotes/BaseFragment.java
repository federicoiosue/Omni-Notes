package it.feio.android.omninotes;

import android.support.v4.app.Fragment;
import com.squareup.leakcanary.RefWatcher;
import it.feio.android.omninotes.helpers.AnalyticsHelper;


public class BaseFragment extends Fragment {


	@Override
	public void onStart() {
		super.onStart();
		AnalyticsHelper.trackScreenView(getClass().getName());
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		RefWatcher refWatcher = OmniNotes.getRefWatcher();
		refWatcher.watch(this);
	}

}
