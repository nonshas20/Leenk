package com.example.leenk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ScanIdActivity extends AppCompatActivity {
    private PreviewView cameraPreview;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private TextRecognizer textRecognizer;
    private DatabaseReference database;

    private String userId;
    private String idType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_id);

        cameraPreview = findViewById(R.id.cameraPreview);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        database = FirebaseDatabase.getInstance().getReference();

        userId = getIntent().getStringExtra("USER_ID");
        idType = getIntent().getStringExtra("SELECTED_ID_TYPE");

        startCamera();
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::processImage);

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImage(ImageProxy imageProxy) {
        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        textRecognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        processRecognizedText(visionText);
                        imageProxy.close();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        imageProxy.close();
                    }
                });
    }

    private void processRecognizedText(Text visionText) {
        Map<String, String> extractedInfo;
        switch (idType) {
            case "Driver's License":
                extractedInfo = extractDriversLicenseInfo(visionText);
                break;
            case "Unified Multipurpose ID":
                extractedInfo = extractUMIDInfo(visionText);
                break;
            case "Postal ID":
                extractedInfo = extractPostalIdInfo(visionText);
                break;
            case "Voter's ID":
                extractedInfo = extractVotersIdInfo(visionText);
                break;
            case "Philippine Passport":
                extractedInfo = extractPassportInfo(visionText);
                break;
            case "Philippine National ID":
                extractedInfo = extractNationalIdInfo(visionText);
                break;
            default:
                extractedInfo = new HashMap<>();
        }

        if (!extractedInfo.isEmpty()) {
            storeInFirebase(extractedInfo);
        }
    }

    private Map<String, String> extractDriversLicenseInfo(Text visionText) {
        Map<String, String> info = new HashMap<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String text = block.getText().toUpperCase();
            if (text.contains("NAME")) {
                info.put("name", text.substring(text.indexOf("NAME") + 4).trim());
            } else if (text.contains("LICENSE NO")) {
                info.put("licenseNumber", text.substring(text.indexOf("LICENSE NO") + 10).trim());
            }
            else if (text.contains("Last Name")) {
                info.put("LastName", text.substring(text.indexOf("Last Name") + 10).trim());
            }
            else if (text.contains("Given Names")) {
                info.put("GivenNames", text.substring(text.indexOf("Given Names") + 10).trim());
            }
            else if (text.contains("Middle Name")) {
                info.put("MiddleName", text.substring(text.indexOf("Middle Name") + 10).trim());
            }
            else if (text.contains("Date of Birth")) {
                info.put("DateofBirth", text.substring(text.indexOf("Date of Birth") + 10).trim());
            }
            // Add more fields as needed
        }
        return info;
    }

    private Map<String, String> extractUMIDInfo(Text visionText) {
        Map<String, String> info = new HashMap<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String text = block.getText().toUpperCase();
            if (text.contains("NAME")) {
                info.put("name", text.substring(text.indexOf("NAME") + 4).trim());
            } else if (text.contains("CRN")) {
                info.put("crn", text.substring(text.indexOf("CRN") + 3).trim());
            }
            // Add more fields as needed
        }
        return info;
    }

    private Map<String, String> extractPostalIdInfo(Text visionText) {
        Map<String, String> info = new HashMap<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String text = block.getText().toUpperCase();
            if (text.contains("NAME")) {
                info.put("name", text.substring(text.indexOf("NAME") + 4).trim());
            } else if (text.contains("POSTAL ID NO")) {
                info.put("postalIdNumber", text.substring(text.indexOf("POSTAL ID NO") + 12).trim());
            }
            // Add more fields as needed
        }
        return info;
    }

    private Map<String, String> extractVotersIdInfo(Text visionText) {
        Map<String, String> info = new HashMap<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String text = block.getText().toUpperCase();
            if (text.contains("NAME")) {
                info.put("name", text.substring(text.indexOf("NAME") + 4).trim());
            } else if (text.contains("VOTER'S ID NO")) {
                info.put("votersIdNumber", text.substring(text.indexOf("VOTER'S ID NO") + 13).trim());
            }
            // Add more fields as needed
        }
        return info;
    }

    private Map<String, String> extractPassportInfo(Text visionText) {
        Map<String, String> info = new HashMap<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String text = block.getText().toUpperCase();
            if (text.contains("SURNAME")) {
                info.put("surname", text.substring(text.indexOf("SURNAME") + 7).trim());
            } else if (text.contains("GIVEN NAMES")) {
                info.put("givenNames", text.substring(text.indexOf("GIVEN NAMES") + 11).trim());
            } else if (text.contains("PASSPORT NO")) {
                info.put("passportNumber", text.substring(text.indexOf("PASSPORT NO") + 11).trim());
            }
            // Add more fields as needed
        }
        return info;
    }

    private Map<String, String> extractNationalIdInfo(Text visionText) {
        Map<String, String> info = new HashMap<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String text = block.getText().toUpperCase();
            if (text.contains("PCN")) {
                info.put("pcn", text.substring(text.indexOf("PCN") + 3).trim());
            } else if (text.contains("LAST NAME")) {
                info.put("lastName", text.substring(text.indexOf("LAST NAME") + 9).trim());
            } else if (text.contains("FIRST NAME")) {
                info.put("firstName", text.substring(text.indexOf("FIRST NAME") + 10).trim());
            }
            // Add more fields as needed
        }
        return info;
    }

    private void storeInFirebase(Map<String, String> data) {
        database.child("users").child(userId).child("idInfo").setValue(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ScanIdActivity.this, "ID information saved successfully", Toast.LENGTH_SHORT).show();

                        // Create an intent to start BasicInfoActivity
                        Intent intent = new Intent(ScanIdActivity.this, BasicInfoActivity.class);
                        intent.putExtra("USER_ID", userId);
                        intent.putExtra("SELECTED_ID_TYPE", idType);

                        // Pass the scanned data
                        for (Map.Entry<String, String> entry : data.entrySet()) {
                            intent.putExtra(entry.getKey(), entry.getValue());
                        }

                        // Start BasicInfoActivity
                        startActivity(intent);

                        // Finish current activity
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanIdActivity.this, "Failed to save ID information", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}