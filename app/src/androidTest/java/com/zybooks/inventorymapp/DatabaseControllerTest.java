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

public class DatabaseControllerTest {

    private DatabaseController dbController;
    private DatabaseHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        dbController = new DatabaseController(context);

        // Ensure database is created before clearing it
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.close();

        dbController.clearDatabase(); // Clean database before tests
    }

    // ✅ Helper method to register user and get userId
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

    // ✅ Test inserting an item
    @Test
    public void testInsertItem() {
        int userId = registerTestUser();
        boolean success = dbController.addItem("Test Item", 5, userId);
        assertTrue("Item should be inserted", success);
    }

    // ✅ Test retrieving items
    @Test
    public void testRetrieveItems() {
        int userId = registerTestUser();
        dbController.addItem("Item 1", 10, userId);
        dbController.addItem("Item 2", 20, userId);
        List<Item> items = dbController.getAllItems(userId);

        assertNotNull(items);
        assertEquals(2, items.size());
    }

    // ✅ Test updating an item
    @Test
    public void testUpdateItem() {
        int userId = registerTestUser();
        dbController.addItem("Old Name", 5, userId);
        List<Item> items = dbController.getAllItems(userId);

        assertFalse(items.isEmpty());

        int itemId = items.get(0).getId();
        boolean updated = dbController.updateItem(itemId, "Updated Name", 15, userId);
        assertTrue("Item should be updated", updated);
    }

    // ✅ Test deleting an item
    @Test
    public void testDeleteItem() {
        int userId = registerTestUser();
        dbController.addItem("Item to Delete", 7, userId);
        List<Item> items = dbController.getAllItems(userId);

        assertFalse(items.isEmpty());

        int itemId = items.get(0).getId();
        boolean deleted = dbController.deleteItem(itemId, userId);
        assertTrue("Item should be deleted", deleted);
    }

    // ✅ Test clearing items for a user
    @Test
    public void testClearItemsForUser() {
        int userId = registerTestUser();
        dbController.addItem("Item 1", 5, userId);
        dbController.addItem("Item 2", 8, userId);

        dbHelper.clearItemsForUser(userId);
        List<Item> items = dbController.getAllItems(userId);

        assertTrue("User's inventory should be empty", items.isEmpty());
    }

    // ✅ Test enabling SMS notifications
    @Test
    public void testEnableSmsNotifications() {
        int userId = registerTestUser();
        boolean result = dbController.updateSmsPreference(userId, true);
        assertTrue("SMS preference should be updated to true", result);

        boolean isEnabled = dbHelper.getSmsPreference(userId);
        assertTrue("SMS should be enabled", isEnabled);
    }

    // ✅ Test disabling SMS notifications
    @Test
    public void testDisableSmsNotifications() {
        int userId = registerTestUser();
        dbController.updateSmsPreference(userId, true);
        dbController.updateSmsPreference(userId, false);

        boolean isEnabled = dbHelper.getSmsPreference(userId);
        assertFalse("SMS should be disabled", isEnabled);
    }

    // ✅ Test retrieving user phone number
    @Test
    public void testRetrieveUserPhoneNumber() {
        int userId = registerTestUser();
        String phoneNumber = dbHelper.getUserPhoneNumber(userId);
        assertEquals("Phone number should be correctly retrieved", "1234567890", phoneNumber);
    }

    // ✅ Test clearing all users
    @Test
    public void testClearUsersTable() {
        dbHelper.registerUser("testuser", "password123", "1234567890");
        dbHelper.clearUsersTable();

        int userId = dbHelper.authenticateUser("testuser", "password123");
        assertEquals("User should not exist after table is cleared", -1, userId);
    }

    @After
    public void tearDown() {
        dbHelper.clearUsersTable(); // Clean up database after tests
    }
}
