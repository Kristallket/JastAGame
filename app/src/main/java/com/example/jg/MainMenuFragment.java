package com.example.jg;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class MainMenuFragment extends Fragment {
    private TextView totalScoreText;
    private TextView maxScoreText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        
        totalScoreText = view.findViewById(R.id.totalScoreText);
        maxScoreText = view.findViewById(R.id.maxScoreText);
        
        Button playButton = view.findViewById(R.id.playButton);
        Button settingsButton = view.findViewById(R.id.settingsButton);
        Button contactsButton = view.findViewById(R.id.contactsButton);
        
        updateCounters();
        
        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });
        
        settingsButton.setOnClickListener(v -> {
            ((MenuActivity) getActivity()).loadFragment(new SettingsFragment());
        });
        
        contactsButton.setOnClickListener(v -> {
            ((MenuActivity) getActivity()).loadFragment(new ContactsFragment());
        });
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCounters();
    }

    private void updateCounters() {
        if (getContext() != null) {
            int totalScore = SettingsFragment.getTotalScore(getContext());
            int maxScore = SettingsFragment.getMaxScore(getContext());
            
            totalScoreText.setText(getString(R.string.total_score, totalScore));
            maxScoreText.setText(getString(R.string.max_score, maxScore));
        }
    }
} 