/**
 * Inventory Management App
 * Developed by Gonzalo Patino
 * Software Engineer
 * Southern New Hampshire University
 * CS360 - Software Architecture
 *
 * Description: This app provides an inventory management system with user authentication,
 * SMS notifications for low inventory and upcoming events, and a structured UI
 * following the MVC architecture. It integrates an SQLite database for user authentication
 * and ensures incremental testing with JUnit at each development stage.
 *
 * Date: February 22, 2025
 */



package com.zybooks.inventorymapp;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private List<Item> itemList;
    private OnItemDeleteListener deleteListener;
    private OnItemUpdateListener updateListener;

    // Interface for delete action
    public interface OnItemDeleteListener {
        void onItemDelete(Item item);
    }

    // Interface for update action
    public interface OnItemUpdateListener {
        void onItemUpdate(Item item);
    }

    public ItemAdapter(List<Item> itemList, OnItemDeleteListener deleteListener, OnItemUpdateListener updateListener) {
        this.itemList = itemList;
        this.deleteListener = deleteListener;
        this.updateListener = updateListener;
    }

    public void updateItems(List<Item> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.itemName.setText(item.getName());
        holder.itemQuantity.setText("Quantity: " + item.getQuantity());

        // Handle delete when clicked
        holder.itemView.setOnClickListener(v -> deleteListener.onItemDelete(item));

        // Handle update on long press
        holder.itemView.setOnLongClickListener(v -> {
            updateListener.onItemUpdate(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemQuantity;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(android.R.id.text1);
            itemQuantity = itemView.findViewById(android.R.id.text2);
        }
    }
}
