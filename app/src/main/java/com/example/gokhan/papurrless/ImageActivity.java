package com.example.gokhan.papurrless;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imageView = (ImageView) findViewById(R.id.img_bon);
        showImage();
    }

    public void showImage(){
        byte[] img = GlobalImage.img;
        Bitmap bMap = BitmapFactory.decodeByteArray(img, 0, img.length);
        imageView.setImageBitmap(bMap);
    }
}
