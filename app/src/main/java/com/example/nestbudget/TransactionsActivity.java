package com.example.nestbudget;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TransactionsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter adapter;
    private ArrayList<Transaction> transactionList;
    private FloatingActionButton fabAddTransaction;
    private BottomNavigationView bottomNavigationView;

    private String familyCode;
    private String userID;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        LoginManager loginManager = new LoginManager(this);
        familyCode = loginManager.getFamilyCode();
        userID = loginManager.getLoggedInUser();

        databaseRef = FirebaseDatabase.getInstance().getReference();

        recyclerViewTransactions = findViewById(R.id.recyler_view);
        fabAddTransaction = findViewById(R.id.floatingActionButton2);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set Transactions as the selected item in the bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.menu_transactions);

        // Setup bottom navigation
        setupBottomNavigation();

        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(TransactionsActivity.this, transactionList);

        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(adapter);

        // Load transactions from Firebase
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
                Toast.makeText(TransactionsActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });

        fabAddTransaction.setOnClickListener(v -> openTransactionDialog());
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_dashboard) {
                Intent intent = new Intent(TransactionsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_transactions) {
                // Already on Transactions, no action needed
                return true;
            } else if (itemId == R.id.menu_insights) {
                Toast.makeText(this, "Insights feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_journal) {
                Intent intent = new Intent(TransactionsActivity.this, ToBuyListActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void openTransactionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Transaction");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_transaction, null);
        EditText etName = view.findViewById(R.id.etName);
        EditText etAmount = view.findViewById(R.id.etAmount);
        EditText etCategory = view.findViewById(R.id.etCategory);
        EditText etDate = view.findViewById(R.id.etDate);
        EditText etNotes = view.findViewById(R.id.etNotes);

        builder.setView(view);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (!name.isEmpty() && !amount.isEmpty()) {
                String id = Long.toString(System.currentTimeMillis());
                Transaction transaction = new Transaction(id, name, amount, category, date, notes);
                databaseRef.child("Groups").child(familyCode).child("transactions").child(id).setValue(transaction);
                Toast.makeText(TransactionsActivity.this, "Transaction added successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TransactionsActivity.this, "Name and amount are required.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make sure Transactions is selected when returning to this activity
        bottomNavigationView.setSelectedItemId(R.id.menu_transactions);
    }
}
