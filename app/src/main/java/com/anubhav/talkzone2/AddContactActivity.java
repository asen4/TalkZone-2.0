package com.anubhav.talkzone2;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

public class AddContactActivity extends AppCompatActivity {

    private boolean existsInDatabase;
    private CountryCodePicker countryCodePicker;
    private ImageButton backButton;
    private TextInputEditText phoneNumber;
    private MaterialButton submitButton;
    private TextView phoneNumberAvailable;

    public static Bundle mBundle = new Bundle();

    private FirebaseAuth mAuth;

    private DatabaseReference mPhoneNumbersReference;
    private String combinedPhoneNumber, currentUserID, receiverUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        mPhoneNumbersReference = FirebaseDatabase.getInstance().getReference().child("Phone Numbers");

        backButton = findViewById(R.id.addContactBackButton);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        phoneNumber = findViewById(R.id.addContactPhoneNumber);
        phoneNumberAvailable = findViewById(R.id.phoneNumberAvailable);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setEnabled(false);

        backButton.setOnClickListener(v -> onBackPressed());

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        String formattedPhoneNumber = PhoneNumberUtils.formatNumber(phoneNumber.getText().toString());
                        combinedPhoneNumber = countryCodePicker.getSelectedCountryCode() + "-" + formattedPhoneNumber;
                        mPhoneNumbersReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (TextUtils.isEmpty(phoneNumber.getText())) {
                                    phoneNumberAvailable.setVisibility(View.GONE);

                                    submitButton.setEnabled(false);
                                    submitButton.setBackgroundTintList(ContextCompat.getColorStateList(AddContactActivity.this, R.color.colorGray));

                                    existsInDatabase = false;
                                }

                                else if (dataSnapshot.hasChild(combinedPhoneNumber)) {
                                    phoneNumberAvailable.setVisibility(View.VISIBLE);
                                    phoneNumberAvailable.setText("Available on " + getResources().getString(R.string.app_name));
                                    phoneNumberAvailable.setTextColor(getResources().getColor(R.color.colorGreen));
                                    phoneNumberAvailable.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);

                                    submitButton.setEnabled(true);
                                    submitButton.setBackgroundTintList(ContextCompat.getColorStateList(AddContactActivity.this, R.color.colorTurquoise));

                                    existsInDatabase = true;
                                }

                                else {
                                    phoneNumberAvailable.setVisibility(View.VISIBLE);
                                    phoneNumberAvailable.setText("Unavailable on " + getResources().getString(R.string.app_name));
                                    phoneNumberAvailable.setTextColor(getResources().getColor(R.color.colorRed));
                                    phoneNumberAvailable.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_clear_24, 0, 0, 0);

                                    submitButton.setEnabled(true);
                                    submitButton.setBackgroundTintList(ContextCompat.getColorStateList(AddContactActivity.this, R.color.colorTurquoise));

                                    existsInDatabase = false;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();

        submitButton.setOnClickListener(v -> {
            if (existsInDatabase) {
                mPhoneNumbersReference.child(combinedPhoneNumber).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        receiverUserID = dataSnapshot.child("userID").getValue().toString();

                        if (currentUserID.equals(receiverUserID))
                            Toast.makeText(AddContactActivity.this, "This is your phone number!", Toast.LENGTH_SHORT).show();

                        else {
                            Intent personProfileIntent = new Intent(AddContactActivity.this, PersonProfileActivity.class);
                            personProfileIntent.putExtra("receiverUserID", receiverUserID);
                            AddContactActivity.mBundle.putString("receiverUserID", receiverUserID);
                            startActivity(personProfileIntent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            else
                Toast.makeText(AddContactActivity.this, "This phone number does not exist in " + getResources().getString(R.string.app_name) + "!", Toast.LENGTH_SHORT).show();
        });
    }
}