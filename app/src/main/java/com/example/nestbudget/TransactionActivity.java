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

public class TransactionActivity extends AppCompatActivity {

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
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_dashboard) {
                startActivity(new Intent(TransactionActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.menu_transactions) {
                return true;
            } else if (itemId == R.id.menu_insights) {
                Toast.makeText(this, "Insights feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_journal) {
                startActivity(new Intent(TransactionActivity.this, ToBuyListActivity.class));
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
        EditText etTransactionCategory = view.findViewById(R.id.etTransactionCategory);
        EditText etTransactionLocation = view.findViewById(R.id.etTransactionLocation);
        EditText etTransactionDate = view.findViewById(R.id.etTransactionDate); // Can use a DatePicker instead for better UX

        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etTransactionName.getText().toString().trim();
            String amount = etTransactionAmount.getText().toString().trim();
            String category = etTransactionCategory.getText().toString().trim();
            String location = etTransactionLocation.getText().toString().trim();
            String date = etTransactionDate.getText().toString().trim();

            if (!name.isEmpty() && !amount.isEmpty() && !category.isEmpty() && !location.isEmpty() && !date.isEmpty()) {
                String id = Long.toString(System.currentTimeMillis());
                Transaction newTransaction = new Transaction(id, name, amount, category, location, date); // Pass amount as String
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
}
