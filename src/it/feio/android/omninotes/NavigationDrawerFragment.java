package it.feio.android.omninotes;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.adapters.NavDrawerAdapter;
import it.feio.android.omninotes.models.adapters.NavDrawerCategoryAdapter;
import it.feio.android.omninotes.utils.AppTourHelper;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.Constants;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
	private ListView mDrawerCategoriesList;
	private View categoriesListHeader, settingsListFooter;
	private Category candidateSelectedCategory;
	private MainActivity mActivity;
	private SharedPreferences prefs;
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
//				Log.d(Constants.TAG, "Selected voice " + navigation + " on navigation menu");
				selectNavigationItem(mDrawerList, position);
				mActivity.updateNavigation(navigation);
				mDrawerList.setItemChecked(position, true);
				if (mDrawerCategoriesList != null)
					mDrawerCategoriesList.setItemChecked(0, false); // Called to force
																// redraw
				// Reset intent
				mActivity.getIntent().setAction(Intent.ACTION_MAIN);
				
				// Call method to update notes list
				mActivity.initNotesList(mActivity.getIntent());
			}
		});

		// Sets the adapter for the TAGS navigation list view

		// Retrieves data to fill tags list
		ArrayList<Category> categories = DbHelper.getInstance(getActivity()).getCategories();

		mDrawerCategoriesList = (ListView) getView().findViewById(R.id.drawer_tag_list);

		// Inflater used for header and footer
		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
					
		// Inflation of header view
		if (categoriesListHeader == null) {
			categoriesListHeader = inflater.inflate(R.layout.drawer_category_list_header, null);
		}
		
		// Inflation of footer view used for settings
		if (settingsListFooter == null) {
			settingsListFooter = inflater.inflate(R.layout.drawer_category_list_footer, null);
		}		
		settingsListFooter.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
				startActivity(settingsIntent);
			}
		});
		mDrawerCategoriesList.removeHeaderView(categoriesListHeader);
		mDrawerCategoriesList.removeHeaderView(settingsListFooter);	
		mDrawerCategoriesList.removeFooterView(settingsListFooter);	
		if (categories.size() == 0) {
			mDrawerCategoriesList.addHeaderView(settingsListFooter);
		}
		else if (categories.size() > 0) {
			mDrawerCategoriesList.addHeaderView(categoriesListHeader);
			mDrawerCategoriesList.addFooterView(settingsListFooter);
		} 
		
		
		mDrawerCategoriesList.setAdapter(new NavDrawerCategoryAdapter(mActivity, categories, mActivity.navigationTmp));

		// Sets click events
		mDrawerCategoriesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				
				// Commits pending deletion or archiviation
				mActivity.commitPending();				
				// Stops search service
				if (mActivity.getSearchMenuItem() != null && MenuItemCompat.isActionViewExpanded(mActivity.getSearchMenuItem()))
					MenuItemCompat.collapseActionView(mActivity.getSearchMenuItem());
				
				if (mDrawerCategoriesList != null) {
					Object item = mDrawerCategoriesList.getAdapter().getItem(position);
					// Ensuring that clicked item is not the ListView header
					if (item != null) {
						Category tag = (Category) item;
						String navigation = tag.getName();
//							Log.d(Constants.TAG, "Selected voice " + navigation + " on navigation menu");
						selectNavigationItem(mDrawerCategoriesList, position);
						mActivity.updateNavigation(String.valueOf(tag.getId()));
						mDrawerCategoriesList.setItemChecked(position, true);
						if (mDrawerList != null)
							mDrawerList.setItemChecked(0, false); // Called to
																	// force
																	// redraw
						mActivity.initNotesList(mActivity.getIntent());
					}
				}
			}
		});

		// Sets long click events
		mDrawerCategoriesList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if (mDrawerCategoriesList.getAdapter() != null) {
					Object item = mDrawerCategoriesList.getAdapter().getItem(position);
					// Ensuring that clicked item is not the ListView header
					if (item != null) {
						mActivity.editTag((Category) item);
					}
				} else {
					Crouton.makeText(mActivity, R.string.category_deleted, ONStyle.ALERT).show();
				}
				return true;
			}
		});		

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

				// Commits all pending actions
				mActivity.commitPending();	

				// Finishes action mode
				mActivity.finishActionMode();	
				
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
					list.add(new Integer[] { R.id.menu_add_category, R.string.tour_listactivity_tag_title,
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
		
        // just styling option
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
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
			mTitle = ((Category)itemSelected).getName();
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
