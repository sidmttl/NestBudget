package com.example.nestbudget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ToBuyAdapter extends RecyclerView.Adapter<ToBuyAdapter.ItemViewHolder> {
    private List<ToBuyList> items;
    private Context context;
    private String familyCode;
    private DatabaseReference databaseRef;

    public ToBuyAdapter(Context context, List<ToBuyList> items, String familyCode) {
        this.context = context;
        this.items = items;
        this.familyCode = familyCode;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView itemContent;
        ImageButton deleteButton;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.textItemName);
            itemContent = itemView.findViewById(R.id.textItemContent);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.to_buy_item_row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        if (position < 0 || position >= items.size()) {
            return; // Safeguard against out of bounds
        }

        ToBuyList item = items.get(position);
        holder.itemName.setText(item.getName());
        holder.itemContent.setText(item.getContent());

        // Delete action
        holder.deleteButton.setOnClickListener(v -> {
            // Get the current position - important because position might change during async operation
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && currentPosition < items.size()) {
                ToBuyList itemToDelete = items.get(currentPosition);

                // Remove from Firebase first
                databaseRef.child("Groups").child(familyCode).child("journal").child(itemToDelete.getId()).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // Important: Let the ValueEventListener in ToBuyListActivity handle the UI update
                            // This ensures our view is always in sync with the database
                            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Edit on click
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && currentPosition < items.size()) {
                showEditDialog(currentPosition);
            }
        });
    }

    private void showEditDialog(int position) {
        if (position < 0 || position >= items.size()) {
            return; // Safeguard against out of bounds
        }

        ToBuyList currentItem = items.get(position);

        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_item, null);
        final EditText editTextName = dialogView.findViewById(R.id.etItemTitle);
        final EditText editTextContent = dialogView.findViewById(R.id.etItemContent);

        editTextName.setText(currentItem.getName());
        editTextContent.setText(currentItem.getContent());

        new AlertDialog.Builder(context)
                .setTitle("Edit Item")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = editTextName.getText().toString().trim();
                    String newContent = editTextContent.getText().toString().trim();

                    if (!newName.isEmpty()) {
                        // Create an updated item
                        ToBuyList updatedItem = new ToBuyList(currentItem.getId(), newName, newContent);

                        // Update in Firebase - let the ValueEventListener handle the UI update
                        databaseRef.child("Groups").child(familyCode).child("journal")
                                .child(currentItem.getId()).setValue(updatedItem)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Item updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}