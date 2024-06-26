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

public class CreateUsernameActivity extends AppCompatActivity {

    private EditText etUsername;
    private Button btnSubmit;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_username);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etUsername = findViewById(R.id.etUsername);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setProgress(80); // This is the fourth step, so 80% progress

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> submitUsername());
    }

    private void submitUsername() {
        String username = etUsername.getText().toString().trim();
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child("users").child(userId).child("username").setValue(username)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateUsernameActivity.this, "Username saved successfully", Toast.LENGTH_SHORT).show();
                            // Navigate to the next activity (passcode creation)
                            // startActivity(new Intent(CreateUsernameActivity.this, CreatePasscodeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(CreateUsernameActivity.this, "Failed to save username", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(CreateUsernameActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}