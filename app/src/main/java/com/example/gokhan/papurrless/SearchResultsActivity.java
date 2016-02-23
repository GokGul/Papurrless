package com.example.gokhan.papurrless;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SearchResultsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent queryIntent = getIntent();
        setContentView(R.layout.activity_search_results);
        doSearchQuery(queryIntent);
    }

    @Override
    public void onNewIntent(final Intent newIntent) {
        super.onNewIntent(newIntent);
        final Intent queryIntent = getIntent();
        doSearchQuery(queryIntent);
    }

    private void doSearchQuery(final Intent queryIntent) {
        //haha nope
    }
}
