package com.example.leenk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<UserTransaction> transactions;

    public TransactionAdapter(List<UserTransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserTransaction transaction = transactions.get(position);
        holder.tvType.setText(transaction.getType());
        holder.tvAmount.setText(String.format("₱ %.2f", transaction.getAmount()));
        holder.tvTimestamp.setText(new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date(transaction.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvAmount, tvTimestamp;

        public ViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
