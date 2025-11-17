package com.example.stocksapp.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.FeedRequest;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.main.adapter.NewsAdapter;
import com.example.stocksapp.ui.main.adapter.StockTipAdapter;
import com.example.stocksapp.ui.main.adapter.TopicCardAdapter;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvNews;
    private RecyclerView rvStockTip;
    private RecyclerView rvTopicCard;
    private ProgressBar progressBar;

    private NewsAdapter newsAdapter;
    private StockTipAdapter stockTipAdapter;
    private TopicCardAdapter topicCardAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvNews = findViewById(R.id.rvNews);
        rvStockTip = findViewById(R.id.rvStockTip);
        rvTopicCard = findViewById(R.id.rvTopicCard);
        progressBar = findViewById(R.id.progressBar);

        rvNews.setLayoutManager(new LinearLayoutManager(this));
        rvStockTip.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTopicCard.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        newsAdapter = new NewsAdapter(this);
        stockTipAdapter = new StockTipAdapter();
        topicCardAdapter = new TopicCardAdapter();

        rvNews.setAdapter(newsAdapter);
        rvStockTip.setAdapter(stockTipAdapter);
        rvTopicCard.setAdapter(topicCardAdapter);

        loadFeed();
    }

    private void loadFeed() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService api = RetrofitClient.getApiService();
        FeedRequest request = new FeedRequest("user1", Arrays.asList("005930", "000660"));

        api.getFeed(request).enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    FeedResponse feed = response.body();
                    newsAdapter.setItems(feed.getNews());
                    stockTipAdapter.setItems(feed.getStockTips());
                    topicCardAdapter.setItems(feed.getTopics());
                } else {
                    Toast.makeText(MainActivity.this, "응답 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
