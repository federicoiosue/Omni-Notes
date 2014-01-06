package it.feio.android.omninotes;

import it.feio.android.omninotes.models.PasswordValidator;
import it.feio.android.omninotes.utils.Constants;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordActivity extends BaseActivity {

	private EditText passwordCheck;
	private EditText password;
	private Button confirm;
	private String oldPassword;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password);
		oldPassword = prefs.getString(Constants.PREF_PASSWORD, null);
		initViews();
	}



	private void initViews() {
		password = (EditText)findViewById(R.id.password);
		passwordCheck = (EditText)findViewById(R.id.password_check);
		confirm = (Button)findViewById(R.id.password_confirm);
		confirm.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (!password.getText().toString().equals(passwordCheck.getText().toString())){
					showToast(getString(R.string.settings_password_not_matching), Toast.LENGTH_SHORT);
				} else {
					if (oldPassword != null) {
						requestPassword(new PasswordValidator() {							
							@Override
							public void onPasswordValidated(boolean result) {
								if (result) {
									updatePassword(password.getText().toString());
								} else {
									showToast(getString(R.string.wrong_password), Toast.LENGTH_SHORT);									
								}
								
							}
						});
					} else {
						updatePassword(password.getText().toString());
					}
				}
			}
		});
	}


	private void updatePassword(String password) {
		// If password have to be removed will be prompted to user to agree to unlock all notes
		if (password.length() == 0) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					mActivity);
			alertDialogBuilder
					.setMessage(R.string.agree_unlocking_all_notes)
					.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									prefs.edit()
											.remove(Constants.PREF_PASSWORD)
											.commit();
									db.unlockAllNotes();
									showToast(
											getString(R.string.password_successfully_removed),
											Toast.LENGTH_SHORT);
									onBackPressed();
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		} else {
			prefs.edit().putString(Constants.PREF_PASSWORD, password).commit();
			showToast(getString(R.string.password_successfully_changed),
					Toast.LENGTH_SHORT);
			onBackPressed();
		}
	}

}
