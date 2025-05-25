package com.example.jg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.app.AlertDialog;
import android.content.Context;

public class SettingsFragment extends Fragment {
    private static final String PREFS_NAME = "GameSettings";
    private static final String WORLD_SIZE_KEY = "worldSize";
    private static final String DIFFICULTY_KEY = "difficulty";
    private static final String TOTAL_SCORE_KEY = "totalScore";
    private static final String MAX_SCORE_KEY = "maxScore";
    private static final String CURRENT_SESSION_SCORE_KEY = "currentSessionScore";
    private static final String PURCHASED_ITEMS_KEY = "purchasedItems";
    
    // ID предметов магазина
    public static final String BOX_UPGRADE_ID = "box_upgrade";
    public static final String GOLD_BOX_ID = "gold_box";
    public static final String DIAMOND_BOX_ID = "diamond_box";
    public static final String PLANKS_ID = "planks";
    
    private Button difficultyButton;
    private int currentDifficulty = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        SeekBar worldSizeSlider = view.findViewById(R.id.worldSizeSlider);
        TextView worldSizeText = view.findViewById(R.id.worldSizeText);
        Button backButton = view.findViewById(R.id.backButton);
        Button resetButton = view.findViewById(R.id.resetButton);

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        int savedWorldSize = settings.getInt(WORLD_SIZE_KEY, 6);
        worldSizeSlider.setProgress(savedWorldSize - 3); // Костыль до 0
        worldSizeText.setText("World Size: " + savedWorldSize);
        
        worldSizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int worldSize = progress + 3; // Convert back to 3-11 range
                worldSizeText.setText("World Size: " + worldSize);
                
                // Сохранить
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(WORLD_SIZE_KEY, worldSize);
                editor.apply();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        backButton.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });
        
        resetButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle("Сброс прогресса")
                .setMessage("Вы уверены, что хотите сбросить весь прогресс? Это действие нельзя отменить.")
                .setPositiveButton("Да", (dialog, which) -> {
                    resetProgress();
                })
                .setNegativeButton("Нет", null)
                .show();
        });
        
        // Загружаем сохраненную сложность
        currentDifficulty = settings.getInt(DIFFICULTY_KEY, 1);
        
        difficultyButton = view.findViewById(R.id.difficultyButton);
        updateDifficultyButtonText();
        
        difficultyButton.setOnClickListener(v -> {
            currentDifficulty = currentDifficulty % 3 + 1;
            updateDifficultyButtonText();
            
            // Сохраняем сложность
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(DIFFICULTY_KEY, currentDifficulty);
            editor.apply();
        });
        
        return view;
    }

    private void updateDifficultyButtonText() {
        difficultyButton.setText("Сложность: " + currentDifficulty);
    }

    public int getDifficulty() {
        return currentDifficulty;
    }

    public static void updateTotalScore(Context context, int newScore) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        int currentTotal = settings.getInt(TOTAL_SCORE_KEY, 0);
        int currentMax = settings.getInt(MAX_SCORE_KEY, 0);
        int currentSessionScore = settings.getInt(CURRENT_SESSION_SCORE_KEY, 0) + newScore;
        
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(TOTAL_SCORE_KEY, currentTotal + newScore);
        editor.putInt(CURRENT_SESSION_SCORE_KEY, currentSessionScore);
        
        // Обновляем максимум очков, если текущий счет за сессию больше
        if (currentSessionScore > currentMax) {
            editor.putInt(MAX_SCORE_KEY, currentSessionScore);
        }
        
        editor.apply();
    }

    public static void resetSessionScore(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putInt(CURRENT_SESSION_SCORE_KEY, 0).apply();
    }

    public static int getTotalScore(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt(TOTAL_SCORE_KEY, 0);
    }

    public static int getMaxScore(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt(MAX_SCORE_KEY, 0);
    }

    public static int getBoxScore(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        String purchasedItems = settings.getString(PURCHASED_ITEMS_KEY, "");
        
        if (purchasedItems.contains(DIAMOND_BOX_ID)) {
            return 25;
        } else if (purchasedItems.contains(GOLD_BOX_ID)) {
            return 20;
        } else if (purchasedItems.contains(BOX_UPGRADE_ID)) {
            return 15;
        } else {
            return 10;
        }
    }

    private void resetProgress() {
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(TOTAL_SCORE_KEY, 0);
        editor.putInt(MAX_SCORE_KEY, 0);
        editor.putInt(CURRENT_SESSION_SCORE_KEY, 0);
        editor.putString(PURCHASED_ITEMS_KEY, ""); // Сбрасываем список купленных предметов
        editor.apply();
        
        updateTotalScore(getContext(), 0);
    }
} 