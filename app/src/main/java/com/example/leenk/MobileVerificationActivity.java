package com.example.leenk;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.content.Intent;
import android.widget.Toast;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class MobileVerificationActivity extends AppCompatActivity {

    private EditText etPhoneNumber, etOTP;
    private Button btnVerify;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        userId = getIntent().getStringExtra("USER_ID");

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etOTP = findViewById(R.id.etOTP);
        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setProgress(40);

        btnBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> {
            if (etOTP.getVisibility() == View.VISIBLE) {
                verifyOTP();
            } else {
                sendOTP();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(MobileVerificationActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String vId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                verificationId = vId;
                Toast.makeText(MobileVerificationActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                etOTP.setVisibility(View.VISIBLE);
                btnVerify.setText("Verify OTP");
            }
        };
    }

    private void sendOTP() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError("Phone number is required");
            etPhoneNumber.requestFocus();
            return;
        }

        // Add your country code here
        phoneNumber = "+63" + phoneNumber; // Example for US numbers

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOTP() {
        String otp = etOTP.getText().toString().trim();
        if (otp.isEmpty()) {
            etOTP.setError("OTP is required");
            etOTP.requestFocus();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Get the verified phone number
                        String phoneNumber = etPhoneNumber.getText().toString().trim();

                        // Save the phone number to the database
                        savePhoneNumberToDatabase(phoneNumber);

                        Toast.makeText(MobileVerificationActivity.this, "Verification successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MobileVerificationActivity.this, EmailVerificationActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MobileVerificationActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void savePhoneNumberToDatabase(String phoneNumber) {
        mDatabase.child(userId).child("phoneNumber").setValue(phoneNumber)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MobileVerificationActivity.this, "Phone number saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MobileVerificationActivity.this, EmailVerificationActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MobileVerificationActivity.this, "Failed to save phone number", Toast.LENGTH_SHORT).show();
                });
    }
}