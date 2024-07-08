package com.example.leenk;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class PayBillsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String userId;
    private double currentBalance;

    private EditText etBillAmount;
    private Button btnPayBill;
    private String selectedBiller = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_bills);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getIntent().getStringExtra("USER_ID");

        etBillAmount = findViewById(R.id.etBillAmount);
        btnPayBill = findViewById(R.id.btnPayBill);

        loadUserBalance();
        setupBillerButtons();
        setupPayBillButton();
    }

    private void loadUserBalance() {
        mDatabase.child("users").child(userId).child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentBalance = dataSnapshot.getValue(Double.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PayBillsActivity.this, "Failed to load balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBillerButtons() {
        int[] billerIds = {R.id.btnEasyTrip, R.id.btnHomeCredit, R.id.btnMeralco, R.id.btnPldt,
                R.id.btnSSS, R.id.btnMaynilad, R.id.btnGlobe, R.id.btnAutoSweep};
        String[] billerNames = {"Easytrip", "Home Credit", "Meralco", "PLDT", "SSS", "Maynilad", "Globe", "Autosweep"};

        for (int i = 0; i < billerIds.length; i++) {
            final String billerName = billerNames[i];
            findViewById(billerIds[i]).setOnClickListener(v -> selectBiller(billerName));
        }
    }

    private void selectBiller(String billerName) {
        selectedBiller = billerName;
        Toast.makeText(this, billerName + " selected", Toast.LENGTH_SHORT).show();
        btnPayBill.setEnabled(true);
    }

    private void setupPayBillButton() {
        btnPayBill.setOnClickListener(v -> {
            if (selectedBiller.isEmpty()) {
                Toast.makeText(PayBillsActivity.this, "Please select a biller", Toast.LENGTH_SHORT).show();
                return;
            }

            String amountStr = etBillAmount.getText().toString();
            if (amountStr.isEmpty()) {
                Toast.makeText(PayBillsActivity.this, "Please enter bill amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(PayBillsActivity.this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amount > currentBalance) {
                Toast.makeText(PayBillsActivity.this, "Insufficient balance", Toast.LENGTH_SHORT).show();
                return;
            }

            processPayment(amount);
        });
    }

    private void processPayment(double amount) {
        mDatabase.child("users").child(userId).child("balance").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Double currentBalance = mutableData.getValue(Double.class);
                if (currentBalance == null || currentBalance < amount) {
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
                    Toast.makeText(PayBillsActivity.this, "Payment failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addTransaction(double amount) {
        String transactionId = mDatabase.child("users").child(userId).child("transactions").push().getKey();
        long timestamp = System.currentTimeMillis();

        UserTransaction transaction = new UserTransaction("bill_payment", -amount, timestamp,
                "Bill payment to " + selectedBiller);

        mDatabase.child("users").child(userId).child("transactions").child(transactionId).setValue(transaction);
    }
}