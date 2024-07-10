package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BasicInfoActivity extends AppCompatActivity {

    private EditText etFirstName, etMiddleName, etLastName;
    private Button btnDateOfBirth, btnSubmit;
    private ImageButton btnBack;
    private DatabaseReference mDatabase;
    private Calendar calendar;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_info);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        userId = getIntent().getStringExtra("USER_ID");
        calendar = Calendar.getInstance();

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        btnDateOfBirth = findViewById(R.id.btnDateOfBirth);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnDateOfBirth.setOnClickListener(v -> showDatePickerDialog());

        btnSubmit.setOnClickListener(v -> submitBasicInfo());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateButtonText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateButtonText() {
        String dateFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        btnDateOfBirth.setText(sdf.format(calendar.getTime()));
    }

    private void submitBasicInfo() {
        String firstName = etFirstName.getText().toString().trim();
        String middleName = etMiddleName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String dateOfBirth = btnDateOfBirth.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || dateOfBirth.equals("Select Date of Birth")) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user is at least 18 years old
        if (!isUserAtLeast18(dateOfBirth)) {
            Toast.makeText(this, "You must be at least 18 years old to register", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = mDatabase.child(userId).child("basic_info");

        userRef.child("first_name").setValue(firstName);
        userRef.child("middle_name").setValue(middleName);
        userRef.child("last_name").setValue(lastName);
        userRef.child("date_of_birth").setValue(dateOfBirth)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BasicInfoActivity.this, "Basic information saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BasicInfoActivity.this, HomeAddressActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(BasicInfoActivity.this, "Failed to save information", Toast.LENGTH_SHORT).show());
    }

    private boolean isUserAtLeast18(String dateOfBirth) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Calendar birthDate = Calendar.getInstance();
            birthDate.setTime(sdf.parse(dateOfBirth));

            Calendar currentDate = Calendar.getInstance();
            int age = currentDate.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

            if (currentDate.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age >= 18;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}