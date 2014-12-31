/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.PasswordValidator;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Security;

public class PasswordActivity extends BaseActivity {

	private EditText passwordCheck;
	private EditText password;
	private Button confirm;
	private EditText question;
	private EditText answer;
	private EditText answerCheck;
	private Button reset;
	private PasswordActivity mActivity;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password);		
		mActivity = this;
		setActionBarTitle(getString(R.string.title_activity_password));		
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
					if (prefs.getString(Constants.PREF_PASSWORD, null) != null) {
						requestPassword(mActivity, new PasswordValidator() {							
							@Override
							public void onPasswordValidated(boolean passwordConfirmed) {
								if (passwordConfirmed) {
									updatePassword(passwordText, questionText, answerText);
								}
								
							}
						});
					} else {
						updatePassword(passwordText, questionText, answerText);
					}
				}
			}
		});
		
		reset = (Button)findViewById(R.id.password_reset);
		reset.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				if (prefs.getString(Constants.PREF_PASSWORD, "").length() == 0) {
					Crouton.makeText(mActivity, R.string.password_not_set, ONStyle.WARN).show();
					return;
				}				
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
				
				// Inflate layout
				View layout = getLayoutInflater().inflate(R.layout.password_reset_dialog_layout, null);
				alertDialogBuilder.setView(layout);
				TextView questionTextView = (TextView) layout.findViewById(R.id.reset_password_question);
				questionTextView.setText(prefs.getString(Constants.PREF_PASSWORD_QUESTION, ""));
				final EditText answerEditText = (EditText) layout.findViewById(R.id.reset_password_answer);
				
				// Set dialog message and button
				alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton(R.string.confirm, null)
					.setNegativeButton(R.string.cancel, null);
				
				AlertDialog dialog = alertDialogBuilder.create();
				
				// Set a listener for dialog button press
				dialog.setOnShowListener(new DialogInterface.OnShowListener() {

				    @Override
				    public void onShow(final DialogInterface dialog) {

				        Button pos = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
				        pos.setOnClickListener(new View.OnClickListener() {

				            @Override
				            public void onClick(View view) {
				            	// When positive button is pressed answer correctness is checked
				            	String oldAnswer = prefs.getString(Constants.PREF_PASSWORD_ANSWER, "");
								String answer = answerEditText.getText().toString();
								// The check is done on password's hash stored in preferences
								boolean result = Security.md5(answer).equals(oldAnswer);

				                if (result) {
				                	dialog.dismiss();
				                	prefs.edit()
									.remove(Constants.PREF_PASSWORD)
									.remove(Constants.PREF_PASSWORD_QUESTION)
									.remove(Constants.PREF_PASSWORD_ANSWER)
									.remove("settings_password_access")
									.commit();
									DbHelper.getInstance(getApplicationContext()).unlockAllNotes();
									Crouton.makeText(mActivity, R.string.password_successfully_removed, ONStyle.ALERT).show();
				                } else {
				                	answerEditText.setError(getString(R.string.wrong_answer));
				                }
				            }
				        });
				        Button neg = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
				        neg.setOnClickListener(new View.OnClickListener() {

				            @Override
				            public void onClick(View view) {
			                	dialog.dismiss();
				            }
				        });
				    }
				});
				
				dialog.show();
			}
		});
	}


	private void updatePassword(String passwordText, String questionText, String answerText) {
		// If password have to be removed will be prompted to user to agree to unlock all notes
		if (password.length() == 0) {
			
			if (prefs.getString(Constants.PREF_PASSWORD, "").length() == 0) {
				Crouton.makeText(mActivity, R.string.password_not_set, ONStyle.WARN).show();
				return;
			}		
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
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
									DbHelper.getInstance(getApplicationContext()).unlockAllNotes();
									Crouton.makeText(mActivity, R.string.password_successfully_removed, ONStyle.ALERT).show();
//									onBackPressed(); 
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
				.putString(Constants.PREF_PASSWORD_ANSWER, Security.md5(answerText))
				.commit();
//			onBackPressed();
			Crouton.makeText(mActivity, R.string.password_successfully_changed, ONStyle.CONFIRM).show();
		}
	}

	
	
	/**
	 * Checks correctness of form data
	 * @return
	 */
	private boolean checkData(){
		boolean res = true;
		
		if (password.getText().length() == passwordCheck.getText().length() 
				&& passwordCheck.getText().length() == 0) {
			return true;
		}
		
		boolean passwordOk = password.getText().toString().length() > 0;
		boolean passwordCheckOk = passwordCheck.getText().toString().length() > 0 && password.getText().toString().equals(passwordCheck.getText().toString());
		boolean questionOk = question.getText().toString().length() > 0;
		boolean answerOk = answer.getText().toString().length() > 0;
		boolean answerCheckOk = answerCheck.getText().toString().length() > 0 && answer.getText().toString().equals(answerCheck.getText().toString());
		
		if (!passwordOk || !passwordCheckOk || !questionOk || !answerOk || !answerCheckOk){
			res = false;
			if (!passwordOk) {
				password.setError(getString(R.string.settings_password_not_matching));
			}
			if (!passwordCheckOk) {
				passwordCheck.setError(getString(R.string.settings_password_not_matching));
			}
			if (!questionOk) {
				question.setError(getString(R.string.settings_password_question));
			}
			if (!answerOk) {
				answer.setError(getString(R.string.settings_answer_not_matching));
			}
			if (!answerCheckOk) {
				answerCheck.setError(getString(R.string.settings_answer_not_matching));
			}
		}	
		return res;
	}
	
	
	
	@Override
	public void onBackPressed() {	
		setResult(RESULT_OK);
		finish();
	}

}
