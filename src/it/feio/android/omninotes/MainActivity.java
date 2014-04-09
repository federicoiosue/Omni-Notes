package it.feio.android.omninotes;

import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.utils.AlphaManager;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.SpinnerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class MainActivity extends BaseActivity {

	public final String FRAGMENT_DRAWER_TAG = "fragment_drawer";
	public final String FRAGMENT_LIST_TAG = "fragment_list";
	public final String FRAGMENT_DETAIL_TAG = "fragment_detail";
	public final String FRAGMENT_SKETCH_TAG = "fragment_sketch";
	public final String FRAGMENT_SPINNER_TAG = "fragment_spinner";

	private FragmentManager mFragmentManager;
	private NavigationDrawerFragment mNavigationDrawerFragment;
	
	public boolean loadNotesSync = Constants.LOAD_NOTES_SYNC;
	public SpinnerDialog mSpinnerDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mFragmentManager = getSupportFragmentManager();
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) mFragmentManager.findFragmentById(R.id.navigation_drawer);
		if (mNavigationDrawerFragment == null) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.replace(R.id.navigation_drawer, new NavigationDrawerFragment(), FRAGMENT_DRAWER_TAG).commit();
		}
		
		if (mFragmentManager.findFragmentByTag(FRAGMENT_LIST_TAG) == null) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.fragment_container, new ListFragment(), FRAGMENT_LIST_TAG).commit();
		}
		
		// Handling of Intent actions
		handleIntents();
	}
	

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.getAction() == null) {
			intent.setAction(Constants.ACTION_START_APP);
		}
		setIntent(intent);
		handleIntents();
		Log.d(Constants.TAG, "onNewIntent");
		super.onNewIntent(intent);
	}

	
	
	public MenuItem getSearchMenuItem() {
		Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
		if (f != null) {
			return ((ListFragment)f).searchMenuItem;			
		} else {
			return null;
		}
	}

	public void editTag(Tag tag) {
		Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
		if (f != null) {
			((ListFragment)f).editTag(tag);			
		} 
	}

	public void initNotesList(Intent intent) {
		Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
		if (f != null) {
//			List view is set as transparent to perform a fade in animation and give a smoother sensation
			AlphaManager.setAlpha(findViewById(R.id.notes_list), 0);
			((ListFragment)f).initNotesList(intent);			
		} 
	}

	public void commitPending() {
		Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
		if (f != null) {
			((ListFragment)f).commitPending();			
		} 
	}

	public void editNote(Note note) {
		Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
		if (f != null) {
			((ListFragment)f).editNote(note);	
		} 
	}

	public void initNavigationDrawer() {
		Fragment f = checkFragmentInstance(R.id.navigation_drawer, NavigationDrawerFragment.class);
		if (f != null) {
			((NavigationDrawerFragment)f).initNavigationDrawer();	
		} 
	}
	
	
	/**
	 * Checks if allocated fragment is of the required type and then returns it or returns null
	 * @param id
	 * @param instanceClass
	 * @return
	 */
	private Fragment checkFragmentInstance(int id, Object instanceClass) {
		Fragment result = null;
		Fragment fragment = mFragmentManager.findFragmentById(id);
		if (instanceClass.equals(fragment.getClass())) {
			result = fragment;
		}
		return result;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v7.app.ActionBarActivity#onBackPressed()
	 * 
	 * Overrides the onBackPressed behavior for the attached fragments
	 */
	public void onBackPressed() {

		Fragment f;
		
		// SketchFragment
		f = checkFragmentInstance(R.id.fragment_container, SketchFragment.class);
		if (f != null) {
//			((SketchFragment)f).save();
			mFragmentManager.popBackStack(); 
			return;
		} 
		
		// DetailFragment
		f = checkFragmentInstance(R.id.fragment_container, DetailFragment.class);
		if (f != null) {
			((DetailFragment)f).saveNote(null);	
			return;
		} 

		// ListFragment
		f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
		if (f != null) {
			// Before exiting from app the navigation drawer is opened
			if (prefs.getBoolean("settings_navdrawer_on_exit",  false) && getDrawerLayout() != null && !getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
				getDrawerLayout().openDrawer(GravityCompat.START);
			} else {
				super.onBackPressed();
			}
			return;
		} 
		super.onBackPressed();
	}

	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("navigationTmp", navigationTmp);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
//		Fragment f = checkFragmentInstance(R.id.fragment_container, DetailFragment.class);
//		if (f != null) {
//			MediaRecorder mRecorder = ((DetailFragment)f).mRecorder;
//			if (mRecorder != null) {
//				mRecorder.release();
//				mRecorder = null;
//			}
//		}
		Crouton.cancelAllCroutons();
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}


	
	public DrawerLayout getDrawerLayout() {
		if ((NavigationDrawerFragment)mFragmentManager.findFragmentById(R.id.navigation_drawer) != null) {
			return ((NavigationDrawerFragment)mFragmentManager.findFragmentById(R.id.navigation_drawer)).mDrawerLayout;
		} else {
			return null;
		}
	}
	
	
	public ActionBarDrawerToggle getDrawerToggle() {
		if ((NavigationDrawerFragment)mFragmentManager.findFragmentById(R.id.navigation_drawer) != null) {
			return ((NavigationDrawerFragment)mFragmentManager.findFragmentById(R.id.navigation_drawer)).mDrawerToggle;
		} else {
			return null;
		}
	}
	
	
	
	private void handleIntents() {
		Intent i = mActivity.getIntent();
		
		if (i.getAction() == null) return;
		
		// Action called from widget
		if (Constants.ACTION_SHORTCUT.equals(i.getAction())
			|| Constants.ACTION_WIDGET.equals(i.getAction())
			|| Constants.ACTION_WIDGET_TAKE_PHOTO.equals(i.getAction())
			
			|| ( ( Intent.ACTION_SEND.equals(i.getAction()) 
					|| Intent.ACTION_SEND_MULTIPLE.equals(i.getAction()) 
					|| Constants.INTENT_GOOGLE_NOW.equals(i.getAction()) ) 
					&& i.getType() != null)		
				
			|| i.getAction().contains(Constants.ACTION_NOTIFICATION_CLICK)
					
					) {
			Note note = i.getParcelableExtra(Constants.INTENT_NOTE);
			if (note == null) {
				note = new Note();
			}
			switchToDetail(note);
		}
	}

	
	public void switchToDetail(Note note) {
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		animateTransition(transaction, TRANSITION_HORIZONTAL);
		DetailFragment mDetailFragment = new DetailFragment();
		Bundle b = new Bundle();
		b.putParcelable(Constants.INTENT_NOTE, note);
		mDetailFragment.setArguments(b);
		transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG).addToBackStack(FRAGMENT_LIST_TAG).commitAllowingStateLoss();
		if (getDrawerToggle() != null) {
			getDrawerToggle().setDrawerIndicatorEnabled(false);
		}
	}
	
	
	
	/**
	 * Shows loading spinner 
	 */
	public void showLoading() {
		mSpinnerDialog = new SpinnerDialog();
		mSpinnerDialog.show(mFragmentManager, FRAGMENT_SPINNER_TAG);
	}
	
	/**
	 * Hides loading spinner 
	 */
	public void hideLoading() {
		if (mSpinnerDialog != null) {
			mSpinnerDialog.dismiss();
		}
	}
	
	
	
}
