package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.MainActivity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtils {

	public static void showKeyboard(View view) {
		if (view == null) {
			return;
		}
		
		view.requestFocus();
		
		InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

		((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(view, 0);
		
		if (!isKeyboardShowed(view)) {
	        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		}
	}

	public static boolean isKeyboardShowed(View view) {
		if (view == null) {
			return false;
		}
		InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		return inputManager.isActive(view);
	}

	public static void hideKeyboard(View view) {
		if (view == null) {
			return;
		}
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (!imm.isActive()) {
			return;
		}
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		
//		if (!isKeyboardShowed(view)) {
//			imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, InputMethodManager.RESULT_HIDDEN);
//		}
		
	}

	public static void hideKeyboard(MainActivity mActivity) {
		mActivity.getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
}
