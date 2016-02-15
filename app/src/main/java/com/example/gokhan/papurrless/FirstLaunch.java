package com.example.gokhan.papurrless;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class FirstLaunch extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch);
    }
}

//Show window only at first launch (or wipe)
    //Three buttons: login, register or skip to app (home)
    //After skip, the only way to login or register is via settings