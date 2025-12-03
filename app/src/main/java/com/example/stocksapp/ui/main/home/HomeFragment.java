package com.example.stocksapp.ui.main.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.data.model.NewsItem;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.main.AudioNewsActivity;
import com.example.stocksapp.ui.main.adapter.NewsAdapter;
import com.example.stocksapp.ui.main.adapter.StockTipAdapter;
import com.example.stocksapp.ui.main.adapter.TopicCardAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvKeywordLine;
    private RecyclerView rvNews, rvStockTip, rvTopicCard;
    private ProgressBar progressBar;

    private NewsAdapter newsAdapter;
    private StockTipAdapter stockTipAdapter;
    private TopicCardAdapter topicCardAdapter;

    // [중요] 전체 뉴스(100개)를 담아둘 변수
    private List<NewsItem> fullNewsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvKeywordLine = view.findViewById(R.id.tvKeywordLine);
        rvNews = view.findViewById(R.id.rvNews);
        rvStockTip = view.findViewById(R.id.rvStockTip);
        rvTopicCard = view.findViewById(R.id.rvTopicCard);
        progressBar = view.findViewById(R.id.progressBar);

        rvNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStockTip.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTopicCard.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        newsAdapter = new NewsAdapter(requireContext(), item -> {
            Intent intent = new Intent(requireContext(), AudioNewsActivity.class);

            // [핵심 로직] 전체 100개 리스트와 클릭 인덱스를 보냄
            intent.putExtra("news_list", (Serializable) new ArrayList<>(fullNewsList));
            int index = fullNewsList.indexOf(item);
            intent.putExtra("current_index", index);

            startActivity(intent);
        });

        stockTipAdapter = new StockTipAdapter();
        topicCardAdapter = new TopicCardAdapter();

        rvNews.setAdapter(newsAdapter);
        rvStockTip.setAdapter(stockTipAdapter);
        rvTopicCard.setAdapter(topicCardAdapter);

        loadFeed();

        return view;
    }

    private void loadFeed() {
        progressBar.setVisibility(View.VISIBLE);

        SharedPreferences sharedPref = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        apiService.getHomeFeed(userId).enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    FeedResponse feed = response.body();

                    if (feed.getNews() != null && !feed.getNews().isEmpty()) {
                        // 1. 서버에서 받은 전체 리스트(100개) 저장
                        fullNewsList = feed.getNews();

                        // 2. 홈 화면용 리스트(상위 5개) 만들기
                        List<NewsItem> displayList;
                        if (fullNewsList.size() > 5) {
                            displayList = fullNewsList.subList(0, 5);
                        } else {
                            displayList = fullNewsList;
                        }

                        // 3. 어댑터에는 '5개짜리 리스트'만 전달
                        newsAdapter.setItems(displayList);

                        tvKeywordLine.setText("AI가 사용자님의 관심 키워드를 분석했습니다.");
                    } else {
                        tvKeywordLine.setText("추천된 뉴스가 없습니다.");
                    }

                    if (feed.getStockTips() != null) stockTipAdapter.setItems(feed.getStockTips());
                    if (feed.getTopics() != null) topicCardAdapter.setItems(feed.getTopics());

                } else {
                    Log.e("HomeFragment", "Load Failed: " + response.code());
                    tvKeywordLine.setText("뉴스를 불러오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("HomeFragment", "Network Error: " + t.getMessage());
                tvKeywordLine.setText("서버 연결에 실패했습니다.");
            }
        });
    }
}