package com.example.gokhan.papurrless;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends AppCompatActivity {

    String date, market, productsS, pricesS, totalprice;
    boolean isFavoriteS;
    int receiptId;
    ParseUser user;

    Spinner marketSpinner;
    EditText productsEdit, pricesEdit, totalpriceEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        user = ParseUser.getCurrentUser();

        TextView dateDisplay = (TextView) findViewById(R.id.date);
        marketSpinner = (Spinner) findViewById(R.id.supermarket);
        ArrayAdapter<CharSequence> adapter;
        productsEdit = (EditText) findViewById(R.id.editProducts);
        pricesEdit = (EditText) findViewById(R.id.editPrices);
        totalpriceEdit = (EditText) findViewById(R.id.editTotalPrice);

        productsEdit.setHorizontallyScrolling(true);
        pricesEdit.setHorizontallyScrolling(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            date = extras.getString("date");
            market = extras.getString("market");
            productsS = extras.getString("products");
            pricesS = extras.getString("prices");
            totalprice = extras.getString("totalprice");
            isFavoriteS = extras.getBoolean("isFavorite");
            receiptId = extras.getInt("receiptId");
            //dateTime = extras.getString("dateTime");
            dateDisplay.setText(date);
            productsEdit.setText(productsS);
            pricesEdit.setText(pricesS);
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

    public void cancel(View view) {
        finish();
    }

    public void ok(View view) {
        getAllStrings();

        if(user.get("isPremium").toString().equals("true")){
            updateOnlineStorage();
        }
        else{
            updateLocalStorage();
        }
        //update het receipt in de storage (en evt. de cloud)



        //restart de app:
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void getAllStrings() {
        market = marketSpinner.getSelectedItem().toString();
        productsS = productsEdit.getText().toString();
        pricesS = pricesEdit.getText().toString();
        totalprice = totalpriceEdit.getText().toString();
    }

    public void updateLocalStorage() {
        try {
            String dateTime = date;
            String groceryStore = market.trim();
            String products[] = productsS.split("\\r?\\n");
            String prices[] = pricesS.split("\\r?\\n");
            String subtotaal = "SUBTOTAAL" + totalprice.replaceAll("[^\\d,.]+", "").trim();
            boolean isFavorite = isFavoriteS;

            ArrayList<String> data = new ArrayList();

            String path = Environment.getExternalStorageDirectory().toString() +
                    "/Papurrless/scanned-data" + dateTime;

            File file = new File(path);
            file.delete();

            if (isFavorite)
                data.add("isFavorite");
            data.add(groceryStore);
            data.add("EUR");

            for(int i = 0; (i<products.length && i<prices.length); i++)
            {
                data.add(products[i].trim() + prices[i].replaceAll("[^\\d,.]+", "").trim());
            }

            data.add(subtotaal);



            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String lines : data) {
                bw.write(lines + "\n");
            }
            bw.flush();
            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateOnlineStorage() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");
        query.whereEqualTo("User", user);
        query.whereEqualTo("date", date);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for(ParseObject selectedReceipt : objects){
                    selectedReceipt.put("store", market);
                    selectedReceipt.put("products", productsS);
                    selectedReceipt.put("prices", pricesS);
                    selectedReceipt.put("subtotaal", totalprice);
                    try {
                        selectedReceipt.save();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

}
