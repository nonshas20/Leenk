package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

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
        btnSend.setOnClickListener(v -> navigateToWithdraw());
        btnTransfer.setOnClickListener(v -> showLeenkToLeenkDialog());
        btnMyAccount.setOnClickListener(v -> navigateToMyAccount());
        btnHelpCenter.setOnClickListener(v -> Toast.makeText(this, "Help Center clicked", Toast.LENGTH_SHORT).show());
    }

    private void navigateToMyAccount() {
        Intent intent = new Intent(this, MyAccountActivity.class);
        startActivity(intent);
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
        if (userId == null) {
            Toast.makeText(this, "User ID is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.child("users").child(userId).child("transactions").limitToLast(5).addValueEventListener(new ValueEventListener() {
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
                rvRecentTransactions.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomeDashboardActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDepositDialog() {
        Intent intent = new Intent(this, DepositActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("CURRENT_BALANCE", currentBalance);
        startActivity(intent);
    }

    private void updateBalance(double amount, String transactionType) {
        if (userId == null) {
            Toast.makeText(this, "User ID is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.child("users").child(userId).child("balance").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Double currentBalance = mutableData.getValue(Double.class);
                if (currentBalance == null) {
                    currentBalance = 0.0;
                }
                mutableData.setValue(currentBalance + amount);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    addTransaction(amount, transactionType, "");
                    Toast.makeText(HomeDashboardActivity.this, "Balance updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeDashboardActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToWithdraw() {
        Intent intent = new Intent(this, WithdrawActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("CURRENT_BALANCE", currentBalance);
        startActivity(intent);
    }

    private void showLeenkToLeenkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_leenk_to_leenk, null);
        builder.setView(view);

        EditText etAmount = view.findViewById(R.id.etAmount);
        EditText etAccountNumber = view.findViewById(R.id.etAccountNumber);
        Spinner spinnerCurrency = view.findViewById(R.id.spinnerCurrency);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String amount = etAmount.getText().toString();
            String accountNumber = etAccountNumber.getText().toString();
            String currency = spinnerCurrency.getSelectedItem().toString();

            if (!amount.isEmpty() && !accountNumber.isEmpty()) {
                double sendAmount = Double.parseDouble(amount);
                processLeenkToLeenkTransfer(sendAmount, accountNumber, currency);
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void processLeenkToLeenkTransfer(double amount, String accountNumber, String currency) {
        if (amount > currentBalance) {
            Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }

        updateBalance(-amount, "withdraw");
        addTransaction(-amount, "withdraw", "Leenk to Leenk transfer to " + accountNumber + " (" + currency + ")");
    }

    private void addTransaction(double amount, String type, String description) {
        if (userId == null) {
            Toast.makeText(this, "User ID is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String transactionId = mDatabase.child("users").child(userId).child("transactions").push().getKey();
        long timestamp = System.currentTimeMillis();

        UserTransaction transaction = new UserTransaction(type, amount, timestamp, description);

        mDatabase.child("users").child(userId).child("transactions").child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HomeDashboardActivity.this, "Transaction added successfully", Toast.LENGTH_SHORT).show();
                    loadRecentTransactions();
                })
                .addOnFailureListener(e -> Toast.makeText(HomeDashboardActivity.this, "Failed to add transaction", Toast.LENGTH_SHORT).show());
    }
}