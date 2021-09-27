package com.anubhav.talkzone2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;

import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;

public class BasicCredentialsActivity extends AppCompatActivity {

    private CountryCodePicker countryNamePicker;
    private ProgressDialog loadingBar;
    private TextInputEditText textInputEditTextPhoneNumber;
    private MaterialButton submitButton;
    private String currentUserID, phoneNumber;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference, mPhoneNumbersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_credentials);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        mPhoneNumbersReference = FirebaseDatabase.getInstance().getReference().child("Phone Numbers");

        countryNamePicker = findViewById(R.id.countryNamePicker);
        textInputEditTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        submitButton = findViewById(R.id.basicCredentialsSubmitButton);

        loadingBar = new ProgressDialog(BasicCredentialsActivity.this);

        submitButton.setOnClickListener(v -> {
            loadingBar.setTitle("Saving credentials...");
            loadingBar.setMessage("Please wait while we are processing your request.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            HashMap userMap = new HashMap();
                userMap.put("phoneNumber", phoneNumber);
                userMap.put("countryName", countryNamePicker.getSelectedCountryName());
                userMap.put("countryNameCode", countryNamePicker.getSelectedCountryNameCode());

            mUsersReference.updateChildren(userMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mPhoneNumbersReference.child(phoneNumber).child("userID").setValue(currentUserID).addOnCompleteListener(task1 -> {
                        sendUserToSetupActivity();
                        Toast.makeText(BasicCredentialsActivity.this, "Your credentials were saved successfully!", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    });
                }
            });
        });

        if (ActivityCompat.checkSelfPermission(this, READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            requestPermission();
        else {
            TelephonyManager telephonyManager = (TelephonyManager) BasicCredentialsActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
            phoneNumber = telephonyManager.getLine1Number();
            phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);

            textInputEditTextPhoneNumber.setText(phoneNumber);
            return;
        }
    }

    private void requestPermission() {
        requestPermissions(new String[]{READ_SMS, READ_PHONE_NUMBERS, READ_PHONE_STATE}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 100:
                TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

                if (ActivityCompat.checkSelfPermission(this, READ_SMS) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                phoneNumber = telephonyManager.getLine1Number();
                phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);

                textInputEditTextPhoneNumber.setText(phoneNumber);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(BasicCredentialsActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}