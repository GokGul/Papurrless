package com.example.gokhan.papurrless;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class FirstLaunch extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch);

        //Hide actionbar
        //getSupportActionBar().hide();
    }

    public void skipLogin(View view){
        Intent skipLogin = new Intent(FirstLaunch.this, MainActivity.class);
        startActivity(skipLogin);
    }

    public void Login(View view){
        Intent Login = new Intent(FirstLaunch.this, LoginActivity.class);
        startActivity(Login);
    }
}

//Show window only at first launch (or wipe)
    //Three buttons: login, register or skip to app (home)
    //After skip, the only way to login or register is via settings