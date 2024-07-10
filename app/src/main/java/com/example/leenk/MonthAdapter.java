package com.example.leenk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {

    private List<String> months;
    private OnMonthClickListener listener;

    public interface OnMonthClickListener {
        void onMonthClick(String month);
    }

    public MonthAdapter(List<String> months, OnMonthClickListener listener) {
        this.months = months;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month, parent, false);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        holder.bind(months.get(position));
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    class MonthViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonth;

        MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tvMonth);
        }

        void bind(final String month) {
            tvMonth.setText(month);
            itemView.setOnClickListener(v -> listener.onMonthClick(month));
        }
    }
}