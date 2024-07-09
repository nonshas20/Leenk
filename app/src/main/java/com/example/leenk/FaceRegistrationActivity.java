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
import com.google.mlkit.vision.face.FaceLandmark;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Size;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.ExecutionException;

public class FaceRegistrationActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FaceDetector faceDetector;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_registration);

        previewView = findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // Retrieve userId from the Intent
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();

        faceDetector = FaceDetection.getClient(options);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                InputImage inputImage = InputImage.fromMediaImage(image.getImage(), rotationDegrees);

                faceDetector.process(inputImage)
                        .addOnSuccessListener(faces -> {
                            if (!faces.isEmpty()) {
                                saveFaceData(faces.get(0));
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle any errors
                        })
                        .addOnCompleteListener(task -> image.close());
            }
        });

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void saveFaceData(Face face) {
        // Extract landmarks from the face object
        Map<String, Float> landmarks = new HashMap<>();
        for (FaceLandmark landmark : face.getAllLandmarks()) {
            if (landmark != null && landmark.getPosition() != null) {
                int landmarkType = landmark.getLandmarkType();
                landmarks.put("landmark_" + landmarkType + "_x", landmark.getPosition().x);
                landmarks.put("landmark_" + landmarkType + "_y", landmark.getPosition().y);
            }
        }

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .child("faceData")
                .setValue(landmarks)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(FaceRegistrationActivity.this, "Face data saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FaceRegistrationActivity.this, CreatePasscodeActivity.class);
                    intent.putExtra("USER_ID", userId);  // Pass the userId to the next activity
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FaceRegistrationActivity.this, "Failed to save face data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
