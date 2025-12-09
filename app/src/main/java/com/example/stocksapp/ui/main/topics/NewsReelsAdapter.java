package com.example.stocksapp.ui.main.topics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stocksapp.R;
import com.example.stocksapp.data.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 인스타 릴스 / 쇼츠처럼 세로로 넘기는 뉴스 카드 어댑터
 */
public class NewsReelsAdapter extends RecyclerView.Adapter<NewsReelsAdapter.NewsReelViewHolder> {

    private final List<NewsItem> items = new ArrayList<>();

    public NewsReelsAdapter(List<NewsItem> data) {
        if (data != null) {
            items.addAll(data);
        }
    }

    public void setItems(List<NewsItem> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_reel, parent, false);
        return new NewsReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsReelViewHolder holder, int position) {
        NewsItem item = items.get(position);

        holder.tvTitle.setText("p" + (position + 1));

        String summary = item.getSummary();
        if (summary == null || summary.trim().isEmpty()) {
            summary = item.getTitle();
        }
        holder.tvSummary.setText(summary);

        // ★ 이미지 로딩 (originUrl 사용)
        String imageUrl = item.getOriginUrl();
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)  // 기본 이미지
                .error(R.drawable.ic_launcher_background)
                .into(holder.ivImage);
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NewsReelViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;    // p1, p2, p3 ... 자리
        TextView tvSummary;  // 삼성전자 내용 자리
        ImageView ivImage;

        NewsReelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvSummary = itemView.findViewById(R.id.tvNewsSummary);
            ivImage = itemView.findViewById(R.id.ivNewsImage);
        }
    }
}
