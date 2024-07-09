package com.example.leenk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BankTransferActivity extends AppCompatActivity implements BankAdapter.OnBankClickListener {

    private RecyclerView rvBanks;
    private EditText etSearch;
    private List<String> bankList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_transfer);

        rvBanks = findViewById(R.id.rvBanks);
        etSearch = findViewById(R.id.etSearch);

        initializeBankList();
        setupRecyclerView();
        setupSearchListener();
    }

    private void initializeBankList() {
        bankList = Arrays.asList(
                "Robinsons Bank Corporation",
                "Banco De Oro Unibank, Inc (BDO)",
                "Bank Of The Philippine Islands (BPI)",
                "G-Xchange, Inc (GCash)",
                "Union Bank of The Philippines",
                "Luzon Development Bank",
                "Paymongo Payments, Inc.",
                "Banana Fintech/BananaPay",
                "Own Bank - Rural Bank of Cavite City Inc.",
                "Infoserve/Nationlink",
                "CARD MRI",
                "City Savings Bank",
                "Rural Bank of Narciso, Inc.",
                "Rural Bank of Silay City, Inc.",
                "Agribusiness Rural Bank, Inc.",
                "Tagcash Ltd., Inc.",
                "Vigan Banco Rural Incorporada",
                "EASYPAY GLOBAL EMI CORP.",
                "Zambales Rural Bank, Inc. (Zambank)",
                "METROPOLITAN BANK AND TRUST CO",
                "Asia United Bank",
                "Philippine Savings Bank",
                "AllBank Inc.",
                "BDO Network",
                "Banko",
                "Banko Mabuhay",
                "PayMaya",
                "Land Bank of The Philippines",
                "Philippine National Bank (PNB)",
                "China Banking Corporation",
                "Rizal Commercial Banking Corp (RCBC)",
                "Security Bank Corporation",
                "Maya Bank, Inc",
                "Al-Amanah Islamic Bank",
                "Australia & New Zealand Bank",
                "Bangko Kabayan Inc",
                "Bangko Nuestra Senora Del Pilar",
                "Bangkok Bank Public Co. Ltd",
                "Bank Of America, Nat'L. Ass'N",
                "Bank Of China Hong Kong Limited",
                "Bank Of Commerce (BOC)",
                "Bank Of Florida",
                "Bank Of Makati",
                "Binan Rural Bank, Inc",
                "Camalig Bank",
                "Cantilan Bank, Inc",
                "Cathay United Bank Co Ltd",
                "Cebuana Lhuillier Rural Bank, Inc",
                "China Bank Savings",
                "CIMB Bank",
                "Citibank, N.A",
                "Netbank",
                "Cooperative Bank Of Quezon Province",
                "Country Builders Bank, Inc",
                "CTBC Bank (Philippines) Corp",
                "DCPay Philippines, Inc (Coins PH)",
                "Deutsche Bank",
                "Devt. Bank Of The Philippines",
                "Dumaguete City Development Bank, Inc",
                "Dungganon Bank, Inc",
                "East West Rural Bank",
                "East-West Banking Corporation",
                "Equicom Savings Bank, Inc",
                "First Consolidated Bank",
                "Guagua Rural Bank, Inc",
                "Hongkong And Shanghai Banking Corp",
                "Hsbc Savings Bank Phils, Inc",
                "Industrial And Commercial Bank Of China",
                "Industrial Bank Of Korea - Manila",
                "ING Bank N.V.",
                "Innovative Bank, Inc",
                "JPMorgan Chase Bank",
                "Keb Hana Bank",
                "Laguna Prestige Banking Corporation",
                "Lulu Financial Services (Phils), Inc",
                "Malarayat Rural Bank, Inc",
                "Malayan Savings Bank, Inc",
                "Maybank Phils, Inc",
                "Mega Intl Comml Bank Co. Ltd",
                "Mizuho Bank, Ltd",
                "Money Mall Rural Bank, Inc",
                "MUFG Bank, Ltd",
                "MVSM Bank, Inc",
                "Phil. Bank Of Communications",
                "Philippine Business Bank",
                "Philippine Trust Company",
                "Philippine Veterans Bank",
                "Producers Savings Bank",
                "Rang-Ay Bank, Inc",
                "Queen City Development Bank",
                "RBT Bank, Inc",
                "Rural Bank Of Bauang, Inc",
                "Rural Bank Of Digos, Inc",
                "Rural Bank Of Guinobatan",
                "Rural Bank Of Lebak (Sultan Kudarat), Inc",
                "Rural Bank Of Montalban, Inc",
                "Rural Bank Of Porac (Pamp), Inc",
                "Rural Bank Of Rosario (La Union), Inc",
                "Rural Bank Of Sta. Ignacia, Inc",
                "Shinhan Bank",
                "Sterling Bank Of Asia",
                "Sumitomo Mitsui Banking Corp",
                "The Standard Chartered Bank",
                "Tonik Digital Bank, Inc",
                "United Coconut Planters Bank (UCPB)",
                "United Overseas Bank Phils",
                "USSC Money Services, Inc",
                "Wealth Development Bank, Inc",
                "Yuanta Savings Bank, Inc",
                "Zybi Tech, Inc",
                "New Rural Bank Of San Leonardo (N.E.), Inc",
                "Rural Bank Of Bacolod City, Inc",
                "Rural Bank Of Sagay, Inc",
                "Rural Bank Of La Paz, Inc",
                "Seabank Philippines, Inc",
                "UNIONDIGITAL BANK",
                "Tayocash Inc",
                "Unobank, Inc",
                "Shopeepay Philippines Inc",
                "Binangonan Rural Bank",
                "CARD Bank Inc."
        );
    }

    private void setupRecyclerView() {
        BankAdapter adapter = new BankAdapter(bankList, this);
        rvBanks.setLayoutManager(new LinearLayoutManager(this));
        rvBanks.setAdapter(adapter);
    }

    @Override
    public void onBankClick(String bankName) {
        Intent intent = new Intent(this, BankTransferDetailsActivity.class);
        intent.putExtra("USER_ID", getIntent().getStringExtra("USER_ID"));
        intent.putExtra("CURRENT_BALANCE", getIntent().getDoubleExtra("CURRENT_BALANCE", 0.0));
        intent.putExtra("BANK_NAME", bankName);
        startActivity(intent);
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBanks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterBanks(String query) {
        List<String> filteredList = new ArrayList<>();
        for (String bank : bankList) {
            if (bank.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(bank);
            }
        }
        ((BankAdapter) rvBanks.getAdapter()).updateList(filteredList);
    }
}
