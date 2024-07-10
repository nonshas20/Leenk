package com.example.leenk;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BankStatementActivity extends AppCompatActivity implements MonthAdapter.OnMonthClickListener {

    private RecyclerView rvMonths, rvTransactions;
    private TextView tvSelectedMonth;
    private MonthAdapter monthAdapter;
    private TransactionAdapter transactionAdapter;
    private List<String> months;
    private List<UserTransaction> allTransactions;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_statement);

        userId = getIntent().getStringExtra("USER_ID");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupMonthsRecyclerView();
        setupTransactionsRecyclerView();
        loadAllTransactions();
    }

    private void initializeViews() {
        rvMonths = findViewById(R.id.rvMonths);
        rvTransactions = findViewById(R.id.rvTransactions);
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
    }

    private void setupMonthsRecyclerView() {
        months = getMonthsOf2024();
        monthAdapter = new MonthAdapter(months, this);
        rvMonths.setLayoutManager(new GridLayoutManager(this, 3));
        rvMonths.setAdapter(monthAdapter);
    }

    private void setupTransactionsRecyclerView() {
        allTransactions = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
    }

    private List<String> getMonthsOf2024() {
        List<String> months = new ArrayList<>();
        String[] monthNames = new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (String month : monthNames) {
            months.add(month + " 2024");
        }
        return months;
    }

    private void loadAllTransactions() {
        mDatabase.child("users").child(userId).child("transactions")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        allTransactions.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            UserTransaction transaction = snapshot.getValue(UserTransaction.class);
                            if (transaction != null) {
                                allTransactions.add(transaction);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle possible errors.
                    }
                });
    }

    @Override
    public void onMonthClick(String month) {
        tvSelectedMonth.setText(month);
        tvSelectedMonth.setVisibility(View.VISIBLE);
        filterTransactionsByMonth(month);
    }

    private void filterTransactionsByMonth(String monthYear) {
        List<UserTransaction> filteredTransactions = new ArrayList<>();
        String[] parts = monthYear.split(" ");
        String month = parts[0];
        int year = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        for (UserTransaction transaction : allTransactions) {
            calendar.setTimeInMillis(transaction.getTimestamp());
            if (calendar.get(Calendar.YEAR) == year &&
                    calendar.get(Calendar.MONTH) == getMonthNumber(month)) {
                filteredTransactions.add(transaction);
            }
        }

        transactionAdapter.updateList(filteredTransactions);
    }

    private int getMonthNumber(String monthName) {
        String[] months = new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(monthName)) {
                return i;
            }
        }
        return -1;
    }
}