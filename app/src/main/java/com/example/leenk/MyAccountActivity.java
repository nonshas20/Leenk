package com.example.leenk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve user data
                        String accountNumber = dataSnapshot.child("accountNumber").getValue(String.class);
                        DataSnapshot basicInfoSnapshot = dataSnapshot.child("basic_info");
                        String firstName = basicInfoSnapshot.child("first_name").getValue(String.class);
                        String middleName = basicInfoSnapshot.child("middle_name").getValue(String.class);
                        String lastName = basicInfoSnapshot.child("last_name").getValue(String.class);
                        String fullName = (firstName != null ? firstName : "") + " " +
                                (middleName != null ? middleName : "") + " " +
                                (lastName != null ? lastName : "");
                        String dateOfBirth = basicInfoSnapshot.child("date_of_birth").getValue(String.class);

                        DataSnapshot homeAddressSnapshot = dataSnapshot.child("home_address");
                        String country = homeAddressSnapshot.child("country").getValue(String.class);
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String emailAddress = dataSnapshot.child("email").getValue(String.class);

                        String houseNumber = homeAddressSnapshot.child("house_number").getValue(String.class);
                        String street = homeAddressSnapshot.child("street").getValue(String.class);
                        String barangay = homeAddressSnapshot.child("barangay").getValue(String.class);
                        String province = homeAddressSnapshot.child("province").getValue(String.class);
                        String homeAddress = (houseNumber != null ? houseNumber + ", " : "") +
                                (street != null ? street + ", " : "") +
                                (barangay != null ? barangay + ", " : "") +
                                (province != null ? province : "");

                        // Set the data to views
                        tvAccountNumber.setText(accountNumber != null ? accountNumber : "N/A");
                        tvFullName.setText(!fullName.trim().isEmpty() ? fullName : "N/A");
                        tvDateOfBirth.setText(dateOfBirth != null ? dateOfBirth : "N/A");
                        tvCountryOfBirth.setText(country != null ? country : "N/A");
                        tvUsername.setText(username != null ? username : "N/A");
                        tvEmailAddress.setText(emailAddress != null ? emailAddress : "N/A");
                        tvHomeAddress.setText(!homeAddress.trim().isEmpty() ? homeAddress : "N/A");

                        // Log the data for debugging
                        Log.d("MyAccountActivity", "Account Number: " + accountNumber);
                        Log.d("MyAccountActivity", "Full Name: " + fullName);
                        Log.d("MyAccountActivity", "Date of Birth: " + dateOfBirth);
                        Log.d("MyAccountActivity", "Country: " + country);
                        Log.d("MyAccountActivity", "Username: " + username);
                        Log.d("MyAccountActivity", "Email Address: " + emailAddress);
                        Log.d("MyAccountActivity", "Home Address: " + homeAddress);
                    } else {
                        Log.d("MyAccountActivity", "No data found for user: " + userId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MyAccountActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    Log.e("MyAccountActivity", "Database Error: " + databaseError.getMessage());
                }
            });
        } else {
            Log.d("MyAccountActivity", "No user is currently logged in");
            // Handle the case where no user is logged in
            // You might want to redirect to a login screen or show a message
        }
    }
}