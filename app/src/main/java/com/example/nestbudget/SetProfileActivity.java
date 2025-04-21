package com.example.nestbudget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.view.ActionMode;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SetProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private Button uploadButton, confirmButton, cancelButton;
    private Uri selectedImageUri;


    private EditText firstNameEditText, lastNameEditText, ageEditText, familyGroupIdEditText;
    private ImageView firstNameEditIcon, lastNameEditIcon, ageEditIcon;
    private String userID;

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pics");
    private DatabaseReference database;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
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
        confirmButton = findViewById(R.id.confirmButton);  // Added Confirm button
        cancelButton = findViewById(R.id.btnCancel);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        familyGroupIdEditText = findViewById(R.id.familyGroupIdEditText);

        firstNameEditIcon = findViewById(R.id.editFirstNameIcon);
        lastNameEditIcon = findViewById(R.id.editLastNameIcon);
        ageEditIcon = findViewById(R.id.editAgeIcon);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(KEY_USERNAME, null);
        database = FirebaseDatabase.getInstance().getReference();

        // Family code is non-editable
        familyGroupIdEditText.setEnabled(false);
        familyGroupIdEditText.setFocusable(false);
        familyGroupIdEditText.setClickable(false);

        if (userId != null) {
            fetchUserProfile(userId);
            fetchFamilyCode(userId);
        }

        LoginManager loginManager = new LoginManager(this);
        userID = loginManager.getLoggedInUser();

        // Load existing profile picture if available
        loadExistingProfilePicture();

        uploadButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));


        firstNameEditIcon.setOnClickListener(v -> enableEditing(firstNameEditText));
        lastNameEditIcon.setOnClickListener(v -> enableEditing(lastNameEditText));
        ageEditIcon.setOnClickListener(v -> enableEditing(ageEditText));

        // Set click listener for the "Confirm" button
        confirmButton.setOnClickListener(v -> {
            // Save the user profile data to Firebase when Confirm is clicked
            saveUserProfileData();
            // Disable editing after saving the data
            disableEditing(firstNameEditText);
            disableEditing(lastNameEditText);
            disableEditing(ageEditText);
        });

        // Disable Enter key from erasing the value
        firstNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                // Disable editing after pressing enter, but do not erase the value
                disableEditing(firstNameEditText);
                return true; // Handle the event
            }
            return false; // Let the system handle the action
        });

        lastNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                disableEditing(lastNameEditText);
                return true; // Handle the event
            }
            return false;
        });

        ageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                disableEditing(ageEditText);
                return true; // Handle the event
            }
            return false;
        });

        Toolbar toolbar = findViewById(R.id.family_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        cancelButton.setOnClickListener(v -> {
            startActivity(new Intent(SetProfileActivity.this, MainActivity.class));
            finish();
        });
    }

    private void fetchUserProfile(String userId) {
        database.child("Users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.hasChild("firstName")) {
                                firstNameEditText.setText(snapshot.child("firstName").getValue(String.class));
                            }
                            if (snapshot.hasChild("lastName")) {
                                lastNameEditText.setText(snapshot.child("lastName").getValue(String.class));
                            }
                            if (snapshot.hasChild("age")) {
                                ageEditText.setText(snapshot.child("age").getValue(String.class));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(SetProfileActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void fetchFamilyCode(String userId) {
        database.child("Users").child(userId).child("familyCode").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String code = snapshot.getValue(String.class);
                            familyGroupIdEditText.setText(code);
                        } else {
                            Toast.makeText(SetProfileActivity.this, "No family code found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(SetProfileActivity.this, "Failed to load family code.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void enableEditing(EditText editText) {
        editText.setEnabled(true);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
    }

    private void disableEditing(EditText editText) {
        editText.setEnabled(false);
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
    }

    private void saveUserProfileData() {
        String userId = sharedPreferences.getString(KEY_USERNAME, null);
        if (userId != null) {
            // Get updated values, trimming any unnecessary spaces
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String age = ageEditText.getText().toString().trim();

            // Update the Firebase database with the new values
            database.child("Users").child(userId).child("firstName").setValue(firstName);
            database.child("Users").child(userId).child("lastName").setValue(lastName);
            database.child("Users").child(userId).child("age").setValue(age)
                    .addOnSuccessListener(aVoid -> Toast.makeText(SetProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(SetProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
        }
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
