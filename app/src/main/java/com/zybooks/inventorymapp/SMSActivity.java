

package com.zybooks.inventorymapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SMSActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100; // Request code for SMS permission
    private EditText etPhoneNumber, etMessage;
    private Button btnSendSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        // Initialize UI elements
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etMessage = findViewById(R.id.etMessage);
        btnSendSMS = findViewById(R.id.btnSendSMS);

        // Apply phone number auto-formatting as the user types
        etPhoneNumber.addTextChangedListener(new PhoneNumberTextWatcher());

        // Set SMS send button listener
        btnSendSMS.setOnClickListener(v -> {
            if (checkSMSPermission()) {
                sendSMS();
            } else {
                requestSMSPermission();
            }
        });
    }

    /**
     * Checks if the SMS permission is granted.
     * @return true if permission is granted, false otherwise.
     */
    private boolean checkSMSPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests SMS permission from the user if not granted.
     */
    private void requestSMSPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
    }

    /**
     * Sends an SMS message if the phone number and message fields are valid.
     */
    private void sendSMS() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (!phoneNumber.isEmpty() && !message.isEmpty()) {
            // Validate phone number format (XXX-XXX-XXXX)
            if (!phoneNumber.matches("\\d{3}-\\d{3}-\\d{4}")) {
                Toast.makeText(this, "Invalid phone number format!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, "SMS Sent Successfully!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to Send SMS", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Enter both Phone Number and Message", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles permission request results for SMS sending.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMS(); // Automatically send SMS after permission is granted
            } else {
                Toast.makeText(this, "SMS Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Inner class to handle phone number formatting (XXX-XXX-XXXX).
     */
    private class PhoneNumberTextWatcher implements TextWatcher {
        private boolean isFormatting;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isFormatting) return;
            isFormatting = true;

            // Remove existing dashes
            String number = s.toString().replaceAll("-", "");

            // Apply formatting (XXX-XXX-XXXX)
            StringBuilder formatted = new StringBuilder();
            int length = number.length();
            for (int i = 0; i < length; i++) {
                formatted.append(number.charAt(i));
                if ((i == 2 || i == 5) && i < length - 1) {
                    formatted.append("-");
                }
            }

            s.replace(0, s.length(), formatted.toString());
            isFormatting = false;
        }
    }
}
