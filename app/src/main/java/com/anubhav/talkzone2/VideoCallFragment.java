package com.anubhav.talkzone2;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class VideoCallFragment extends Fragment {

    private TextInputEditText textInputEditTextSecretCode;
    private TextInputLayout textInputLayoutSecretCode;
    private FloatingActionButton castScreenButton;
    private MaterialButton joinButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_video_call, container, false);

        initializeVariables(view);

        return view;
    }

    private void initializeVariables(View view) {
        textInputEditTextSecretCode = view.findViewById(R.id.editTextSecretCode);
        textInputLayoutSecretCode = view.findViewById(R.id.textInputSecretCode);
        joinButton = view.findViewById(R.id.joinButton);
        joinButton.setEnabled(false);

        castScreenButton = view.findViewById(R.id.castScreenButton);
        castScreenButton.setOnClickListener(v -> startActivity(new Intent("android.settings.CAST_SETTINGS")));

        textInputEditTextSecretCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    textInputLayoutSecretCode.setErrorEnabled(false);
                    joinButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorGray));
                    joinButton.setEnabled(false);
                }

                else if (s.toString().matches(".*\\s+.*")) {
                    textInputLayoutSecretCode.setError("No spaces allowed.");
                    textInputLayoutSecretCode.setErrorEnabled(true);
                    joinButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorGray));
                    joinButton.setEnabled(false);
                }

                else if (s.length() > 10) {
                    textInputLayoutSecretCode.setError("The maximum character length is 10.");
                    textInputLayoutSecretCode.setErrorEnabled(true);
                    joinButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorGray));
                    joinButton.setEnabled(false);
                }

                else {
                    textInputLayoutSecretCode.setErrorEnabled(false);
                    joinButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorTurquoise));
                    joinButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        joinButton.setOnClickListener(v -> {
            JitsiMeetConferenceOptions conferenceOptions = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(textInputEditTextSecretCode.getText().toString().trim())
                    .setWelcomePageEnabled(false)
                    .build();

            JitsiMeetActivity.launch(getActivity(), conferenceOptions);
        });

        URL serverURL;

        try {
            serverURL = new URL("https://meet.jit.si/config.prejoinPageEnabled=false");

            JitsiMeetConferenceOptions defaultOptions =
                    new JitsiMeetConferenceOptions.Builder()
                            .setServerURL(serverURL)
                            .setWelcomePageEnabled(false)
                            .build();

            JitsiMeet.setDefaultConferenceOptions(defaultOptions);
        }

        catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}