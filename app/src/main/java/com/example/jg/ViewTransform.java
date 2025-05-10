package com.example.jg;

import android.content.Context;
import android.view.MotionEvent;

public class ViewTransform {
    private static final float FIXED_SCALE = 0.3f;
    private float translateX = 0f;
    private float translateY = 0f;
    private float lastTouchX = 0f;
    private float lastTouchY = 0f;
    private boolean isPanning = false;
    private OnTransformListener listener;

    public interface OnTransformListener {
        void onTransformChanged();
    }

    public ViewTransform(Context context, OnTransformListener listener) {
        this.listener = listener;
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
        return FIXED_SCALE;
    }

    public float getFocusX() {
        return 0f;
    }

    public float getFocusY() {
        return 0f;
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