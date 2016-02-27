package com.example.gokhan.papurrless;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

public class LoginActivity extends AppCompatActivity {

    private boolean premiumEnabled = false;
    private SharedPreferences.Editor editor;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.premiumEnabled_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        premiumEnabled = sharedPref.getBoolean(getString(R.string.premiumEnabled), false);

        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setChecked(premiumEnabled);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkBox:
                editor.putBoolean(getString(R.string.premiumEnabled), checked);
                editor.commit();
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
