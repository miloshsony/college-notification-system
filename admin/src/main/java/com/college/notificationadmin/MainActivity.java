package com.college.notificationadmin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText titleInput, messageInput;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleInput = findViewById(R.id.titleInput);
        messageInput = findViewById(R.id.messageInput);
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

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("notifications");

        String notificationId = databaseRef.push().getKey();
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", message);
        notification.put("timestamp", System.currentTimeMillis());

        if (notificationId != null) {
            databaseRef.child(notificationId).setValue(notification)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this,
                                "Notification sent to all users!", Toast.LENGTH_LONG).show();

                        // Clear input fields
                        titleInput.setText("");
                        messageInput.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this,
                                "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}
