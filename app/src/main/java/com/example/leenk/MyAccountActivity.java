package com.example.leenk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import java.util.HashMap;
import java.util.Map;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyAccountActivity extends AppCompatActivity {

    private TextView tvAccountNumber, tvFullName, tvDateOfBirth, tvCountryOfBirth, tvUsername, tvMobileNumber, tvEmailAddress, tvHomeAddress;
    private ImageButton btnBack, btnCopy;
    private TabLayout tabLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        initializeViews();

        // Set up TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Info"));

        // Set up click listeners
        btnBack.setOnClickListener(v -> finish());
        btnCopy.setOnClickListener(v -> copyAccountNumber());

        // Load user data
        loadUserData(userId);
    }

    private void initializeViews() {
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvFullName = findViewById(R.id.layoutFullName).findViewById(R.id.tvValue);
        tvDateOfBirth = findViewById(R.id.layoutDateOfBirth).findViewById(R.id.tvValue);
        tvCountryOfBirth = findViewById(R.id.layoutCountryOfBirth).findViewById(R.id.tvValue);

        tvUsername = findViewById(R.id.layoutUsername).findViewById(R.id.tvValue);
        tvMobileNumber = findViewById(R.id.layoutMobileNumber).findViewById(R.id.tvValue);
        tvEmailAddress = findViewById(R.id.layoutEmailAddress).findViewById(R.id.tvValue);
        tvHomeAddress = findViewById(R.id.layoutHomeAddress).findViewById(R.id.tvValue);

        btnBack = findViewById(R.id.btnBack);
        btnCopy = findViewById(R.id.btnCopy);
        tabLayout = findViewById(R.id.tabLayout);
    }

    private void setInfoItemValue(View layout, String label, String value) {
        TextView tvLabel = layout.findViewById(R.id.tvLabel);
        TextView tvValue = layout.findViewById(R.id.tvValue);
        tvLabel.setText(label);
        tvValue.setText(value != null ? value : "N/A");
    }

    private void setInfoItemWithButtonValue(View layout, String label, String value, String buttonText) {
        setInfoItemValue(layout, label, value);
        Button btnAction = layout.findViewById(R.id.btnAction);
        btnAction.setText(buttonText);
        btnAction.setOnClickListener(v -> {
            if (label.equals("Home address")) {
                openEditHomeAddressDialog(value);
            } else {
                Toast.makeText(MyAccountActivity.this, buttonText + " clicked for " + label, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadUserData(String userId) {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String accountNumber = dataSnapshot.child("accountNumber").getValue(String.class);
                    tvAccountNumber.setText(accountNumber != null ? accountNumber : "N/A");

                    DataSnapshot basicInfoSnapshot = dataSnapshot.child("basic_info");
                    String firstName = basicInfoSnapshot.child("first_name").getValue(String.class);
                    String middleName = basicInfoSnapshot.child("middle_name").getValue(String.class);
                    String lastName = basicInfoSnapshot.child("last_name").getValue(String.class);
                    String fullName = (firstName != null ? firstName : "") + " " +
                            (middleName != null ? middleName : "") + " " +
                            (lastName != null ? lastName : "");
                    setInfoItemValue(findViewById(R.id.layoutFullName), "Full name", fullName.trim());

                    String dateOfBirth = basicInfoSnapshot.child("date_of_birth").getValue(String.class);
                    setInfoItemValue(findViewById(R.id.layoutDateOfBirth), "Date of birth", dateOfBirth);

                    DataSnapshot homeAddressSnapshot = dataSnapshot.child("home_address");
                    String country = homeAddressSnapshot.child("country").getValue(String.class);
                    setInfoItemValue(findViewById(R.id.layoutCountryOfBirth), "Country of birth", country);



                    String username = dataSnapshot.child("username").getValue(String.class);
                    setInfoItemValue(findViewById(R.id.layoutUsername), "Username", username);

                    // Get the mobile number from the database
                    String mobileNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    setInfoItemWithButtonValue(findViewById(R.id.layoutMobileNumber), "Mobile number", mobileNumber, "Change");

                    String email = dataSnapshot.child("email").getValue(String.class);
                    setInfoItemWithButtonValue(findViewById(R.id.layoutEmailAddress), "Email address", email, "Verify");


                    String houseNumber = homeAddressSnapshot.child("house_number").getValue(String.class);
                    String street = homeAddressSnapshot.child("street").getValue(String.class);
                    String barangay = homeAddressSnapshot.child("barangay").getValue(String.class);
                    String province = homeAddressSnapshot.child("province").getValue(String.class);
                    String homeAddress = formatHomeAddress(houseNumber, street, barangay, province);
                    setInfoItemWithButtonValue(findViewById(R.id.layoutHomeAddress), "Home address", homeAddress, "Change");

                } else {
                    Toast.makeText(MyAccountActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyAccountActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatHomeAddress(String houseNumber, String street, String barangay, String province) {
        StringBuilder sb = new StringBuilder();
        if (houseNumber != null) sb.append(houseNumber).append(", ");
        if (street != null) sb.append(street).append(", ");
        if (barangay != null) sb.append(barangay).append(", ");
        if (province != null) sb.append(province);
        return sb.toString().trim();
    }

    private void openEditHomeAddressDialog(String currentAddress) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Home Address");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentAddress);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newAddress = input.getText().toString();
            updateHomeAddress(newAddress);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateHomeAddress(String newAddress) {
        String[] addressParts = newAddress.split(",");
        Map<String, Object> updates = new HashMap<>();

        if (addressParts.length >= 1) updates.put("house_number", addressParts[0].trim());
        if (addressParts.length >= 2) updates.put("street", addressParts[1].trim());
        if (addressParts.length >= 3) updates.put("barangay", addressParts[2].trim());
        if (addressParts.length >= 4) updates.put("province", addressParts[3].trim());

        mDatabase.child(userId).child("home_address").updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyAccountActivity.this, "Home address updated successfully", Toast.LENGTH_SHORT).show();
                    tvHomeAddress.setText(newAddress);
                })
                .addOnFailureListener(e -> Toast.makeText(MyAccountActivity.this, "Failed to update home address", Toast.LENGTH_SHORT).show());
    }

    private void copyAccountNumber() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Account Number", tvAccountNumber.getText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Account number copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}