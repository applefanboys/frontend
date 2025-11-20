package com.example.stocksapp.ui.main;

import android.content.Intent;
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
import com.example.stocksapp.data.repo.FakeFeedRepository;
import com.example.stocksapp.ui.main.adapter.NewsAdapter;
import com.example.stocksapp.ui.main.adapter.StockTipAdapter;
import com.example.stocksapp.ui.main.adapter.TopicCardAdapter;

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

        // 1. 뷰 초기화
        rvNews = findViewById(R.id.rvNews);
        rvStockTip = findViewById(R.id.rvStockTip);
        rvTopicCard = findViewById(R.id.rvTopicCard);
        progressBar = findViewById(R.id.progressBar);

        // 2. 레이아웃 매니저 설정
        rvNews.setLayoutManager(new LinearLayoutManager(this));
        rvStockTip.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTopicCard.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 3. 어댑터 초기화 및 클릭 리스너 설정
        // 뉴스를 클릭하면 AudioNewsActivity로 이동
        newsAdapter = new NewsAdapter(this, item -> {
            Intent intent = new Intent(MainActivity.this, AudioNewsActivity.class);
            // (선택사항) 나중에 데이터를 넘겨줄 때 주석 해제
            // intent.putExtra("newsTitle", item.getTitle());
            startActivity(intent);
        });

        stockTipAdapter = new StockTipAdapter();
        topicCardAdapter = new TopicCardAdapter();

        // 4. 리사이클러뷰에 어댑터 연결
        rvNews.setAdapter(newsAdapter);
        rvStockTip.setAdapter(stockTipAdapter);
        rvTopicCard.setAdapter(topicCardAdapter);

        // 5. 데이터 불러오기
        loadFeed();
    }

    private void loadFeed() {
        progressBar.setVisibility(View.VISIBLE);

        // -------------------------------------------------------------
        // [테스트 모드] FakeFeedRepository를 사용하여 가짜 데이터를 바로 보여줍니다.
        // -------------------------------------------------------------

        // 1. 가짜 데이터 가져오기
        FeedResponse fakeData = FakeFeedRepository.getFeed();

        if (fakeData != null) {
            // 2. 각 어댑터에 데이터 채워 넣기
            // 🔴 수정된 부분: 메서드 이름을 올바르게 변경했습니다.

            // getPersonal() -> getNews()로 변경 (FeedResponse에 정의된 실제 이름)
            newsAdapter.setItems(fakeData.getNews());

            // getStocks() -> getStockTips()로 변경
            stockTipAdapter.setItems(fakeData.getStockTips());

            // getTopics()는 그대로 유지
            topicCardAdapter.setItems(fakeData.getTopics());
        } else {
            Toast.makeText(this, "데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
        }

        progressBar.setVisibility(View.GONE);

        // -------------------------------------------------------------
        // [네트워크 모드] 나중에 실제 서버와 통신할 때는 아래 주석을 풀고 위 코드를 지우세요.
        // -------------------------------------------------------------
        /*
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
        */
    }
}