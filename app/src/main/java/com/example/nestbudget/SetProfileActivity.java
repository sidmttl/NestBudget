package com.example.nestbudget;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SetProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private Button uploadButton;
    private Uri selectedImageUri;

    private String userID;

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

    ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    uploadImageToFirebase(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        profileImageView = findViewById(R.id.profileImageView);
        uploadButton = findViewById(R.id.uploadButton);

        LoginManager loginManager = new LoginManager(this);
        userID = loginManager.getLoggedInUser();

        // Load existing profile picture if available
        loadExistingProfilePicture();

        uploadButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

    }

    private void loadExistingProfilePicture() {
        StorageReference imageRef = storageRef.child(userID + ".jpg");

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Profile picture exists, load it using Glide
            Glide.with(this)
                    .load(uri)
                    .circleCrop() // Make the image circular
                    .into(profileImageView);
        }).addOnFailureListener(e -> {
            // Profile picture doesn't exist or error occurred, keep the default icon
            // No action needed as the default icon is already set in the layout
        });
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String fileName = userID + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(this).load(uri).circleCrop().into(profileImageView);
                    Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes this activity and returns to previous
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}