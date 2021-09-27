package com.anubhav.talkzone2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPersonProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference;
    private StorageReference mUserProfileImageReference;
    private String currentUserID, downloadUrl;

    private CircleImageView profileImage;
    private CountryCodePicker countryNamePicker;
    private ImageButton backButton;
    private MaterialButton saveButton;
    private ProgressBar profileImageLoadingBar;
    private ProgressDialog loadingBar;
    private TextInputLayout profileStatusTIL;
    private TextInputEditText firstName, lastName, emailAddress, phoneNumber, profileStatus;

    private final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_person_profile);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        currentUserID = mAuth.getCurrentUser().getUid();

        profileImage = findViewById(R.id.editPersonProfileImage);
        profileImage.setOnClickListener(v -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_PICK);
        });

        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);
        emailAddress = findViewById(R.id.editTextEmailAddress);
        phoneNumber = findViewById(R.id.editTextPhoneNumber);
        profileStatus = findViewById(R.id.editPersonProfileStatus);
        countryNamePicker = findViewById(R.id.countryNamePicker);

        profileStatusTIL = findViewById(R.id.editPersonProfileStatusTIL);

        loadingBar = new ProgressDialog(EditPersonProfileActivity.this);
        profileImageLoadingBar = findViewById(R.id.editPersonProfileLoadingBar);

        profileStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s))
                    profileStatusTIL.setErrorEnabled(false);

                else if (s.length() > 100) {
                    profileStatusTIL.setErrorEnabled(true);
                    profileStatusTIL.setError("There is a maximum 100-character limit.");
                }

                else
                    profileStatusTIL.setErrorEnabled(false);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileI = dataSnapshot.child("profileImage").getValue().toString();
                    downloadUrl = profileI;

                    String fullName = dataSnapshot.child("fullName").getValue().toString();
                    int spaceIndex = fullName.indexOf(" ");
                    String firstN = fullName.substring(0, spaceIndex);
                    String lastN = fullName.substring(spaceIndex + 1);
                    String emailA = dataSnapshot.child("emailAddress").getValue().toString();
                    String phoneN = dataSnapshot.child("phoneNumber").getValue().toString();
                    String country = dataSnapshot.child("countryNameCode").getValue().toString();
                    String profileS = dataSnapshot.child("profileStatus").getValue().toString();

                    firstName.setText(firstN);
                    lastName.setText(lastN);
                    emailAddress.setText(emailA);
                    phoneNumber.setText(phoneN);
                    countryNamePicker.setCountryForNameCode(country);
                    profileStatus.setText(profileS);

                    if (!profileI.equals("-1"))
                        Picasso.get().load(profileI).placeholder(R.drawable.ic_baseline_person_black_125).into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        saveButton = findViewById(R.id.cirSaveButton);
        saveButton.setOnClickListener(view -> saveInformation());

        backButton = findViewById(R.id.editPersonProfileBackBtn);
        backButton.setOnClickListener(view -> onBackPressed());

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (! isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        final String mETFirstName = firstName.getText().toString().trim();
                        final String mETLastName = lastName.getText().toString().trim();
                        final String mETProfileStatus = profileStatus.getText().toString().trim();

                        EditPersonProfileActivity.this.runOnUiThread(() -> {
                            if (! mETFirstName.isEmpty() && ! mETLastName.isEmpty() && mETProfileStatus.length() <= 100) {
                                saveButton.setBackgroundTintList(EditPersonProfileActivity.this.getColorStateList(R.color.colorTurquoise));
                                saveButton.setEnabled(true);
                            }

                            else {
                                saveButton.setBackgroundTintList(EditPersonProfileActivity.this.getColorStateList(R.color.colorGray));
                                saveButton.setEnabled(false);
                            }
                        });
                    }

                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }

    private void saveInformation() {
        loadingBar.setTitle("Saving...");
        loadingBar.setMessage("Please wait while we are processing your request.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        String firstNameET = firstName.getText().toString().trim();
        String revisedFirstNameET  = firstNameET.substring(0, 1).toUpperCase() + firstNameET.substring(1).toLowerCase();
        String lastNameET = lastName.getText().toString().trim();
        String revisedLastNameET = lastNameET.substring(0, 1).toUpperCase() + lastNameET.substring(1).toLowerCase();
        String country = countryNamePicker.getSelectedCountryName();
        String countryNameCode = countryNamePicker.getSelectedCountryNameCode();
        String profileStatusET = profileStatus.getText().toString().trim();

        HashMap userMap = new HashMap();
        userMap.put("fullName", revisedFirstNameET + " " + revisedLastNameET);
        userMap.put("profileImage", downloadUrl);
        userMap.put("profileStatus", profileStatusET);
        userMap.put("countryName", country);
        userMap.put("countryNameCode", countryNameCode);

        mUsersReference.child(currentUserID).updateChildren(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendUserToMainActivity();
                Toast.makeText(EditPersonProfileActivity.this, "Your changes were saved successfully!", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }

            else {
                String errorMessage = task.getException().toString();
                Toast.makeText(EditPersonProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(EditPersonProfileActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Setting...");
                loadingBar.setMessage("Please wait while we are processing your request.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                profileImageLoadingBar.setVisibility(View.VISIBLE);

                Uri resultUri = result.getUri();
                StorageReference filePath = mUserProfileImageReference.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        task.addOnSuccessListener(taskSnapshot -> {
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result1 = taskSnapshot.getStorage().getDownloadUrl();
                                    result1.addOnSuccessListener(uri -> {
                                        downloadUrl = uri.toString();

                                        Toast.makeText(EditPersonProfileActivity.this, "Your profile image was cropped successfully!", Toast.LENGTH_LONG).show();
                                        Picasso.get().load(downloadUrl).into(profileImage, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                profileImageLoadingBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onError(Exception e) {

                                            }
                                        });

                                        loadingBar.dismiss();
                                    });
                                }
                            }
                        });
                    }

                    else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(EditPersonProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                });
            }

            else {
                Toast.makeText(EditPersonProfileActivity.this, "Your image cannot be cropped! Please try again!", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(EditPersonProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}