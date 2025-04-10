package com.example.nestbudget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private String familyCode;
    private String userID;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_buy_list);

        LoginManager loginManager = new LoginManager(this);
        familyCode = loginManager.getFamilyCode();
        userID = loginManager.getLoggedInUser();

        databaseRef = FirebaseDatabase.getInstance().getReference();

        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        fabAddItem = findViewById(R.id.fabAddItem);

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
                ToBuyList newItem = new ToBuyList(id, itemTitle, itemContent);
                databaseRef.child("Groups").child(familyCode).child("journal").child(id).setValue(newItem);

                toBuyList.add(newItem);
                adapter.notifyItemInserted(toBuyList.size() - 1);
            } else {
                Toast.makeText(ToBuyListActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}