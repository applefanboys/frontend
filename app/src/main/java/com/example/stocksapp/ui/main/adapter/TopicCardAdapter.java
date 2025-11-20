package com.example.stocksapp.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // 만약 Glide가 없다면 이미지 로딩 부분은 주석 처리하세요.
import com.example.stocksapp.R;
import com.example.stocksapp.data.model.TopicCard;

import java.util.ArrayList;
import java.util.List;

public class TopicCardAdapter extends RecyclerView.Adapter<TopicCardAdapter.TopicViewHolder> {

    private final List<TopicCard> items = new ArrayList<>();

    public void setItems(List<TopicCard> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_topic_card.xml 레이아웃을 연결합니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic_card, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        TopicCard item = items.get(position);

        // 1. 텍스트 설정
        if (holder.tvTitle != null) {
            holder.tvTitle.setText(item.getTitle());
        }

        // 2. 이미지 설정 (Glide 라이브러리 사용 시)
        // Glide가 없다면 이 부분은 잠시 주석 처리하거나 기본 이미지를 넣으세요.
        /*
        if (holder.ivImage != null) {
            Glide.with(holder.itemView.getContext())
                 .load(item.getImageUrl())
                 .into(holder.ivImage);
        }
        */
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivImage;

        TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            // 🟡 [중요] XML 파일의 ID와 정확히 일치해야 합니다.
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }
}