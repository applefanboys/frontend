package com.example.stocksapp.ui.main.adapter;

import android.content.Context;
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

    private final Context context;
    private final OnItemClickListener listener;
    private List<StockTip> items = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(StockTip item);
    }

    public StockTipAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<StockTip> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StockTipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stock_tip, parent, false);
        return new StockTipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockTipViewHolder holder, int position) {
        StockTip item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    // ViewHolder 클래스
    static class StockTipViewHolder extends RecyclerView.ViewHolder {
        // XML 파일의 ID와 일치하도록 변수 이름을 수정
        TextView tvSymbol;
        TextView tvDescription;

        public StockTipViewHolder(@NonNull View itemView) {
            super(itemView);
            // 실제 레이아웃 파일에 있는 ID로 뷰를 찾도록 수정
            tvSymbol = itemView.findViewById(R.id.tvSymbol);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        public void bind(final StockTip item, final OnItemClickListener listener) {
            // 수정된 변수를 사용하여 실제 데이터를 뷰에 설정
            tvSymbol.setText(item.getSymbol());
            tvDescription.setText(item.getDescription());

            // 아이템 뷰 전체에 클릭 리스너 설정
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
