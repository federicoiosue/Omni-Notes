package it.feio.android.omninotes.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Category;
import android.content.Context;

public class Navigation {

	public static final int NOTES = 0;
	public static final int ARCHIVED = 1;
	public static final int REMINDERS = 2;
	public static final int TRASH = 3;
	public static final int CATEGORY = 4;
	
	
	/**
	 * Returns actual navigation status
	 * @return
	 */
	public static int getNavigation() {
		Context mContext = OmniNotes.getAppContext();
		String[] navigationListCodes = mContext.getResources().getStringArray(R.array.navigation_list_codes);
		@SuppressWarnings("static-access")
		String navigation = mContext.getSharedPreferences(Constants.PREFS_NAME, mContext.MODE_MULTI_PROCESS).getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);
		
		if (navigationListCodes[0].equals(navigation)) {
			return NOTES;
		} 
		else if (navigationListCodes[1].equals(navigation)) {
			return ARCHIVED;
		} 
		else if (navigationListCodes[2].equals(navigation)) {
			return REMINDERS;
		} 
		else if (navigationListCodes[3].equals(navigation)) {
			return TRASH;
		} 
		else {
			return CATEGORY;
		} 
	}
	
	
	
	/**
	 * Retrieves category currently shown
	 * @return id of category or 0 if current navigation is not a category
	 */
	public static String getCategory() {
		if (getNavigation() == CATEGORY) {
			Context mContext = OmniNotes.getAppContext();
			return mContext.getSharedPreferences(Constants.PREFS_NAME, mContext.MODE_MULTI_PROCESS).getString(Constants.PREF_NAVIGATION, "");
		} else {
			return null;
		}
	}
	
	
	
	/**
	 * Checks if passed parameters is the actual navigation status
	 * @param navigationToCheck
	 * @return
	 */
	public static boolean checkNavigation(int navigationToCheck) {
		return checkNavigation(new Integer[]{navigationToCheck});
	}
	
	public static boolean checkNavigation(Integer[] navigationsToCheck) {
		boolean res = false;
		int navigation = getNavigation();
		for (int navigationToCheck : new ArrayList<Integer>(Arrays.asList(navigationsToCheck))) {
			if (navigation == navigationToCheck) {
				res = true;
				break;
			}
		}
		return res;
	}
	
	
	/**
	 * Checks if passed parameters is the category user is actually navigating in
	 * @param categoryToCheck
	 * @return
	 */
	public static boolean checkNavigationCategory(Category categoryToCheck) {
		Context mContext = OmniNotes.getAppContext();
		String[] navigationListCodes = mContext.getResources().getStringArray(R.array.navigation_list_codes);
		String navigation = mContext.getSharedPreferences(Constants.PREFS_NAME, mContext.MODE_MULTI_PROCESS).getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);
		if (categoryToCheck != null && navigation.equals(String.valueOf(categoryToCheck.getId()))) {
			return true;
		} else {
			return false;
		}
	}

}
