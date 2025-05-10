package com.example.jg;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class MainMenuFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        
        Button playButton = view.findViewById(R.id.playButton);
        Button settingsButton = view.findViewById(R.id.settingsButton);
        Button contactsButton = view.findViewById(R.id.contactsButton);
        
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
} 