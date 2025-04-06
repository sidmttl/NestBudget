package com.example.nestbudget;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
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
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_profile);
        profileImageView = findViewById(R.id.profileImageView);
        uploadButton = findViewById(R.id.uploadButton);

        uploadButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String fileName = "user_profile_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(this).load(uri).into(profileImageView);
                    Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}