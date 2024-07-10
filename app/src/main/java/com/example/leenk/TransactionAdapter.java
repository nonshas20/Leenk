package com.example.leenk;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<UserTransaction> transactions;
    private Context context;

    // Updated constructor
    public TransactionAdapter(List<UserTransaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserTransaction transaction = transactions.get(position);
        holder.bind(transaction);

        holder.itemView.setOnClickListener(v -> showTransactionDetails(transaction));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateList(List<UserTransaction> newList) {
        this.transactions = newList;
        notifyDataSetChanged();
    }

    private void showTransactionDetails(UserTransaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Transaction Details");

        String details = String.format(
                "Type: %s\nAmount: ₱ %.2f\nDescription: %s\nDate: %s\nPayment Method: %s\nReference ID: %s",
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                formatDate(transaction.getTimestamp()),
                transaction.getPaymentMethod(),
                transaction.getReferenceId()
        );

        builder.setMessage(details);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTransactionIcon;
        TextView tvType, tvAmount, tvDescription, tvTimestamp;

        ViewHolder(View itemView) {
            super(itemView);
            ivTransactionIcon = itemView.findViewById(R.id.ivTransactionIcon);
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

            // Set icon based on transaction type
            int iconResourceId;
            switch (transaction.getType().toLowerCase()) {
                case "transfer":
                    iconResourceId = R.drawable.transfer1;
                    break;
                case "bills":
                    iconResourceId = R.drawable.bills;
                    break;
                case "load":
                    iconResourceId = R.drawable.load;
                    break;
                case "deposit":
                    iconResourceId = R.drawable.deposit;
                    break;
                case "bank":
                    iconResourceId = R.drawable.bank;
                    break;
                default:
                    iconResourceId = R.drawable.bank; // You should create a default icon
                    break;
            }
            ivTransactionIcon.setImageResource(iconResourceId);

            // Set color based on transaction type
            int color = transaction.getType().equalsIgnoreCase("deposit") ?
                    itemView.getContext().getColor(R.color.green) :
                    itemView.getContext().getColor(R.color.red);
            tvAmount.setTextColor(color);
        }
    }



    private static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}