package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CitizenshipQuestionActivity extends AppCompatActivity {

    private Button btnYes, btnNo, btnNext;
    private ImageButton btnClose;
    private ProgressBar progressBar;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String selectedOption = "";
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citizenship_question);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        btnNext = findViewById(R.id.btnNext);
        btnClose = findViewById(R.id.btnClose);
        progressBar = findViewById(R.id.progressBar);

        createNewUser();

        btnYes.setOnClickListener(v -> selectOption("Yes"));
        btnNo.setOnClickListener(v -> selectOption("No"));

        btnNext.setOnClickListener(v -> {
            if (!selectedOption.isEmpty()) {
                saveToDatabase();
                Intent intent = new Intent(CitizenshipQuestionActivity.this, EmailVerificationActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        btnClose.setOnClickListener(v -> finish());

        progressBar.setProgress(20);
    }

    private void createNewUser() {
        String email = "user" + System.currentTimeMillis() + "@example.com";
        String password = "tempPassword" + new Random().nextInt(1000000);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("CitizenshipActivity", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            userId = user.getUid();
                            initializeUserData();
                        }
                    } else {
                        Log.w("CitizenshipActivity", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(CitizenshipQuestionActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initializeUserData() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("accountNumber", generateAccountNumber());
        userData.put("balance", 0);
        userData.put("isFilipinoitizen", "");

        mDatabase.child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> Log.d("CitizenshipActivity", "User data initialized successfully"))
                .addOnFailureListener(e -> Log.w("CitizenshipActivity", "Error initializing user data", e));
    }

    private String generateAccountNumber() {
        // Generate a random 16-digit account number
        Random random = new Random();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void selectOption(String option) {
        selectedOption = option;
        btnYes.setBackgroundResource(option.equals("Yes") ? R.drawable.button_selected : R.drawable.button_background);
        btnNo.setBackgroundResource(option.equals("No") ? R.drawable.button_selected : R.drawable.button_background);
    }

    private void saveToDatabase() {
        mDatabase.child(userId).child("isFilipinoitizen").setValue(selectedOption);
    }
}