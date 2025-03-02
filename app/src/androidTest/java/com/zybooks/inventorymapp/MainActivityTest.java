package com.zybooks.inventorymapp;

import static org.junit.Assert.*;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

public class MainActivityTest {
    private DatabaseHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        dbHelper.clearUsersTable(); // Ensures a fresh test environment
    }

    @After
    public void tearDown() {
        dbHelper.clearUsersTable(); // Cleanup after tests
        dbHelper.close();
    }

    // ✅ Helper method to register user and return userId
    private int registerTestUser() {
        boolean success = dbHelper.registerUser("testuser", "password123", "1234567890");
        assertTrue("User should be registered successfully", success);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM users WHERE username = ?", new String[]{"testuser"});

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();

        assertNotEquals("User ID should be valid", -1, userId);
        return userId;
    }

    // ✅ Test User Authentication
    @Test
    public void testAuthenticatedUserAccess() {
        int userId = registerTestUser();
        assertNotEquals("User should be authenticated successfully", -1, userId);
    }

    // ✅ Test Adding an Item Successfully
    @Test
    public void testAddItem_Success() {
        int userId = registerTestUser();
        boolean result = dbHelper.insertItem("Laptop", 2, userId);
        assertTrue("Item should be added successfully", result);
    }

    // ✅ Test Retrieving Items
    @Test
    public void testGetAllItems_Success() {
        int userId = registerTestUser();

        dbHelper.insertItem("Phone", 5, userId);
        dbHelper.insertItem("Tablet", 3, userId);

        List<Item> items = dbHelper.getAllItems(userId);
        assertFalse("Item list should not be empty", items.isEmpty());
        assertEquals("Should contain 2 items", 2, items.size());
    }

    // ✅ Test Updating an Item
    @Test
    public void testUpdateItem_Success() {
        int userId = registerTestUser();

        dbHelper.insertItem("Monitor", 1, userId);
        int itemId = dbHelper.getAllItems(userId).get(0).getId();

        boolean updated = dbHelper.updateItem(itemId, "Updated Monitor", 2, userId);
        assertTrue("Item should be updated successfully", updated);
    }

    // ✅ Test Deleting an Item
    @Test
    public void testDeleteItem_Success() {
        int userId = registerTestUser();

        dbHelper.insertItem("Headphones", 4, userId);
        int itemId = dbHelper.getAllItems(userId).get(0).getId();

        boolean deleted = dbHelper.deleteItem(itemId, userId);
        assertTrue("Item should be deleted successfully", deleted);
    }

    // ✅ Test Enabling SMS Notifications
    @Test
    public void testEnableSmsNotifications() {
        int userId = registerTestUser();
        boolean updated = dbHelper.updateSmsPreference(userId, true);
        assertTrue("SMS preference should be updated to true", updated);

        boolean isEnabled = dbHelper.getSmsPreference(userId);
        assertTrue("SMS should be enabled", isEnabled);
    }

    // ✅ Test Disabling SMS Notifications
    @Test
    public void testDisableSmsNotifications() {
        int userId = registerTestUser();
        dbHelper.updateSmsPreference(userId, true);
        dbHelper.updateSmsPreference(userId, false);

        boolean isEnabled = dbHelper.getSmsPreference(userId);
        assertFalse("SMS should be disabled", isEnabled);
    }

    // ✅ Test Retrieving User Phone Number
    @Test
    public void testRetrieveUserPhoneNumber() {
        int userId = registerTestUser();
        String phoneNumber = dbHelper.getUserPhoneNumber(userId);
        assertEquals("Phone number should be correctly retrieved", "1234567890", phoneNumber);
    }

    // ✅ Test Clearing All Items for a User
    @Test
    public void testClearItemsForUser() {
        int userId = registerTestUser();
        dbHelper.insertItem("Item 1", 5, userId);
        dbHelper.insertItem("Item 2", 8, userId);

        dbHelper.clearItemsForUser(userId);
        List<Item> items = dbHelper.getAllItems(userId);
        assertTrue("User's inventory should be empty", items.isEmpty());
    }
}
