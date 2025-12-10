package com.example.stocksapp.ui.main.topics;

import android.util.Log;
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

    private static final String TAG = "NewsReelsAdapter";

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

        // 🔥 1순위: TTS가 헤더로 준 imageUrl
        String imageUrl = item.getImageUrl();

        // 🔁 혹시 이미지 못 구했으면, originUrl이 이미지일 수도 있으니 한 번 더 체크 (옵션)
        if ((imageUrl == null || imageUrl.trim().isEmpty())
                && item.getOriginUrl() != null) {
            String candidate = item.getOriginUrl().trim();
            if (candidate.endsWith(".jpg") || candidate.endsWith(".jpeg")
                    || candidate.endsWith(".png") || candidate.endsWith(".webp")) {
                imageUrl = candidate;
            }
        }

        Log.d(TAG, "bind pos=" + position
                + ", imageUrl=" + imageUrl
                + ", originUrl=" + item.getOriginUrl());

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_background);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.ivImage);
        }
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
