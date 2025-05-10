package com.example.jg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class BackgroundRenderer {
    private Bitmap backgroundImage;
    private final Context context;

    public BackgroundRenderer(Context context) {
        this.context = context;
        loadBackground();
    }

    private void loadBackground() {
        backgroundImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.fon);
    }

    public void draw(Canvas canvas, float worldWidth, float worldHeight) {
        if (backgroundImage != null) {
            int tileWidth = backgroundImage.getWidth();
            int tileHeight = backgroundImage.getHeight();
            
            // считаем кол-во полей по парам. х и у
            int tilesX = (int) Math.ceil(worldWidth / tileWidth);
            int tilesY = (int) Math.ceil(worldHeight / tileHeight);
            
            // рисуем
            for (int x = 0; x < tilesX; x++) {
                for (int y = 0; y < tilesY; y++) {
                    Rect src = new Rect(0, 0, tileWidth, tileHeight);
                    Rect dst = new Rect(
                        x * tileWidth,
                        y * tileHeight,
                        (x + 1) * tileWidth,
                        (y + 1) * tileHeight
                    );
                    canvas.drawBitmap(backgroundImage, src, dst, null);
                }
            }
        }
    }

    public void cleanup() {
        if (backgroundImage != null) {
            backgroundImage.recycle();
            backgroundImage = null;
        }
    }
} 