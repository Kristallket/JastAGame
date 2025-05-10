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

public class SpriteView extends View implements ViewTransform.OnTransformListener {
    private Bitmap spriteSheet;
    private Bitmap boxImage;
    private int spriteWidth;
    private int spriteHeight;
    private int currentFrame = 0;
    private int totalFrames = 9; // 3x3 sprite sheet
    private float currentX = 0;
    private float currentY = 0;
    private float targetX = 0;
    private float targetY = 0;
    private boolean isMoving = false;
    private boolean isFacingRight = true;
    private ValueAnimator moveAnimator;
    private ValueAnimator frameAnimator;
    private Matrix transformMatrix;
    private BackgroundRenderer backgroundRenderer;
    private ViewTransform viewTransform;
    private ArrayList<Box> boxes;
    private int score = 0;
    private Paint scorePaint;
    private Random random;
    
    // Скорость персонажа в пикселях
    private static final float CHARACTER_SPEED = 300f;
    private static final float DASH_SPEED = 600f; // 3x
    private static final float MOVEMENT_THRESHOLD = 1f;
    private static final long DASH_COOLDOWN = 2000; // 2 s

    private static final int DEFAULT_WORLD_SIZE = 9;
    private float worldWidth;
    private float worldHeight;
    private float screenWidth;
    private float screenHeight;
    
    // Перезарядка деша
    private long lastDashTime = 0;
    private boolean canDash = true;
    private int touchCount = 0;

    private SettingsFragment settingsFragment;

    public void setSettingsFragment(SettingsFragment fragment) {
        this.settingsFragment = fragment;
    }

    private void spawnNewBox() {
        float x = random.nextFloat() * (worldWidth - boxImage.getWidth());
        float y = random.nextFloat() * (worldHeight - boxImage.getHeight());
        boxes.add(new Box(x, y, boxImage));
    }

    private void checkBoxCollisions() {
        ArrayList<Box> boxesCopy = new ArrayList<>(boxes);
        for (Box box : boxesCopy) {
            if (!box.isCollected() && box.isColliding(currentX, currentY, spriteWidth, spriteHeight)) {
                box.collect();
                score += 10;
                spawnNewBox();
                invalidate();
            }
        }
    }

    private void setupAnimations() {
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
                currentX = newX;
                currentY = newY;
                checkBoxCollisions();
            }
            invalidate();
        });

        frameAnimator = ValueAnimator.ofInt(1, totalFrames - 1);
        frameAnimator.setDuration(500);
        frameAnimator.setRepeatCount(ValueAnimator.INFINITE);
        frameAnimator.addUpdateListener(animation -> {
            currentFrame = (int) animation.getAnimatedValue();
            invalidate();
        });
    }

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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

            float charCenterX = currentX + spriteWidth / 2;
            float charCenterY = currentY + spriteHeight / 2;
            float dx = worldX - charCenterX;
            float dy = worldY - charCenterY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance > spriteWidth) {
                isFacingRight = worldX > charCenterX;

                targetX = worldX - spriteWidth / 2;
                targetY = worldY - spriteHeight / 2;

                targetX = Math.max(0, Math.min(targetX, worldWidth - spriteWidth));
                targetY = Math.max(0, Math.min(targetY, worldHeight - spriteHeight));

                if (isMoving) {
                    stopMovement();
                }
                
                startMovement();
                return true;
            }
        }
        
        return super.onTouchEvent(event);
    }

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
        
        for (Box box : boxes) {
            box.draw(canvas);
        }
        
        if (currentX < 0 || currentX > worldWidth - spriteWidth ||
            currentY < 0 || currentY > worldHeight - spriteHeight) {
            if (getContext() instanceof Activity) {
                ((Activity) getContext()).finish();
            }
            return;
        }

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

        canvas.drawText("Score: " + score, 50, 50, scorePaint);
    }

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
        
        float scale = viewTransform.getScaleFactor();
        float centerX = (screenWidth / 2) - (currentX * scale);
        float centerY = (screenHeight / 2) - (currentY * scale);
        viewTransform.centerOn(centerX, centerY);

        generateInitialBoxes();
    }

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
        if (backgroundRenderer != null) {
            backgroundRenderer.cleanup();
        }
    }

    public SpriteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.sprites);
        boxImage = BitmapFactory.decodeResource(getResources(), R.drawable.box);
        boxImage = Bitmap.createScaledBitmap(boxImage, 
            boxImage.getWidth() / 4, 
            boxImage.getHeight() / 4, 
            true);
        spriteWidth = spriteSheet.getWidth() / 3;
        spriteHeight = spriteSheet.getHeight() / 3;
        
        backgroundRenderer = new BackgroundRenderer(getContext());
        viewTransform = new ViewTransform(getContext(), this);
        transformMatrix = new Matrix();
        random = new Random();
        boxes = new ArrayList<>();
        
        scorePaint = new Paint();
        scorePaint.setColor(0xFFFFFFFF);
        scorePaint.setTextSize(75);
        scorePaint.setTypeface(Typeface.DEFAULT_BOLD);
        scorePaint.setAntiAlias(true);

        setupAnimations();
    }

    @Override
    public void onTransformChanged() {
        invalidate();
    }
} 