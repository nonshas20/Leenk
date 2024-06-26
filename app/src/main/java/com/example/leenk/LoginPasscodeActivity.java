package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginPasscodeActivity extends AppCompatActivity {

    private EditText[] passcodeInputs;
    private Button[] numberButtons;
    private ImageButton btnBackspace;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_passcode);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getIntent().getStringExtra("USER_ID");

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
            verifyPasscode(passcode.toString());
        }
    }

    private void verifyPasscode(String enteredPasscode) {
        mDatabase.child("users").child(userId).child("passcode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedPasscode = dataSnapshot.getValue(String.class);
                    if (storedPasscode != null && storedPasscode.equals(enteredPasscode)) {
                        // Passcode is correct, proceed to home page dashboard
                        Toast.makeText(LoginPasscodeActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginPasscodeActivity.this, HomeDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Incorrect passcode
                        Toast.makeText(LoginPasscodeActivity.this, "Incorrect passcode", Toast.LENGTH_SHORT).show();
                        clearPasscodeInputs();
                    }
                } else {
                    Toast.makeText(LoginPasscodeActivity.this, "Error: Passcode not found", Toast.LENGTH_SHORT).show();
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
}