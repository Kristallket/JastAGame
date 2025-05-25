package com.example.jg;

import android.content.Context;
import android.view.MotionEvent;
import android.util.DisplayMetrics;

/**
 * Класс ViewTransform отвечает за трансформацию игрового вида.
 * Позволяет масштабировать и перемещать игровой мир.
 */
public class ViewTransform {
    private float scaleFactor = 1.0f;    // Масштаб отображения
    private float translateX = 0f;       // Смещение по X
    private float translateY = 0f;       // Смещение по Y
    private float focusX = 0f;           // Точка фокуса X
    private float focusY = 0f;           // Точка фокуса Y
    private float screenWidth;           // Ширина экрана
    private float screenHeight;          // Высота экрана
    private boolean isScaling = false;   // Флаг масштабирования
    private OnTransformListener listener;// Слушатель изменений трансформации

    /**
     * Интерфейс для отслеживания изменений трансформации
     */
    public interface OnTransformListener {
        void onTransformChanged();
    }

    /**
     * Конструктор
     * @param context Контекст приложения
     * @param listener Слушатель изменений трансформации
     */
    public ViewTransform(Context context, OnTransformListener listener) {
        this.listener = listener;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    /**
     * Устанавливает размеры экрана
     */
    public void setScreenSize(float width, float height) {
        screenWidth = width;
        screenHeight = height;
    }

    /**
     * Устанавливает масштаб отображения
     */
    public void setScaleFactor(float scale) {
        scaleFactor = scale;
        if (listener != null) {
            listener.onTransformChanged();
        }
    }

    /**
     * Возвращает текущий масштаб
     */
    public float getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Возвращает смещение по X
     */
    public float getTranslateX() {
        return translateX;
    }

    /**
     * Возвращает смещение по Y
     */
    public float getTranslateY() {
        return translateY;
    }

    /**
     * Возвращает точку фокуса X
     */
    public float getFocusX() {
        return focusX;
    }

    /**
     * Возвращает точку фокуса Y
     */
    public float getFocusY() {
        return focusY;
    }

    /**
     * Проверяет, происходит ли масштабирование
     */
    public boolean isScaling() {
        return isScaling;
    }

    /**
     * Центрирует вид на указанных координатах
     */
    public void centerOn(float x, float y) {
        translateX = x;
        translateY = y;
        if (listener != null) {
            listener.onTransformChanged();
        }
    }

    /**
     * Обрабатывает события касания для масштабирования и перемещения
     */
    public void onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 2) {
                    isScaling = true;
                    focusX = (event.getX(0) + event.getX(1)) / 2;
                    focusY = (event.getY(0) + event.getY(1)) / 2;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    float oldDist = getDistance(event, 0, 1);
                    float newDist = getDistance(event, 0, 1);
                    float scale = newDist / oldDist;
                    scaleFactor *= scale;
                    
                    // Ограничиваем масштаб
                    scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 2.0f));
                    
                    if (listener != null) {
                        listener.onTransformChanged();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() < 2) {
                    isScaling = false;
                }
                break;
        }
    }

    /**
     * Вычисляет расстояние между двумя точками касания
     */
    private float getDistance(MotionEvent event, int index1, int index2) {
        float dx = event.getX(index1) - event.getX(index2);
        float dy = event.getY(index1) - event.getY(index2);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
} 