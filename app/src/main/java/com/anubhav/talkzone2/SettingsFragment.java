package com.anubhav.talkzone2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference mUsersReference;

    private FloatingActionButton editPersonProfileButton;
    private MaterialButton logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeVariables(view);

        return view;
    }

    private void initializeVariables(View view) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        
        editPersonProfileButton = view.findViewById(R.id.editPersonProfileButton);
        editPersonProfileButton.setOnClickListener(v -> sendUserToEditPersonProfileActivity());

        logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginFragment();
        });
    }

    private void sendUserToEditPersonProfileActivity() {
        Intent editPersonProfileIntent = new Intent(getActivity(), EditPersonProfileActivity.class);
        startActivity(editPersonProfileIntent);
    }

    private void sendUserToLoginFragment() {
        Intent loginIntent = new Intent(getActivity(), LRContainerActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    private void updateUserStatus(String status) {
        String saveCurrentDate, saveCurrentTime;

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm aa");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        Map currentStateMap = new HashMap();
            currentStateMap.put("time", saveCurrentTime);
            currentStateMap.put("date", saveCurrentDate);
            currentStateMap.put("type", status);

        mUsersReference.child(currentUserID).child("userState").updateChildren(currentStateMap);
    }
}