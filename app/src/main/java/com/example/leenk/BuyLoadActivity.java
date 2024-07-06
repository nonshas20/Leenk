package com.example.leenk;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class BuyLoadActivity extends AppCompatActivity {

    private CardView cardGlobe, cardSmart, cardTnt, cardTm, cardDito;
    private EditText etPhoneNumber, etAmount;
    private Button btnBuyLoad;
    private TextView tvAvailableBalance, tvBalanceAfter;
    private String selectedNetwork = "";
    private DatabaseReference mDatabase;
    private String userId;
    private double userBalance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_load);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getIntent().getStringExtra("USER_ID");

        initializeViews();
        setupClickListeners();
        loadUserBalance();
    }

    private void initializeViews() {
        cardGlobe = findViewById(R.id.cardGlobe);
        cardSmart = findViewById(R.id.cardSmart);
        cardTnt = findViewById(R.id.cardTnt);
        cardTm = findViewById(R.id.cardTm);
        cardDito = findViewById(R.id.cardDito);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAmount = findViewById(R.id.etAmount);
        btnBuyLoad = findViewById(R.id.btnBuyLoad);
        tvAvailableBalance = findViewById(R.id.tvAvailableBalance);
        tvBalanceAfter = findViewById(R.id.tvBalanceAfter);
    }

    private void setupClickListeners() {
        View.OnClickListener networkClickListener = view -> {
            resetNetworkSelection();
            view.setSelected(true);
            selectedNetwork = getNetworkName(view.getId());

        };

        cardGlobe.setOnClickListener(networkClickListener);
        cardSmart.setOnClickListener(networkClickListener);
        cardTnt.setOnClickListener(networkClickListener);
        cardTm.setOnClickListener(networkClickListener);
        cardDito.setOnClickListener(networkClickListener);

        btnBuyLoad.setOnClickListener(v -> processBuyLoad());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateBalanceAfter();
            }
        });
        findViewById(R.id.card10).setOnClickListener(v -> setAmount(10));
        findViewById(R.id.card50).setOnClickListener(v -> setAmount(50));
        findViewById(R.id.card100).setOnClickListener(v -> setAmount(100));
        findViewById(R.id.card300).setOnClickListener(v -> setAmount(300));
        findViewById(R.id.card500).setOnClickListener(v -> setAmount(500));
        findViewById(R.id.card1000).setOnClickListener(v -> setAmount(1000));
    }
    private void setAmount(int amount) {
        etAmount.setText(String.valueOf(amount));
    }


    private void loadUserBalance() {
        mDatabase.child("users").child(userId).child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userBalance = dataSnapshot.getValue(Double.class);
                    tvAvailableBalance.setText(String.format("₱ %.2f", userBalance));
                    updateBalanceAfter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BuyLoadActivity.this, "Failed to load balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBalanceAfter() {
        String amountStr = etAmount.getText().toString();
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                double balanceAfter = userBalance - amount;
                tvBalanceAfter.setText(String.format("₱ %.2f", balanceAfter));
            } catch (NumberFormatException e) {
                tvBalanceAfter.setText(String.format("₱ %.2f", userBalance));
            }
        } else {
            tvBalanceAfter.setText(String.format("₱ %.2f", userBalance));
        }
    }

    private void resetNetworkSelection() {
        cardGlobe.setSelected(false);
        cardSmart.setSelected(false);
        cardTnt.setSelected(false);
        cardTm.setSelected(false);
        cardDito.setSelected(false);
    }

    private String getNetworkName(int viewId) {
        if (viewId == R.id.cardGlobe) return "Globe";
        if (viewId == R.id.cardSmart) return "Smart";
        if (viewId == R.id.cardTnt) return "TNT";
        if (viewId == R.id.cardTm) return "TM";
        if (viewId == R.id.cardDito) return "Dito";
        return "";
    }

    private void processBuyLoad() {
        String phoneNumber = etPhoneNumber.getText().toString();
        String amountStr = etAmount.getText().toString();

        if (selectedNetwork.isEmpty() || phoneNumber.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select a network", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > userBalance) {
            Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update user balance
        double newBalance = userBalance - amount;
        mDatabase.child("users").child(userId).child("balance").setValue(newBalance)
                .addOnSuccessListener(aVoid -> {
                    // Record transaction
                    String transactionId = mDatabase.child("users").child(userId).child("transactions").push().getKey();
                    Map<String, Object> transactionData = new HashMap<>();
                    transactionData.put("type", "buy_load");
                    transactionData.put("network", selectedNetwork);
                    transactionData.put("phoneNumber", phoneNumber);
                    transactionData.put("amount", amount);
                    transactionData.put("timestamp", ServerValue.TIMESTAMP);

                    mDatabase.child("users").child(userId).child("transactions").child(transactionId).setValue(transactionData)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(BuyLoadActivity.this, "Load purchase successful", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(BuyLoadActivity.this, "Failed to record transaction", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(BuyLoadActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show());
    }
}