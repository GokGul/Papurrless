package com.example.gokhan.papurrless;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class EditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        EditText productsEdit = (EditText) findViewById(R.id.editProducts);
        EditText pricesEdit = (EditText) findViewById(R.id.editPrices);
        productsEdit.setHorizontallyScrolling(true);
        pricesEdit.setHorizontallyScrolling(true);
    }
}
