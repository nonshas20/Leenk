package com.example.leenk;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PayBillsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String userId;
    private double currentBalance;

    private TextView tvCurrentBalance;
    private EditText etBillAmount;
    private TextView tvFee;
    private Button btnPayBill;
    private String selectedBiller = "";

    private static final double MAINTAINING_BALANCE = 500.0;
    private static final double FIXED_FEE = 15.0; // New fixed fee
    private RecyclerView rvRecentTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_bills);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getIntent().getStringExtra("USER_ID");

        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        etBillAmount = findViewById(R.id.etBillAmount);
        tvFee = findViewById(R.id.tvFee);
        btnPayBill = findViewById(R.id.btnPayBill);
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));

        loadUserBalance();
        setupBillerButtons();
        setupBillAmountListener();
        setupPayBillButton();
        loadRecentTransactions();

        // Set the fixed fee display
        tvFee.setText(String.format("Fee: ₱ %.2f", FIXED_FEE));
    }

    private void loadUserBalance() {
        mDatabase.child("users").child(userId).child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentBalance = dataSnapshot.getValue(Double.class);
                    tvCurrentBalance.setText(String.format("₱ %.2f", currentBalance));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PayBillsActivity.this, "Failed to load balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBillerButtons() {
        int[] billerIds = {R.id.btnEasyTrip, R.id.btnAutoSweep, R.id.btnGlobe, R.id.btnHomeCredit,
                R.id.btnPLDT, R.id.btnSSS, R.id.btnMaynilad, R.id.btnMeralco};
        String[] billerNames = {"Easytrip", "AutoSweep", "Globe", "Home Credit", "PLDT", "SSS", "Maynilad", "Meralco"};

        for (int i = 0; i < billerIds.length; i++) {
            final String billerName = billerNames[i];
            CardView billerCard = findViewById(billerIds[i]);
            billerCard.setOnClickListener(v -> selectBiller(billerName));
        }
    }

    private void selectBiller(String billerName) {
        selectedBiller = billerName;
        Toast.makeText(this, billerName + " selected", Toast.LENGTH_SHORT).show();
        updatePayButtonState();
    }

    private void setupBillAmountListener() {
        etBillAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateFeeAndPayButton();
            }
        });
    }

    private void updateFeeAndPayButton() {
        updatePayButtonState();
    }

    private void updatePayButtonState() {
        String amountStr = etBillAmount.getText().toString();
        btnPayBill.setEnabled(!selectedBiller.isEmpty() && !amountStr.isEmpty() && Double.parseDouble(amountStr) > 0);
    }

    private void setupPayBillButton() {
        btnPayBill.setOnClickListener(v -> {
            String amountStr = etBillAmount.getText().toString();
            if (amountStr.isEmpty()) {
                Toast.makeText(PayBillsActivity.this, "Please enter bill amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            double totalAmount = amount + FIXED_FEE;

            if (totalAmount > currentBalance - MAINTAINING_BALANCE) {
                Toast.makeText(PayBillsActivity.this, "Insufficient balance. You need to maintain a balance of ₱" + MAINTAINING_BALANCE, Toast.LENGTH_SHORT).show();
                return;
            }

            processPayment(totalAmount);
        });
    }

    private void processPayment(double amount) {
        mDatabase.child("users").child(userId).child("balance").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Double currentBalance = mutableData.getValue(Double.class);
                if (currentBalance == null || (currentBalance - amount) < MAINTAINING_BALANCE) {
                    return Transaction.abort();
                }
                mutableData.setValue(currentBalance - amount);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    addTransaction(amount);
                    Toast.makeText(PayBillsActivity.this, "Payment successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PayBillsActivity.this, "Payment failed. Insufficient balance or unable to maintain minimum balance.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadRecentTransactions() {
        mDatabase.child("users").child(userId).child("transactions")
                .orderByChild("timestamp")
                .limitToLast(5)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<UserTransaction> transactions = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            UserTransaction transaction = snapshot.getValue(UserTransaction.class);
                            if (transaction != null && transaction.getType().equals("bill_payment")) {
                                transactions.add(transaction);
                            }
                        }
                        Collections.reverse(transactions);
                        TransactionAdapter adapter = new TransactionAdapter(transactions);
                        rvRecentTransactions.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(PayBillsActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addTransaction(double amount) {
        String transactionId = mDatabase.child("users").child(userId).child("transactions").push().getKey();
        long timestamp = System.currentTimeMillis();

        UserTransaction transaction = new UserTransaction("bill_payment", -amount, timestamp,
                "Bill payment to " + selectedBiller + " (including ₱" + FIXED_FEE + " fee)");

        mDatabase.child("users").child(userId).child("transactions").child(transactionId).setValue(transaction);
    }
}