package com.example.jg;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {
    private SpriteView spriteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spriteView = findViewById(R.id.spriteView);
        startScaleAnimation();

        // Находим SettingsFragment и связываем его со SpriteView
        Fragment settingsFragment = getSupportFragmentManager().findFragmentByTag("settings");
        if (settingsFragment instanceof SettingsFragment) {
            spriteView.setSettingsFragment((SettingsFragment) settingsFragment);
        }
    }

    private void startScaleAnimation() {
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(5.0f, 0.3f);
        scaleAnimator.setDuration(2000); // 2 секунды на анимацию
        scaleAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            if (spriteView != null) {
                spriteView.setInitialScale(scale);
            }
        });
        scaleAnimator.start();
    }
}