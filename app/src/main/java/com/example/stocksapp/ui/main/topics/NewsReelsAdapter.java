package com.example.stocksapp.ui.main.topics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        // 기존 모델에 확실히 있는 메서드는 getTitle() 뿐이라서
        // 일단 제목만 바인딩해 두고, 나머지는 추후 필요하면 직접 연결해도 됨.
        holder.tvTitle.setText(item.getTitle());

        // 만약 NewsItem에 getSummary(), getImageResId() 같은 게 있다면
        // 여기서 직접 연결해서 쓰면 된다.
        // holder.tvSummary.setText(item.getSummary());
        // holder.ivImage.setImageResource(item.getImageResId());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NewsReelViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvSummary;
        ImageView ivImage;

        NewsReelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvSummary = itemView.findViewById(R.id.tvNewsSummary);
            ivImage = itemView.findViewById(R.id.ivNewsImage);
        }
    }
}
