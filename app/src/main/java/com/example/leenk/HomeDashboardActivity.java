package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

    private TextView tvBalance, tvAccountNumber, tvCardNumber, tvExpirationDate, tvCVV, tvName;
    private ImageButton btnToggleBalance, btnDeposit, btnScanQR, btnSend, btnTransfer, btnToggleCardDetails;
    private ImageButton btnMyAccount, btnHelpCenter; // Changed to ImageButton
    private CardView btnBuyLoad, btnPayBills; // Added new buttons
    private CardView btnAllTransactions;
    private RecyclerView rvRecentTransactions;
    private DatabaseReference mDatabase;


    private String userId;
    private boolean isBalanceVisible = true;
    private boolean isCardDetailsVisible = true;
    private double currentBalance = 0.00;

    private String accountNumber;
    private String expirationDate;
    private String cvv;


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
        tvCardNumber = findViewById(R.id.tvCardNumber);
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
        btnBuyLoad = findViewById(R.id.btnBuyLoad);
        btnPayBills = findViewById(R.id.btnPayBills);
        btnToggleCardDetails = findViewById(R.id.btnToggleCardDetails);
        btnAllTransactions = findViewById(R.id.btnAllTransactions);
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
        btnToggleCardDetails.setOnClickListener(v -> toggleCardDetailsVisibility());
        btnBuyLoad.setOnClickListener(v -> navigateToBuyLoad());
        btnPayBills.setOnClickListener(v -> navigateToPayBills());
        btnScanQR.setOnClickListener(v -> startQRScanner());
        btnAllTransactions.setOnClickListener(v -> showAllTransactions());
    }

    private void startQRScanner() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("CURRENT_BALANCE", currentBalance);
        startActivity(intent);
    }

    private void toggleBalanceVisibility() {
        if (isBalanceVisible) {
            String balance = tvBalance.getText().toString();
            String pesoSign = balance.substring(0, 2); // Assuming "₱ " is always at the start
            String hiddenBalance = pesoSign + "•".repeat(balance.length() - 2);
            tvBalance.setText(hiddenBalance);
        } else {
            tvBalance.setText(String.format("₱ %.2f", currentBalance));
        }
        isBalanceVisible = !isBalanceVisible;
        btnToggleBalance.setImageResource(isBalanceVisible ? R.drawable.eye_icon_show : R.drawable.eye_icon_hidden);
    }
    private void navigateToBuyLoad() {
        Intent intent = new Intent(this, BuyLoadActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    private void navigateToPayBills() {
        Intent intent = new Intent(this, PayBillsActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    private void toggleCardDetailsVisibility() {
        isCardDetailsVisible = !isCardDetailsVisible;
        if (isCardDetailsVisible) {
            showCardDetails();
            btnToggleCardDetails.setImageResource(R.drawable.eye_icon_show);
        } else {
            hideCardDetails();
            btnToggleCardDetails.setImageResource(R.drawable.eye_icon_hidden);
        }
    }

    private void showCardDetails() {
        tvCardNumber.setText(formatCardNumber(accountNumber));
        tvExpirationDate.setText(expirationDate);
        tvCVV.setText(cvv);
    }

    private void hideCardDetails() {
        tvCardNumber.setText("**** **** **** ****");
        tvExpirationDate.setText("**/**");
        tvCVV.setText("***");
    }

    private String formatCardNumber(String number) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(number.charAt(i));
        }
        return formatted.toString();
    }

    private void navigateToMyAccount() {
        Intent intent = new Intent(this, MyAccountActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    private void loadUserData() {
        mDatabase.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double balance = dataSnapshot.child("balance").getValue(Double.class);
                    currentBalance = balance != null ? balance : 0.0;
                    tvBalance.setText(String.format("₱ %.2f", currentBalance));

                    accountNumber = dataSnapshot.child("accountNumber").getValue(String.class);
                    tvAccountNumber.setText(accountNumber);

                    expirationDate = dataSnapshot.child("expirationDate").getValue(String.class);
                    tvExpirationDate.setText(expirationDate);

                    cvv = dataSnapshot.child("cvv").getValue(String.class);
                    tvCVV.setText(cvv);

                    // Retrieve first name from basic_info
                    String firstName = dataSnapshot.child("basic_info").child("first_name").getValue(String.class);
                    String lastName = dataSnapshot.child("basic_info").child("last_name").getValue(String.class);
                    String fullName = firstName + " " + lastName; // Or adjust as per your naming convention
                    tvName.setText("Hi, " + fullName); // Set the greeting message with the retrieved name

                    if (isCardDetailsVisible) {
                        showCardDetails();
                    } else {
                        hideCardDetails();
                    }
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

    private void processLeenkToLeenkTransfer(double amount, String recipientAccountNumber, String currency) {
        if (amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > currentBalance) {
            Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }

        // First, find the recipient user by account number
        mDatabase.child("users").orderByChild("accountNumber").equalTo(recipientAccountNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String recipientUserId = userSnapshot.getKey();
                                performTransfer(amount, recipientUserId, recipientAccountNumber, currency);
                                return;
                            }
                        } else {
                            Toast.makeText(HomeDashboardActivity.this, "Recipient account not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(HomeDashboardActivity.this, "Failed to find recipient", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performTransfer(double amount, String recipientUserId, String recipientAccountNumber, String currency) {
        mDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                MutableData senderBalanceData = mutableData.child("users").child(userId).child("balance");
                MutableData recipientBalanceData = mutableData.child("users").child(recipientUserId).child("balance");

                Double senderBalance = senderBalanceData.getValue(Double.class);
                Double recipientBalance = recipientBalanceData.getValue(Double.class);

                if (senderBalance == null || recipientBalance == null) {
                    return Transaction.abort();
                }

                if (senderBalance < amount) {
                    return Transaction.abort();
                }

                senderBalanceData.setValue(senderBalance - amount);
                recipientBalanceData.setValue(recipientBalance + amount);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    // Add transaction for sender
                    addTransaction(-amount, "transfer_out", "Transfer to " + recipientAccountNumber + " (" + currency + ")");

                    // Add transaction for recipient
                    String recipientTransactionId = mDatabase.child("users").child(recipientUserId).child("transactions").push().getKey();
                    UserTransaction recipientTransaction = new UserTransaction("transfer_in", amount, System.currentTimeMillis(), "Transfer from " + accountNumber + " (" + currency + ")");
                    mDatabase.child("users").child(recipientUserId).child("transactions").child(recipientTransactionId).setValue(recipientTransaction);

                    currentBalance -= amount;
                    updateBalanceDisplay();
                    Toast.makeText(HomeDashboardActivity.this, "Transfer successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeDashboardActivity.this, "Transfer failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateBalanceDisplay() {
        tvBalance.setText(String.format("₱ %.2f", currentBalance));
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

    private void showAllTransactions() {
        Intent intent = new Intent(this, AllTransactionsActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("BALANCE", currentBalance);
        intent.putExtra("ACCOUNT_NUMBER", accountNumber);
        startActivity(intent);
    }
}