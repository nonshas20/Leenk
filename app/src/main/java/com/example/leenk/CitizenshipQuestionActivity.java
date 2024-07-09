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

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.drawable.ColorDrawable;
import android.view.View;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Calendar;

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
                if (userId != null) {
                    saveToDatabase();
                    Intent intent = new Intent(CitizenshipQuestionActivity.this, MobileVerificationActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                } else {
                    Log.e("CitizenshipActivity", "userId is null. Cannot proceed to the next activity.");
                    Toast.makeText(CitizenshipQuestionActivity.this, "User ID is not initialized. Please try again.", Toast.LENGTH_SHORT).show();
                }
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
        if (userId != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("accountNumber", generateAccountNumber());
            userData.put("balance", 0.00);
            userData.put("isFilipinoCitizen", ""); // Corrected key
            userData.put("expirationDate", generateExpirationDate());
            userData.put("cvv", generateCVV());

            mDatabase.child(userId).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("CitizenshipActivity", "User data initialized successfully");
                        verifyAccountInitialization();
                    })
                    .addOnFailureListener(e -> Log.w("CitizenshipActivity", "Error initializing user data", e));
        } else {
            Log.e("CitizenshipActivity", "userId is null. Cannot initialize user data.");
        }
    }

    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 3); // Set expiration to 3 years from now
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        int year = calendar.get(Calendar.YEAR) % 100; // Get last two digits of year
        return String.format("%02d/%02d", month, year);
    }

    private String generateCVV() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }

    private void verifyAccountInitialization() {
        mDatabase.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> userData = (Map<String, Object>) task.getResult().getValue();
                if (userData != null) {
                    Log.d("CitizenshipActivity", "Account details verified: " +
                            "AccountNumber: " + userData.get("accountNumber") +
                            ", ExpirationDate: " + userData.get("expirationDate") +
                            ", CVV: " + userData.get("cvv"));
                } else {
                    Log.e("CitizenshipActivity", "User data is null after initialization");
                }
            } else {
                Log.e("CitizenshipActivity", "Error verifying account initialization", task.getException());
            }
        });
    }




    private void selectOption(String option) {
        selectedOption = option;

        int colorSelected = getResources().getColor(R.color.button_background       );
        int colorDefault = getResources().getColor(R.color.button_selected);

        if (option.equals("Yes")) {
            btnYes.setBackgroundColor(colorSelected);
            btnNo.setBackgroundColor(colorDefault);
        } else {
            btnYes.setBackgroundColor(colorDefault);
            btnNo.setBackgroundColor(colorSelected);
        }
    }



    private void saveToDatabase() {
        if (userId != null) {
            mDatabase.child(userId).child("isFilipinoCitizen").setValue(selectedOption)
                    .addOnSuccessListener(aVoid -> Log.d("CitizenshipActivity", "Citizenship status saved successfully"))
                    .addOnFailureListener(e -> Log.w("CitizenshipActivity", "Error saving citizenship status", e));
        } else {
            Log.e("CitizenshipActivity", "userId is null. Cannot save citizenship status.");
        }
    }
}