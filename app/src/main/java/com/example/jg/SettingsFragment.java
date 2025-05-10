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

public class SettingsFragment extends Fragment {
    private static final String PREFS_NAME = "GameSettings";
    private static final String WORLD_SIZE_KEY = "worldSize";
    private static final String DIFFICULTY_KEY = "difficulty";
    
    private Button difficultyButton;
    private int currentDifficulty = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        SeekBar worldSizeSlider = view.findViewById(R.id.worldSizeSlider);
        TextView worldSizeText = view.findViewById(R.id.worldSizeText);
        Button backButton = view.findViewById(R.id.backButton);

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
} 