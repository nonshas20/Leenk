package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CitizenshipQuestionActivity extends AppCompatActivity {

    private Button btnYes, btnNo, btnNext;
    private ImageButton btnClose;
    private ProgressBar progressBar;
    private DatabaseReference mDatabase;
    private String selectedOption = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citizenship_question);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        btnNext = findViewById(R.id.btnNext);
        btnClose = findViewById(R.id.btnClose);
        progressBar = findViewById(R.id.progressBar);

        btnYes.setOnClickListener(v -> selectOption("Yes"));
        btnNo.setOnClickListener(v -> selectOption("No"));

        btnNext.setOnClickListener(v -> {
            if (!selectedOption.isEmpty()) {
                saveToDatabase();
                Intent intent = new Intent(CitizenshipQuestionActivity.this, MobileVerificationActivity.class);
                startActivity(intent);
            }
        });

        btnClose.setOnClickListener(v -> finish());

        // Set initial progress (assuming this is the first step of registration)
        progressBar.setProgress(20);
    }

    private void selectOption(String option) {
        selectedOption = option;
        btnYes.setBackgroundResource(option.equals("Yes") ? R.drawable.button_selected : R.drawable.button_background);
        btnNo.setBackgroundResource(option.equals("No") ? R.drawable.button_selected : R.drawable.button_background);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    private void saveToDatabase() {
        String userId = mDatabase.push().getKey();
        mDatabase.child(userId).child("isFilipinoitizen").setValue(selectedOption);
    }

}