package it.feio.android.omninotes.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SpinnerDialog extends DialogFragment {

	public SpinnerDialog() {
		// use empty constructors. If something is needed use onCreate's
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {

		ProgressDialog dialog = new ProgressDialog(getActivity());
//		this.setStyle(STYLE_NO_TITLE, getTheme()); // You can use styles or
//													// inflate a view
//		dialog.setMessage("Spinning.."); // set your messages if not inflated
											// from XML
//		dialog.setView(new Spinner(getActivity(), STYLE_NO_TITLE));
		dialog.setCancelable(false);

		return dialog;
	}
}