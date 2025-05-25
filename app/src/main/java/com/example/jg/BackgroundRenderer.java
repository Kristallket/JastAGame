package com.example.jg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Класс BackgroundRenderer отвечает за отрисовку фона игрового мира.
 * Создает сетку с градиентной заливкой для визуального разделения пространства.
 */
public class BackgroundRenderer {
    private Bitmap backgroundImage;
    private final Context context;
    private Paint backgroundPaint;
    private Paint gridPaint;      // Кисть для отрисовки сетки
    private Paint gradientPaint;  // Кисть для градиентной заливки
    private int gridSize = 100;   // Размер ячейки сетки
    private int gridColor;        // Цвет сетки
    private int backgroundColor;  // Цвет фона

    /**
     * Конструктор
     * @param context Контекст приложения
     */
    public BackgroundRenderer(Context context) {
        this.context = context;
        loadBackground();
        // Инициализация кистей
        gridPaint = new Paint();
        gradientPaint = new Paint();
        
        // Настройка цветов
        gridColor = context.getResources().getColor(R.color.grid_color);
        backgroundColor = context.getResources().getColor(R.color.background_color);
        
        // Настройка кисти для сетки
        gridPaint.setColor(gridColor);
        gridPaint.setStrokeWidth(1);
        
        // Настройка кисти для градиента
        gradientPaint.setStyle(Paint.Style.FILL);
        gradientPaint.setColor(backgroundColor);
    }

    private void loadBackground() {
        backgroundImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.fon);
    }

    /**
     * Отрисовывает фон на канвасе
     * @param canvas Канвас для отрисовки
     * @param worldWidth Ширина игрового мира
     * @param worldHeight Высота игрового мира
     */
    public void draw(Canvas canvas, float worldWidth, float worldHeight) {
        // Отрисовка фона
        canvas.drawRect(0, 0, worldWidth, worldHeight, gradientPaint);
        
        // Отрисовка вертикальных линий сетки
        for (int x = 0; x <= worldWidth; x += gridSize) {
            canvas.drawLine(x, 0, x, worldHeight, gridPaint);
        }
        
        // Отрисовка горизонтальных линий сетки
        for (int y = 0; y <= worldHeight; y += gridSize) {
            canvas.drawLine(0, y, worldWidth, y, gridPaint);
        }
    }

    /**
     * Очищает ресурсы
     */
    public void cleanup() {
        if (backgroundImage != null) {
            backgroundImage.recycle();
            backgroundImage = null;
        }
        gridPaint = null;
        gradientPaint = null;
    }
} 