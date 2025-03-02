package com.zybooks.inventorymapp;

import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTest {
    private DatabaseHelper dbHelper;
    private Context context = ApplicationProvider.getApplicationContext();

    @Before
    public void setUp() {
        context.deleteDatabase("inventory.db"); // Reset DB before each test
        dbHelper = new DatabaseHelper(context);
        dbHelper.clearUsersTable(); // Ensure clean database before tests
    }

    @After
    public void tearDown() {
        dbHelper.clearUsersTable(); // Clean up database after tests
        dbHelper.close();
    }

    // ✅ Helper Method to Register a Test User and Get userId
    private int registerTestUser() {
        boolean success = dbHelper.registerUser("testuser", "password123", "1234567890"); // ✅ Include phone number
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

    // ✅ Test User Registration Success
    @Test
    public void testRegisterUser_Success() {
        boolean result = dbHelper.registerUser("testuser", "password123", "9876543210");
        assertTrue("User should be registered successfully", result);
    }

    // ❌ Test Registering Duplicate Username
    @Test
    public void testRegisterUser_DuplicateUsername() {
        dbHelper.registerUser("testuser", "password123", "9876543210");
        boolean result = dbHelper.registerUser("testuser", "newpassword", "1234567890");
        assertFalse("Duplicate username should not be allowed", result);
    }

    // ✅ Test User Login Success
    @Test
    public void testLoginUser_Success() {
        dbHelper.registerUser("validUser", "securePass", "5555555555");
        int userId = dbHelper.authenticateUser("validUser", "securePass");
        assertNotEquals("User should login successfully", -1, userId);
    }

    // ❌ Test Login with Wrong Password
    @Test
    public void testLoginUser_WrongPassword() {
        dbHelper.registerUser("user1", "correctPass", "5555555555");
        int userId = dbHelper.authenticateUser("user1", "wrongPass");
        assertEquals("Login should fail with incorrect password", -1, userId);
    }

    // ❌ Test Login for Non-Existent User
    @Test
    public void testLoginUser_NonExistentUser() {
        int userId = dbHelper.authenticateUser("nonExistentUser", "anyPass");
        assertEquals("Non-existent user should not be able to log in", -1, userId);
    }

    // ✅ Test Retrieving User Phone Number
    @Test
    public void testRetrieveUserPhoneNumber() {
        int userId = registerTestUser();
        String phoneNumber = dbHelper.getUserPhoneNumber(userId);
        assertEquals("Phone number should be correctly retrieved", "1234567890", phoneNumber);
    }

    // ✅ Test Enabling SMS Notifications for a User
    @Test
    public void testEnableSmsNotifications() {
        int userId = registerTestUser();
        boolean result = dbHelper.updateSmsPreference(userId, true);
        assertTrue("SMS preference should be updated to true", result);

        boolean isEnabled = dbHelper.getSmsPreference(userId);
        assertTrue("SMS should be enabled", isEnabled);
    }

    // ✅ Test Disabling SMS Notifications for a User
    @Test
    public void testDisableSmsNotifications() {
        int userId = registerTestUser();
        dbHelper.updateSmsPreference(userId, true);
        dbHelper.updateSmsPreference(userId, false);

        boolean isEnabled = dbHelper.getSmsPreference(userId);
        assertFalse("SMS should be disabled", isEnabled);
    }

    // ✅ Test Inserting Items
    @Test
    public void testInsertItem() {
        int userId = registerTestUser();
        boolean result = dbHelper.insertItem("Mouse", 15, userId);
        assertTrue("Item should be inserted", result);
    }

    // ✅ Test Retrieving Items
    @Test
    public void testRetrieveItems() {
        int userId = registerTestUser();
        dbHelper.insertItem("Keyboard", 10, userId);
        List<Item> items = dbHelper.getAllItems(userId);
        assertFalse("Item list should not be empty", items.isEmpty());
    }

    // ✅ Test Updating an Item
    @Test
    public void testUpdateItem() {
        int userId = registerTestUser();
        dbHelper.insertItem("Monitor", 5, userId);
        List<Item> items = dbHelper.getAllItems(userId);
        int id = items.get(0).getId();
        boolean result = dbHelper.updateItem(id, "Updated Monitor", 8, userId);
        assertTrue("Item should be updated", result);
    }

    // ✅ Test Deleting an Item
    @Test
    public void testDeleteItem() {
        int userId = registerTestUser();
        dbHelper.insertItem("Webcam", 7, userId);
        List<Item> items = dbHelper.getAllItems(userId);
        int id = items.get(0).getId();
        boolean result = dbHelper.deleteItem(id, userId);
        assertTrue("Item should be deleted", result);
    }

    // ✅ Test Clearing Items Table for a User
    @Test
    public void testClearItemsForUser() {
        int userId = registerTestUser();
        dbHelper.insertItem("Headphones", 5, userId);
        dbHelper.clearItemsForUser(userId);
        List<Item> items = dbHelper.getAllItems(userId);
        assertTrue("User's inventory should be empty", items.isEmpty());
    }

    // ✅ Test Clearing Users Table
    @Test
    public void testClearUsersTable() {
        dbHelper.registerUser("testuser", "password123", "9876543210");
        dbHelper.clearUsersTable();
        int userId = dbHelper.authenticateUser("testuser", "password123");
        assertEquals("User should not exist after table is cleared", -1, userId);
    }
}
