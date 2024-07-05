package com.example.leenk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserTransaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateList(List<UserTransaction> newList) {
        this.transactions = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvAmount, tvDescription, tvTimestamp;

        ViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(UserTransaction transaction) {
            tvType.setText(transaction.getType());
            tvAmount.setText(String.format(Locale.getDefault(), "₱ %.2f", transaction.getAmount()));
            tvDescription.setText(transaction.getDescription());
            tvTimestamp.setText(formatDate(transaction.getTimestamp()));

            // Set color based on transaction type
            int color = transaction.getType().equalsIgnoreCase("deposit") ?
                    itemView.getContext().getColor(R.color.green) :
                    itemView.getContext().getColor(R.color.red);
            tvAmount.setTextColor(color);
        }

        private String formatDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}