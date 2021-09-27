package com.anubhav.talkzone2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class OtherCredentialsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String currentUserID, emailAddress;
    private DatabaseReference mUsersReference;

    private ProgressDialog loadingBar;
    private MaterialButton submitButton;
    private TextInputEditText textInputEditTextFirstName, textInputEditTextLastName, textInputEditTextEmailAddress, textInputEditTextProfileStatus;
    private TextInputLayout textInputLayoutProfileStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_credentials);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        emailAddress = mAuth.getCurrentUser().getEmail();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        loadingBar = new ProgressDialog(OtherCredentialsActivity.this);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setEnabled(false);
        textInputEditTextFirstName = findViewById(R.id.editTextFirstName);
        textInputEditTextLastName = findViewById(R.id.editTextLastName);
        textInputEditTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        textInputEditTextEmailAddress.setText(emailAddress);
        textInputEditTextEmailAddress.setEnabled(false);
        textInputEditTextProfileStatus = findViewById(R.id.editTextProfileStatus);
        textInputLayoutProfileStatus = findViewById(R.id.textInputProfileStatus);

        textInputEditTextProfileStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 100) {
                    textInputLayoutProfileStatus.setErrorEnabled(true);
                    textInputLayoutProfileStatus.setError("There is a 100 character limit.");
                }

                else
                    textInputLayoutProfileStatus.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        submitButton.setOnClickListener(v -> {
            loadingBar.setTitle("Saving credentials...");
            loadingBar.setMessage("Please wait while we are processing your request.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            String firstName = textInputEditTextFirstName.getText().toString().trim();
            firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
            String lastName = textInputEditTextLastName.getText().toString().trim();
            lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();

            String fullName = firstName + " " + lastName;
            String profileStatus = textInputEditTextProfileStatus.getText().toString().trim();

            HashMap userMap = new HashMap();
                userMap.put("fullName", fullName);
                userMap.put("emailAddress", emailAddress);

                if (! profileStatus.equals(""))
                    userMap.put("profileStatus", profileStatus);
                else
                    userMap.put("profileStatus", "Available");

            mUsersReference.updateChildren(userMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendUserToSetupActivity();
                    Toast.makeText(OtherCredentialsActivity.this, "Your other credentials were saved successfully!", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }

                else {
                    String errorMessage = task.getException().toString();
                    Toast.makeText(OtherCredentialsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            });
        });

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (! isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        final String firstName = textInputEditTextFirstName.getText().toString().trim();
                        final String lastName = textInputEditTextLastName.getText().toString().trim();
                        final String profileStatus = textInputEditTextProfileStatus.getText().toString().trim();

                        runOnUiThread(() -> {
                            if (! firstName.isEmpty() && ! lastName.isEmpty() && profileStatus.length() <= 100) {
                                submitButton.setBackgroundTintList(ContextCompat.getColorStateList(OtherCredentialsActivity.this, R.color.colorTurquoise));
                                submitButton.setEnabled(true);
                            }

                            else {
                                submitButton.setBackgroundTintList(ContextCompat.getColorStateList(OtherCredentialsActivity.this, R.color.colorGray));
                                submitButton.setEnabled(false);
                            }

                        });
                    }

                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(OtherCredentialsActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}