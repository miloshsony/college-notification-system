package com.college.notificationuser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Button btnSignIn, btnRegister;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        btnSignIn = findViewById(R.id.btn_sign_in);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);

        // Check if already logged in
        checkExistingLogin();

        // Sign In button
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignIn();
            }
        });

        // Register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkExistingLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        // Only auto-login if BOTH Firebase user exists AND SharedPreferences says logged in
        if (currentUser != null && isLoggedIn) {
            // User is already logged in, fetch their role and navigate
            fetchUserRoleAndNavigate(currentUser.getUid());
        }
    }

    private void handleSignIn() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnSignIn.setEnabled(false);

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Fetch user role from database
                                fetchUserRoleAndNavigate(user.getUid());
                            }
                        } else {
                            // Login failed
                            progressBar.setVisibility(View.GONE);
                            btnSignIn.setEnabled(true);
                            Toast.makeText(LoginActivity.this,
                                    "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserRoleAndNavigate(String userId) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                btnSignIn.setEnabled(true);

                if (snapshot.exists()) {
                    String userType = snapshot.child("userType").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    // Save to SharedPreferences for offline access
                    saveUserInfo(email, userType);

                    // Navigate to appropriate dashboard
                    navigateToDashboard(userType);
                } else {
                    Toast.makeText(LoginActivity.this,
                            "User data not found. Please contact admin.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                btnSignIn.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "Database error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo(String email, String userType) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", email);
        editor.putString("userType", userType);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void navigateToDashboard(String userType) {
        Intent intent;
        String message;

        // Check user type and navigate accordingly
        switch (userType) {
            case "ADMIN":
                // Admin goes to AdminActivity (can create notifications)
                intent = new Intent(LoginActivity.this, AdminActivity.class);
                message = "Welcome Admin!";
                break;

            case "STUDENT":
            case "TEACHING_STAFF":
            case "NON_TEACHING_STAFF":
                // All regular users go to MainActivity (view only)
                intent = new Intent(LoginActivity.this, MainActivity.class);
                message = "Welcome!";
                break;

            default:
                // Fallback to MainActivity
                intent = new Intent(LoginActivity.this, MainActivity.class);
                message = "Welcome!";
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

}
