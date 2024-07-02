package com.example.leenk;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeAddressActivity extends AppCompatActivity {

    private Spinner spinnerCountry, spinnerProvince;
    private EditText etHouseNumber, etStreet, etBarangay;
    private Button btnConfirm;
    private ImageButton btnBack;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_address);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        userId = getIntent().getStringExtra("USER_ID");

        spinnerCountry = findViewById(R.id.spinnerCountry);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        etHouseNumber = findViewById(R.id.etHouseNumber);
        etStreet = findViewById(R.id.etStreet);
        etBarangay = findViewById(R.id.etBarangay);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        setupSpinners();

        btnBack.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> submitHomeAddress());
    }


    private void setupSpinners() {
        // Setup country spinner
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries_array, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);
        spinnerCountry.setSelection(countryAdapter.getPosition("Philippines"));

        // Setup province spinner
        ArrayAdapter<CharSequence> provinceAdapter = ArrayAdapter.createFromResource(this,
                R.array.philippines_provinces_array, android.R.layout.simple_spinner_item);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);
    }

    private void submitHomeAddress() {
        String country = spinnerCountry.getSelectedItem().toString();
        String houseNumber = etHouseNumber.getText().toString().trim();
        String street = etStreet.getText().toString().trim();
        String province = spinnerProvince.getSelectedItem().toString();
        String barangay = etBarangay.getText().toString().trim();

        if (houseNumber.isEmpty() || street.isEmpty() || barangay.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = mDatabase.child(userId).child("home_address");

        userRef.child("country").setValue(country);
        userRef.child("house_number").setValue(houseNumber);
        userRef.child("street").setValue(street);
        userRef.child("province").setValue(province);
        userRef.child("barangay").setValue(barangay)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HomeAddressActivity.this, "Home address saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HomeAddressActivity.this, CreateUsernameActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(HomeAddressActivity.this, "Failed to save address", Toast.LENGTH_SHORT).show());
    }
}