package com.example.leenk;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AllTransactionsActivity extends AppCompatActivity {

    private RecyclerView rvAllTransactions;
    private TransactionAdapter adapter;
    private List<UserTransaction> transactions;
    private Button btnSortDate, btnSortAmount, btnBankStatement;
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
        btnSortAmount = findViewById(R.id.btnSortAmount);
        btnBankStatement = findViewById(R.id.btnBankStatement);
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

        // Set up sorting buttons with dropdown menus
        setupSortingButtons();

        // Set up bank statement button
        setupBankStatementButton();

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


    private void setupSortingButtons() {
        btnSortDate.setOnClickListener(v -> showDateSortingMenu());

        btnSortAmount.setOnClickListener(v -> showAmountSortingMenu());
    }


    private void showDateSortingMenu() {
        PopupMenu popup = new PopupMenu(this, btnSortDate);
        popup.getMenuInflater().inflate(R.menu.menu_sort_date, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.sort_recent) {
                sortByDate(true);
            } else if (itemId == R.id.sort_oldest) {
                sortByDate(false);
            }
            return true;
        });
        popup.show();
    }



    private void showAmountSortingMenu() {
        PopupMenu popup = new PopupMenu(this, btnSortAmount);
        popup.getMenuInflater().inflate(R.menu.menu_sort_amount, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.sort_highest) {
                sortByAmount(true);
            } else if (itemId == R.id.sort_lowest) {
                sortByAmount(false);
            }
            return true;
        });
        popup.show();
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

    private void sortByDate(boolean recent) {
        Collections.sort(transactions, (t1, t2) ->
                recent ? Long.compare(t2.getTimestamp(), t1.getTimestamp())
                        : Long.compare(t1.getTimestamp(), t2.getTimestamp()));
        adapter.notifyDataSetChanged();
    }

    private void setupBankStatementButton() {
        btnBankStatement.setOnClickListener(v -> {
            Intent intent = new Intent(AllTransactionsActivity.this, BankStatementActivity.class);
            intent.putExtra("USER_ID", getIntent().getStringExtra("USER_ID"));
            startActivity(intent);
        });
    }

    private void showBankStatementMenu() {
        PopupMenu popup = new PopupMenu(this, btnBankStatement);
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            popup.getMenu().add(monthName);
            calendar.add(Calendar.MONTH, -1);
        }
        popup.setOnMenuItemClickListener(item -> {
            filterTransactionsByMonth(item.getTitle().toString());
            return true;
        });
        popup.show();
    }

    private void filterTransactionsByMonth(String month) {
        List<UserTransaction> filteredList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (UserTransaction transaction : transactions) {
            calendar.setTimeInMillis(transaction.getTimestamp());
            String transactionMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            if (transactionMonth.equals(month)) {
                filteredList.add(transaction);
            }
        }
        adapter.updateList(filteredList);
    }


    private void sortByAmount(boolean highest) {
        Collections.sort(transactions, (t1, t2) ->
                highest ? Double.compare(Math.abs(t2.getAmount()), Math.abs(t1.getAmount()))
                        : Double.compare(Math.abs(t1.getAmount()), Math.abs(t2.getAmount())));
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