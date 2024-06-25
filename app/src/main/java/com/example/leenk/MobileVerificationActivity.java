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

    private EditText etPhoneNumber;
    private Button btnVerify;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);

        mAuth = FirebaseAuth.getInstance();

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setProgress(40); // This is the second step, so 40% progress

        btnBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            if (phoneNumber.isEmpty()) {
                etPhoneNumber.setError("Phone number is required");
                etPhoneNumber.requestFocus();
                return;
            }

            // Add your country code here
            phoneNumber = "+1" + phoneNumber; // Example for US numbers

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(mCallbacks)
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(MobileVerificationActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Toast.makeText(MobileVerificationActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                // You might want to start a new activity here to enter the OTP
                // startActivity(new Intent(MobileVerificationActivity.this, OTPVerificationActivity.class));
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(MobileVerificationActivity.this, "Verification successful", Toast.LENGTH_SHORT).show();
                        // You might want to start a new activity here
                        // startActivity(new Intent(MobileVerificationActivity.this, NextActivity.class));
                        finish();
                    } else {
                        // Sign in failed, display a message and update the UI
                        Toast.makeText(MobileVerificationActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}