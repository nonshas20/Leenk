package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SelectIdTypeActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_id_type);

        userId = getIntent().getStringExtra("USER_ID");

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupIdTypeItem(R.id.driversLicense, R.drawable.driversid, "Driver's License");
        setupIdTypeItem(R.id.unifiedMultipurposeId, R.drawable.umid_id, "Unified Multipurpose ID");
        setupIdTypeItem(R.id.postalId, R.drawable.postal_id, "Postal ID");
        setupIdTypeItem(R.id.votersId, R.drawable.voters_id, "Voter's ID");
        setupIdTypeItem(R.id.philippinePassport, R.drawable.passport_id, "Philippine Passport");
        setupIdTypeItem(R.id.philippineNationalId, R.drawable.national_id, "Philippine National ID");
    }

    private void setupIdTypeItem(int itemId, int iconResId, String idName) {
        View itemView = findViewById(itemId);
        ImageView ivIdIcon = itemView.findViewById(R.id.ivIdIcon);
        TextView tvIdName = itemView.findViewById(R.id.tvIdName);

        ivIdIcon.setImageResource(iconResId);
        tvIdName.setText(idName);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectIdTypeActivity.this, ScanIdActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("SELECTED_ID_TYPE", idName);
                startActivity(intent);
            }
        });
    }
}
