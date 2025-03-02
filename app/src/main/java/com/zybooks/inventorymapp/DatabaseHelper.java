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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.SmsManager;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 3;
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "password TEXT, " +
                "sms_enabled INTEGER DEFAULT 0, " +
                "phone_number TEXT)";

        String CREATE_ITEMS_TABLE = "CREATE TABLE items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "quantity INTEGER, " +
                "user_id INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(user_id))";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS items");
        onCreate(db);
    }

    // / Fix: Get User Phone Number
    public String getUserPhoneNumber(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String phoneNumber = null;

        Cursor cursor = db.rawQuery("SELECT phone_number FROM users WHERE user_id = ?",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            phoneNumber = cursor.getString(0);
        }
        cursor.close();
        db.close();

        return phoneNumber;
    }

    // / Fix: Clear Users Table for Testing
    public void clearUsersTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users", null, null);
        db.close();
    }

    // / Fix: Delete an Item for a Specific User
    public boolean deleteItem(int itemId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("items", "id = ? AND user_id = ?",
                new String[]{String.valueOf(itemId), String.valueOf(userId)});
        db.close();
        return rowsDeleted > 0;
    }

    // / Fix: Clear All Items for a Specific User
    public void clearItemsForUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("items", "user_id=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    // / Fix: Update an Item for a Specific User
    public boolean updateItem(int itemId, String name, int quantity, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);

        int rowsUpdated = db.update("items", values, "id = ? AND user_id = ?",
                new String[]{String.valueOf(itemId), String.valueOf(userId)});

        // / Check if SMS should be sent
        if (quantity < 3) {
            sendLowStockAlert(userId, name, quantity);
        }
        db.close();

        return rowsUpdated > 0;
    }

    // / Fix: Register a New User
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // / Check if the username already exists
        Cursor cursor = db.rawQuery("SELECT user_id FROM users WHERE username = ?", new String[]{username});
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false; // ❌ Username already exists
        }
        cursor.close();

        // / Proceed with inserting the new user
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("sms_enabled", 0);
        values.put("phone_number", "");

        long result = db.insert("users", null, values);
        db.close();

        return result != -1; // / Returns true if insert was successful
    }

    // / Fix: Authenticate User and Return userId
    public int authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM users WHERE username = ? AND password = ?",
                new String[]{username, password});

        int userId = -1; // Default to -1 (invalid login)
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    // / Fix: Insert Item for a Specific User
    public boolean insertItem(String name, int quantity, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("user_id", userId);

        long result = db.insert("items", null, values);
        db.close();



        return result != -1;
    }

    // / Fix: Clear the Entire Database (For Testing)
    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users", null, null);
        db.delete("items", null, null);
        db.close();
    }

    // / Fix: Get All Items for a Specific User
    public List<Item> getAllItems(int userId) {
        List<Item> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, quantity FROM items WHERE user_id = ?",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int quantity = cursor.getInt(2);
                itemList.add(new Item(id, name, quantity));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return itemList;
    }

    // / Fix: Update SMS Preference for a User
    public boolean updateSmsPreference(int userId, boolean smsEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sms_enabled", smsEnabled ? 1 : 0);

        int rowsUpdated = db.update("users", values, "user_id = ?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsUpdated > 0;
    }

    // / Fix: Retrieve SMS Preference for a User
    public boolean getSmsPreference(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT sms_enabled FROM users WHERE user_id = ?", new String[]{String.valueOf(userId)});
        boolean isEnabled = false;

        if (cursor.moveToFirst()) {
            isEnabled = cursor.getInt(0) == 1;
        }
        cursor.close();
        db.close();
        return isEnabled;
    }

    // / Fix: Register a New User with Phone Number
    public boolean registerUser(String username, String password, String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("sms_enabled", 0); // Default to SMS off
        values.put("phone_number", phoneNumber); // Store phone number

        long result = db.insert("users", null, values);
        db.close();
        return result != -1;
    }

    // / Send SMS Alert When Stock is Low
    private void sendLowStockAlert(int userId, String itemName, int quantity) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT sms_enabled, phone_number FROM users WHERE user_id = ?",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            boolean smsEnabled = cursor.getInt(0) == 1;
            String phoneNumber = cursor.getString(1);

            if (smsEnabled && phoneNumber != null && !phoneNumber.isEmpty()) {
                String message = "Attention, you are running low on " + itemName + " with a quantity of " + quantity + ". Buy more!";
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }
        }
        cursor.close();
        db.close();
    }
}
