package it.feio.android.omninotes;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.models.adapters.NavDrawerTagAdapter;
import it.feio.android.omninotes.models.adapters.NavDrawerAdapter;
import it.feio.android.omninotes.utils.AppTourHelper;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.Constants;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews.OnShowcaseAcknowledged;

import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Fragment used for managing interactions for and presentation of a navigation
 * drawer. See the <a href=
 * "https://developer.android.com/design/patterns/navigation-drawer.html#Interaction"
 * > design guidelines</a> for a complete explanation of the behaviors
 * implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

	ActionBarDrawerToggle mDrawerToggle;
	DrawerLayout mDrawerLayout;
	String[] mNavigationArray;
	TypedArray mNavigationIconsArray;
	private ListView mDrawerList;
	private ListView mDrawerTagList;
	private View tagListHeader;
	private Tag candidateSelectedTag;
	private MainActivity mActivity;
	private SharedPreferences prefs;
	private DbHelper db;
	private CharSequence mTitle;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mActivity = (MainActivity) getActivity();
		
		prefs = mActivity.prefs;
		db = mActivity.db;
	}
	
	

	/**
	 * Initialization of compatibility navigation drawer
	 */
	public void initNavigationDrawer() {

		mDrawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
		mDrawerLayout.setFocusableInTouchMode(false);

		// Sets the adapter for the MAIN navigation list view
		mDrawerList = (ListView) getView().findViewById(R.id.drawer_nav_list);
		mNavigationArray = getResources().getStringArray(R.array.navigation_list);
		mNavigationIconsArray = getResources().obtainTypedArray(R.array.navigation_list_icons);
		mDrawerList.setAdapter(new NavDrawerAdapter(mActivity, mNavigationArray, mNavigationIconsArray));

		// Sets click events
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				mActivity.commitPending();
				String navigation = getResources().getStringArray(R.array.navigation_list_codes)[position];
				Log.d(Constants.TAG, "Selected voice " + navigation + " on navigation menu");
				selectNavigationItem(mDrawerList, position);
				mActivity.updateNavigation(navigation);
				mDrawerList.setItemChecked(position, true);
				if (mDrawerTagList != null)
					mDrawerTagList.setItemChecked(0, false); // Called to force
																// redraw
				mActivity.initNotesList(mActivity.getIntent());
			}
		});

		// Sets the adapter for the TAGS navigation list view

		// Retrieves data to fill tags list
		ArrayList<Tag> tags = db.getTags();

		if (tags.size() > 0) {
			mDrawerTagList = (ListView) getView().findViewById(R.id.drawer_tag_list);
			// Inflation of header view
			LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			if (tagListHeader == null) {
				tagListHeader = inflater.inflate(R.layout.drawer_tag_list_header,
						(ViewGroup) getView().findViewById(R.id.layout_root));
			}
			// Adds the "tag" label as header
			if (mDrawerTagList.getHeaderViewsCount() == 0) {
				mDrawerTagList.addHeaderView(tagListHeader);
				mDrawerTagList.setHeaderDividersEnabled(true);
			}
			mDrawerTagList.setAdapter(new NavDrawerTagAdapter(mActivity, tags, mActivity.navigationTmp));

			// Sets click events
			mDrawerTagList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					mActivity.commitPending();
					Object item = mDrawerTagList.getAdapter().getItem(position);
					// Ensuring that clicked item is not the ListView header
					if (item != null) {
						Tag tag = (Tag) item;
						String navigation = tag.getName();
						Log.d(Constants.TAG, "Selected voice " + navigation + " on navigation menu");
						selectNavigationItem(mDrawerTagList, position);
						mActivity.updateNavigation(String.valueOf(tag.getId()));
						mDrawerTagList.setItemChecked(position, true);
						if (mDrawerList != null)
							mDrawerList.setItemChecked(0, false); // Called to
																	// force
																	// redraw
						mActivity.initNotesList(mActivity.getIntent());
					}
				}
			});

			// Sets long click events
			mDrawerTagList.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
					if (mDrawerTagList.getAdapter() != null) {
						Object item = mDrawerTagList.getAdapter().getItem(position);
						// Ensuring that clicked item is not the ListView header
						if (item != null) {
							mActivity.editTag((Tag) item);
						}
					} else {
						Crouton.makeText(mActivity, R.string.tag_deleted, ONStyle.ALERT).show();
					}
					return true;
				}
			});
		} else {
			if (mDrawerTagList != null) {
				mDrawerTagList.removeAllViewsInLayout();
				mDrawerTagList = null;
			}
		}

		// enable ActionBar app icon to behave as action to toggle nav drawer
		mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mActivity.getSupportActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle± ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(mActivity, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {

			public void onDrawerClosed(View view) {
				mActivity.getSupportActionBar().setTitle(mTitle);
				mActivity.supportInvalidateOptionsMenu(); // creates call to
															// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				// Stops search service
				if (mActivity.getSearchMenuItem() != null && MenuItemCompat.isActionViewExpanded(mActivity.getSearchMenuItem()))
					MenuItemCompat.collapseActionView(mActivity.getSearchMenuItem());

				mTitle = mActivity.getSupportActionBar().getTitle();
				mActivity.getSupportActionBar()
						.setTitle(mActivity.getApplicationContext().getString(R.string.app_name));
				mActivity.supportInvalidateOptionsMenu(); // creates call to
															// onPrepareOptionsMenu()

				// Show instructions on first launch
				final String instructionName = Constants.PREF_TOUR_PREFIX + "navdrawer";
				if (
//						&& !prefs.getBoolean(instructionName, false)
			    		AppTourHelper.isMyTurn(mActivity, instructionName)) {
					ArrayList<Integer[]> list = new ArrayList<Integer[]>();
					list.add(new Integer[] { R.id.menu_add_tag, R.string.tour_listactivity_tag_title,
							R.string.tour_listactivity_tag_detail, ShowcaseView.ITEM_ACTION_ITEM });
					mActivity.showCaseView(list, new OnShowcaseAcknowledged() {
						@Override
						public void onShowCaseAcknowledged(ShowcaseView showcaseView) {
							AppTourHelper.complete(mActivity, instructionName);
							mDrawerLayout.closeDrawer(GravityCompat.START);

							// Attaches a dummy image as example
							Note note = new Note();
							Attachment attachment = new Attachment(BitmapHelper.getUri(mActivity,
									R.drawable.ic_launcher), Constants.MIME_TYPE_IMAGE);
							note.getAttachmentsList().add(attachment);
							note.setTitle("http://www.opensource.org");
							mActivity.editNote(note);
						}
					});
				}
			}
		};
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		mDrawerToggle.syncState();
	}


	/** Swaps fragments in the main content view 
	 * @param list */
	private void selectNavigationItem(ListView list, int position) {
		Object itemSelected = list.getItemAtPosition(position);
		if (itemSelected.getClass().isAssignableFrom(String.class)) {
			mTitle = (CharSequence)itemSelected;	
		// Is a tag
		} else {
			mTitle = ((Tag)itemSelected).getName();
		}
		// Navigation drawer is closed after a while to avoid lag
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mActivity.getSupportActionBar().setTitle(mTitle);
				mDrawerLayout.closeDrawer(GravityCompat.START);
			}
		}, 500);
		
	}

	/**
	 * Callbacks interface that all activities using this fragment must
	 * implement.
	 */
//	public static interface NavigationDrawerCallbacks {
//		/**
//		 * Called when an item in the navigation drawer is selected.
//		 */
//		void onNavigationDrawerItemSelected(int position);
//		/**
//		 * Called when a note should be edited.
//		 */
//		void onRequestNoteEdit(int position);
//	}
	
	
	
//	public void refreshNavigationDrawerData() {
//		initNavigationDrawer();
//	}
	
	
}
