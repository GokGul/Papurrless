package com.example.gokhan.papurrless;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    EditText password2;
    CheckBox isPremium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.action_register);
        initViews();
    }

    public void registerUser(View view){
        String usr = username.getText().toString();
        String pwd = password.getText().toString();
        String pwd2 = password2.getText().toString();

        Log.d("isPremiumOrNot??", "Value: " + isPremium);

        if(pwd.equals(pwd2)) {
            ParseUser user = new ParseUser();
            user.setUsername(usr);
            user.setPassword(pwd);
            user.put("isPremium", isPremium.isChecked());

            user.signUpInBackground(new SignUpCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(RegisterActivity.this, R.string.toast_account_created, Toast.LENGTH_SHORT).show();

                        Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(login);
                    } else {
                        Toast.makeText(RegisterActivity.this, R.string.error_generic_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            Toast.makeText(RegisterActivity.this, R.string.error_password_no_match, Toast.LENGTH_SHORT).show();
        }
    }

    public void login(View view){
        Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(login);
    }

    public void initViews(){
        username = (EditText) findViewById(R.id.txt_username);
        password = (EditText) findViewById(R.id.txt_password);
        password2 = (EditText) findViewById(R.id.txt_password2);
        isPremium = (CheckBox) findViewById(R.id.chk_premium);
    }
}
