package com.example.facerecognization.View;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private static final int[] COLOR_CHOICES = {
            Color.BLUE //, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW
    };
    private static int currentColorIndex = 0;
    private final Paint facePositionPaint;
    private final Paint idPaint, centerPoint;
    private final Paint boxPaint, screenCenterPaint;
    private final Paint movePaint;
    GraphicOverlay graphicOverlay;
    private int facing;
    private volatile FirebaseVisionFace firebaseVisionFace;

    public FaceGraphic(GraphicOverlay overlay) {
        super(overlay);
        this.graphicOverlay = overlay;

        currentColorIndex = (currentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[currentColorIndex];

        screenCenterPaint = new Paint();
        screenCenterPaint.setColor(Color.GREEN);

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(Color.WHITE);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(Color.WHITE);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        centerPoint = new Paint();
        centerPoint.setStrokeWidth(5f);
        centerPoint.setColor(Color.RED);
        centerPoint.setStyle(Paint.Style.STROKE);

        movePaint = new Paint();
        movePaint.setColor(Color.RED);
        movePaint.setTextSize(38);
    }


    public void updateFace(FirebaseVisionFace face, int facing) {
        firebaseVisionFace = face;
        this.facing = facing;
        postInvalidate();
    }


    @Override
    public void draw(Canvas canvas) {

        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 10, screenCenterPaint);
        FirebaseVisionFace face = firebaseVisionFace;
        if (face == null) {
            return;
        }
        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);

        Log.d("myFaceBounds", String.valueOf(face.getBoundingBox()));
        float faceRightOrLeftAngle = face.getHeadEulerAngleY();
        float faceTiltAngle = face.getHeadEulerAngleZ();

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getBoundingBox().width() / 2.0f);
        float yOffset = scaleY(face.getBoundingBox().height() / 2.0f);
        float left = x - xOffset - 100;
        float top = y - yOffset - 100;
        float right = x + xOffset + 100;
        float bottom = y + yOffset + 100;

        canvas.drawRect(left, top, right, bottom, boxPaint);
    }
}