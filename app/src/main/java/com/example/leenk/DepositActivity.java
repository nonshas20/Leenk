package com.example.leenk;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class DepositActivity extends AppCompatActivity {

    private EditText etDepositAmount;
    private Button btnConfirmDeposit;
    private TextView tvCurrentBalance, tvAccountNumber, tvAccountName;
    private ImageView ivQRCode;
    private CardView btnGCash, btnPayMaya;
    private DatabaseReference mDatabase;
    private String userId;
    private String selectedPaymentMethod = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getIntent().getStringExtra("USER_ID");

        initializeViews();
        setupClickListeners();
        fetchAccountDetails();
    }

    private void initializeViews() {
        etDepositAmount = findViewById(R.id.etDepositAmount);
        btnConfirmDeposit = findViewById(R.id.btnConfirmDeposit);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvAccountName = findViewById(R.id.tvAccountName);
        ivQRCode = findViewById(R.id.ivQRCode);
        btnGCash = findViewById(R.id.btnGCash);
        btnPayMaya = findViewById(R.id.btnPayMaya);
    }

    private void setupClickListeners() {
        btnConfirmDeposit.setOnClickListener(v -> confirmDeposit());
        btnGCash.setOnClickListener(v -> selectPaymentMethod("GCash"));
        btnPayMaya.setOnClickListener(v -> selectPaymentMethod("PayMaya"));
    }
    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;
        // You can add visual feedback here to show which method is selected
        Toast.makeText(this, method + " selected", Toast.LENGTH_SHORT).show();
    }

    private void confirmDeposit() {
        String amountStr = etDepositAmount.getText().toString();
        Double amount = amountStr.isEmpty() ? null : Double.parseDouble(amountStr);

        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
        } else if (selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
        } else {
            processDeposit(amount, selectedPaymentMethod);
        }
    }










    private void fetchAccountDetails() {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String accountNumber = dataSnapshot.child("accountNumber").getValue(String.class);
                String firstName = dataSnapshot.child("basic_info").child("first_name").getValue(String.class);
                String lastName = dataSnapshot.child("basic_info").child("last_name").getValue(String.class);
                Double balance = dataSnapshot.child("balance").getValue(Double.class);

                tvAccountNumber.setText("Account Number: " + accountNumber);
                tvAccountName.setText("Account Name: " + firstName + " " + lastName);
                tvCurrentBalance.setText(String.format("₱ %.2f", balance != null ? balance : 0.00));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DepositActivity.this, "Failed to load account details", Toast.LENGTH_SHORT).show();
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