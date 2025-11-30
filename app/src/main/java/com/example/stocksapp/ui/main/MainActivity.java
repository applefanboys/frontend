package com.example.stocksapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.data.repo.FakeFeedRepository;
import com.example.stocksapp.ui.main.adapter.NewsAdapter;
import com.example.stocksapp.ui.main.adapter.StockTipAdapter;
import com.example.stocksapp.ui.main.adapter.TopicCardAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvNews;
    private RecyclerView rvStockTip;
    private RecyclerView rvTopicCard;
    private ProgressBar progressBar;

    private NewsAdapter newsAdapter;
    private StockTipAdapter stockTipAdapter;
    private TopicCardAdapter topicCardAdapter;

    private ArrayList<String> categories;
    private ArrayList<String> newsTypes;
    private ArrayList<String> themes;
    private String profileType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        categories = intent.getStringArrayListExtra("categories");
        newsTypes = intent.getStringArrayListExtra("newsTypes");
        themes = intent.getStringArrayListExtra("themes");
        profileType = intent.getStringExtra("profileType");

        rvNews = findViewById(R.id.rvNews);
        rvStockTip = findViewById(R.id.rvStockTip);
        rvTopicCard = findViewById(R.id.rvTopicCard);
        progressBar = findViewById(R.id.progressBar);

        rvNews.setLayoutManager(new LinearLayoutManager(this));
        rvStockTip.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTopicCard.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        newsAdapter = new NewsAdapter(this, item -> {
            Intent audioIntent = new Intent(MainActivity.this, AudioNewsActivity.class);
            startActivity(audioIntent);
        });

        stockTipAdapter = new StockTipAdapter();
        topicCardAdapter = new TopicCardAdapter();

        rvNews.setAdapter(newsAdapter);
        rvStockTip.setAdapter(stockTipAdapter);
        rvTopicCard.setAdapter(topicCardAdapter);

        loadFeed();
    }

    private void loadFeed() {
        progressBar.setVisibility(View.VISIBLE);

        FeedResponse fakeData = FakeFeedRepository.getFeed();

        if (fakeData != null) {
            newsAdapter.setItems(fakeData.getNews());
            stockTipAdapter.setItems(fakeData.getStockTips());
            topicCardAdapter.setItems(fakeData.getTopics());

            String message = "오늘의 추천 뉴스를 가져왔습니다 📈";
            if (categories != null && !categories.isEmpty()) {
                message = categories.get(0) + " 관련 추천 뉴스를 가져왔습니다 📈";
            }
            showCustomToast(message);
        } else {
            showCustomToast("데이터를 가져오지 못했습니다.");
        }

        progressBar.setVisibility(View.GONE);
    }

    private void showCustomToast(String message) {
        android.view.LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.view_custom_toast, null);

        android.widget.TextView text = layout.findViewById(R.id.tvToastMessage);
        text.setText(message);

        android.widget.Toast toast = new android.widget.Toast(getApplicationContext());
        toast.setDuration(android.widget.Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
