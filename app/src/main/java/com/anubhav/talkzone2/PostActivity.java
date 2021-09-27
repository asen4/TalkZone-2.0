package com.anubhav.talkzone2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private EditText postMessage, postTitle;
    private ImageButton selectPostImage, backButton;
    private MaterialButton postButton, cancelButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference, mPostsReference;
    private StorageReference mPostImagesReference;

    private long countPosts = 0;
    private String currentUserID, postDescription, saveCurrentDate, saveCurrentTime, postUniqueName, downloadURL;
    private Uri imageURI;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initializeVariables();

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (! isInterrupted()) {
                    try {
                        Thread.sleep(500);

                        final String mETPostDescription = postMessage.getText().toString().trim();
                        final String mETPostTitle = postTitle.getText().toString().trim();

                        runOnUiThread(() -> {

                            if (! mETPostTitle.equals("") && mETPostTitle.length() > 0 && ! mETPostDescription.equals("") && mETPostDescription.length() <= 250 && imageURI != null) {
                                postButton.setBackgroundColor(getResources().getColor(R.color.colorTurquoise));
                                postButton.setEnabled(true);
                            }

                            else {
                                postButton.setBackgroundColor(getResources().getColor(R.color.colorGray));
                                postButton.setEnabled(false);
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

    private void validatePostInformation() {
        postDescription = postMessage.getText().toString().trim();

        loadingBar.setTitle("Posting...");
        loadingBar.setMessage("Please wait while we are processing your request.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        storingPostImageToFirebaseStorage();
    }

    private void storingPostImageToFirebaseStorage() {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm aa");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        postUniqueName = saveCurrentDate + "|" + saveCurrentTime;

        StorageReference filePath = mPostImagesReference.child("Post Images").child(imageURI.getLastPathSegment() + postUniqueName + ".jpg");
        filePath.putFile(imageURI).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                task.addOnSuccessListener(taskSnapshot -> {
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(uri -> {
                                downloadURL = uri.toString();
                                savingPostDescriptionToFirebaseDatabase();
                            });
                        }
                    }
                });
            }

            else {
                String errorMessage = task.getException().getMessage();
                Toast.makeText(PostActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savingPostDescriptionToFirebaseDatabase() {
        mPostsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    countPosts = dataSnapshot.getChildrenCount();
                else
                    countPosts = 0;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullName = dataSnapshot.child("fullName").getValue().toString();
                    String title = postTitle.getText().toString().trim();

                    HashMap postMap = new HashMap();
                    postMap.put("userID", currentUserID);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("postTitle", title);
                    postMap.put("postDescription", postDescription);
                    postMap.put("postImage", downloadURL);
                    postMap.put("fullName", fullName);
                    postMap.put("counter", countPosts);

                    if (! dataSnapshot.child("profileImage").getValue().equals("-1")) {
                        String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                        postMap.put("profileImage", profileImage);
                    }

                    else
                        postMap.put("profileImage", "-1");

                    mPostsReference.child(currentUserID + "|" + postUniqueName).updateChildren(postMap)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    sendUserToMainActivity();
                                    Toast.makeText(PostActivity.this, "Your new post was created successfully!", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }

                                else {
                                    Toast.makeText(PostActivity.this, "An error occured while creating your post!", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeVariables () {
        postMessage = findViewById(R.id.editTextPost);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mPostsReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        mPostImagesReference = FirebaseStorage.getInstance().getReference();

        selectPostImage = findViewById(R.id.postPicture);
        postButton = findViewById(R.id.postButton);
        backButton = findViewById(R.id.postBackBtn);
        cancelButton = findViewById(R.id.postCancelButton);
        postTitle = findViewById(R.id.postTitle);
        postMessage = findViewById(R.id.editTextPost);
        loadingBar = new ProgressDialog(PostActivity.this);

        selectPostImage.setOnClickListener(v -> openGallery());

        postButton.setOnClickListener(v -> validatePostInformation());

        backButton.setOnClickListener(view -> onBackPressed());

        cancelButton.setOnClickListener(v -> onBackPressed());
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            imageURI = data.getData();
            selectPostImage.setImageURI(imageURI);
        }
    }
}