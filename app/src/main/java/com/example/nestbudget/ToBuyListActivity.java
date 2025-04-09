package com.example.nestbudget;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ToBuyListActivity extends AppCompatActivity {

    private EditText editTextItem;
    private Button buttonAdd;
    private RecyclerView recyclerViewItems;
    private ToBuyAdapter adapter;
    private ArrayList<ToBuyList> toBuyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_buy_list);

        editTextItem = findViewById(R.id.editTextItem);
        buttonAdd = findViewById(R.id.buttonAdd);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);

        toBuyList = new ArrayList<>();
        adapter = new ToBuyAdapter(toBuyList);

        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(adapter);

        buttonAdd.setOnClickListener(v -> {
            String itemText = editTextItem.getText().toString().trim();
            if (!itemText.isEmpty()) {
                toBuyList.add(new ToBuyList(itemText));
                adapter.notifyItemInserted(toBuyList.size() - 1);
                editTextItem.setText("");
            }
        });

    }
}