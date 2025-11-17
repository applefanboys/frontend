package com.example.stocksapp.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.TopicCard;

import java.util.ArrayList;
import java.util.List;

public class TopicCardAdapter extends RecyclerView.Adapter<TopicCardAdapter.TopicCardViewHolder> {

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
    public TopicCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic_card, parent, false);
        return new TopicCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicCardViewHolder holder, int position) {
        TopicCard item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TopicCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        TopicCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
