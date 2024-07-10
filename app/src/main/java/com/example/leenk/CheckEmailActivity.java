package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CheckEmailActivity extends AppCompatActivity {

    private Button btnResendEmail;
    private Button btnContinue;
    private FirebaseAuth mAuth;
    private String userId;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_email);

        mAuth = FirebaseAuth.getInstance();
        userId = getIntent().getStringExtra("USER_ID");
        userEmail = getIntent().getStringExtra("EMAIL");

        btnResendEmail = findViewById(R.id.btnResendEmail);
        btnContinue = findViewById(R.id.btnContinue);

        btnResendEmail.setOnClickListener(v -> resendVerificationEmail());
        btnContinue.setOnClickListener(v -> checkEmailVerification());
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(CheckEmailActivity.this, "Verification email resent. Please check your inbox.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(CheckEmailActivity.this, "Failed to resend verification email. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(CheckEmailActivity.this, "No user signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkEmailVerification() {
        mAuth.signInWithEmailAndPassword(userEmail, "temporary_password")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.reload().addOnCompleteListener(reloadTask -> {
                                if (reloadTask.isSuccessful()) {
                                    if (user.isEmailVerified()) {
                                        // Email is verified, proceed to the next activity
                                        Toast.makeText(CheckEmailActivity.this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
                                        // TODO: Update user's password here
                                        // TODO: Replace NextActivity.class with your next activity
                                        Intent intent = new Intent(CheckEmailActivity.this, ScanIdSplashActivity.class);
                                        intent.putExtra("USER_ID", userId);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(CheckEmailActivity.this, "Email is not verified yet. Please check your inbox and verify your email.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(CheckEmailActivity.this, "Failed to check email verification status. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(CheckEmailActivity.this, "Failed to sign in. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}