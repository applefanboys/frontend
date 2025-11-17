package com.example.stocksapp.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.StockTip;

import java.util.ArrayList;
import java.util.List;

public class StockTipAdapter extends RecyclerView.Adapter<StockTipAdapter.StockTipViewHolder> {

    private final List<StockTip> items = new ArrayList<>();

    public void setItems(List<StockTip> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StockTipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_tip, parent, false);
        return new StockTipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockTipViewHolder holder, int position) {
        StockTip item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StockTipViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        StockTipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
