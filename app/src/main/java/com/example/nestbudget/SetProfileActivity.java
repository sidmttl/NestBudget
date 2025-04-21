package com.example.nestbudget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class SetProfileActivity extends AppCompatActivity {

    private static final String TAG = "SetProfileActivity";

    private ImageView profileImageView;
    private Button uploadButton, confirmButton, cancelButton;
    private Uri selectedImageUri;

    private EditText firstNameEditText, lastNameEditText, ageEditText, familyGroupIdEditText;
    private ImageView firstNameEditIcon, lastNameEditIcon, ageEditIcon;
    private String userID;
    private String currentFamilyCode;

    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pics");
    private DatabaseReference database;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";

    private TextView familyMembersCountTextView;
    private LinearLayout familyMembersContainer;
    private List<String> familyMembers = new ArrayList<>();

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
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.btnCancel);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        familyGroupIdEditText = findViewById(R.id.familyGroupIdEditText);

        firstNameEditIcon = findViewById(R.id.editFirstNameIcon);
        lastNameEditIcon = findViewById(R.id.editLastNameIcon);
        ageEditIcon = findViewById(R.id.editAgeIcon);

        // Initialize family members section
        familyMembersCountTextView = findViewById(R.id.family_members_count);
        familyMembersContainer = findViewById(R.id.family_members_container);

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
        setupEditorActionListeners();

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

    private void setupEditorActionListeners() {
        firstNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                disableEditing(firstNameEditText);
                return true;
            }
            return false;
        });

        lastNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                disableEditing(lastNameEditText);
                return true;
            }
            return false;
        });

        ageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                disableEditing(ageEditText);
                return true;
            }
            return false;
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
                            currentFamilyCode = snapshot.getValue(String.class);
                            familyGroupIdEditText.setText(currentFamilyCode);

                            // Now fetch family members using this code
                            loadFamilyMembers(currentFamilyCode);
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
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SetProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                        // Reload family members list to reflect name changes
                        if (currentFamilyCode != null && !currentFamilyCode.isEmpty()) {
                            loadFamilyMembers(currentFamilyCode);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(SetProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
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

    private void loadFamilyMembers(String familyCode) {
        if (familyCode == null || familyCode.isEmpty()) {
            return;
        }

        // Clear any existing members
        if (familyMembersContainer != null) {
            familyMembersContainer.removeAllViews();
        }

        // Reference to the members node in the Groups database
        DatabaseReference membersRef = database.child("Groups").child(familyCode).child("members");

        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Count members
                    long memberCount = snapshot.getChildrenCount();

                    // Update count display
                    if (familyMembersCountTextView != null) {
                        familyMembersCountTextView.setText("Family Members (" + memberCount + ")");
                    }

                    // Debug log
                    Log.d(TAG, "Found " + memberCount + " family members");

                    // Process each member
                    for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                        String memberId = memberSnapshot.getKey();
                        if (memberId != null) {
                            // Fetch details for this member
                            fetchMemberDetails(memberId);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load family members: " + error.getMessage());
                Toast.makeText(SetProfileActivity.this,
                        "Error loading family members", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMemberDetails(String memberId) {
        database.child("Users").child(memberId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String firstName = snapshot.child("firstName").getValue(String.class);
                            String lastName = snapshot.child("lastName").getValue(String.class);

                            // Debug log
                            Log.d(TAG, "Member details - ID: " + memberId + ", Name: " + firstName + " " + lastName);

                            if (firstName != null && lastName != null && familyMembersContainer != null) {
                                // Create and configure TextView for member
                                TextView memberView = new TextView(SetProfileActivity.this);
                                memberView.setText(firstName + " " + lastName);
                                memberView.setTextSize(16);

                                // Set padding (convert dp to pixels)
                                int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
                                memberView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

                                // Set layout parameters
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                memberView.setLayoutParams(params);

                                // Highlight current user
                                if (memberId.equals(userID)) {
                                    memberView.setText(firstName + " " + lastName + " (You)");
                                    memberView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                                    memberView.setTypeface(null, Typeface.BOLD);
                                }

                                // Add to container
                                familyMembersContainer.addView(memberView);

                                // Add divider
                                View divider = new View(SetProfileActivity.this);
                                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        (int) (1 * getResources().getDisplayMetrics().density)
                                );
                                divider.setLayoutParams(dividerParams);
                                divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                familyMembersContainer.addView(divider);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching member details: " + error.getMessage());
                    }
                }
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(SetProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SetProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}