package com.college.notificationuser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    private Button btnCreateNotice, btnViewAllNotices, btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_admin);

            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            // Initialize buttons
            btnCreateNotice = findViewById(R.id.btn_create_notice);
            btnViewAllNotices = findViewById(R.id.btn_view_all_notices);
            btnLogout = findViewById(R.id.btn_logout);

            // Create Notice button
            btnCreateNotice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if Notification.java exists
                    try {
                        Intent intent = new Intent(AdminActivity.this, Notification.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(AdminActivity.this,
                                "Create notification page not available",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // View All Notices button
            btnViewAllNotices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

            // Logout button
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error loading admin page: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void logout() {
        try {
            // Sign out from Firebase
            if (mAuth != null) {
                mAuth.signOut();
            }

            // Clear SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Navigate to login and clear back stack
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Logout error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
