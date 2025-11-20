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

        // 1. 종목명 설정 (getTitle()이 Symbol을 반환하도록 되어 있음)
        if (holder.tvSymbol != null) {
            holder.tvSymbol.setText(item.getTitle());
        }

        // 2. 설명 설정
        if (holder.tvDescription != null) {
            holder.tvDescription.setText(item.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StockTipViewHolder extends RecyclerView.ViewHolder {
        // 🟡 [중요] 변수 이름을 XML ID와 비슷하게 변경 (tvTitle -> tvSymbol)
        TextView tvSymbol;
        TextView tvDescription;

        StockTipViewHolder(@NonNull View itemView) {
            super(itemView);
            // 🟡 [중요] XML 파일의 ID와 정확히 일치해야 합니다.
            tvSymbol = itemView.findViewById(R.id.tvSymbol);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}