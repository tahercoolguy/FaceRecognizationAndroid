package com.example.facerecognization.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.facerecognization.AI.Recognizer;
import com.example.facerecognization.R;
import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.io.IOException;

public class DetectedFace extends AppCompatActivity {
 Recognizer recognizer;
 ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detected_face);
        imageView=(ImageView) findViewById(R.id.detectedFaces);
        Bitmap bmp = null;
        Bitmap one=null;
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            one = bmp.copy(bmp.getConfig(),bmp.isMutable());
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            recognizer=new Recognizer(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
       imageView.setImageBitmap(bmp);
        recognizer.recognize(one);
//
    }
}
