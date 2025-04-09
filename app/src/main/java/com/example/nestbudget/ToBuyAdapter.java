package com.example.nestbudget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ToBuyAdapter extends RecyclerView.Adapter<ToBuyAdapter.ItemViewHolder> {
    private List<ToBuyList> items;
    private Context context;

    public ToBuyAdapter(Context context, List<ToBuyList> items) {
        this.context = context;
        this.items = items;
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
        ToBuyList item = items.get(position);
        holder.itemName.setText(item.getName());
        holder.itemContent.setText(item.getContent());

        // Delete action
        holder.deleteButton.setOnClickListener(v -> {
            items.remove(position);
            notifyItemRemoved(position);
        });

        // Edit on click
        holder.itemView.setOnClickListener(v -> {
            showEditDialog(position);
        });
    }

    private void showEditDialog(int position) {
        ToBuyList currentItem = items.get(position);

        // Inflate custom dialog layout
        final EditText editTextName = new EditText(context);
        final EditText editTextContent = new EditText(context);
        final LinearLayout lay = new LinearLayout(context);

        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(editTextName);
        lay.addView(editTextContent);


        editTextName.setText(currentItem.getName());
        editTextContent.setText(currentItem.getContent());

        new AlertDialog.Builder(context)
                .setTitle("Edit Item")
                .setView(lay)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newText = editTextName.getText().toString().trim();
                    String newtext2 = editTextContent.getText().toString().trim();
                    if (!newText.isEmpty() || !newtext2.isEmpty()) {
                        currentItem.setName(newText);
                        currentItem.setContent(newtext2);
                        notifyItemChanged(position);
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
