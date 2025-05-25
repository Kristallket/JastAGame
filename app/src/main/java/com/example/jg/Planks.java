package com.example.jg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Planks {
    private float x;
    private float y;
    private Bitmap image;
    private int width;
    private int height;
    private boolean isBuilt;
    private Paint paint;

    public Planks(float x, float y, Bitmap image) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.isBuilt = false;
        
        paint = new Paint();
        paint.setAlpha(128); // Полупрозрачность для разметки
    }

    public void draw(Canvas canvas) {
        if (!isBuilt) {
            canvas.drawBitmap(image, x, y, paint);
        } else {
            canvas.drawBitmap(image, x, y, null);
        }
    }

    public boolean isColliding(float charX, float charY, int charWidth, int charHeight) {
        // Проверяем столкновение в любом случае
        boolean collision = !(charX + charWidth < x ||
                charX > x + width ||
                charY + charHeight < y ||
                charY > y + height);
        
        // Если есть столкновение и доска не построена, строим её
        if (collision && !isBuilt) {
            build();
        }
        
        // Возвращаем true только для построенных досок
        return collision && isBuilt;
    }

    public void build() {
        isBuilt = true;
    }

    public boolean isBuilt() {
        return isBuilt;
    }
} 