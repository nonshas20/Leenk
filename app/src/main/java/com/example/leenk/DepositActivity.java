package com.example.leenk;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class DepositActivity extends AppCompatActivity {

    private EditText etDepositAmount;
    private Button btnConfirmDeposit;
    private TextView tvCurrentBalance, tvAccountNumber, tvAccountName;
    private ImageView ivQRCode;
    private CardView btnGCash, btnPayMaya;
    private DatabaseReference mDatabase;
    private String userId;
    private String selectedPaymentMethod = "";

    private static final double MIN_DEPOSIT = 100.0;
    private static final double MAX_DEPOSIT = 10000.0;

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
        ivQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRCodeDisplayActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }



    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;
        Toast.makeText(this, method + " selected", Toast.LENGTH_SHORT).show();
    }

    private void confirmDeposit() {
        String amountStr = etDepositAmount.getText().toString();
        Double amount = amountStr.isEmpty() ? null : Double.parseDouble(amountStr);

        if (amount == null || amount < MIN_DEPOSIT) {
            Toast.makeText(this, "Minimum deposit amount is ₱" + MIN_DEPOSIT, Toast.LENGTH_SHORT).show();
        } else if (amount > MAX_DEPOSIT) {
            Toast.makeText(this, "Maximum deposit amount is ₱" + MAX_DEPOSIT, Toast.LENGTH_SHORT).show();
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

                generateAndDisplayQRCode();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DepositActivity.this, "Failed to load account details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateAndDisplayQRCode() {
        String accountNumber = tvAccountNumber.getText().toString().replace("Account Number: ", "");
        String accountName = tvAccountName.getText().toString().replace("Account Name: ", "");

        JSONObject qrData = new JSONObject();
        try {
            qrData.put("accountNumber", accountNumber);
            qrData.put("accountName", accountName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = writer.encode(qrData.toString(), BarcodeFormat.QR_CODE, 512, 512);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ivQRCode.setImageBitmap(bitmap);
            ivQRCode.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            e.printStackTrace();
        }
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
                if (amount >= MIN_DEPOSIT && amount <= MAX_DEPOSIT) {
                    mutableData.setValue(currentBalance + amount);
                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (committed) {
                    addTransaction(amount, "deposit", paymentMethod);
                    Toast.makeText(DepositActivity.this, "Deposit successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DepositActivity.this, "Deposit failed. Please try again.", Toast.LENGTH_SHORT).show();
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