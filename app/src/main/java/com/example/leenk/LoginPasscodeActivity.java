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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;
import java.util.concurrent.Executor;

public class LoginPasscodeActivity extends AppCompatActivity {
    private EditText[] passcodeInputs;
    private Button[] numberButtons;
    private ImageButton btnBackspace;
    private DatabaseReference mDatabase;
    private String userId;

    private ImageButton btnFaceLogin;
    private ImageButton btnFingerprintLogin;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_passcode);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getIntent().getStringExtra("USER_ID");

        initializeViews();
        setupNumberPad();
        setupBiometricOptions();
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
        btnFaceLogin = findViewById(R.id.btnFaceLogin);
        btnFingerprintLogin = findViewById(R.id.btnFingerprintLogin);
    }
    private void setupBiometricOptions() {
        btnFaceLogin.setOnClickListener(v -> startFaceLogin());
        btnFingerprintLogin.setOnClickListener(v -> startFingerprintLogin());

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(LoginPasscodeActivity.this,
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
                proceedToHomeDashboard();
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
                .setTitle("Login with Fingerprint")
                .setSubtitle("Place your finger on the sensor")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void startFaceLogin() {
        // Start the face recognition activity
        Intent intent = new Intent(LoginPasscodeActivity.this, FaceLoginActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    private void startFingerprintLogin() {
        biometricPrompt.authenticate(promptInfo);
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
            verifyPasscode(passcode.toString());
        }
    }

    private void verifyPasscode(String enteredPasscode) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedPasscode = dataSnapshot.child("passcode").getValue(String.class);
                    if (storedPasscode != null && storedPasscode.equals(enteredPasscode)) {
                        // Passcode is correct
                        if (!dataSnapshot.hasChild("accountNumber")) {
                            // First time login, initialize account details
                            initializeAccountDetails();
                        } else {
                            // Proceed to home page dashboard
                            proceedToHomeDashboard();
                        }
                    } else {
                        // Incorrect passcode
                        Toast.makeText(LoginPasscodeActivity.this, "Incorrect passcode", Toast.LENGTH_SHORT).show();
                        clearPasscodeInputs();
                    }
                } else {
                    Toast.makeText(LoginPasscodeActivity.this, "Error: User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginPasscodeActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearPasscodeInputs() {
        for (EditText input : passcodeInputs) {
            input.setText("");
        }
    }
    private void initializeAccountDetails() {
        String accountNumber = generateAccountNumber();
        String expirationDate = generateExpirationDate();
        String cvv = generateCVV();

        Map<String, Object> accountDetails = new HashMap<>();
        accountDetails.put("balance", 0.00);
        accountDetails.put("accountNumber", accountNumber);
        accountDetails.put("expirationDate", expirationDate);
        accountDetails.put("cvv", cvv);

        mDatabase.child("users").child(userId).updateChildren(accountDetails)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoginPasscodeActivity.this, "Account initialized successfully", Toast.LENGTH_SHORT).show();
                    proceedToHomeDashboard();
                })
                .addOnFailureListener(e -> Toast.makeText(LoginPasscodeActivity.this, "Failed to initialize account", Toast.LENGTH_SHORT).show());
    }

    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
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

    private void proceedToHomeDashboard() {
        Intent intent = new Intent(LoginPasscodeActivity.this, HomeDashboardActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}
