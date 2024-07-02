package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeDashboardActivity extends AppCompatActivity {

    private TextView tvBalance, tvAccountNumber, tvExpirationDate, tvCVV, tvName;
    private ImageButton btnToggleBalance, btnDeposit, btnScanQR, btnSend, btnTransfer;
    private Button btnMyAccount, btnHelpCenter;
    private RecyclerView rvRecentTransactions;
    private DatabaseReference mDatabase;
    private String userId;
    private boolean isBalanceVisible = true;
    private double currentBalance = 0.00;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getIntent().getStringExtra("USER_ID");

        initializeViews();
        setupClickListeners();
        loadUserData();
        loadRecentTransactions();
    }

    private void initializeViews() {
        tvBalance = findViewById(R.id.tvBalance);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvExpirationDate = findViewById(R.id.tvExpirationDate);
        tvCVV = findViewById(R.id.tvCVV);
        tvName = findViewById(R.id.tvName);
        btnToggleBalance = findViewById(R.id.btnToggleBalance);
        btnDeposit = findViewById(R.id.btnDeposit);
        btnScanQR = findViewById(R.id.btnScanQR);
        btnSend = findViewById(R.id.btnSend);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnMyAccount = findViewById(R.id.btnMyAccount);
        btnHelpCenter = findViewById(R.id.btnHelpCenter);
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnToggleBalance.setOnClickListener(v -> toggleBalanceVisibility());
        btnDeposit.setOnClickListener(v -> showDepositDialog());
        btnScanQR.setOnClickListener(v -> Toast.makeText(this, "Scan QR clicked", Toast.LENGTH_SHORT).show());
        btnSend.setOnClickListener(v -> showSendDialog());
        btnTransfer.setOnClickListener(v -> Toast.makeText(this, "Transfer clicked", Toast.LENGTH_SHORT).show());
        btnMyAccount.setOnClickListener(v -> Toast.makeText(this, "My Account clicked", Toast.LENGTH_SHORT).show());
        btnHelpCenter.setOnClickListener(v -> Toast.makeText(this, "Help Center clicked", Toast.LENGTH_SHORT).show());
    }

    private void toggleBalanceVisibility() {
        isBalanceVisible = !isBalanceVisible;
        if (isBalanceVisible) {
            tvBalance.setVisibility(View.VISIBLE);
            btnToggleBalance.setImageResource(R.drawable.eye_icon_show);
        } else {
            tvBalance.setVisibility(View.INVISIBLE);
            btnToggleBalance.setImageResource(R.drawable.eye_icon_hidden);
        }
    }

    private void loadUserData() {
        mDatabase.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double balance = dataSnapshot.child("balance").getValue(Double.class);
                    currentBalance = balance != null ? balance : 0.0;
                    tvBalance.setText(String.format("₱ %.2f", currentBalance));

                    String accountNumber = dataSnapshot.child("accountNumber").getValue(String.class);
                    tvAccountNumber.setText(accountNumber);

                    String expirationDate = dataSnapshot.child("expirationDate").getValue(String.class);
                    tvExpirationDate.setText(expirationDate);

                    String cvv = dataSnapshot.child("cvv").getValue(String.class);
                    tvCVV.setText(cvv);

                    String name = dataSnapshot.child("name").getValue(String.class);
                    tvName.setText(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomeDashboardActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecentTransactions() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("transactions").limitToLast(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<UserTransaction> transactions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Adapt Firebase DataSnapshot to UserTransaction
                    String type = snapshot.child("type").getValue(String.class);
                    double amount = snapshot.child("amount").getValue(Double.class);
                    long timestamp = snapshot.child("timestamp").getValue(Long.class);
                    UserTransaction transaction = new UserTransaction(type, amount, timestamp);
                    transactions.add(transaction);
                }
                // Reverse the list to show most recent first
                Collections.reverse(transactions);
                TransactionAdapter adapter = new TransactionAdapter(transactions);
                rvRecentTransactions.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomeDashboardActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDepositDialog() {
        // TODO: Implement a dialog to enter deposit amount
        // For now, we'll just add a fixed amount
        updateBalance(50.00, "deposit");
    }

    private void showSendDialog() {
        // TODO: Implement a dialog to enter send amount and recipient
        // For now, we'll just subtract a fixed amount
        updateBalance(-30.00, "send");
    }

    private void updateBalance(double amount, String transactionType) {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(MutableData mutableData) {
                Double currentBalance = mutableData.child("balance").getValue(Double.class);
                if (currentBalance == null) {
                    currentBalance = 0.0;
                }
                double newBalance = currentBalance + amount;
                mutableData.child("balance").setValue(newBalance);
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    // Balance updated successfully, now add the transaction
                    addTransaction(amount, transactionType);
                    Toast.makeText(HomeDashboardActivity.this, "Balance updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeDashboardActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addTransaction(double amount, String type) {
        String userId = mAuth.getCurrentUser().getUid();
        String transactionId = mDatabase.child("users").child(userId).child("transactions").push().getKey();
        UserTransaction transaction = new UserTransaction(type, amount, System.currentTimeMillis());

        mDatabase.child("users").child(userId).child("transactions").child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> Toast.makeText(HomeDashboardActivity.this, "Transaction added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(HomeDashboardActivity.this, "Failed to add transaction", Toast.LENGTH_SHORT).show());
    }
}
