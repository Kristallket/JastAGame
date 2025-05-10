package com.example.jg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;


// он пуст, пока что
public class ContactsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        
        Button backButton = view.findViewById(R.id.backButton);
        
        backButton.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });
        
        return view;
    }
} 