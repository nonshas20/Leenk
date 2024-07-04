package com.example.leenk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    private TextView tvAccountNumber, tvFullName, tvDateOfBirth, tvCountryOfBirth,
            tvIdNumber, tvUsername, tvMobileNumber, tvEmailAddress, tvHomeAddress;
    private ImageButton btnBack, btnCopy;
    private TabLayout tabLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvFullName = findViewById(R.id.layoutFullName).findViewById(R.id.tvValue);
        tvDateOfBirth = findViewById(R.id.layoutDateOfBirth).findViewById(R.id.tvValue);
        tvCountryOfBirth = findViewById(R.id.layoutCountryOfBirth).findViewById(R.id.tvValue);
        tvIdNumber = findViewById(R.id.layoutIdNumber).findViewById(R.id.tvValue);
        tvUsername = findViewById(R.id.layoutUsername).findViewById(R.id.tvValue);
        tvMobileNumber = findViewById(R.id.layoutMobileNumber).findViewById(R.id.tvValue);
        tvEmailAddress = findViewById(R.id.layoutEmailAddress).findViewById(R.id.tvValue);
        tvHomeAddress = findViewById(R.id.layoutHomeAddress).findViewById(R.id.tvValue);

        btnBack = findViewById(R.id.btnBack);
        btnCopy = findViewById(R.id.btnCopy);
        tabLayout = findViewById(R.id.tabLayout);

        // Set up TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Info"));
        tabLayout.addTab(tabLayout.newTab().setText("Limits"));
        tabLayout.addTab(tabLayout.newTab().setText("Settings"));

        // Set up click listeners
        btnBack.setOnClickListener(v -> finish());

        // Load user data
        loadUserData(userId);
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
            // Handle button click (e.g., change or verify)
            Toast.makeText(MyAccountActivity.this, buttonText + " clicked for " + label, Toast.LENGTH_SHORT).show();
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

                    // ID number is not present in your structure, so we'll set it to N/A
                    setInfoItemValue(findViewById(R.id.layoutIdNumber), "ID number", "N/A");

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
                    String homeAddress = (houseNumber != null ? houseNumber + ", " : "") +
                            (street != null ? street + ", " : "") +
                            (barangay != null ? barangay + ", " : "") +
                            (province != null ? province : "");
                    setInfoItemWithButtonValue(findViewById(R.id.layoutHomeAddress), "Home address", homeAddress.trim(), "Change");

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
}