package it.feio.android.omninotes;

import it.feio.android.omninotes.models.PasswordValidator;
import it.feio.android.omninotes.utils.Constants;
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
		oldPassword = prefs.getString(Constants.PREF_PASSWORD, "");
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
					if (oldPassword != "") {
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
		prefs.edit().putString(Constants.PREF_PASSWORD, password).commit();
		showToast(getString(R.string.password_successfully_changed), Toast.LENGTH_SHORT);
		onBackPressed();
	}

}
