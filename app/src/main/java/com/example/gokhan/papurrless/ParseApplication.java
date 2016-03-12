package com.example.gokhan.papurrless;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "6HNBirFawMZ2KHoFESD00r1Xt8PY1vA7XXvi1g2r", "gnHTt4uOZBs37UOhVGMI4tMiI4CB2zXMHzQHS2XK");
    }
}
