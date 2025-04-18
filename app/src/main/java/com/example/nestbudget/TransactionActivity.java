package com.example.nestbudget;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class TransactionActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter adapter;
    private ArrayList<Transaction> transactionList;
    private FloatingActionButton fabAddTransaction;
    private BottomNavigationView bottomNavigationView;

    private String familyCode;
    private String userID;

    private DatabaseReference databaseRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        LoginManager loginManager = new LoginManager(this);
        familyCode = loginManager.getFamilyCode();
        userID = loginManager.getLoggedInUser();

        storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

        databaseRef = FirebaseDatabase.getInstance().getReference();

        recyclerViewTransactions = findViewById(R.id.recyler_view);
        fabAddTransaction = findViewById(R.id.floatingActionButton2);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.menu_transactions);

        setupBottomNavigation();

        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactionList, familyCode);

        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(adapter);

        databaseRef.child("Groups").child(familyCode).child("transactions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Transaction transaction = itemSnapshot.getValue(Transaction.class);
                    transactionList.add(transaction);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransactionActivity.this, "Failed to load transactions.", Toast.LENGTH_SHORT).show();
            }
        });

        fabAddTransaction.setOnClickListener(v -> showAddTransactionDialog());



        ImageView profileIcon = findViewById(R.id.profile_icon);
        if (profileIcon != null) {
            loadProfilePicture(profileIcon);

            profileIcon.setOnClickListener(v -> {
                Intent intent = new Intent(TransactionActivity.this, SetProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_dashboard) {
                Intent intent = new Intent(TransactionActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_transactions) {
                return true;
            } else if (itemId == R.id.menu_insights) {
                Intent intent = new Intent(TransactionActivity.this, InsightsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_journal) {
                Intent intent = new Intent(TransactionActivity.this, ToBuyListActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void showAddTransactionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Transaction");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_transaction, null);
        EditText etTransactionName = view.findViewById(R.id.etTransactionName);
        EditText etTransactionAmount = view.findViewById(R.id.etTransactionAmount);
        Spinner spinnerTransactionCategory = view.findViewById(R.id.SpinTransactionCategory);
        EditText etTransactionLocation = view.findViewById(R.id.etTransactionLocation);
        EditText etTransactionDate = view.findViewById(R.id.etTransactionDate);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.transaction_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransactionCategory.setAdapter(adapter);

        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etTransactionName.getText().toString().trim();
            String amount = etTransactionAmount.getText().toString().trim();
            String category = spinnerTransactionCategory.getSelectedItem().toString(); // Get selected category
            String location = etTransactionLocation.getText().toString().trim();
            String date = etTransactionDate.getText().toString().trim();

            if (!name.isEmpty() && !amount.isEmpty() && !category.isEmpty() && !location.isEmpty() && !date.isEmpty()) {
                String id = Long.toString(System.currentTimeMillis());
                Transaction newTransaction = new Transaction(id, name, category, amount, location, date); // Pass amount as String
                databaseRef.child("Groups").child(familyCode).child("transactions").child(id).setValue(newTransaction);
            } else {
                Toast.makeText(TransactionActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }



    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.menu_transactions);
    }

    private void loadProfilePicture(ImageView profileIcon) {
        if (this.userID != null) {
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
