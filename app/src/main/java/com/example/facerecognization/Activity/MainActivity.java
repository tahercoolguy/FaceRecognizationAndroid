package com.example.facerecognization.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import com.example.facerecognization.AI.Recognizer;
import com.example.facerecognization.AI.Recognizer_Quantize;
import com.example.facerecognization.R;
import com.example.facerecognization.View.Rectangle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;


import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    FirebaseVisionFaceDetectorOptions highAccuracyOpts;
    LinearLayout linearLayout;
    Recognizer recognizer,recognizer1;
    FirebaseVisionFaceDetector detector;
//
//    Recognizer recognizer;

String Purpose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(getApplicationContext());

        Intent intent=getIntent();
        if(intent!=null)
            Purpose=intent.getStringExtra("class");
        textureView = findViewById(R.id.view_finder);

        linearLayout=findViewById(R.id.surface);

        if(allPermissionsGranted()){
            try {
                startCamera(); //start camera if permission has been granted by user
            } catch (CameraInfoUnavailableException e) {
                e.printStackTrace();
            }
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts);
        try {
            recognizer=new Recognizer(MainActivity.this);
            recognizer1=new Recognizer(MainActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    FirebaseVisionImage image ;

    @SuppressLint("RestrictedApi")
    private void startCamera() throws CameraInfoUnavailableException {

        CameraX.unbindAll();

        Rational aspectRatio = new Rational (textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen


        PreviewConfig pConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setLensFacing(CameraX.LensFacing.FRONT)
                .setTargetResolution(screen)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();

        final Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    //to update the surface texture we  have to destroy it first then re-add it
                    @Override
                    public void onUpdated(Preview.PreviewOutput output){
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });


        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setLensFacing(CameraX.LensFacing.FRONT)
//                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetAspectRatio(aspectRatio)
                .setTargetResolution(screen)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);

//        findViewById(R.id.imgCapture).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".png");
//                imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
//                    @Override
//                    public void onImageSaved(@NonNull File file) {
//                        String msg = "Pic captured at " + file.getAbsolutePath();
//                        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
//
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
//                        String msg = "Pic capture failed : " + message;
//                        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
//                        if(cause != null){
//                            cause.printStackTrace();
//                        }
//                    }
//                });
//            }
//        });

        ImageAnalysisConfig config =
                new ImageAnalysisConfig.Builder()
//                        .setTargetResolution(new Size(1280, 720))
//                        .setTargetResolution(new Size(256, 256))
                        .setLensFacing(CameraX.LensFacing.FRONT)
                        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                        .setTargetAspectRatio(aspectRatio)
                        .setTargetResolution(screen)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

        final ImageAnalysis imageAnalysis = new ImageAnalysis(config);
        imageAnalysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {
                    private AtomicBoolean isBusy = new AtomicBoolean(false);// this is for not choking input pipline

                    private AtomicBoolean StopNow=new AtomicBoolean(false);
                    @Override
                    public void analyze(ImageProxy imageProxy, int degrees) {
                        // insert your code here.
                        Log.v("checker","checker");
                        if (imageProxy != null && imageProxy.getImage() != null &&  isBusy.compareAndSet(false, true) &&  !StopNow.get()) {
                            Image mediaImage = imageProxy.getImage();
                            int rotation = degreesToFirebaseRotation(degrees);
                            image =FirebaseVisionImage.fromMediaImage(mediaImage, rotation);

//                            Task<List<FirebaseVisionFace>> result =
//                                    detector.detectInImage(image)
//                                            .addOnSuccessListener(
//                                                    new OnSuccessListener<List<FirebaseVisionFace>>() {
//                                                        @Override
//                                                        public void onSuccess(List<FirebaseVisionFace> faces) {
//                                                            // Task completed successfully
//                                                            Toast.makeText(MainActivity.this, "Successfull", Toast.LENGTH_SHORT).show();
//                                                            for (FirebaseVisionFace fc:faces
//                                                                 ) {
//                                                                linearLayout.addView(new Rectangle(getApplicationContext(),fc.getBoundingBox()));
//                                                            }
//
//                                                            isBusy.set(false);
//                                                            // ...
//                                                        }
//                                                    })
//                                            .addOnFailureListener(
//                                                    new OnFailureListener() {
//                                                        @Override
//                                                        public void onFailure(@NonNull Exception e) {
//                                                            // Task failed with an exception
//                                                            Toast.makeText(MainActivity.this, "failure", Toast.LENGTH_SHORT).show();
//                                                            isBusy.set(false);
//
//                                                            // ...
//                                                        }
//                                                    });

                            detector.detectInImage(image).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionFace>>() {
                                @Override
                                public void onComplete(@NonNull Task<List<FirebaseVisionFace>> task) {
                                    if (!StopNow.get()) {
                                        Toast.makeText(MainActivity.this, "Successfull", Toast.LENGTH_SHORT).show();
                                        linearLayout.removeAllViews();
                                        for (FirebaseVisionFace fc : task.getResult()
                                        ) {
                                            linearLayout.addView(new Rectangle(getApplicationContext(), fc.getBoundingBox()));
                                            int height = (fc.getBoundingBox().bottom - fc.getBoundingBox().top);
                                            int width = (fc.getBoundingBox().right - fc.getBoundingBox().left);
                                            Bitmap croppedImage = Bitmap.createBitmap(image.getBitmap(), fc.getBoundingBox().left, fc.getBoundingBox().top, width, height);
//                                            try {
//                                                //Write file
//                                                String filename = "bitmap.png";
//                                                FileOutputStream stream = MainActivity.this.openFileOutput(filename, Context.MODE_PRIVATE);
//                                                croppedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
//
//                                                stream.close();
//                                                croppedImage.recycle();
//                                                StopNow.set(true);
//                                                //Pop intent
//                                                Intent in1 = new Intent(MainActivity.this, DetectedFace.class);
//                                                in1.putExtra("image", filename);
//                                                startActivity(in1);
//                                                //Cleanup
//
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        Toast.makeText(MainActivity.this, "Taher is ", Toast.LENGTH_SHORT).show();

                                            if(Purpose.equalsIgnoreCase("facesignin"))
                                            {
                                                Bitmap oldBitmap=loadBitmap(MainActivity.this,"saveinput");
                                                float[] New=recognizer.recognize(croppedImage);
                                                float[] old=recognizer1.recognize(oldBitmap);

                                                float identical=0;
                                                for(int i=0;i<New.length;i++)
                                                {
                                                    identical += old[i]-New[i];
                                                }
                                                float similarity=identical/New.length;
                                                if(similarity < 1 && similarity > -1)
                                                {
                                                    Intent in=new Intent(MainActivity.this,WelcomeToApp.class);
                                                    startActivity(in);
                                                }else
                                                {
                                                    Toast.makeText(MainActivity.this, "You Are note Authorised.. ", Toast.LENGTH_SHORT).show();
                                                }
                                            }else if(Purpose.equalsIgnoreCase("facesignup"))
                                            {
                                                saveFile(MainActivity.this,croppedImage,"saveinput");
                                                MainActivity.this.finish();
                                            }

                                        }

//                                    isBusy.set(false);
                                        isBusy.set(false);
                                    }
                                }
                            });
                        }else
                        {
                            return;
                        }


                    }
                });



        //bind to lifecycle:
        CameraX.getCameraWithLensFacing(CameraX.LensFacing.FRONT);
        CameraX.bindToLifecycle((LifecycleOwner)this, preview, imgCap,imageAnalysis);
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    public static void saveFile(Context context, Bitmap b, String picName){
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        }
        catch (FileNotFoundException e) {
            Log.d("Error Occured", "file not found");
            e.printStackTrace();
        }
        catch (IOException e) {
            Log.d("Error Occured", "io exception");
            e.printStackTrace();
        } finally {
//            fos.close();
        }
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
    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)textureView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                try {
                    startCamera();
                } catch (CameraInfoUnavailableException e) {
                    e.printStackTrace();
                }
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }




}
