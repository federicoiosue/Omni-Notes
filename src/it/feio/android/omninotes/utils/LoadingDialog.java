package it.feio.android.omninotes.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class LoadingDialog extends ProgressDialog {

	Context context;

	public LoadingDialog(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.cancel();
	}

}
