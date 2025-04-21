package com.example.nestbudget;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ToBuyListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewItems;
    private ToBuyAdapter adapter;
    private ArrayList<ToBuyList> toBuyList;
    private FloatingActionButton fabAddItem;
    private BottomNavigationView bottomNavigationView;

    private String familyCode;
    private String userID;

    private DatabaseReference databaseRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_buy_list);

        LoginManager loginManager = new LoginManager(this);
        familyCode = loginManager.getFamilyCode();
        userID = loginManager.getLoggedInUser();

        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        fabAddItem = findViewById(R.id.fabAddItem);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set Journal as the selected item in the bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.menu_journal);

        // Setup bottom navigation
        setupBottomNavigation();

        toBuyList = new ArrayList<>();
        adapter = new ToBuyAdapter(ToBuyListActivity.this, toBuyList, familyCode);

        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(adapter);

        databaseRef.child("Groups").child(familyCode).child("journal").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                toBuyList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ToBuyList item = itemSnapshot.getValue(ToBuyList.class);
                    toBuyList.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ToBuyListActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });

        fabAddItem.setOnClickListener(v -> showAddItemDialog());

        ImageView profileIcon = findViewById(R.id.profile_icon);
        if (profileIcon != null) {
            loadProfilePicture(profileIcon);

            profileIcon.setOnClickListener(v -> {
                Intent intent = new Intent(ToBuyListActivity.this, SetProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_dashboard) {
                // Navigate to Dashboard/MainActivity
                Intent intent = new Intent(ToBuyListActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_transactions) {
                Intent intent = new Intent(ToBuyListActivity.this, TransactionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_insights) {
                Intent intent = new Intent(ToBuyListActivity.this, InsightsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_journal) {
                // Already on journal, no action needed
                return true;
            }
            return false;
        });
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Item");

        // Set up the dialog layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        EditText etItemTitle = view.findViewById(R.id.etItemTitle);
        EditText etItemContent = view.findViewById(R.id.etItemContent);

        builder.setView(view);

        // Set up the buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String itemTitle = etItemTitle.getText().toString().trim();
            String itemContent = etItemContent.getText().toString().trim();

            if (!itemTitle.isEmpty()) {
                String id = Long.toString(System.currentTimeMillis());
                ToBuyList newItem = new ToBuyList(userID, itemTitle, itemContent);
                databaseRef.child("Groups").child(familyCode).child("journal").child(id).setValue(newItem);
                // The ValueEventListener will update the UI
            } else {
                Toast.makeText(ToBuyListActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make sure Journal is selected when returning to this activity
        bottomNavigationView.setSelectedItemId(R.id.menu_journal);
    }

    private void loadProfilePicture(ImageView profileIcon) {
        if (userID != null) {
            StorageReference imageRef = storageRef.child(userID + ".jpg");

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Profile picture exists, load it using Glide
                Glide.with(this)
                        .load(uri)
                        .circleCrop() // Make the image circular
                        .into(profileIcon);
            }).addOnFailureListener(e -> {
                // Profile picture doesn't exist or error occurred, keep the default icon
                // No action needed as the default icon is already set in the layout
            });
        }
    }
}