package com.anubhav.talkzone2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PersonProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference, mContactsReference;

    private ImageButton backButton, submitButton;
    private ImageView profileImage;
    private TextView fullName, phoneNumber, emailAddress, profileStatus, countryOfResidence;
    private String currentUserID, senderFullName, receiverFullName;
    public String receiverUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        receiverUserID = getIntent().getStringExtra("receiverUserID");
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mContactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                senderFullName = dataSnapshot.child("fullName").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                receiverFullName = dataSnapshot.child("fullName").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        backButton = findViewById(R.id.personProfileBackButton);
        submitButton = findViewById(R.id.personProfileSubmitButton);
        profileImage = findViewById(R.id.personProfileImage);
        fullName = findViewById(R.id.personProfileFullName);
        phoneNumber = findViewById(R.id.personProfilePhoneNumber);
        emailAddress = findViewById(R.id.personProfileEmailAddress);
        profileStatus = findViewById(R.id.personProfileStatus);
        countryOfResidence = findViewById(R.id.personCountryOfResidence);

        backButton.setOnClickListener(v -> onBackPressed());

        submitButton.setOnClickListener(v -> mContactsReference.child(currentUserID).child(receiverUserID).child("fullName").setValue(receiverFullName)
                .addOnCompleteListener(PersonProfileActivity.this, task -> mContactsReference.child(receiverUserID).child(currentUserID).child("fullName").setValue(senderFullName)
                        .addOnCompleteListener(task1 -> {
                            Toast.makeText(PersonProfileActivity.this, receiverFullName + " was successfully added to your list of contacts!", Toast.LENGTH_LONG).show();
                            sendUserToHomeFragment();
                        })));

        mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String image = dataSnapshot.child("profileImage").getValue().toString();
                    String name = dataSnapshot.child("fullName").getValue().toString();
                    String phone = dataSnapshot.child("phoneNumber").getValue().toString();
                    String email = dataSnapshot.child("emailAddress").getValue().toString();
                    String status = dataSnapshot.child("profileStatus").getValue().toString();
                    String country = dataSnapshot.child("countryName").getValue().toString();

                    if (! image.equals("-1"))
                        Picasso.get().load(image).placeholder(R.drawable.ic_baseline_person_black_75).into(profileImage);

                    profileImage.setOnClickListener(view -> {
                        Intent imageViewerIntent = new Intent(PersonProfileActivity.this, ImageViewerActivity.class);
                        imageViewerIntent.putExtra("URL", image);
                        startActivity(imageViewerIntent);
                    });

                    fullName.setText(name);
                    phoneNumber.setText(phone);
                    emailAddress.setText(email);
                    profileStatus.setText(status);
                    countryOfResidence.setText(country);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToHomeFragment() {
        Intent homeIntent = new Intent(PersonProfileActivity.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();
    }
}