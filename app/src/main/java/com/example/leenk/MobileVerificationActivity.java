package com.example.leenk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class MobileVerificationActivity extends AppCompatActivity {

    private EditText etPhoneNumber, etOTP;
    private Button btnVerify;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);

        mAuth = FirebaseAuth.getInstance();

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etOTP = findViewById(R.id.etOTP);
        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setProgress(40); // This is the second step, so 40% progress

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
                        Toast.makeText(MobileVerificationActivity.this, "Verification successful", Toast.LENGTH_SHORT).show();
                        // TODO: Start next activity
                        // startActivity(new Intent(MobileVerificationActivity.this, NextActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MobileVerificationActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}