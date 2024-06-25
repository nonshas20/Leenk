package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.example.leenk.CitizenshipQuestionActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            // TODO: Implement login functionality
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CitizenshipQuestionActivity.class);
            startActivity(intent);
        });
    }
}