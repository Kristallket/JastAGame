package com.example.jg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Box {
    private float x;
    private float y;
    private Bitmap image;
    private int width;
    private int height;
    private boolean isCollected = false;

    public Box(float x, float y, Bitmap image) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public void draw(Canvas canvas) {
        if (!isCollected) {
            canvas.drawBitmap(image, x, y, null);
        }
    }

    public boolean isColliding(float charX, float charY, int charWidth, int charHeight) {
        if (isCollected) return false;
        
        return !(charX + charWidth < x ||
                charX > x + width ||
                charY + charHeight < y ||
                charY > y + height);
    }

    public void collect() {
        isCollected = true;
    }

    public boolean isCollected() {
        return isCollected;
    }
} 