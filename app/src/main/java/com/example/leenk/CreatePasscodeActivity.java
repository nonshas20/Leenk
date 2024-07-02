package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreatePasscodeActivity extends AppCompatActivity {

    private EditText[] passcodeInputs;
    private Button[] numberButtons;
    private ImageButton btnBackspace;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode_creation);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupNumberPad();
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
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("passcode").setValue(passcode)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreatePasscodeActivity.this, "Passcode saved successfully", Toast.LENGTH_SHORT).show();
                    completeRegistration();
                })
                .addOnFailureListener(e -> Toast.makeText(CreatePasscodeActivity.this, "Failed to save passcode", Toast.LENGTH_SHORT).show());
    }

    private void completeRegistration() {
        // Set a flag in SharedPreferences to indicate that registration is complete
        getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("isRegistrationComplete", true)
                .apply();

        // Navigate back to MainActivity
        Intent intent = new Intent(CreatePasscodeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}