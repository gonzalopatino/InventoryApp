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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        dbHelper = new DatabaseHelper(this);

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToMainActivity();
        }

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> navigateToRegisterActivity());
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        int userId = dbHelper.authenticateUser(username, password);
        if (userId != -1) {
            SharedPreferences preferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("is_logged_in", true);
            editor.putInt("userId", userId);
            editor.putString("username", username);
            editor.apply();

            // Redirect to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private boolean isUserLoggedIn() {
        SharedPreferences preferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return preferences.contains("userId");
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
