package com.example.facerecognization.AI;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;



import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import android.graphics.Matrix;
import android.widget.Toast;

import com.google.android.gms.common.logging.Logger;

import org.tensorflow.lite.Interpreter;

public class Recognizer {

    private static final String LOG_TAG = Recognizer.class.getSimpleName();

    // Name of the model file (under assets folder)
//    private static final String MODEL_PATH = "my_facenet_19_12_2019.tflite";

    private static final String MODEL_PATH = "my_facenet_with_out_quantize.tflite";

    // TensorFlow Lite interpreter for running inference with the tflite model
    private final Interpreter interpreter;

    /* Input */
    // A ByteBuffer to hold image data for input to model
    private final ByteBuffer inputImage;

    private final int[] imagePixels = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE];

    // Input size
    private static final int DIM_BATCH_SIZE = 1;    // batch size
    public static final int DIM_IMG_SIZE_X = 160;   // height
    public static final int DIM_IMG_SIZE_Y = 160;   // width
    private static final int DIM_PIXEL_SIZE = 3;    // 1 for gray scale & 3 for color images

    /* Output*/
    // Output size is 10 (number of digits)
    private static final int EMBEDDINGS = 512;

    // Output array [batch_size, number of digits]
    // 10 floats, each corresponds to the probability of each digit
    private float[][] outputArray = new float[DIM_BATCH_SIZE][EMBEDDINGS];

    byte[][] outputArray1 = new byte[1][EMBEDDINGS];
    ;

    Activity context;

    public Recognizer(Activity activity) throws IOException {
        interpreter = new Interpreter(loadModelFile(activity));
        inputImage =
                ByteBuffer.allocateDirect(4
                        * DIM_BATCH_SIZE
                        * DIM_IMG_SIZE_X
                        * DIM_IMG_SIZE_Y
                        * DIM_PIXEL_SIZE);

//        inputImage =
//                ByteBuffer.allocate(1//number of bytes per
//                        *DIM_BATCH_SIZE
//                        * DIM_IMG_SIZE_X
//                        * DIM_IMG_SIZE_Y
//                        * DIM_PIXEL_SIZE);// Worked for me

//        inputImage =
//                ByteBuffer.allocateDirect(602112);
        inputImage.order(ByteOrder.nativeOrder());
        context = activity;
    }

    // Memory-map the model file in Assets
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * To classify an image, follow these steps:
     * 1. pre-process the input image
     * 2. run inference with the model
     * 3. post-process the output result for display in UI
     *
     * @param bitmap
     * @return the digit with the highest probability
     */
    public float[] recognize(Bitmap bitmap) {
        preprocess(bitmap);
//        runInference();
        interpreter.run(inputImage, outputArray);

        return postprocess();
    }

    /**
     * Preprocess the bitmap by converting it to ByteBuffer & grayscale
     *
     * @param bitmap
     */
    private void preprocess(Bitmap bitmap) {
        convertBitmapToByteBuffer(bitmap);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (inputImage == null) {
            return;
        }
        inputImage.rewind();
        Bitmap resized_bitmap = getResizedBitmap(bitmap, 160, 160);

        resized_bitmap.getPixels(imagePixels, 0, resized_bitmap.getWidth(), 0, 0,
                resized_bitmap.getWidth(), resized_bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
//                for (int k = 0; k < DIM_PIXEL_SIZE; k++) {
//                    try {
//                        final int val = imagePixels[pixel++];
//                        inputImage.putFloat(Normalize(val));
//                    } catch (Exception e) {
//                        Log.v("Checker", "New");
                final int val = imagePixels[pixel++];
                inputImage.putFloat((((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                inputImage.putFloat((((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                inputImage.putFloat(((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

            }

        }
        //        int pixel = 0;
//        long startTime = SystemClock.uptimeMillis();
//        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
//            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
//                final int val = imagePixels[pixel++];
//                addPixelValue(val);
//            }
//        }
//        long endTime = SystemClock.uptimeMillis();
//        Log.v("Timecost" ,"" +(endTime - startTime));
    }






    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    protected void addPixelValue(int pixelValue) {
        inputImage.put((byte) ((pixelValue >> 16) & 0xFF));
        inputImage.put((byte) ((pixelValue >> 8) & 0xFF));
        inputImage.put((byte) (pixelValue & 0xFF));
    }
//    private static final Logger LOGGER = new Logger();
    private float Normalize(int color) {

        float preprocessedValue = color/ 255.0f; // normalize the value by dividing by 255.0f
        return preprocessedValue;
    }
//    private float convertToGreyScale(int color) {
//        float r = ((color >> 16) & 0xFF);
//        float g = ((color >> 8) & 0xFF);
//        float b = ((color) & 0xFF);
//
//        int grayscaleValue = (int) (0.299f * r + 0.587f * g + 0.114f * b);
//        float preprocessedValue = grayscaleValue / 255.0f; // normalize the value by dividing by 255.0f
//        return preprocessedValue;
//    }

    /**
     * Run inference with the classifier model
     * Input is image
     * Output is an array of probabilities
     */
    private void runInference() {
//        interpreter.run(inputImage, outputArray);
        interpreter.run(inputImage, outputArray1);

    }

    /**
     * Figure out the prediction of digit by finding the index with the highest probability
     *
     * @return
     */
    float[][] outputFloatValues=new float[1][EMBEDDINGS];
    private float[] postprocess() {
        // Index with highest probability
//        outputFloatValues=new float[1][EMBEDDINGS];
//        for(int i=0;i<EMBEDDINGS;i++)
//        {
//            outputFloatValues[0][i]=(outputArray1[0][i] & 0xff) / 255.0f;
//        }

//        Log.d("output Values",outputArray.toString());
//        float maxProb = 0.0f;
//        for (int i = 0; i < outputArray[0].length; i++) {
//            if (outputArray[0][i] > maxProb) {
//                maxProb = outputArray[0][i];
//                maxIndex = i;
//            }
//        }

//        float identity=0;
//        for (float o:outputArray[0]
//             ) {
//            identity += o - o;
//        }

//        identity=identity/outputArray[0].length;
//        Toast.makeText(context, identity + "", Toast.LENGTH_SHORT).show();
        return outputArray[0];
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
//        bm.recycle();
        return resizedBitmap;
    }
}
