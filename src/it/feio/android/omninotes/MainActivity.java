package it.feio.android.omninotes;

import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.utils.Constants;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends BaseActivity {

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private FragmentManager mFragmentManager;
	private NavigationDrawerFragment mNavigationDrawerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mFragmentManager = getSupportFragmentManager();

//		mNavigationDrawerFragment = (NavigationDrawerFragment) mFragmentManager.findFragmentById(
//				R.id.navigation_drawer);
//		mTitle = getTitle();
//
//		 Set up the drawer.
//		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
//		mNavigationDrawerFragment.initNavigationDrawer();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.getAction() == null) {
			intent.setAction(Constants.ACTION_START_APP);
		}
		setIntent(intent);
		Log.d(Constants.TAG, "onNewIntent");
		super.onNewIntent(intent);
	}

	public MenuItem getSearchMenuItem() {
		return ((ListFragment)mFragmentManager.findFragmentById(R.id.container_list)).searchMenuItem;
	}

	public void editTag(Tag tag) {
		((ListFragment)mFragmentManager.findFragmentById(R.id.container_list)).editTag(tag);
	}

	public void initNotesList(Intent intent) {
		((ListFragment)mFragmentManager.findFragmentById(R.id.container_list)).initNotesList(intent);
	}

	public void commitPending() {
		((ListFragment)mFragmentManager.findFragmentById(R.id.container_list)).commitPending();
	}

	public void editNote(Note note) {
		((ListFragment)mFragmentManager.findFragmentById(R.id.container_list)).editNote(note);
	}

//	@Override
//	public void onNavigationDrawerItemSelected(int position) {
//		// update the main content by replacing fragments
//		FragmentManager fragmentManager = getSupportFragmentManager();
//		fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
//				.commit();
//	}
//
//	public void onSectionAttached(int number) {
//		switch (number) {
//		case 1:
//			mTitle = getString(R.string.title_section1);
//			break;
//		case 2:
//			mTitle = getString(R.string.title_section2);
//			break;
//		case 3:
//			mTitle = getString(R.string.title_section3);
//			break;
//		}
//	}
//
//	public void restoreActionBar() {
//		ActionBar actionBar = getSupportActionBar();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//		actionBar.setDisplayShowTitleEnabled(true);
//		actionBar.setTitle(mTitle);
//	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		if (!mNavigationDrawerFragment.isDrawerOpen()) {
//			// Only show items in the action bar relevant to this screen
//			// if the drawer is not showing. Otherwise, let the drawer
//			// decide what to show in the action bar.
//			getMenuInflater().inflate(R.menu.menu, menu);
//			restoreActionBar();
//			return true;
//		}
//		return super.onCreateOptionsMenu(menu);
//	}

////	@Override
////	public boolean onOptionsItemSelected(MenuItem item) {
////		// Handle action bar item clicks here. The action bar will
////		// automatically handle clicks on the Home/Up button, so long
////		// as you specify a parent activity in AndroidManifest.xml.
////		int id = item.getItemId();
////		if (id == R.id.action_settings) {
////			return true;
////		}
////		return super.onOptionsItemSelected(item);
////	}
////
////	/**
////	 * A placeholder fragment containing a simple view.
////	 */
////	public static class PlaceholderFragment extends Fragment {
////		/**
////		 * The fragment argument representing the section number for this
////		 * fragment.
////		 */
////		private static final String ARG_SECTION_NUMBER = "section_number";
////
////		/**
////		 * Returns a new instance of this fragment for the given section number.
////		 */
////		public static PlaceholderFragment newInstance(int sectionNumber) {
////			PlaceholderFragment fragment = new PlaceholderFragment();
////			Bundle args = new Bundle();
////			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
////			fragment.setArguments(args);
////			return fragment;
////		}
////
////		public PlaceholderFragment() {
////		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//			TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//			textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
//			return rootView;
//		}
//
//		@Override
//		public void onAttach(Activity activity) {
//			super.onAttach(activity);
//			((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
//		}
//	}

//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		mDrawerToggle.onConfigurationChanged(newConfig);
//	}

	public DrawerLayout getDrawerLayout() {
		return ((NavigationDrawerFragment)mFragmentManager.findFragmentById(R.id.navigation_drawer)).mDrawerLayout;
	}

}
