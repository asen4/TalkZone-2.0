package com.anubhav.talkzone2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference mUsersReference;

    private ImageButton backButton;
    private CircleImageView profileImage;
    private TextView personProfileHeader, fullName, phoneNumber, emailAddress, profileStatus, countryOfResidence;
    private String receiverUserID;

    public static Bundle mBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeVariables();
    }

    private void initializeVariables() {
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        receiverUserID = getIntent().getStringExtra("receiverUserID");
        ProfileActivity.mBundle.putString("receiverUserID", receiverUserID);

        personProfileHeader = findViewById(R.id.personProfileText);
        backButton = findViewById(R.id.profileBackButton);
        profileImage = findViewById(R.id.profileImage);
        fullName = findViewById(R.id.profileFullName);
        phoneNumber = findViewById(R.id.profilePhoneNumber);
        emailAddress = findViewById(R.id.profileEmailAddress);
        profileStatus = findViewById(R.id.profileStatus);
        countryOfResidence = findViewById(R.id.profileCountryOfResidence);

        backButton.setOnClickListener(v -> onBackPressed());

        mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("profileImage").getValue().toString();
                String name = dataSnapshot.child("fullName").getValue().toString();
                String phone = dataSnapshot.child("phoneNumber").getValue().toString();
                String email = dataSnapshot.child("emailAddress").getValue().toString();
                String status = dataSnapshot.child("profileStatus").getValue().toString();
                String countryName = dataSnapshot.child("countryName").getValue().toString();

                personProfileHeader.setText(name + "'s Profile");

                if (! image.equals("-1"))
                    Picasso.get().load(image).placeholder(R.drawable.ic_baseline_person_black_75).into(profileImage);

                profileImage.setOnClickListener(view -> {
                    Intent imageViewerIntent = new Intent(ProfileActivity.this, ImageViewerActivity.class);
                    imageViewerIntent.putExtra("URL", image);
                    startActivity(imageViewerIntent);
                });

                fullName.setText(name);
                phoneNumber.setText(phone);
                emailAddress.setText(email);
                profileStatus.setText(status);
                countryOfResidence.setText(countryName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}