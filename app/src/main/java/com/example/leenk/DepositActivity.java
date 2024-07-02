package com.example.leenk;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class DepositActivity extends AppCompatActivity {

    private TextInputEditText etAmount;
    private RadioGroup rgPaymentMethod;
    private Button btnConfirmDeposit;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etAmount = findViewById(R.id.etAmount);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        btnConfirmDeposit = findViewById(R.id.btnConfirmDeposit);
    }

    private void setupClickListeners() {
        btnConfirmDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountStr = etAmount.getText().toString();
                Double amount = amountStr.isEmpty() ? null : Double.parseDouble(amountStr);
                int selectedPaymentMethodId = rgPaymentMethod.getCheckedRadioButtonId();

                if (amount == null || amount <= 0) {
                    Toast.makeText(DepositActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                } else if (selectedPaymentMethodId == -1) {
                    Toast.makeText(DepositActivity.this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                } else {
                    String paymentMethod;
                    if (selectedPaymentMethodId == R.id.rbGcash) {
                        paymentMethod = "GCash";
                    } else if (selectedPaymentMethodId == R.id.rbPayMaya) {
                        paymentMethod = "PayMaya";
                    } else if (selectedPaymentMethodId == R.id.rbBankTransfer) {
                        paymentMethod = "Bank Transfer";
                    } else {
                        paymentMethod = "Unknown";
                    }
                    processDeposit(amount, paymentMethod);
                }
            }
        });
    }

    private void processDeposit(final double amount, final String paymentMethod) {
        mDatabase.child("users").child(userId).child("balance").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Double currentBalance = mutableData.getValue(Double.class);
                if (currentBalance == null) {
                    currentBalance = 0.0;
                }
                mutableData.setValue(currentBalance + amount);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (committed) {
                    addTransaction(amount, "deposit", paymentMethod);
                    Toast.makeText(DepositActivity.this, "Deposit successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DepositActivity.this, "Deposit failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addTransaction(double amount, String type, String paymentMethod) {
        String transactionId = mDatabase.child("users").child(userId).child("transactions").push().getKey();
        UserTransaction transaction = new UserTransaction(type, amount, System.currentTimeMillis(), paymentMethod);

        if (transactionId != null) {
            mDatabase.child("users").child(userId).child("transactions").child(transactionId).setValue(transaction)
                    .addOnSuccessListener(aVoid -> Toast.makeText(DepositActivity.this, "Transaction recorded", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(DepositActivity.this, "Failed to record transaction", Toast.LENGTH_SHORT).show());
        }
    }
}