package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BankTransferDetailsActivity extends AppCompatActivity {

    private TextView tvAvailableBalance, tvBalanceAfter;
    private EditText etAmount;
    private Button btnSend;
    private ImageButton btnBack;
    private DatabaseReference mDatabase;
    private String userId, bankName;
    private double currentBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_transfer_details);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getIntent().getStringExtra("USER_ID");
        bankName = getIntent().getStringExtra("BANK_NAME");
        currentBalance = getIntent().getDoubleExtra("CURRENT_BALANCE", 0.0);

        initializeViews();
        setupClickListeners();
        updateBalanceDisplay();
    }

    private void initializeViews() {
        tvAvailableBalance = findViewById(R.id.tvAvailableBalance);
        tvBalanceAfter = findViewById(R.id.tvBalanceAfter);
        etAmount = findViewById(R.id.etAmount);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> processTransfer());
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBalanceAfterDisplay();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateBalanceDisplay() {
        tvAvailableBalance.setText(String.format(Locale.getDefault(), "₱ %.2f", currentBalance));
        updateBalanceAfterDisplay();
    }

    private void updateBalanceAfterDisplay() {
        double amount = 0;
        try {
            amount = Double.parseDouble(etAmount.getText().toString());
        } catch (NumberFormatException e) {
            // Invalid input, ignore
        }
        double balanceAfter = currentBalance - amount;
        tvBalanceAfter.setText(String.format(Locale.getDefault(), "₱ %.2f", balanceAfter));
    }

    private void processTransfer() {
        String amountStr = etAmount.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > currentBalance) {
            Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update user's balance
        mDatabase.child("users").child(userId).child("balance").setValue(currentBalance - amount);

        // Add transaction record
        String transactionId = mDatabase.child("users").child(userId).child("transactions").push().getKey();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        UserTransaction transaction = new UserTransaction("bank_transfer", -amount, System.currentTimeMillis(),
                "Transfer to " + bankName, currentTime);

        mDatabase.child("users").child(userId).child("transactions").child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BankTransferDetailsActivity.this, "Transfer successful", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(BankTransferDetailsActivity.this, "Transfer failed", Toast.LENGTH_SHORT).show());
    }
}