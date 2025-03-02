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

import android.content.Context;
import java.util.List;

public class DatabaseController {
    private DatabaseHelper dbHelper;

    public DatabaseController(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // / Insert Item with userId
    public boolean addItem(String name, int quantity, int userId) {
        if (name == null || name.trim().isEmpty() || quantity <= 0) {
            return false; // Prevent invalid input
        }
        return dbHelper.insertItem(name, quantity, userId);
    }

    // / Get All Items for a Specific User
    public List<Item> getAllItems(int userId) {
        return dbHelper.getAllItems(userId);
    }

    // / Update SMS Preference in Database
    // / Update SMS preference
    public boolean updateSmsPreference(int userId, boolean isEnabled) {
        return dbHelper.updateSmsPreference(userId, isEnabled); // Save SMS preference in database
    }

    // / Fix: Update an Item (Ensure userId is checked)
    // / Update Item with quantity and SMS preference
    public boolean updateItem(int id, String name, int quantity, int userId) {
        return dbHelper.updateItem(id, name, quantity, userId); // Update in database
    }

    // / Fix: Delete an Item (Ensure userId is checked)
    public boolean deleteItem(int itemId, int userId) {
        return dbHelper.deleteItem(itemId, userId);
    }

    public boolean getSmsPreference(int userId) {
        return dbHelper.getSmsPreference(userId);
    }

    public boolean setSmsPreference(int userId, boolean isEnabled) {
        return dbHelper.updateSmsPreference(userId, isEnabled); // Correct method to update SMS preference
    }




    // / Fix: Clear the Database
    public void clearDatabase() {
        dbHelper.clearDatabase();
    }
}
