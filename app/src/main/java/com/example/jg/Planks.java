package com.example.jg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Класс Planks представляет собой игровой объект "доски".
 * Доски могут находиться в двух состояниях:
 * 1. Полупрозрачные (не построенные) - для разметки
 * 2. Непрозрачные (построенные) - как препятствие
 */
public class Planks {
    private float x;          // X-координата доски
    private float y;          // Y-координата доски
    private Bitmap image;     // Изображение доски
    private int width;        // Ширина доски
    private int height;       // Высота доски
    private boolean isBuilt;  // Флаг построения доски
    private Paint paint;      // Кисть для отрисовки

    /**
     * Конструктор создает новую доску
     * @param x X-координата
     * @param y Y-координата
     * @param image Изображение доски
     */
    public Planks(float x, float y, Bitmap image) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.isBuilt = false;
        
        // Настраиваем кисть для отрисовки
        this.paint = new Paint();
        paint.setAlpha(128); // Полупрозрачность для непостроенной доски
    }

    /**
     * Отрисовывает доску на канвасе
     * @param canvas Канвас для отрисовки
     */
    public void draw(Canvas canvas) {
        if (isBuilt) {
            paint.setAlpha(255); // Полная непрозрачность для построенной доски
        }
        canvas.drawBitmap(image, x, y, paint);
    }

    /**
     * Проверяет столкновение с персонажем
     * @param charX X-координата персонажа
     * @param charY Y-координата персонажа
     * @param charWidth Ширина персонажа
     * @param charHeight Высота персонажа
     * @return true если произошло столкновение
     */
    public boolean isColliding(float charX, float charY, float charWidth, float charHeight) {
        return !(charX + charWidth < x ||
                charX > x + width ||
                charY + charHeight < y ||
                charY > y + height);
    }

    /**
     * Строит доску (делает её непрозрачной)
     */
    public void build() {
        isBuilt = true;
        paint.setAlpha(255);
    }

    /**
     * Возвращает X-координату доски
     */
    public float getX() {
        return x;
    }

    /**
     * Возвращает Y-координату доски
     */
    public float getY() {
        return y;
    }

    /**
     * Возвращает ширину доски
     */
    public int getWidth() {
        return width;
    }

    /**
     * Возвращает высоту доски
     */
    public int getHeight() {
        return height;
    }

    /**
     * Проверяет, построена ли доска
     */
    public boolean isBuilt() {
        return isBuilt;
    }
} 