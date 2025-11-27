package com.example.stocksapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
// import android.widget.Toast; // 기본 Toast 미사용

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
        newsAdapter = new NewsAdapter(this, item -> {
            Intent intent = new Intent(MainActivity.this, AudioNewsActivity.class);
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

        // 1. 가짜 데이터 가져오기
        FeedResponse fakeData = FakeFeedRepository.getFeed();

        if (fakeData != null) {
            // 2. 데이터 세팅
            newsAdapter.setItems(fakeData.getNews());
            stockTipAdapter.setItems(fakeData.getStockTips());
            topicCardAdapter.setItems(fakeData.getTopics());

            // 🟢 [수정됨] 성공했을 때도 토스트 메시지 띄우기!
            showCustomToast("오늘의 추천 뉴스를 가져왔습니다 📈");

        } else {
            // 실패 시 토스트
            showCustomToast("데이터를 가져오지 못했습니다.");
        }

        progressBar.setVisibility(View.GONE);

        /* 네트워크 코드 (주석 처리됨)
        ApiService api = RetrofitClient.getApiService();
        ...
        api.getFeed(request).enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(...) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ...
                    // 🟢 네트워크 성공 시에도 추가 가능
                    showCustomToast("최신 뉴스로 업데이트되었습니다.");
                } else {
                    showCustomToast("응답 오류");
                }
            }
            @Override
            public void onFailure(...) {
                progressBar.setVisibility(View.GONE);
                showCustomToast("네트워크 오류");
            }
        });
        */
    }

    // 커스텀 토스트 함수
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