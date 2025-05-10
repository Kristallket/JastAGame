package com.example.jg;

import android.content.Context;
import android.view.MotionEvent;

public class ViewTransform {
    private float scaleFactor = 0.3f;
    private float translateX = 0f;
    private float translateY = 0f;
    private float lastTouchX = 0f;
    private float lastTouchY = 0f;
    private boolean isPanning = false;
    private OnTransformListener listener;
    private float screenWidth = 0f;
    private float screenHeight = 0f;

    public interface OnTransformListener {
        void onTransformChanged();
    }

    public ViewTransform(Context context, OnTransformListener listener) {
        this.listener = listener;
    }

    public void setScreenSize(float width, float height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public void setScaleFactor(float scale) {
        // Сохраняем центр экрана до изменения масштаба
        float oldScale = this.scaleFactor;
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;
        
        // Вычисляем позицию центра в мировых координатах до изменения масштаба
        float worldCenterX = (centerX - translateX) / oldScale;
        float worldCenterY = (centerY - translateY) / oldScale;
        
        // Устанавливаем новый масштаб
        this.scaleFactor = scale;
        
        // Вычисляем новую позицию камеры, чтобы сохранить центр
        translateX = centerX - (worldCenterX * scale);
        translateY = centerY - (worldCenterY * scale);
        
        listener.onTransformChanged();
    }

    public void onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isPanning = true;
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (isPanning) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;
                    
                    translateX += dx;
                    translateY += dy;
                    
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    
                    listener.onTransformChanged();
                }
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isPanning = false;
                break;
        }
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public float getFocusX() {
        return screenWidth / 2;
    }

    public float getFocusY() {
        return screenHeight / 2;
    }

    public float getTranslateX() {
        return translateX;
    }

    public float getTranslateY() {
        return translateY;
    }

    public boolean isPanning() {
        return isPanning;
    }

    public boolean isScaling() {
        return false;
    }

    public void reset() {
        translateX = 0f;
        translateY = 0f;
        listener.onTransformChanged();
    }

    public void centerOn(float x, float y) {
        translateX = x;
        translateY = y;
        listener.onTransformChanged();
    }
} 