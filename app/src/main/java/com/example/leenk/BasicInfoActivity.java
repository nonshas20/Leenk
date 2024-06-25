package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BasicInfoActivity extends AppCompatActivity {

    private EditText etFirstName, etMiddleName, etLastName, etDateOfBirth;
    private Button btnSubmit;
    private ImageButton btnBack;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_info);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> submitBasicInfo());
    }

    private void submitBasicInfo() {
        String firstName = etFirstName.getText().toString().trim();
        String middleName = etMiddleName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = mDatabase.child("users").child(userId).child("basic_info");

        userRef.child("first_name").setValue(firstName);
        userRef.child("middle_name").setValue(middleName);
        userRef.child("last_name").setValue(lastName);
        userRef.child("date_of_birth").setValue(dateOfBirth)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BasicInfoActivity.this, "Basic information saved successfully", Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to the next activity or finish the process
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(BasicInfoActivity.this, "Failed to save information", Toast.LENGTH_SHORT).show());
    }
}