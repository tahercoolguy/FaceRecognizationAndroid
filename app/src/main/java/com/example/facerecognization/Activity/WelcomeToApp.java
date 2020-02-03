package com.example.facerecognization.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.facerecognization.AI.Recognizer;
import com.example.facerecognization.R;
import com.example.facerecognization.View.Rectangle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeToApp extends AppCompatActivity {
    FirebaseVisionImage image;
    FirebaseVisionFaceDetector firebaseVisionFaceDetector;
    Bitmap croppedImage;
    Recognizer recognizer;
    FirebaseVisionFaceDetectorOptions highAccuracyOpts;
    @BindView(R.id.newImage)
    ImageView newImage;
    @BindView(R.id.oldImage)
    ImageView oldImage;

    Recognizer recognizer1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_to_app);
        ButterKnife.bind(this);
        try {
            recognizer=new Recognizer(WelcomeToApp.this);
            recognizer1=new Recognizer(WelcomeToApp.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap lady=BitmapFactory.decodeResource(getResources(),R.drawable.face6);
        image=FirebaseVisionImage.fromBitmap(lady);
        highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        firebaseVisionFaceDetector = FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts);
        firebaseVisionFaceDetector.detectInImage(image).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionFace>>() {
            @Override
            public void onComplete(@NonNull Task<List<FirebaseVisionFace>> task) {

                int height = (task.getResult().get(0).getBoundingBox().bottom - task.getResult().get(0).getBoundingBox().top);
                int width = (task.getResult().get(0).getBoundingBox().right - task.getResult().get(0).getBoundingBox().left);
                croppedImage = Bitmap.createBitmap(image.getBitmap(), task.getResult().get(0).getBoundingBox().left, task.getResult().get(0).getBoundingBox().top, width, height);
                if(croppedImage!=null)
                {
                    Bitmap myImage=loadBitmap(WelcomeToApp.this,"saveinput");
//                    oldImage.setImageBitmap(myImage);
//                    newImage.setImageBitmap(croppedImage);
                    float[] old=recognizer.recognize(myImage);
                    float[] New=recognizer1.recognize(croppedImage);
                    float identical=0;
                    for(int i=0;i<New.length;i++)
                    {
                        identical += old[i]-New[i];
                    }
                    float similarity=identical;
                    if(similarity < 1 && similarity >-1)
                    {
//                        Intent in=new Intent(MainActivity.this,WelcomeToApp.class);
//                        startActivity(in);
                        Toast.makeText(WelcomeToApp.this, "Welcome", Toast.LENGTH_SHORT).show();

                    }else
                    {
                        Toast.makeText(WelcomeToApp.this, "You Are note Authorised.. ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

    public static Bitmap loadBitmap(Context context, String picName){
        Bitmap b = null;
        FileInputStream fis;
        try {
            fis = context.openFileInput(picName);
            b = BitmapFactory.decodeStream(fis);
            fis.close();

        }
        catch (FileNotFoundException e) {
            Log.d("Error Occured", "file not found");
            e.printStackTrace();
        }
        catch (IOException e) {
            Log.d("Error Occured", "io exception");
            e.printStackTrace();
        } finally {
//            fis.close();
        }
        return b;
    }
}
