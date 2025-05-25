package com.example.jg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Класс Box представляет собой игровой объект "коробка".
 * Коробки являются собираемыми предметами, которые игрок может собирать
 * для получения очков.
 */
public class Box {
    private float x;          // X-координата коробки
    private float y;          // Y-координата коробки
    private Bitmap image;     // Изображение коробки
    private int width;
    private int height;
    private boolean collected;// Флаг сбора коробки

    /**
     * Конструктор создает новую коробку
     * @param x X-координата
     * @param y Y-координата
     * @param image Изображение коробки
     */
    public Box(float x, float y, Bitmap image) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.collected = false;
    }

    /**
     * Отрисовывает коробку на канвасе
     * @param canvas Канвас для отрисовки
     */
    public void draw(Canvas canvas) {
        if (!collected) {
            canvas.drawBitmap(image, x, y, null);
        }
    }

    /**
     * Проверяет столкновение с персонажем
     * @param charX X-координата персонажа
     * @param charY Y-координата персонажа
     * @param charWidth Ширина персонажа
     * @param charHeight Высота персонажа
     * @return true если произошло столкновение
     */
    public boolean isColliding(float charX, float charY, int charWidth, int charHeight) {
        if (collected) return false;
        
        return !(charX + charWidth < x ||
                charX > x + width ||
                charY + charHeight < y ||
                charY > y + height);
    }

    /**
     * Отмечает коробку как собранную
     */
    public void collect() {
        collected = true;
    }

    /**
     * Проверяет, собрана ли коробка
     */
    public boolean isCollected() {
        return collected;
    }
} 