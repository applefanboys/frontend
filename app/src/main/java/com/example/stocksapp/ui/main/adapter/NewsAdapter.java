package com.example.stocksapp.ui.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    // 1. 클릭 이벤트를 전달하기 위한 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(NewsItem item);
    }

    private final Context context;
    private final List<NewsItem> items = new ArrayList<>();
    private final OnItemClickListener listener; // 2. 리스너 변수 추가

    // 3. 생성자 수정: 리스너를 매개변수로 받도록 변경
    public NewsAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<NewsItem> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_line, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());

        // 4. 클릭 시 직접 이동하지 않고, MainActivity(리스너)에게 알림
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}