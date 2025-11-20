package com.college.notificationadmin;

import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private TextInputEditText titleInput, messageInput;
    private RadioGroup targetAudienceGroup;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        titleInput = findViewById(R.id.titleInput);
        messageInput = findViewById(R.id.messageInput);
        targetAudienceGroup = findViewById(R.id.targetAudienceGroup);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
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
        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            titleInput.requestFocus();
            return;
        }

        if (message.isEmpty()) {
            messageInput.setError("Message is required");
            messageInput.requestFocus();
            return;
        }

        // Get selected target audience
        String targetAudience = getTargetAudience();

        // Get current timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Save to Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("notifications");

        String notificationId = databaseRef.push().getKey();
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("targetAudience", targetAudience);
        notification.put("timestamp", timestamp);
        notification.put("sender", "Admin");

        if (notificationId != null) {
            databaseRef.child(notificationId).setValue(notification)
                    .addOnSuccessListener(aVoid -> {
                        String audienceName = getAudienceName(targetAudience);
                        Toast.makeText(MainActivity.this,
                                "Notification sent to " + audienceName + "!",
                                Toast.LENGTH_LONG).show();

                        // Clear input fields
                        titleInput.setText("");
                        messageInput.setText("");
                        targetAudienceGroup.check(R.id.radioAllUsers); // Reset to "All Users"
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        }
    }

    private String getTargetAudience() {
        int selectedId = targetAudienceGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.radioAllUsers) {
            return "ALL";
        } else if (selectedId == R.id.radioStudents) {
            return "STUDENT";
        } else if (selectedId == R.id.radioTeachingStaff) {
            return "TEACHING_STAFF";
        } else if (selectedId == R.id.radioNonTeachingStaff) {
            return "NON_TEACHING_STAFF";
        }

        return "ALL"; // Default
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
