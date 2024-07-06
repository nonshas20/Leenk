package com.example.leenk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.graphics.Color;
import android.view.View;

public class QRScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);
        barcodeView.setStatusText("Align QR code within the square");

        // Add square guide
        View squareGuide = new View(this);
        squareGuide.setBackgroundResource(R.drawable.square_guide);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        addContentView(squareGuide, params);

        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    Intent intent = new Intent(QRScannerActivity.this, WithdrawActivity.class);
                    intent.putExtra("USER_ID", getIntent().getStringExtra("USER_ID"));
                    intent.putExtra("CURRENT_BALANCE", getIntent().getDoubleExtra("CURRENT_BALANCE", 0.0));
                    intent.putExtra("QR_RESULT", result.getText());
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}