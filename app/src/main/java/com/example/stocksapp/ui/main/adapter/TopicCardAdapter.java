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
import com.example.stocksapp.data.model.TopicCard;

import java.util.ArrayList;
import java.util.List;

public class TopicCardAdapter extends RecyclerView.Adapter<TopicCardAdapter.TopicCardViewHolder> {

    private final Context context;
    private final OnItemClickListener listener;
    private List<TopicCard> items = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(TopicCard item);
    }

    public TopicCardAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<TopicCard> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopicCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_topic_card, parent, false);
        return new TopicCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicCardViewHolder holder, int position) {
        TopicCard item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    class TopicCardViewHolder extends RecyclerView.ViewHolder {
        // XML 파일의 ID와 일치하도록 변수 수정 (ImageView 추가)
        ImageView ivImage;
        TextView tvTitle;

        public TopicCardViewHolder(@NonNull View itemView) {
            super(itemView);
            // 실제 레이아웃 파일에 있는 ID로 뷰를 찾도록 수정
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }

        public void bind(final TopicCard item, final OnItemClickListener listener) {
            // 수정된 변수를 사용하여 실제 데이터를 뷰에 설정
            tvTitle.setText(item.getTitle());

            // Glide 라이브러리를 사용해 이미지 URL을 ImageView에 로드
            // TopicCard 모델에 getImageUrl()과 같은 메서드가 있다고 가정
            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .centerCrop()
                    .into(ivImage);

            // 아이템 뷰 전체에 클릭 리스너 설정
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
