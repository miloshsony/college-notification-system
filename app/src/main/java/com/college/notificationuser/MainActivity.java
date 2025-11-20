package com.college.notificationuser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private TextView emptyView;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "Layout set successfully");

            // Initialize Firebase
            try {
                mAuth = FirebaseAuth.getInstance();
                mDatabase = FirebaseDatabase.getInstance().getReference();
                Log.d(TAG, "Firebase initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Firebase initialization error: " + e.getMessage());
                Toast.makeText(this, "Firebase error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            // Initialize views
            try {
                recyclerView = findViewById(R.id.notifications_recycler_view);
                emptyView = findViewById(R.id.empty_view);
                btnLogout = findViewById(R.id.btn_logout);
                Log.d(TAG, "Views initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "View initialization error: " + e.getMessage());
                Toast.makeText(this, "View error: Check activity_main.xml IDs", Toast.LENGTH_LONG).show();
                return;
            }

            // Initialize notification list
            notificationList = new ArrayList<>();
            adapter = new NotificationAdapter(notificationList);

            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            // Load notifications from Firebase
            loadNotificationsFromFirebase();

            // Setup logout button
            if (btnLogout != null) {
                btnLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logout();
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "onCreate error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadNotificationsFromFirebase();
        } catch (Exception e) {
            Log.e(TAG, "onResume error: " + e.getMessage());
        }
    }

    private void loadNotificationsFromFirebase() {
        try {
            // Get current user type from SharedPreferences
            SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String currentUserType = userPrefs.getString("userType", "STUDENT");
            Log.d(TAG, "Current user type: " + currentUserType);

            // Show loading
            if (emptyView != null) {
                emptyView.setText("Loading notifications...");
                emptyView.setVisibility(View.VISIBLE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }

            // Check if Firebase is available
            if (mDatabase == null) {
                Log.e(TAG, "Firebase Database is null");
                if (emptyView != null) {
                    emptyView.setText("Database connection error");
                }
                return;
            }

            // Fetch notifications from Firebase
            mDatabase.child("notifications").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Data received from Firebase");

                    // Clear existing list
                    notificationList.clear();

                    // Loop through all notifications
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            String title = snapshot.child("title").getValue(String.class);
                            String message = snapshot.child("message").getValue(String.class);
                            String targetAudience = snapshot.child("targetAudience").getValue(String.class);
                            String timestamp = snapshot.child("timestamp").getValue(String.class);
                            String sender = snapshot.child("sender").getValue(String.class);

                            Log.d(TAG, "Notification: " + title + " - Target: " + targetAudience);

                            // Filter notifications based on user type
                            if (targetAudience != null && (targetAudience.equals("ALL") || targetAudience.equals(currentUserType))) {
                                NotificationModel notification = new NotificationModel(
                                        title != null ? title : "No Title",
                                        message != null ? message : "No Message",
                                        timestamp != null ? timestamp : "Unknown",
                                        targetAudience,
                                        sender != null ? sender : "Admin"
                                );
                                notificationList.add(notification);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing notification: " + e.getMessage());
                        }
                    }

                    Log.d(TAG, "Total notifications loaded: " + notificationList.size());

                    // Update UI
                    if (notificationList.isEmpty()) {
                        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
                        if (emptyView != null) {
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText("No notifications yet.\nCheck back later!");
                        }
                    } else {
                        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
                        if (emptyView != null) emptyView.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase error: " + error.getMessage());
                    Toast.makeText(MainActivity.this,
                            "Failed to load notifications: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    if (emptyView != null) {
                        emptyView.setText("Failed to load notifications.\nPlease try again.");
                        emptyView.setVisibility(View.VISIBLE);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "loadNotificationsFromFirebase error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading notifications: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

            // Navigate to login and clear back stack
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Logout error: " + e.getMessage());
            Toast.makeText(this, "Logout error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
