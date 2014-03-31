package it.feio.android.omninotes;

import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.utils.AlphaManager;
import it.feio.android.omninotes.utils.Constants;
import android.content.Intent;
import android.media.MediaRecorder;
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
	
	
	final String FRAGMENT_LIST_TAG = "fragment_list";
	final String FRAGMENT_DETAIL_TAG = "fragment_detail";

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
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) mFragmentManager.findFragmentById(R.id.navigation_drawer);
		if (mNavigationDrawerFragment == null) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.replace(R.id.navigation_drawer, new NavigationDrawerFragment()).commit();
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
	
	
	public void onBackPressed() {
		Fragment f = checkFragmentInstance(R.id.fragment_container, DetailFragment.class);
		if (f != null) {
			((DetailFragment)f).saveNote(null);	
			return;
		} 
		
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
		Fragment f = checkFragmentInstance(R.id.fragment_container, DetailFragment.class);
		if (f != null) {
			MediaRecorder mRecorder = ((DetailFragment)f).mRecorder;
			if (mRecorder != null) {
				mRecorder.release();
				mRecorder = null;
			}
		}
		Crouton.cancelAllCroutons();
	}


	public DrawerLayout getDrawerLayout() {
		return ((NavigationDrawerFragment)mFragmentManager.findFragmentById(R.id.navigation_drawer)).mDrawerLayout;
	}
	
	public ActionBarDrawerToggle getDrawerToggle() {
		return ((NavigationDrawerFragment)mFragmentManager.findFragmentById(R.id.navigation_drawer)).mDrawerToggle;
	}
	
	
	private void handleIntents() {
		Intent i = mActivity.getIntent();
		
		// Action called from widget
		if (Intent.ACTION_PICK.equals(i.getAction())
				|| Constants.ACTION_SHORTCUT.equals(i.getAction())
//				|| i.hasExtra(Constants.INTENT_WIDGET)
				|| Constants.ACTION_WIDGET.equals(i.getAction())
				|| Constants.ACTION_WIDGET_TAKE_PHOTO.equals(i.getAction())
				
				|| ( ( Intent.ACTION_SEND.equals(i.getAction()) 
						|| Intent.ACTION_SEND_MULTIPLE.equals(i.getAction()) 
						|| Constants.INTENT_GOOGLE_NOW.equals(i.getAction()) ) 
						&& i.getType() != null)		
						
						) {
			Note note = i.getParcelableExtra(Constants.INTENT_NOTE);
			if (note == null) {
				note = new Note();
			}
			switchToDetail(note);
		}
		
		
//		/**
//		 * Handles third party apps requests of sharing
//		 */
//		else if ( ( Intent.ACTION_SEND.equals(i.getAction()) 
//				|| Intent.ACTION_SEND_MULTIPLE.equals(i.getAction()) 
//				|| Constants.INTENT_GOOGLE_NOW.equals(i.getAction()) ) 
//				&& i.getType() != null) {
//
//			Note note = new Note();
//			
//			// Text title
//			String title = i.getStringExtra(Intent.EXTRA_SUBJECT);
//			if (title != null) {
//				note.setTitle(title);
//			}
//			
//			// Text content
//			String content = i.getStringExtra(Intent.EXTRA_TEXT);
//			if (content != null) {
//				note.setContent(content);
//			}
//			
//			// Single attachment data
//			Uri uri = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
//	    	// Due to the fact that Google Now passes intent as text but with 
//	    	// audio recording attached the case must be handled in specific way
//		    if (uri != null && !Constants.INTENT_GOOGLE_NOW.equals(i.getAction())) {
//		    	String mimeType = StorageManager.getMimeTypeInternal(mActivity, i.getType());
//		    	note.addAttachment(new Attachment(uri, mimeType));
//		    }
//		    
//		    // Multiple attachment data
//		    ArrayList<Uri> uris = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//		    if (uris != null) {
//		    	for (Uri uriSingle : uris) {
//		    		String mimeGeneral = StorageManager.getMimeType(mActivity, uriSingle);
//		    		if (mimeGeneral != null) {
//		    			String mimeType = StorageManager.getMimeTypeInternal(mActivity, mimeGeneral);
//		    			note.addAttachment(new Attachment(uriSingle, mimeType));	
//		    		} else {
////		    			showToast(getString(R.string.error_importing_some_attachments), Toast.LENGTH_SHORT);
//		    			Crouton.makeText(mActivity, R.string.error_importing_some_attachments, ONStyle.ALERT).show();
//		    		}
//				}
//		    }
//		    switchToDetail(note);
//		}
		

		
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

}
