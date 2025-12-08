package com.example.stocksapp.ui.main.adapter;

import android.content.Context;
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

public class PersonalizedNewsCardAdapter extends RecyclerView.Adapter<PersonalizedNewsCardAdapter.CardViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(NewsItem item);
    }

    private final Context context;
    private final List<NewsItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public PersonalizedNewsCardAdapter(Context context, OnItemClickListener listener) {
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
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        NewsItem item = items.get(position);

        // CardView의 제목
        holder.tvTitle.setText(item.getTitle());

        // 썸네일 URL 우선순위: originUrl -> url
        String imageUrl = item.getOriginUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = item.getUrl();
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.ivImage);


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

    static class CardViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        TextView tvTitle;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
