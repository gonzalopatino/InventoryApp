package com.zybooks.inventorymapp;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoginTest {
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
        dbHelper.close();
    }

    // ✅ Test User Registration Success
    @Test
    public void testRegisterUser_Success() {
        boolean result = dbHelper.registerUser("testUser", "password123", "1234567890");
        assertTrue("User should be registered successfully", result);
    }

    // ❌ Test Registering Duplicate Username
    @Test
    public void testRegisterUser_DuplicateUsername() {
        dbHelper.registerUser("duplicateUser", "password1", "1234567890");
        boolean result = dbHelper.registerUser("duplicateUser", "password2", "0987654321");
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

    // ✅ Test Enabling SMS Notifications for a User
    @Test
    public void testEnableSmsNotifications() {
        dbHelper.registerUser("smsUser", "smsPass", "5555555555");
        int userId = dbHelper.authenticateUser("smsUser", "smsPass");

        boolean updated = dbHelper.updateSmsPreference(userId, true);
        assertTrue("SMS preference should be updated to true", updated);

        boolean isEnabled = dbHelper.getSmsPreference(userId);
        assertTrue("SMS should be enabled", isEnabled);
    }

    // ✅ Test Disabling SMS Notifications for a User
    @Test
    public void testDisableSmsNotifications() {
        dbHelper.registerUser("smsUser", "smsPass", "5555555555");
        int userId = dbHelper.authenticateUser("smsUser", "smsPass");

        dbHelper.updateSmsPreference(userId, true);
        dbHelper.updateSmsPreference(userId, false);

        boolean isEnabled = dbHelper.getSmsPreference(userId);
        assertFalse("SMS should be disabled", isEnabled);
    }

    // ✅ Test Retrieving User Phone Number
    @Test
    public void testRetrieveUserPhoneNumber() {
        dbHelper.registerUser("phoneUser", "phonePass", "9876543210");
        int userId = dbHelper.authenticateUser("phoneUser", "phonePass");

        String phoneNumber = dbHelper.getUserPhoneNumber(userId);
        assertEquals("Phone number should be correctly retrieved", "9876543210", phoneNumber);
    }
}
