package com.example.leenk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BankAdapter extends RecyclerView.Adapter<BankAdapter.BankViewHolder> {

    private List<String> banks;
    private OnBankClickListener listener;

    public interface OnBankClickListener {
        void onBankClick(String bankName);
    }

    public BankAdapter(List<String> banks, OnBankClickListener listener) {
        this.banks = banks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bank, parent, false);
        return new BankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BankViewHolder holder, int position) {
        holder.bind(banks.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return banks.size();
    }

    public void updateList(List<String> newList) {
        banks = newList;
        notifyDataSetChanged();
    }

    static class BankViewHolder extends RecyclerView.ViewHolder {
        TextView tvBankName;

        BankViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tvBankName);
        }

        void bind(String bankName, OnBankClickListener listener) {
            tvBankName.setText(bankName);
            itemView.setOnClickListener(v -> listener.onBankClick(bankName));
        }
    }
}
