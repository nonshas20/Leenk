package com.example.leenk;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WithdrawActivity extends AppCompatActivity {

    private TextView tvCurrentBalance;
    private EditText etWithdrawAmount, etAccountNumber;
    private Button btnWithdraw;
    private RecyclerView rvWithdrawTransactions;
    private CardView cardNoTransfers;
    private String userId;
    private double currentBalance;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getIntent().getStringExtra("USER_ID");
        currentBalance = getIntent().getDoubleExtra("CURRENT_BALANCE", 0.0);



        initializeViews();
        setupClickListeners();
        updateBalanceDisplay();
        loadWithdrawTransactions();

        String qrResult = getIntent().getStringExtra("QR_RESULT");
        if (qrResult != null) {
            handleQRResult(qrResult);
        }
    }

    private void initializeViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        etWithdrawAmount = findViewById(R.id.etWithdrawAmount);
        etAccountNumber = findViewById(R.id.etAccountNumber);
        btnWithdraw = findViewById(R.id.btnWithdraw);
        rvWithdrawTransactions = findViewById(R.id.rvWithdrawTransactions);
        cardNoTransfers = findViewById(R.id.cardNoTransfers);
        rvWithdrawTransactions.setLayoutManager(new LinearLayoutManager(this));
    }
    private void handleQRResult(String qrResult) {
        // Here you can parse the QR result and populate the withdrawal fields
        // For example, if the QR code contains an account number:
        etAccountNumber.setText(qrResult);
        // You might also want to set a default amount or parse it from the QR code
    }

    private void setupClickListeners() {
        btnWithdraw.setOnClickListener(v -> processWithdrawal());
    }

    private void updateBalanceDisplay() {
        tvCurrentBalance.setText(String.format("Current Balance: ₱ %.2f", currentBalance));
    }

    private void processWithdrawal() {
        String amountStr = etWithdrawAmount.getText().toString();
        String accountNumber = etAccountNumber.getText().toString();

        if (amountStr.isEmpty() || accountNumber.isEmpty()) {
            Toast.makeText(this, "Please enter both amount and account number", Toast.LENGTH_SHORT).show();
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

        // Check if it's a transfer to another user
        if (isTransferToAnotherUser(accountNumber)) {
            transferToUser(amount, accountNumber);
        } else {
            updateBalance(-amount, accountNumber);
        }
    }
    private boolean isTransferToAnotherUser(String accountNumber) {
        // This is a simplification. In a real app, you'd want to validate the account number format
        return accountNumber.length() == 16;
    }
    private void transferToUser(double amount, String recipientAccountNumber) {
        mDatabase.child("users").orderByChild("accountNumber").equalTo(recipientAccountNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String recipientUserId = userSnapshot.getKey();
                                double recipientBalance = userSnapshot.child("balance").getValue(Double.class);

                                // Update sender's balance
                                mDatabase.child("users").child(userId).child("balance").setValue(currentBalance - amount)
                                        .addOnSuccessListener(aVoid -> {
                                            // Update recipient's balance
                                            mDatabase.child("users").child(recipientUserId).child("balance").setValue(recipientBalance + amount)
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        addTransaction(-amount, "Transfer to " + recipientAccountNumber);
                                                        currentBalance -= amount;
                                                        updateBalanceDisplay();
                                                        Toast.makeText(WithdrawActivity.this, "Transfer successful", Toast.LENGTH_SHORT).show();
                                                        etWithdrawAmount.setText("");
                                                        etAccountNumber.setText("");
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(WithdrawActivity.this, "Failed to update recipient's balance", Toast.LENGTH_SHORT).show());
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(WithdrawActivity.this, "Failed to update sender's balance", Toast.LENGTH_SHORT).show());

                                return; // Exit the loop after finding the first match
                            }
                        } else {
                            Toast.makeText(WithdrawActivity.this, "Recipient account not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(WithdrawActivity.this, "Failed to process transfer", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateBalance(double amount, String accountNumber) {
        mDatabase.child("users").child(userId).child("balance").setValue(currentBalance + amount)
                .addOnSuccessListener(aVoid -> {
                    addTransaction(amount, accountNumber);
                    currentBalance += amount;
                    updateBalanceDisplay();
                    Toast.makeText(WithdrawActivity.this, "Withdrawal successful", Toast.LENGTH_SHORT).show();
                    etWithdrawAmount.setText("");
                    etAccountNumber.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(WithdrawActivity.this, "Failed to process withdrawal", Toast.LENGTH_SHORT).show());
    }

    private void addTransaction(double amount, String accountNumber) {
        String transactionId = mDatabase.child("users").child(userId).child("transactions").push().getKey();
        long timestamp = System.currentTimeMillis();

        UserTransaction transaction = new UserTransaction("withdraw", amount, timestamp, "Withdrawal to " + accountNumber);

        mDatabase.child("users").child(userId).child("transactions").child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> loadWithdrawTransactions())
                .addOnFailureListener(e -> Toast.makeText(WithdrawActivity.this, "Failed to add transaction", Toast.LENGTH_SHORT).show());
    }

    private void loadWithdrawTransactions() {
        mDatabase.child("users").child(userId).child("transactions")
                .orderByChild("type")
                .equalTo("withdraw")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<UserTransaction> transactions = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            UserTransaction transaction = snapshot.getValue(UserTransaction.class);
                            if (transaction != null) {
                                transactions.add(transaction);
                            }
                        }
                        Collections.reverse(transactions);
                        updateUI(transactions);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(WithdrawActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(List<UserTransaction> transactions) {
        if (transactions.isEmpty()) {
            cardNoTransfers.setVisibility(View.VISIBLE);
            rvWithdrawTransactions.setVisibility(View.GONE);
        } else {
            cardNoTransfers.setVisibility(View.GONE);
            rvWithdrawTransactions.setVisibility(View.VISIBLE);
            TransactionAdapter adapter = new TransactionAdapter(transactions);
            rvWithdrawTransactions.setAdapter(adapter);
        }
    }
}