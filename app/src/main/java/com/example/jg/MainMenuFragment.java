package com.example.jg;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class MainMenuFragment extends Fragment {
    private TextView totalScoreText;
    private TextView maxScoreText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        
        totalScoreText = view.findViewById(R.id.totalScoreText);
        maxScoreText = view.findViewById(R.id.maxScoreText);
        
        MaterialButton playButton = view.findViewById(R.id.playButton);
        MaterialButton settingsButton = view.findViewById(R.id.settingsButton);
        MaterialButton contactsButton = view.findViewById(R.id.contactsButton);
        MaterialButton shopButton = view.findViewById(R.id.shopButton);
        
        updateCounters();
        
        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });
        
        settingsButton.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new SettingsFragment())
                .addToBackStack(null)
                .commit();
        });
        
        contactsButton.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new ContactsFragment())
                .addToBackStack(null)
                .commit();
        });
        
        shopButton.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new ShopFragment())
                .addToBackStack(null)
                .commit();
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