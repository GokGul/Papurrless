package com.example.gokhan.papurrless;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static String tag = "Main Activity";
    private SharedPreferences.Editor editor;

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private ListFragment.AllFragment allFrag;
    private ListFragment.FavFragment favFrag;

    private final int TAKE_PICTURE = 0;
    private final int SELECT_FILE = 1;

    private String outputPath = "result.txt";

    private boolean premiumEnabled = false;

    ParseUser user = ParseUser.getCurrentUser();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.premiumEnabled_key), Context.MODE_PRIVATE);
        premiumEnabled = sharedPref.getBoolean(getString(R.string.premiumEnabled), false);
        editor = sharedPref.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        final CharSequence opts[] = new CharSequence[]{"Camera", "Storage"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select image from");
        builder.setItems(opts, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        captureImageFromCamera();
                        break;
                    case 1:
                        captureImageFromSdCard();
                        break;
                }
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.show();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem loginMenuItem = menu.findItem(R.id.action_login);

        if(user != null)
        {
            loginMenuItem.setTitle(R.string.action_log_out);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_login) {
            if(user != null)
                logOut();
            else
                logIn();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logIn()
    {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void logOut()
    {
        premiumEnabled = false;
        editor.putBoolean(getString(R.string.premiumEnabled), false);
        editor.commit();

        Toast.makeText(this, R.string.toast_logged_out, Toast.LENGTH_SHORT).show();

        this.recreate();
    }

    private static Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Papurrless");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }

        // Create a media file name
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "image.jpg");

        return mediaFile;
    }

    public String getDateTime(){

        ExifInterface exitInterface = null;
        String dateTime = null;
        try
        {
            String path =  getOutputMediaFile().getPath();
            exitInterface = new ExifInterface(path);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        if(exitInterface != null)
        {
            dateTime = exitInterface.getAttribute(ExifInterface.TAG_DATETIME);
        }

        return dateTime;
    }

    public void saveDataToStorage(ArrayList<String> data){

        try{
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Papurrless");
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs();
            }

            String dateTaken = getDateTime();

            File file = new File(mediaStorageDir.getPath() + File.separator + "scanned-data" + dateTaken + ".txt");
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
        }
        catch(Exception e) {
            e.printStackTrace();

        }
    }

    public void captureImageFromSdCard() {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");

            startActivityForResult(intent, SELECT_FILE);
    }

    public void captureImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Uri fileUri = getOutputMediaFileUri(); // create a file to save the image
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;

        String imageFilePath = null;

        switch (requestCode) {
            case TAKE_PICTURE:
                if(resultCode == RESULT_OK) {
                    saveImageToParse(data);
                }
                imageFilePath = getOutputMediaFile().getPath();
                break;
            case SELECT_FILE:
                Uri imageUri = data.getData();
                String[] projection = { MediaStore.Images.Media.DATA };
                Cursor cur = getContentResolver().query(imageUri, projection, null, null, null);
                cur.moveToFirst();
                imageFilePath = cur.getString(cur.getColumnIndex(MediaStore.Images.Media.DATA));
                break;
        }

        new AsyncProcessTask(this).execute(imageFilePath, outputPath);
    }

    private void saveImageToParse(Intent data) {
        // Resize photo from camera byte array

        Bitmap mealImage = (Bitmap) data.getExtras().get("data");



        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mealImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] scaledData = bos.toByteArray();

        // Save the scaled image to Parse
        ParseFile photoFile = new ParseFile("image.jpg", scaledData);

        ParseUser user = ParseUser.getCurrentUser();
        ParseObject img = new ParseObject("Image");
        img.put("Image", photoFile);
        img.put("User", user);

        img.saveInBackground();
    }

    public void updateResults(Boolean success) {
        if (!success)
            return;
        try {
            StringBuffer contents = new StringBuffer();
            List<String> receiptLines = new ArrayList();
            FileInputStream fis = openFileInput(outputPath);
            try {
                Reader reader = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufReader = new BufferedReader(reader);
                String text = null;

                while ((text = bufReader.readLine()) != null) {
                    contents.append(text).append(System.getProperty("line.separator"));

                    //remove all spaces
                    text = text.replaceAll("\\s+", "");

                    //ignore lines that only contain whitespace and no characters
                    if(text.matches(".*\\w.*")) {
                        //this might seem redundant, but somehow the text variable still has leading and trailing spaces.
                        text = text.trim();
                        receiptLines.add(text);
                    }
                }
            } finally {
                fis.close();
            }
            processReceipt(receiptLines);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void processReceipt(List<String> lines){

        String groceryStore = "";
        String products = "";
        String prices = "";
        String subtotaal = "";
        ArrayList<String> linesToFile = new ArrayList();

        boolean isGroceryStore = true;
        boolean isProduct = false;

        for(String line : lines){

            //
            String _value = null;
            if(groceryStore.equals("")) {
                _value = line.substring(1, 5);
            }
            //
            if(isGroceryStore) {
                switch (_value) {
                    case "Albe":
                        groceryStore = "AH";
                        isGroceryStore = false;
                        break;
                    case "Aibe":
                        groceryStore = "AH";
                        isGroceryStore = false;
                        break;
                    case "Jumb":
                        groceryStore = "Jumbo";
                        isGroceryStore = false;
                        break;
                    case "Aldi":
                        groceryStore = "Aldi";
                        isGroceryStore = false;
                        break;
                    case "Aidi":
                        groceryStore = "Aldi";
                        isGroceryStore = false;
                        break;
                    case "Dirk":
                        groceryStore = "Dirk";
                        isGroceryStore = false;
                        break;
                    case "Dink":
                        groceryStore = "Dirk";
                        isGroceryStore = false;
                        break;
                    default:_value= "";
                }
            }

            //for Albert Heijn receipts only!
            if(groceryStore.equals("AH")){

                //go to next item in the map if the line matches EUR. on the AH receipts products appear right underneath this line
                if(line.substring(0, line.length()).equals("EUR")){
                    isProduct = true;
                    continue;
                }
                if(isProduct) {

                    //only keep uppercase and lowercase letters
                    String _product = line.replaceAll("[^A-Za-z]", "") + "\n";
                    products += _product;

                    //retains the digits, dots and commas
                    String _price = line.replaceAll("[^\\d,.]+", " ") + "\n";
                    prices += "€"+ _price;

                    String _subTotaal = _product.trim();

                    linesToFile.add(line);
                    if (_subTotaal.equals("SUBTOTAAL") || _subTotaal.equals("TOTAAL")) {
                        subtotaal = _price;
                        isProduct = false;
                        break;
                    }
                }
            }
        }
        allFrag.addReceipt(allFrag.new ReceiptContent(groceryStore, "01-01-1000", products, prices, subtotaal, false, 666));
        saveDataToStorage(linesToFile);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.gokhan.papurrless/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.gokhan.papurrless/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ListFragment.FavFragment.newInstance(position + 1, premiumEnabled);
                case 1:
                    return ListFragment.AllFragment.newInstance(position + 1, premiumEnabled);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.favorites_section);
                case 1:
                    return getString(R.string.all_section);
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    favFrag = (ListFragment.FavFragment) createdFragment;
                    if (allFrag != null) {
                        favFrag.setOtherFrag(allFrag);
                        allFrag.setOtherFrag(favFrag);
                    }
                    break;
                case 1:
                    allFrag = (ListFragment.AllFragment) createdFragment;
                    if (favFrag != null) {
                        allFrag.setOtherFrag(favFrag);
                        favFrag.setOtherFrag(allFrag);
                    }
                    break;
            }
            return createdFragment;
        }
    }

}
