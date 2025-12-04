package com.example.stocksapp.ui.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.data.model.NewsItem;
import com.example.stocksapp.data.model.StockTip;
import com.example.stocksapp.data.model.TopicCard;
import com.example.stocksapp.data.repo.FakeFeedRepository;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.main.AudioNewsActivity;
import com.example.stocksapp.ui.main.adapter.NewsAdapter;
import com.example.stocksapp.ui.main.adapter.StockTipAdapter;
import com.example.stocksapp.ui.main.adapter.TopicCardAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvMainTitle;
    private TextView tvKeywordLine;
    private TextView tvSectionTopic;
    private TextView tvSectionStock;
    private TextView tvSectionNews;

    private RecyclerView rvNews;
    private RecyclerView rvStockTip;
    private RecyclerView rvTopicCard;
    private ProgressBar progressBar;

    private NewsAdapter newsAdapter;
    private StockTipAdapter stockTipAdapter;
    private TopicCardAdapter topicCardAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvMainTitle = view.findViewById(R.id.tvMainTitle);
        tvKeywordLine = view.findViewById(R.id.tvKeywordLine);
        tvSectionTopic = view.findViewById(R.id.tvSectionTopic);
        tvSectionStock = view.findViewById(R.id.tvSectionStock);
        tvSectionNews = view.findViewById(R.id.tvSectionNews);

        rvNews = view.findViewById(R.id.rvNews);
        rvStockTip = view.findViewById(R.id.rvStockTip);
        rvTopicCard = view.findViewById(R.id.rvTopicCard);
        progressBar = view.findViewById(R.id.progressBar);

        rvNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStockTip.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvTopicCard.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        // 어댑터는 context / listener만 넣어서 생성
        newsAdapter = new NewsAdapter(requireContext(), item -> {
            Intent intent = new Intent(requireContext(), AudioNewsActivity.class);
            intent.putExtra("news_title", item.getTitle());
            startActivity(intent);
        });

        stockTipAdapter = new StockTipAdapter();
        topicCardAdapter = new TopicCardAdapter();

        rvNews.setAdapter(newsAdapter);
        rvStockTip.setAdapter(stockTipAdapter);
        rvTopicCard.setAdapter(topicCardAdapter);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        loadNewsFromServer();

        loadFeed();

        return view;
    }

    private void loadNewsFromServer() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getTodayNews().enqueue(new Callback<List<NewsItem>>() {
            @Override
            public void onResponse(Call<List<NewsItem>> call, Response<List<NewsItem>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> newsList = response.body();
                    newsAdapter.setItems(newsList);
                    tvKeywordLine.setText("추천된 실시간 경제 뉴스입니다.");
                } else {
                    tvKeywordLine.setText("뉴스 데이터를 불러오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(Call<List<NewsItem>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvKeywordLine.setText("서버 연결 실패");
            }
        });
    }

    private void loadFeed() {
        progressBar.setVisibility(View.VISIBLE);

        FeedResponse feed = FakeFeedRepository.getFeed();

        if (feed != null) {
            List<NewsItem> newsList = feed.getNews();
            List<StockTip> stockTips = feed.getStockTips();
            List<TopicCard> topicCards = feed.getTopics();

            if (newsList == null) newsList = new ArrayList<>();
            if (stockTips == null) stockTips = new ArrayList<>();
            if (topicCards == null) topicCards = new ArrayList<>();

            // 여기서 setItems 로 데이터 넣어줌
            newsAdapter.setItems(newsList);
            stockTipAdapter.setItems(stockTips);
            topicCardAdapter.setItems(topicCards);

            tvKeywordLine.setText("선호 키워드 기반으로 추천된 뉴스입니다");
        } else {
            tvKeywordLine.setText("데이터를 불러오지 못했습니다");
        }

        progressBar.setVisibility(View.GONE);
    }
}
