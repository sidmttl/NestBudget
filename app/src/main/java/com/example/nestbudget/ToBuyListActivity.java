package com.example.nestbudget;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.util.ArrayList;

public class ToBuyListActivity extends AppCompatActivity {

    private EditText editTextItem;
    private Button buttonAdd;
    private RecyclerView recyclerViewItems;
    private ToBuyAdapter adapter;
    private ArrayList<ToBuyList> toBuyList;

    private String familyCode;
    private String userID;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_buy_list);

        LoginManager loginManager =  new LoginManager(this);
        familyCode = loginManager.getFamilyCode();
        userID = loginManager.getLoggedInUser();


        databaseRef = FirebaseDatabase.getInstance().getReference();

        editTextItem = findViewById(R.id.editTextItem);
        buttonAdd = findViewById(R.id.buttonAdd);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);

        toBuyList = new ArrayList<>();
        adapter = new ToBuyAdapter(ToBuyListActivity.this, toBuyList);

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
                Toast.makeText(ToBuyListActivity.this,"Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonAdd.setOnClickListener(v -> {
            String itemText = editTextItem.getText().toString().trim();
            if (!itemText.isEmpty()) {
                String id = Long.toString(System.currentTimeMillis());
                ToBuyList newItem = new ToBuyList(userID, itemText, "");
                databaseRef.child("Groups").child(familyCode).child("journal").child(id).setValue(newItem);

                toBuyList.add(newItem);

                adapter.notifyItemInserted(toBuyList.size() - 1);
                editTextItem.setText("");
            }
        });

    }
}