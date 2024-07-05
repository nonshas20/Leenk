package com.example.leenk;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllTransactionsActivity extends AppCompatActivity {

    private RecyclerView rvAllTransactions;
    private TransactionAdapter adapter;
    private List<UserTransaction> transactions;
    private Button btnSortDate, btnSortTransaction, btnSortAmount;
    private EditText etSearch;
    private DatabaseReference mDatabase;
    private TextView tvBalance, tvAccountNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transactions);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        rvAllTransactions = findViewById(R.id.rvAllTransactions);
        btnSortDate = findViewById(R.id.btnSortDate);
        btnSortTransaction = findViewById(R.id.btnSortTransaction);
        btnSortAmount = findViewById(R.id.btnSortAmount);
        etSearch = findViewById(R.id.etSearch);
        tvBalance = findViewById(R.id.tvBalance);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);

        // Set up RecyclerView
        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(transactions);
        rvAllTransactions.setAdapter(adapter);
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(this));

        // Get and display balance and account number
        double balance = getIntent().getDoubleExtra("BALANCE", 0.0);
        String accountNumber = getIntent().getStringExtra("ACCOUNT_NUMBER");
        tvBalance.setText(String.format("₱ %.2f", balance));
        tvAccountNumber.setText(accountNumber);

        // Load transactions from Firebase
        loadTransactions();

        // Set up sorting buttons
        btnSortDate.setOnClickListener(v -> sortByDate());
        btnSortTransaction.setOnClickListener(v -> sortByTransactionType());
        btnSortAmount.setOnClickListener(v -> sortByAmount());

        // Set up search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTransactions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadTransactions() {
        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            // Handle the case where USER_ID is not provided
            return;
        }

        mDatabase.child("users").child(userId).child("transactions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        transactions.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            UserTransaction transaction = snapshot.getValue(UserTransaction.class);
                            if (transaction != null) {
                                transactions.add(transaction);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle possible errors.
                    }
                });
    }

    private void sortByDate() {
        Collections.sort(transactions, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
        adapter.notifyDataSetChanged();
    }

    private void sortByTransactionType() {
        Collections.sort(transactions, (t1, t2) -> t1.getType().compareTo(t2.getType()));
        adapter.notifyDataSetChanged();
    }

    private void sortByAmount() {
        Collections.sort(transactions, (t1, t2) -> Double.compare(Math.abs(t2.getAmount()), Math.abs(t1.getAmount())));
        adapter.notifyDataSetChanged();
    }

    private void filterTransactions(String query) {
        List<UserTransaction> filteredList = new ArrayList<>();
        for (UserTransaction transaction : transactions) {
            if (transaction.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                    transaction.getType().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(transaction);
            }
        }
        adapter.updateList(filteredList);
    }
}