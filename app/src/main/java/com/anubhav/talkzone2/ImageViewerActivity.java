package com.anubhav.talkzone2;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.imageViewer);
        imageURL = getIntent().getStringExtra("URL");
        Picasso.get().load(imageURL).into(imageView);
    }
}