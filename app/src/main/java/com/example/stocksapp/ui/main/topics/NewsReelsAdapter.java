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

        // 🔹 위 작은 텍스트(p1 자리)는 페이지 번호처럼 표시
        //    (원래 XML에 "p1" 박아놓았으면 이 한 줄로 p1, p2, p3 ... 자동 변경)
        holder.tvTitle.setText("p" + (position + 1));

        // 🔹 아래 큰 텍스트(삼성전자… 자리)에 뉴스 요약 세팅
        String summary = item.getSummary();   // NewsItem에 summary 필드 있다고 가정
        if (summary == null || summary.trim().isEmpty()) {
            // 요약이 없으면 제목이라도 대신 보여주기
            summary = item.getTitle();
        }
        holder.tvSummary.setText(summary);

        // 이미지도 있으면 여기서 세팅
        // 예: holder.ivImage.setImageResource(item.getImageResId());
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
