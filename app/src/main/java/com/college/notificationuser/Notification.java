package com.college.notificationuser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Notification extends AppCompatActivity {

    private TextInputEditText titleInput, messageInput;
    private RadioGroup targetAudienceGroup;
    private Button btnSendNotification;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        titleInput = findViewById(R.id.notification_title);
        messageInput = findViewById(R.id.notification_message);
        targetAudienceGroup = findViewById(R.id.target_audience_radio_group);
        btnSendNotification = findViewById(R.id.btn_send_notification);

        btnSendNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });
    }

    private void sendNotification() {
        String title = titleInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(title)) {
            titleInput.setError("Title is required");
            titleInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(message)) {
            messageInput.setError("Message is required");
            messageInput.requestFocus();
            return;
        }

        // Get target audience
        String targetAudience = getTargetAudience();

        // Get current timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Save notification to Firebase
        saveNotificationToFirebase(title, message, targetAudience, timestamp);
    }

    private String getTargetAudience() {
        int selectedId = targetAudienceGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.radio_all_users) {
            return "ALL";
        } else if (selectedId == R.id.radio_students_only) {
            return "STUDENT";
        } else if (selectedId == R.id.radio_teaching_staff_only) {
            return "TEACHING_STAFF";
        } else if (selectedId == R.id.radio_non_teaching_staff_only) {
            return "NON_TEACHING_STAFF";
        }

        return "ALL";
    }

    private void saveNotificationToFirebase(String title, String message, String targetAudience, String timestamp) {
        // Show progress
        btnSendNotification.setEnabled(false);
        btnSendNotification.setText("Sending...");

        // Get admin info
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String senderEmail = userPrefs.getString("email", "Admin");

        // Create notification data
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("targetAudience", targetAudience);
        notification.put("timestamp", timestamp);
        notification.put("sender", senderEmail);

        // Generate unique notification ID
        String notificationId = mDatabase.child("notifications").push().getKey();

        if (notificationId != null) {
            // Save to Firebase
            mDatabase.child("notifications").child(notificationId).setValue(notification)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                        String audienceName = getAudienceName(targetAudience);
                        Toast.makeText(Notification.this,
                                "Notification sent to " + audienceName + "!",
                                Toast.LENGTH_LONG).show();

                        // Clear fields
                        titleInput.setText("");
                        messageInput.setText("");
                        targetAudienceGroup.check(R.id.radio_all_users);

                        // Re-enable button
                        btnSendNotification.setEnabled(true);
                        btnSendNotification.setText("SEND NOTIFICATION");

                        // Go back
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Failed
                        Toast.makeText(Notification.this,
                                "Failed to send notification: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();

                        // Re-enable button
                        btnSendNotification.setEnabled(true);
                        btnSendNotification.setText("SEND NOTIFICATION");
                    });
        } else {
            Toast.makeText(this, "Error generating notification ID", Toast.LENGTH_SHORT).show();
            btnSendNotification.setEnabled(true);
            btnSendNotification.setText("SEND NOTIFICATION");
        }
    }

    private String getAudienceName(String targetAudience) {
        switch (targetAudience) {
            case "ALL":
                return "All Users";
            case "STUDENT":
                return "Students";
            case "TEACHING_STAFF":
                return "Teaching Staff";
            case "NON_TEACHING_STAFF":
                return "Non-Teaching Staff";
            default:
                return "All Users";
        }
    }
}
