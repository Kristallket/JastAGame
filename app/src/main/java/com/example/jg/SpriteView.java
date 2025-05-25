package com.example.jg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.animation.ValueAnimator;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Random;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.FrameLayout;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;

/**
 * SpriteView - основной класс для отображения и управления игровым процессом.
 * Отвечает за:
 * - Отрисовку персонажа и игрового мира
 * - Обработку столкновений
 * - Управление движением персонажа
 * - Взаимодействие с досками и коробками
 * - Отображение счета
 */
public class SpriteView extends View implements ViewTransform.OnTransformListener {
    // Изображения и размеры
    private Bitmap spriteSheet;      // Спрайт-лист персонажа
    private Bitmap boxImage;         // Изображение коробки
    private Bitmap planksImage;      // Изображение доски
    private int spriteWidth;         // Ширина спрайта персонажа
    private int spriteHeight;        // Высота спрайта персонажа
    private int currentFrame = 0;    // Текущий кадр анимации
    private int totalFrames = 9;     // Общее количество кадров (3x3 спрайт-лист)

    // Позиция и движение
    private float currentX = 0;      // Текущая X-координата персонажа
    private float currentY = 0;      // Текущая Y-координата персонажа
    private float targetX = 0;       // Целевая X-координата для движения
    private float targetY = 0;       // Целевая Y-координата для движения
    private boolean isMoving = false;// Флаг движения персонажа
    private boolean isFacingRight = true; // Направление персонажа

    // Анимации
    private ValueAnimator moveAnimator;   // Аниматор движения
    private ValueAnimator frameAnimator;  // Аниматор кадров
    private Matrix transformMatrix;       // Матрица для отражения спрайта

    // Игровые объекты
    private BackgroundRenderer backgroundRenderer; // Рендерер фона
    private ViewTransform viewTransform;          // Трансформация вида
    private ArrayList<Box> boxes;                 // Список коробок
    private ArrayList<Planks> planks;            // Список досок
    private int score = 0;                       // Счет игрока
    private Paint scorePaint;                    // Кисть для отрисовки счета
    private Random random;                       // Генератор случайных чисел

    // Механика досок
    private boolean isPlacingPlanks = false;     // Флаг размещения досок
    private MaterialButton planksButton;         // Кнопка размещения досок
    private TextView modeTextView;               // Текст режима
    private boolean isMarkingOnlyMode = false;   // Флаг режима только разметки

    // Константы движения
    private static final float CHARACTER_SPEED = 300f;    // Скорость персонажа
    private static final float DASH_SPEED = 600f;         // Скорость рывка (3x)
    private static final float MOVEMENT_THRESHOLD = 1f;   // Порог остановки движения
    private static final long DASH_COOLDOWN = 2000;       // Перезарядка рывка (2 сек)
    private static final float SWIPE_THRESHOLD = 100f;    // Порог для свайпа

    // Размеры мира
    private static final int DEFAULT_WORLD_SIZE = 9;      // Размер мира по умолчанию
    private float worldWidth;                             // Ширина игрового мира
    private float worldHeight;                            // Высота игрового мира
    private float screenWidth;                            // Ширина экрана
    private float screenHeight;                           // Высота экрана

    // Механика рывка
    private long lastDashTime = 0;                        // Время последнего рывка
    private boolean canDash = true;                       // Возможность рывка
    private int touchCount = 0;                           // Счетчик касаний
    private float lastTouchX = 0f;                        // Последняя X-координата касания
    private float lastTouchY = 0f;                        // Последняя Y-координата касания

    private SettingsFragment settingsFragment;            // Фрагмент настроек
    private float initialScale = 1.0f;                    // Начальный масштаб

    /**
     * Устанавливает фрагмент настроек для доступа к игровым настройкам
     */
    public void setSettingsFragment(SettingsFragment fragment) {
        this.settingsFragment = fragment;
    }

    /**
     * Устанавливает начальный масштаб отображения
     */
    public void setInitialScale(float scale) {
        this.initialScale = scale;
        if (viewTransform != null) {
            viewTransform.setScaleFactor(scale);
            invalidate();
        }
    }

    /**
     * Создает новую коробку в случайной позиции игрового мира
     */
    private void spawnNewBox() {
        float x = random.nextFloat() * (worldWidth - boxImage.getWidth());
        float y = random.nextFloat() * (worldHeight - boxImage.getHeight());
        boxes.add(new Box(x, y, boxImage));
    }

    /**
     * Проверяет столкновения с коробками и досками
     * - Собирает коробки при столкновении
     * - Обрабатывает столкновения с досками
     * - Строит доски в обычном режиме
     */
    private void checkBoxCollisions() {
        // Проверка столкновений с коробками
        ArrayList<Box> boxesCopy = new ArrayList<>(boxes);
        for (Box box : boxesCopy) {
            if (!box.isCollected() && box.isColliding(currentX, currentY, spriteWidth, spriteHeight)) {
                box.collect();
                int boxScore = SettingsFragment.getBoxScore(getContext());
                score += boxScore;
                SettingsFragment.updateTotalScore(getContext(), boxScore);
                spawnNewBox();
                invalidate();
            }
        }

        // Проверка столкновений с досками
        for (Planks plank : planks) {
            if (plank.isBuilt()) {
                // Проверяем столкновение с построенной доской (обычный хитбокс)
                boolean isColliding = !(currentX + spriteWidth < plank.getX() ||
                        currentX > plank.getX() + plank.getWidth() ||
                        currentY + spriteHeight < plank.getY() ||
                        currentY > plank.getY() + plank.getHeight());

                if (isColliding) {
                    // Останавливаем движение при столкновении
                    if (moveAnimator != null && moveAnimator.isRunning()) {
                        moveAnimator.cancel();
                    }
                    if (frameAnimator != null && frameAnimator.isRunning()) {
                        frameAnimator.cancel();
                    }
                    isMoving = false;
                    touchCount = 0;
                    currentFrame = 0;
                    
                    // Обновляем целевую позицию на текущую
                    targetX = currentX;
                    targetY = currentY;
                    
                    invalidate();
                    return; // Прерываем проверку других столкновений
                }
            } else if (!isMarkingOnlyMode) {
                // Проверяем столкновение для строительства (увеличенный хитбокс)
                float buildHitboxWidth = plank.getWidth() * 1.5f;
                float buildHitboxHeight = plank.getHeight() * 1.5f;
                float buildHitboxX = plank.getX() - (buildHitboxWidth - plank.getWidth()) / 2;
                float buildHitboxY = plank.getY() - (buildHitboxHeight - plank.getHeight()) / 2;
                
                boolean isBuildColliding = !(currentX + spriteWidth < buildHitboxX ||
                        currentX > buildHitboxX + buildHitboxWidth ||
                        currentY + spriteHeight < buildHitboxY ||
                        currentY > buildHitboxY + buildHitboxHeight);

                if (isBuildColliding) {
                    plank.build();
                    invalidate();
                }
            }
        }
    }

    /**
     * Настраивает анимации движения и кадров
     */
    private void setupAnimations() {
        // Аниматор движения
        moveAnimator = ValueAnimator.ofFloat(0f, 1f);
        moveAnimator.setInterpolator(new LinearInterpolator());
        moveAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            float newX = currentX + (targetX - currentX) * fraction;
            float newY = currentY + (targetY - currentY) * fraction;
            
            float dx = targetX - newX;
            float dy = targetY - newY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance <= MOVEMENT_THRESHOLD) {
                currentX = targetX;
                currentY = targetY;
                stopMovement();
            } else {
                // Сохраняем старые координаты
                float oldX = currentX;
                float oldY = currentY;
                
                // Пробуем обновить позицию
                currentX = newX;
                currentY = newY;
                
                // Проверяем столкновения
                checkBoxCollisions();
                
                // Если произошло столкновение с построенной доской, возвращаем старую позицию
                if (currentX == targetX && currentY == targetY) {
                    currentX = oldX;
                    currentY = oldY;
                }
            }
            invalidate();
        });

        // Аниматор кадров
        frameAnimator = ValueAnimator.ofInt(1, totalFrames - 1);
        frameAnimator.setDuration(500);
        frameAnimator.setRepeatCount(ValueAnimator.INFINITE);
        frameAnimator.addUpdateListener(animation -> {
            currentFrame = (int) animation.getAnimatedValue();
            invalidate();
        });
    }

    /**
     * Останавливает движение персонажа
     */
    private void stopMovement() {
        isMoving = false;
        touchCount = 0;
        currentFrame = 0;
        if (frameAnimator.isRunning()) {
            frameAnimator.cancel();
        }
        if (moveAnimator.isRunning()) {
            moveAnimator.cancel();
        }
        invalidate();
    }

    /**
     * Начинает движение персонажа к целевой точке
     */
    private void startMovement() {
        if (isMoving) {
            if (touchCount == 1) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDashTime >= DASH_COOLDOWN) {
                    canDash = true;
                }
                
                if (canDash) {
                    lastDashTime = currentTime;
                    canDash = false;

                    moveAnimator.cancel();
                    
                    float dx = targetX - currentX;
                    float dy = targetY - currentY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    long duration = (long) (distance / DASH_SPEED * 1000);
                    moveAnimator.setDuration(duration);

                    if (!frameAnimator.isRunning()) {
                        currentFrame = 1;
                        frameAnimator.start();
                    }
                    
                    moveAnimator.start();
                }
            }
            touchCount++;
            return;
        }
        
        isMoving = true;
        touchCount = 1;
        
        if (moveAnimator.isRunning()) {
            moveAnimator.cancel();
        }
        if (frameAnimator.isRunning()) {
            frameAnimator.cancel();
        }

        float dx = targetX - currentX;
        float dy = targetY - currentY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        long duration = (long) (distance / CHARACTER_SPEED * 1000);
        moveAnimator.setDuration(duration);
        
        currentFrame = 1;
        frameAnimator.start();
        moveAnimator.start();
    }

    /**
     * Настраивает кнопку размещения досок и текст режима
     */
    private void setupPlanksButton() {
        if (getContext() instanceof Activity && planksButton == null) {
            Activity activity = (Activity) getContext();
            
            // Создаем TextView для отображения режима
            modeTextView = new TextView(activity);
            modeTextView.setTextColor(getResources().getColor(android.R.color.white));
            modeTextView.setTextSize(16);
            modeTextView.setPadding(16, 0, 16, 8);
            updateModeText();
            
            // Создаем кнопку размещения досок
            planksButton = new MaterialButton(activity);
            planksButton.setIcon(getResources().getDrawable(R.drawable.planks));
            planksButton.setIconGravity(MaterialButton.ICON_GRAVITY_START);
            planksButton.setIconPadding(8);
            planksButton.setIconSize(48);
            planksButton.setIconTint(null);
            planksButton.setBackgroundTintList(getResources().getColorStateList(R.color.button_background));
            planksButton.setCornerRadius(8);
            planksButton.setPadding(16, 16, 16, 16);
            
            // Добавляем кнопку и текст в родительский контейнер
            ViewGroup parent = (ViewGroup) getParent();
            if (parent != null) {
                // Добавляем TextView
                FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                textParams.gravity = Gravity.BOTTOM | Gravity.START;
                textParams.setMargins(16, 0, 0, 240);
                parent.addView(modeTextView, textParams);
                
                // Добавляем кнопку
                FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                buttonParams.gravity = Gravity.BOTTOM | Gravity.START;
                buttonParams.setMargins(16, 0, 0, 96);
                parent.addView(planksButton, buttonParams);
                
                // Проверяем, куплены ли доски
                if (ShopFragment.isItemPurchased(getContext(), SettingsFragment.PLANKS_ID)) {
                    planksButton.setVisibility(View.VISIBLE);
                    modeTextView.setVisibility(View.VISIBLE);
                    planksButton.setOnClickListener(v -> {
                        isPlacingPlanks = !isPlacingPlanks;
                        planksButton.setBackgroundTintList(getResources().getColorStateList(
                            isPlacingPlanks ? R.color.button_background_pressed : R.color.button_background
                        ));
                    });
                } else {
                    planksButton.setVisibility(View.GONE);
                    modeTextView.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * Обновляет текст режима
     */
    private void updateModeText() {
        if (modeTextView != null) {
            modeTextView.setText(isMarkingOnlyMode ? "Режим: Только разметка" : "Режим: Обычный");
        }
    }

    /**
     * Обрабатывает касания экрана
     * - Обрабатывает жесты двумя пальцами для смены режима
     * - Обрабатывает размещение досок
     * - Обрабатывает движение персонажа
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            // Обработка жеста двумя пальцами
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (event.getPointerCount() == 2) {
                        lastTouchX = event.getX(0);
                        lastTouchY = event.getY(0);
                    }
                    break;
                    
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 2) {
                        float dx = event.getX(0) - lastTouchX;
                        float dy = event.getY(0) - lastTouchY;
                        
                        // Проверяем, что это горизонтальный свайп
                        if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > SWIPE_THRESHOLD) {
                            // Свайп слева направо
                            if (dx > 0) {
                                isMarkingOnlyMode = !isMarkingOnlyMode;
                                updateModeText();
                                lastTouchX = event.getX(0);
                                lastTouchY = event.getY(0);
                            }
                        }
                    }
                    break;
            }
            return true;
        }
        
        viewTransform.onTouchEvent(event);
        
        if (event.getAction() == MotionEvent.ACTION_DOWN && !viewTransform.isScaling()) {
            if (isMoving && touchCount >= 2) {
                return true;
            }

            float touchX = event.getX();
            float touchY = event.getY();
            float scale = viewTransform.getScaleFactor();
            float translateX = viewTransform.getTranslateX();
            float translateY = viewTransform.getTranslateY();
            float focusX = viewTransform.getFocusX();
            float focusY = viewTransform.getFocusY();

            float worldX = (touchX - translateX - focusX) / scale + focusX;
            float worldY = (touchY - translateY - focusY) / scale + focusY;

            if (isPlacingPlanks) {
                // Размещаем доски
                planks.add(new Planks(
                    worldX - planksImage.getWidth() / 2,
                    worldY - planksImage.getHeight() / 2,
                    planksImage
                ));
                isPlacingPlanks = false;
                planksButton.setBackgroundTintList(getResources().getColorStateList(R.color.button_background));
                invalidate();
                return true;
            }

            float charCenterX = currentX + spriteWidth / 2;
            float charCenterY = currentY + spriteHeight / 2;
            float dx = worldX - charCenterX;
            float dy = worldY - charCenterY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance > spriteWidth) {
                isFacingRight = worldX > charCenterX;

                float newTargetX = worldX - spriteWidth / 2;
                float newTargetY = worldY - spriteHeight / 2;

                // Проверяем границы мира
                newTargetX = Math.max(0, Math.min(newTargetX, worldWidth - spriteWidth));
                newTargetY = Math.max(0, Math.min(newTargetY, worldHeight - spriteHeight));

                // Проверяем столкновение с досками
                if (!willCollideWithPlank(newTargetX, newTargetY)) {
                    targetX = newTargetX;
                    targetY = newTargetY;

                    if (isMoving) {
                        stopMovement();
                    }
                    
                    startMovement();
                }
                return true;
            }
        }
        
        return super.onTouchEvent(event);
    }

    /**
     * Отрисовывает игровой мир
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(0xFF2D1B4D);

        canvas.save();

        float scale = viewTransform.getScaleFactor();
        float translateX = viewTransform.getTranslateX();
        float translateY = viewTransform.getTranslateY();
        float focusX = viewTransform.getFocusX();
        float focusY = viewTransform.getFocusY();
        
        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale, focusX, focusY);
        
        backgroundRenderer.draw(canvas, worldWidth, worldHeight);
        
        // Рисуем доски
        for (Planks plank : planks) {
            plank.draw(canvas);
        }
        
        // Рисуем коробки
        for (Box box : boxes) {
            box.draw(canvas);
        }
        
        // Проверяем выход за границы мира
        if (currentX < 0 || currentX > worldWidth - spriteWidth ||
            currentY < 0 || currentY > worldHeight - spriteHeight) {
            if (getContext() instanceof Activity) {
                ((Activity) getContext()).finish();
            }
            return;
        }

        // Рисуем персонажа
        int row = currentFrame / 3;
        int col = currentFrame % 3;
        Rect src = new Rect(
            col * spriteWidth,
            row * spriteHeight,
            (col + 1) * spriteWidth,
            (row + 1) * spriteHeight
        );

        Rect dst = new Rect(
            (int) currentX,
            (int) currentY,
            (int) currentX + spriteWidth,
            (int) currentY + spriteHeight
        );

        canvas.save();

        if (!isFacingRight) {
            transformMatrix.reset();
            transformMatrix.setScale(-1, 1, currentX + spriteWidth/2, currentY + spriteHeight/2);
            canvas.concat(transformMatrix);
        }

        canvas.drawBitmap(spriteSheet, src, dst, null);
        
        canvas.restore();
        canvas.restore();

        // Рисуем счет
        canvas.drawText("Score: " + score, 50, 50, scorePaint);
    }

    /**
     * Обрабатывает изменение размера view
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        SharedPreferences settings = getContext().getSharedPreferences("GameSettings", 0);
        int worldSize = settings.getInt("worldSize", DEFAULT_WORLD_SIZE);
        
        worldWidth = w * worldSize;
        worldHeight = h * worldSize;

        currentX = (worldWidth - spriteWidth) / 2;
        currentY = (worldHeight - spriteHeight) / 2;
        targetX = currentX;
        targetY = currentY;
        
        viewTransform.setScreenSize(w, h);
        viewTransform.setScaleFactor(initialScale);
        
        float scale = viewTransform.getScaleFactor();
        float centerX = (screenWidth / 2) - (currentX * scale);
        float centerY = (screenHeight / 2) - (currentY * scale);
        viewTransform.centerOn(centerX, centerY);

        generateInitialBoxes();
    }

    /**
     * Генерирует начальные коробки
     */
    private void generateInitialBoxes() {
        boxes.clear();
        SharedPreferences settings = getContext().getSharedPreferences("GameSettings", 0);
        int worldSize = settings.getInt("worldSize", DEFAULT_WORLD_SIZE);
        
        int initialBoxCount = worldSize * 2;
        
        for (int i = 0; i < initialBoxCount; i++) {
            float x = random.nextFloat() * (worldWidth - boxImage.getWidth());
            float y = random.nextFloat() * (worldHeight - boxImage.getHeight());
            boxes.add(new Box(x, y, boxImage));
        }
        invalidate();
    }

    /**
     * Очищает ресурсы при уничтожении view
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (moveAnimator != null) {
            moveAnimator.cancel();
        }
        if (frameAnimator != null) {
            frameAnimator.cancel();
        }
        if (spriteSheet != null) {
            spriteSheet.recycle();
        }
        if (boxImage != null) {
            boxImage.recycle();
        }
        if (planksImage != null) {
            planksImage.recycle();
        }
        if (backgroundRenderer != null) {
            backgroundRenderer.cleanup();
        }
        if (planksButton != null) {
            ((ViewGroup) planksButton.getParent()).removeView(planksButton);
        }
        if (modeTextView != null) {
            ((ViewGroup) modeTextView.getParent()).removeView(modeTextView);
        }
    }

    /**
     * Конструктор
     */
    public SpriteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Инициализация view
     */
    private void init() {
        // Загружаем изображения
        spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.sprites);
        boxImage = BitmapFactory.decodeResource(getResources(), R.drawable.box);
        planksImage = BitmapFactory.decodeResource(getResources(), R.drawable.planks);
        
        // Масштабируем изображения
        boxImage = Bitmap.createScaledBitmap(boxImage, 
            boxImage.getWidth() / 4, 
            boxImage.getHeight() / 4, 
            true);
        
        // Делаем доску квадратной и равной размеру персонажа
        spriteWidth = spriteSheet.getWidth() / 3;
        spriteHeight = spriteSheet.getHeight() / 3;
        planksImage = Bitmap.createScaledBitmap(planksImage,
            spriteWidth,
            spriteWidth, // Делаем квадратной
            true);
        
        // Инициализируем игровые объекты
        backgroundRenderer = new BackgroundRenderer(getContext());
        viewTransform = new ViewTransform(getContext(), this);
        transformMatrix = new Matrix();
        random = new Random();
        boxes = new ArrayList<>();
        planks = new ArrayList<>();
        
        // Настраиваем отображение счета
        scorePaint = new Paint();
        scorePaint.setColor(0xFFFFFFFF);
        scorePaint.setTextSize(75);
        scorePaint.setTypeface(Typeface.DEFAULT_BOLD);
        scorePaint.setAntiAlias(true);

        setupAnimations();
    }

    /**
     * Обработчик изменения трансформации
     */
    @Override
    public void onTransformChanged() {
        invalidate();
    }

    private boolean willCollideWithPlank(float newX, float newY) {
        for (Planks plank : planks) {
            if (plank.isBuilt()) {
                if (!(newX + spriteWidth < plank.getX() ||
                    newX > plank.getX() + plank.getWidth() ||
                    newY + spriteHeight < plank.getY() ||
                    newY > plank.getY() + plank.getHeight())) {
                    return true;
                }
            }
        }
        return false;
    }
} 