package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EmailVerificationActivity extends AppCompatActivity {

    private EditText etEmailAddress;
    private Button btnSubmit;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        userId = getIntent().getStringExtra("USER_ID");

        etEmailAddress = findViewById(R.id.etEmailAddress);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setProgress(60); // This is the third step, so 60% progress

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> submitEmail());
    }

    private void submitEmail() {
        String email = etEmailAddress.getText().toString().trim();
        if (email.isEmpty()) {
            etEmailAddress.setError("Email address is required");
            etEmailAddress.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Ensure userId is not null
            if (userId == null) {
                Toast.makeText(EmailVerificationActivity.this, "User ID is null", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the email in the database
            mDatabase.child(userId).child("email").setValue(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailVerificationActivity.this, "Email saved successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EmailVerificationActivity.this, ScanIdSplashActivity.class);
                            intent.putExtra("USER_ID", userId);
                            startActivity(intent);
                        } else {
                            Toast.makeText(EmailVerificationActivity.this, "Failed to save email", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(EmailVerificationActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

}
