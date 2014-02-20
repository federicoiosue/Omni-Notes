package it.feio.android.omninotes;

import it.feio.android.omninotes.models.PasswordValidator;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Security;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PasswordActivity extends BaseActivity {

	private EditText passwordCheck;
	private EditText password;
	private Button confirm;
	private String oldPassword;
	private EditText question;
	private EditText answer;
	private EditText answerCheck;
	private Button reset;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password);

		// Show the Up button in the action bar.
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayShowTitleEnabled(false);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		oldPassword = prefs.getString(Constants.PREF_PASSWORD, null);
		
		initViews();
	}



	private void initViews() {
		password = (EditText)findViewById(R.id.password);
		passwordCheck = (EditText)findViewById(R.id.password_check);
		question = (EditText)findViewById(R.id.question);
		answer = (EditText)findViewById(R.id.answer);
		answerCheck = (EditText)findViewById(R.id.answer_check);
		
		confirm = (Button)findViewById(R.id.password_confirm);
		confirm.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (checkData()){
					final String passwordText = password.getText().toString();
					final String questionText = question.getText().toString();
					final String answerText = answer.getText().toString();
					if (oldPassword != null) {
						requestPassword(new PasswordValidator() {							
							@Override
							public void onPasswordValidated(boolean result) {
								updatePassword(passwordText, questionText, answerText);
								
							}
						});
					} else {
						updatePassword(passwordText, questionText, answerText);
					}
				}
			}
		});
		
		reset = (Button)findViewById(R.id.password_confirm);
		reset.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						mActivity);
				alertDialogBuilder
						.setMessage(prefs.getString(Constants.PREF_PASSWORD_QUESTION, ""))
						.setPositiveButton(R.string.confirm,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										prefs.edit()
												.remove(Constants.PREF_PASSWORD)
												.commit();
										db.unlockAllNotes();
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
				
			}
		});
	}


	private void updatePassword(String passwordText, String questionText, String answerText) {
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
											.remove(Constants.PREF_PASSWORD_QUESTION)
											.remove(Constants.PREF_PASSWORD_ANSWER)
											.commit();
									db.unlockAllNotes();
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
			prefs.edit()
				.putString(Constants.PREF_PASSWORD, Security.md5(passwordText))
				.putString(Constants.PREF_PASSWORD_QUESTION, questionText)
				.putString(Constants.PREF_PASSWORD, Security.md5(answerText))
				.commit();
			onBackPressed();
		}
	}

	
	
	/**
	 * Checks correctness of form data
	 * @return
	 */
	private boolean checkData(){
		boolean res = true;
		boolean passwordMatch = password.getText().toString().equals(passwordCheck.getText().toString());
		boolean answerMatch = answer.getText().toString().equals(answerCheck.getText().toString());
		if (!passwordMatch || !answerMatch){
			res = false;
			if (!passwordMatch)
				passwordCheck.setError(getString(R.string.settings_password_not_matching));
			if (!answerMatch)
				answerCheck.setError(getString(R.string.settings_answer_not_matching));
		}	
		return res;
	}
	
	
	
	@Override
	public void onBackPressed() {	
		setResult(RESULT_OK);
		finish();
	}

}
