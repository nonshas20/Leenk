package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class ScanIdSplashActivity extends AppCompatActivity {

    private Button btnScanId;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_id_splash);

        userId = getIntent().getStringExtra("USER_ID");
        btnScanId = findViewById(R.id.btnScanId);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setProgress(80); // This is the fourth step, so 80% progress

        btnBack.setOnClickListener(v -> finish());

        btnScanId.setOnClickListener(v -> {
            Intent intent = new Intent(ScanIdSplashActivity.this, SelectIdTypeActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }
}