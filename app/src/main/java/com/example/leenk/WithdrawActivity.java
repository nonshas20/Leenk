package com.example.leenk;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
    private EditText etWithdrawAmount;
    private Button btnWithdraw;
    private RecyclerView rvWithdrawTransactions;
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
    }

    private void initializeViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        etWithdrawAmount = findViewById(R.id.etWithdrawAmount);
        btnWithdraw = findViewById(R.id.btnWithdraw);
        rvWithdrawTransactions = findViewById(R.id.rvWithdrawTransactions);
        rvWithdrawTransactions.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnWithdraw.setOnClickListener(v -> processWithdrawal());
    }

    private void updateBalanceDisplay() {
        tvCurrentBalance.setText(String.format("Current Balance: ₱ %.2f", currentBalance));
    }

    private void processWithdrawal() {
        String amountStr = etWithdrawAmount.getText().toString();
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

        updateBalance(-amount);
    }

    private void updateBalance(double amount) {
        mDatabase.child("users").child(userId).child("balance").setValue(currentBalance + amount)
                .addOnSuccessListener(aVoid -> {
                    addTransaction(amount);
                    currentBalance += amount;
                    updateBalanceDisplay();
                    Toast.makeText(WithdrawActivity.this, "Withdrawal successful", Toast.LENGTH_SHORT).show();
                    etWithdrawAmount.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(WithdrawActivity.this, "Failed to process withdrawal", Toast.LENGTH_SHORT).show());
    }

    private void addTransaction(double amount) {
        String transactionId = mDatabase.child("users").child(userId).child("transactions").push().getKey();
        long timestamp = System.currentTimeMillis();

        UserTransaction transaction = new UserTransaction("withdraw", amount, timestamp, "Withdrawal");

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
                        TransactionAdapter adapter = new TransactionAdapter(transactions);
                        rvWithdrawTransactions.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(WithdrawActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}