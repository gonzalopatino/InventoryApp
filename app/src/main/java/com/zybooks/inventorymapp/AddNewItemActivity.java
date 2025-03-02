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
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class AddNewItemActivity extends AppCompatActivity {
    private TextInputEditText etItemName, etQuantity;
    private Button btnAddItem;
    private DatabaseController dbController;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_item);

        // Initialize UI elements
        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        btnAddItem = findViewById(R.id.btnAddItem);

        dbController = new DatabaseController(this);

        // Retrieve userId from Intent
        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: No user found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Add Item button functionality
        btnAddItem.setOnClickListener(v -> {
            String name = etItemName.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();

            if (name.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(this, "Please enter both item name and quantity.", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            boolean success = dbController.addItem(name, quantity, userId);

            if (success) {
                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Notify MainActivity to refresh list
                finish();
            } else {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
