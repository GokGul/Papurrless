package com.example.gokhan.papurrless;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class EditorActivity extends AppCompatActivity {

    String date, market, products, prices, totalprice;
    boolean isFavorite;
    int receiptId;

    Spinner marketSpinner;

    EditText productsEdit, pricesEdit, totalpriceEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        TextView dateDisplay = (TextView) findViewById(R.id.date);
        marketSpinner = (Spinner) findViewById(R.id.supermarket);
        ArrayAdapter<CharSequence> adapter;
        productsEdit = (EditText) findViewById(R.id.editProducts);
        pricesEdit = (EditText) findViewById(R.id.editPrices);
        totalpriceEdit = (EditText) findViewById(R.id.editTotalPrice);

        productsEdit.setHorizontallyScrolling(true);
        pricesEdit.setHorizontallyScrolling(true);

        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            date = extras.getString("date");
            market = extras.getString("market");
            products = extras.getString("products");
            prices = extras.getString("prices");
            totalprice = extras.getString("totalprice");
            isFavorite = extras.getBoolean("isFavorite");
            receiptId = extras.getInt("receiptId");

            dateDisplay.setText(date);
            productsEdit.setText(products);
            pricesEdit.setText(prices);
            totalpriceEdit.setText(totalprice);

            adapter = ArrayAdapter.createFromResource(this, R.array.markets_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            marketSpinner.setAdapter(adapter);

            if (!market.equals(null)) {
                int spinnerPosition = adapter.getPosition(market);
                marketSpinner.setSelection(spinnerPosition);
            }
        }
    }

    public void cancel(View view)
    {
        finish();
    }

    public void ok(View view)
    {
        getAllStrings();

//        Bundle b = new Bundle();
//        b.putString("date", date);
//        b.putString("market", market);
//        b.putString("products", products);
//        b.putString("prices", prices);
//        b.putString("totalprice", totalprice);
//        b.putBoolean("isFavorite", isFavorite);
//        b.putInt("receiptId", receiptId);
//
//        Intent returnIntent = new Intent();
//        returnIntent.putExtras(b);
//        setResult(1342, returnIntent);
//        finish();

        //update het receipt in de storage (en evt. de cloud)

        //restart de app:
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void getAllStrings()
    {
        market = marketSpinner.getSelectedItem().toString();
        products = productsEdit.getText().toString();
        prices = pricesEdit.getText().toString();
        totalprice = totalpriceEdit.getText().toString();
    }

}
