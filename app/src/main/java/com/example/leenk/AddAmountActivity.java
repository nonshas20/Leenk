package com.example.leenk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddAmountActivity extends AppCompatActivity {

    private EditText etAmount;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_amount);

        etAmount = findViewById(R.id.etAmount);
        btnSave = findViewById(R.id.btnSave);

        double currentAmount = getIntent().getDoubleExtra("CURRENT_AMOUNT", 0.0);
        etAmount.setText(String.format("%.2f", currentAmount));

        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString();
            if (!amountStr.isEmpty()) {
                double newAmount = Double.parseDouble(amountStr);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("NEW_AMOUNT", newAmount);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}