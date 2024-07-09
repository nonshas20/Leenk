package com.example.leenk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class CreatePasscodeActivity extends AppCompatActivity {

    private EditText[] passcodeInputs;
    private Button[] numberButtons;
    private ImageButton btnBackspace;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String userId;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode_creation);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Retrieve the userId from the intent
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is missing. Please start over.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupNumberPad();
        initializeBiometricPrompt();
    }
    private void initializeViews() {
        passcodeInputs = new EditText[]{
                findViewById(R.id.etPasscode1),
                findViewById(R.id.etPasscode2),
                findViewById(R.id.etPasscode3),
                findViewById(R.id.etPasscode4),
                findViewById(R.id.etPasscode5),
                findViewById(R.id.etPasscode6)
        };

        numberButtons = new Button[]{
                findViewById(R.id.btn0),
                findViewById(R.id.btn1),
                findViewById(R.id.btn2),
                findViewById(R.id.btn3),
                findViewById(R.id.btn4),
                findViewById(R.id.btn5),
                findViewById(R.id.btn6),
                findViewById(R.id.btn7),
                findViewById(R.id.btn8),
                findViewById(R.id.btn9)
        };

        btnBackspace = findViewById(R.id.btnBackspace);
    }

    private void setupNumberPad() {
        for (int i = 0; i < numberButtons.length; i++) {
            final int number = i;
            numberButtons[i].setOnClickListener(v -> addNumber(String.valueOf(number)));
        }

        btnBackspace.setOnClickListener(v -> removeNumber());
    }

    private void addNumber(String number) {
        for (EditText input : passcodeInputs) {
            if (input.getText().toString().isEmpty()) {
                input.setText(number);
                break;
            }
        }
        checkPasscodeCompletion();
    }

    private void removeNumber() {
        for (int i = passcodeInputs.length - 1; i >= 0; i--) {
            if (!passcodeInputs[i].getText().toString().isEmpty()) {
                passcodeInputs[i].setText("");
                break;
            }
        }
    }

    private void checkPasscodeCompletion() {
        StringBuilder passcode = new StringBuilder();
        for (EditText input : passcodeInputs) {
            passcode.append(input.getText().toString());
        }
        if (passcode.length() == 6) {
            savePasscode(passcode.toString());
        }
    }

    private void savePasscode(String passcode) {
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        // Create a map to update multiple children
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("passcode", passcode);
        userUpdates.put("balance", 0.0);
        userUpdates.put("transactions", null); // This creates an empty transactions node

        userRef.updateChildren(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreatePasscodeActivity.this, "Passcode saved successfully", Toast.LENGTH_SHORT).show();
                    completeRegistration();
                })
                .addOnFailureListener(e -> Toast.makeText(CreatePasscodeActivity.this, "Failed to save passcode", Toast.LENGTH_SHORT).show());
    }

    private void completeRegistration() {
        getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("isRegistrationComplete", true)
                .apply();

        // Start fingerprint registration
        startFingerprintRegistration();
    }

    private void initializeBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(CreatePasscodeActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                saveFingerprintData();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Register Fingerprint")
                .setSubtitle("Place your finger on the sensor")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void startFingerprintRegistration() {
        biometricPrompt.authenticate(promptInfo);
    }

    private void saveFingerprintData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("fingerprintEnabled", true);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreatePasscodeActivity.this, "Fingerprint registered successfully", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreatePasscodeActivity.this, "Failed to register fingerprint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(CreatePasscodeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}