package com.example.leenk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class QRCodeDisplayActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private TextView tvName, tvAccountNumber, tvAmount;
    private Button btnAddAmount;
    private DatabaseReference mDatabase;
    private String userId;
    private double currentAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_display);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getIntent().getStringExtra("USER_ID");

        initViews();
        fetchUserData();
        setupListeners();
    }

    private void initViews() {
        ivQRCode = findViewById(R.id.ivQRCode);
        tvName = findViewById(R.id.tvName);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvAmount = findViewById(R.id.tvAmount);
        btnAddAmount = findViewById(R.id.btnAddAmount);
    }

    private void setupListeners() {
        btnAddAmount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAmountActivity.class);
            intent.putExtra("CURRENT_AMOUNT", currentAmount);
            startActivityForResult(intent, 1);
        });
    }

    private void fetchUserData() {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firstName = dataSnapshot.child("basic_info").child("first_name").getValue(String.class);
                String lastName = dataSnapshot.child("basic_info").child("last_name").getValue(String.class);
                String accountNumber = dataSnapshot.child("accountNumber").getValue(String.class);

                tvName.setText(firstName + " " + lastName);
                tvAccountNumber.setText("****" + accountNumber.substring(accountNumber.length() - 4));

                generateQRCode(accountNumber, firstName + " " + lastName, currentAmount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void generateQRCode(String accountNumber, String name, double amount) {
        JSONObject qrData = new JSONObject();
        try {
            qrData.put("accountNumber", accountNumber);
            qrData.put("name", name);
            qrData.put("amount", amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = writer.encode(qrData.toString(), BarcodeFormat.QR_CODE, 512, 512);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ivQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            currentAmount = data.getDoubleExtra("NEW_AMOUNT", 0.0);
            tvAmount.setText(String.format("₱%.2f", currentAmount));
            fetchUserData(); // Regenerate QR code with new amount
        }
    }
}