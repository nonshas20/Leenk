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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class FaceLoginActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FaceDetector faceDetector;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_login);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is missing. Please start over.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        previewView = findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

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
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                verifyFace(faces.get(0));
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FaceLoginActivity.this, "Face detection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnCompleteListener(task -> image.close());
            }
        });

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void verifyFace(Face detectedFace) {
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userId)
                .child("faceData")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Here you should implement a proper face comparison algorithm
                            // This is a simplified example and is not secure for production use
                            if (compareFaces(detectedFace, dataSnapshot)) {
                                Toast.makeText(FaceLoginActivity.this, "Face verified successfully", Toast.LENGTH_SHORT).show();
                                proceedToHomeDashboard();
                            } else {
                                Toast.makeText(FaceLoginActivity.this, "Face verification failed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(FaceLoginActivity.this, "No face data found for this user", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(FaceLoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean compareFaces(Face detectedFace, DataSnapshot storedFaceData) {
        // This is a placeholder for face comparison logic
        // In a real application, you would implement a proper face comparison algorithm here
        // For demonstration purposes, we're just checking if a face was detected
        return true;
    }

    private void proceedToHomeDashboard() {
        Intent intent = new Intent(FaceLoginActivity.this, HomeDashboardActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}