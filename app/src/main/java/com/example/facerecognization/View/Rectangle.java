package com.example.facerecognization.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class Rectangle extends View {
    Paint paint = new Paint();
    Rect rect;
    public Rectangle(Context context,Rect rect) {
        super(context);
    this.rect=rect;
        
    }


    @Override
    public void onDraw(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        canvas.drawRect(rect, paint );
    }
}