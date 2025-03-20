/*
 * Copyright (C) 2013-2024 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.utils;

import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PASSWORD;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PASSWORD_ANSWER;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PASSWORD_QUESTION;

import android.app.Activity;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pixplicity.easyprefs.library.Prefs;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.bus.PasswordRemovedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.PasswordValidator;
import lombok.experimental.UtilityClass;
import android.content.DialogInterface;

@UtilityClass
public class PasswordHelper {
  public static void requestPassword(final Activity activity, final PasswordValidator passwordValidator) {
    LayoutInflater inflater = activity.getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.password_request_dialog_layout, null);
    EditText passwordEditText = dialogView.findViewById(R.id.password_request);

    if (passwordEditText == null) {
        throw new IllegalStateException("View not found.Ensure the view exists in the layout file.");
    }

    MaterialDialog dialog = new MaterialDialog.Builder(activity)
        .autoDismiss(false)
        .title(R.string.insert_security_password)
        .customView(dialogView, false)
        .positiveText(R.string.ok)
        .positiveColorRes(R.color.colorPrimary)
        .onPositive((dialogInterface, which) -> validatePassword(activity, passwordEditText, passwordValidator, dialogInterface))
        .neutralText(activity.getString(R.string.password_forgot))
        .onNeutral((dialogInterface, which) -> handleForgotPassword(activity, passwordValidator, dialogInterface))
        .build();

    dialog.setOnCancelListener(dialogInterface -> handleCancel(passwordEditText, passwordValidator, dialogInterface));

    passwordEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            dialog.getActionButton(DialogAction.POSITIVE).callOnClick();
            return true;
        }
        return false;
    });

    dialog.show();
    new Handler().postDelayed(() -> KeyboardUtils.showKeyboard(passwordEditText), 100);
}

private static void validatePassword(Activity activity, EditText passwordEditText, 
                                     PasswordValidator passwordValidator, DialogInterface dialogInterface) {
    String storedPassword = Prefs.getString(PREF_PASSWORD, "");
    String enteredPassword = passwordEditText.getText().toString();
    boolean isValid = Security.md5(enteredPassword).equals(storedPassword);

    if (isValid) {
        KeyboardUtils.hideKeyboard(passwordEditText);
        dialogInterface.dismiss();
        passwordValidator.onPasswordValidated(PasswordValidator.Result.SUCCEED);
    } else {
        passwordEditText.setError(activity.getString(R.string.wrong_password));
    }
}

private static void handleForgotPassword(Activity activity, PasswordValidator passwordValidator, 
                                         DialogInterface dialogInterface) {
    PasswordHelper.resetPassword(activity);
    passwordValidator.onPasswordValidated(PasswordValidator.Result.RESTORE);
    dialogInterface.dismiss();
}

private static void handleCancel(EditText passwordEditText, PasswordValidator passwordValidator, 
                                 DialogInterface dialogInterface) {
    KeyboardUtils.hideKeyboard(passwordEditText);
    dialogInterface.dismiss();
    passwordValidator.onPasswordValidated(PasswordValidator.Result.FAIL);
}
public static void resetPassword(final Activity activity) {
  View layout = activity.getLayoutInflater().inflate(R.layout.password_reset_dialog_layout, null);
  final EditText answerEditText = layout.findViewById(R.id.reset_password_answer);

  if (answerEditText == null) {
      throw new IllegalStateException("View not found.Ensure the view exists in the layout file.");
  }

  MaterialDialog dialog = new MaterialDialog.Builder(activity)
      .title(Prefs.getString(PREF_PASSWORD_QUESTION, ""))
      .customView(layout, false)
      .autoDismiss(false)
      .contentColorRes(R.color.text_color)
      .positiveText(R.string.ok)
      .onPositive((dialogElement, which) -> {
          // When positive button is pressed answer correctness is checked          
          String oldAnswer = Prefs.getString(PREF_PASSWORD_ANSWER, "");
          String newAnswer = answerEditText.getText().toString();
          // The check is done on password's hash stored in preferences
          boolean isCorrect = Security.md5(newAnswer).equals(oldAnswer);

          if (isCorrect) {
              dialogElement.dismiss();
              removePassword();
          } else {
              answerEditText.setError(activity.getString(R.string.wrong_answer));
          }
      })
      .build();

  dialog.show();

  // Configura o listener apenas se answerEditText não for nulo
  answerEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
          dialog.getActionButton(DialogAction.POSITIVE).callOnClick();
          return true;
      }
      return false;
  });

  new Handler().postDelayed(() -> KeyboardUtils.showKeyboard(answerEditText), 100);
}


  public static void removePassword() {
    DbHelper.getInstance().getNotesWithLock(true).forEach(note -> {
      note.setLocked(false);
      DbHelper.getInstance().updateNote(note, false);
    });
    Prefs.edit()
        .remove(PREF_PASSWORD)
        .remove(PREF_PASSWORD_QUESTION)
        .remove(PREF_PASSWORD_ANSWER)
        .remove("settings_password_access")
        .apply();
    EventBus.getDefault().post(new PasswordRemovedEvent());
  }

}
