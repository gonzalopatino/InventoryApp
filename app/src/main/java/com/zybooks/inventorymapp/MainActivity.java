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
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import android.widget.EditText;
import android.text.InputType;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;







public class MainActivity extends AppCompatActivity {
    private DatabaseController dbController;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<Item> itemList;
    private Button btnAdd, btnClear, btnLogout;
    private TextView tvUsername;
    private Switch smsToggleSwitch;
    public boolean isSmsEnabled;
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private int userId; // Store the logged-in user's ID
    private ActivityResultLauncher<Intent> addItemLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // / Initialize UI elements
        tvUsername = findViewById(R.id.tvUsername);
        smsToggleSwitch = findViewById(R.id.smsToggleSwitch);

        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.btnAddItem);
        btnClear = findViewById(R.id.btnClear);
        btnLogout = findViewById(R.id.btnLogout);
        dbController = new DatabaseController(this);
        itemList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        // / Retrieve user info from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = preferences.getInt("userId", -1);
        String username = preferences.getString("username", "User");

        if (userId == -1) {
            Toast.makeText(this, "Error: No logged-in user.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvUsername.setText("Welcome, " + username + "!");

        // / Load SMS preference from database
        boolean isSmsEnabled = dbController.getSmsPreference(userId);
        //boolean isSmsEnabled = preferences.getBoolean("sms_enabled", false);
        smsToggleSwitch.setChecked(isSmsEnabled);

        // / Handle SMS toggle switch change
        smsToggleSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            boolean success = dbController.updateSmsPreference(userId, isChecked);
            if (success) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("sms_enabled", isChecked);
                editor.apply();

                if (isChecked) {
                    requestSmsPermission(); // / Request permission if enabled
                }
            } else {

                Toast.makeText(MainActivity.this, "Failed to update SMS preference!", Toast.LENGTH_SHORT).show();
            }
        });

        // / Load items for the logged-in user
        loadItems();

        // / Initialize ActivityResultLauncher to handle results from AddNewItemActivity
        addItemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadItems(); // Refresh the list when returning from AddNewItemActivity
                    }
                }
        );

        btnAdd.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Add Item button clicked!", Toast.LENGTH_SHORT).show();
            System.out.println("DEBUG: Add Item button clicked!");

            try {
                Intent intent = new Intent(MainActivity.this, AddNewItemActivity.class);
                intent.putExtra("userId", userId);
                System.out.println("DEBUG: Intent created successfully!");

                addItemLauncher.launch(intent);
                System.out.println("DEBUG: Intent launched successfully!");

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MainActivity", "ERROR: Failed to launch AddNewItemActivity", e);
                Toast.makeText(MainActivity.this, "Error launching Add Item screen!", Toast.LENGTH_LONG).show();
            }
        });

        // / Clear all items
        btnClear.setOnClickListener(v -> {
            dbController.clearDatabase();
            loadItems();
            Toast.makeText(MainActivity.this, "All items cleared", Toast.LENGTH_SHORT).show();
        });

        // / Logout functionality
        btnLogout.setOnClickListener(v -> logoutUser());

        // / Long Press to edit item
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Item selectedItem = itemList.get(position);
            showUpdateDialog(selectedItem); // Open update dialog on long press
            return true;
        });

        // / Short Press to delete item
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Item selectedItem = itemList.get(position);

            // Show a confirmation dialog for deletion
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Proceed with item deletion if user confirms
                        boolean success = dbController.deleteItem(selectedItem.getId(), userId);
                        if (success) {
                            Toast.makeText(MainActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Delete Failed", Toast.LENGTH_SHORT).show();
                        }
                        loadItems(); // Refresh item list
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // Do nothing on cancel
                    .show();
        });
    }

    private void loadItems() {
        itemList = dbController.getAllItems(userId);  // Fetch the items for the logged-in user
        List<String> itemNames = new ArrayList<>();

        for (Item item : itemList) {
            itemNames.add(item.getName() + " - " + item.getQuantity());  // Display name and quantity
        }

        adapter.clear();  // Clear the previous data
        adapter.addAll(itemNames);  // Add the new list of items
        adapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
    }

    private void showUpdateDialog(Item item) {
        // Create the dialog for updating an item
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Item");

        // Create layout to hold input fields for item name and quantity
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameInput = new EditText(this);
        nameInput.setText(item.getName()); // Pre-fill the name of the item to be updated
        layout.addView(nameInput);

        final EditText quantityInput = new EditText(this);
        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        quantityInput.setText(String.valueOf(item.getQuantity())); // Pre-fill the quantity of the item to be updated
        layout.addView(quantityInput);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String updatedName = nameInput.getText().toString().trim();
            int updatedQuantity;

            try {
                updatedQuantity = Integer.parseInt(quantityInput.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            if (updatedName.isEmpty() || updatedQuantity <= 0) {
                Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the item in the database
            boolean success = dbController.updateItem(item.getId(), updatedName, updatedQuantity, userId);
            if (success) {
                Toast.makeText(MainActivity.this, "Item Updated", Toast.LENGTH_SHORT).show();
                loadItems(); // Refresh the list after updating
            } else {
                Toast.makeText(MainActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted!", Toast.LENGTH_SHORT).show();
                smsToggleSwitch.setChecked(true); // Reset switch if denied
                dbController.setSmsPreference(userId, true);

            } else {
                Toast.makeText(this, "SMS Permission Denied!", Toast.LENGTH_SHORT).show();
                smsToggleSwitch.setChecked(false); // Reset switch if denied
                dbController.setSmsPreference(userId, false);
            }
        }
    }

    private void logoutUser() {
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finishAffinity();
    }


}
