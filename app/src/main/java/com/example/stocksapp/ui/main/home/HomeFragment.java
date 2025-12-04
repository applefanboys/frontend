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
import com.example.stocksapp.data.model.StockTip;
import com.example.stocksapp.data.model.TopicCard;
import com.example.stocksapp.data.repo.FakeFeedRepository;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.KeywordResponse;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.main.AudioNewsActivity;
import com.example.stocksapp.ui.main.adapter.NewsAdapter;
import com.example.stocksapp.ui.main.adapter.StockTipAdapter;
import com.example.stocksapp.ui.main.adapter.TopicCardAdapter;
import com.google.android.material.card.MaterialCardView;
import android.graphics.Color;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {
    private MaterialCardView cardNewsContainer;

    // 상단 텍스트들
    private TextView tvMainTitle;
    private TextView tvKeywordLine;
    private TextView tvSectionTopic;
    private TextView tvSectionStock;
    private TextView tvSectionNews;

    // 오늘의 키워드 해시태그 라인
    private TextView tvUserKeywords;

    // 뉴스 탭: "주요 뉴스" / "사용자 맞춤 뉴스"
    private TextView tvMainNewsTab;
    private TextView tvUserNewsTab;

    // 리스트 뷰들
    private RecyclerView rvNews;
    private RecyclerView rvStockTip;
    private RecyclerView rvTopicCard;
    private ProgressBar progressBar;

    // 어댑터
    private NewsAdapter newsAdapter;
    private StockTipAdapter stockTipAdapter;
    private TopicCardAdapter topicCardAdapter;

    // 전체 뉴스 / 맞춤 뉴스 / 키워드 리스트
    private final List<NewsItem> allNews = new ArrayList<>();
    private final List<NewsItem> personalNews = new ArrayList<>();
    private final List<String> userKeywords = new ArrayList<>();

    // 현재 모드: false = 주요 뉴스, true = 사용자 맞춤 뉴스
    private boolean isPersonalMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("HomeFragment", "onCreateView 호출됨");

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 뷰 찾기
        tvMainTitle = view.findViewById(R.id.tvMainTitle);
        tvKeywordLine = view.findViewById(R.id.tvKeywordLine);
        tvSectionTopic = view.findViewById(R.id.tvSectionTopic);
        tvSectionStock = view.findViewById(R.id.tvSectionStock);
        tvSectionNews = view.findViewById(R.id.tvSectionNews);
        tvUserKeywords = view.findViewById(R.id.tvUserKeywords);

        tvMainNewsTab = view.findViewById(R.id.tvMainNewsTab);
        tvUserNewsTab = view.findViewById(R.id.tvUserNewsTab);

        rvNews = view.findViewById(R.id.rvNews);
        rvStockTip = view.findViewById(R.id.rvStockTip);
        rvTopicCard = view.findViewById(R.id.rvTopicCard);
        progressBar = view.findViewById(R.id.progressBar);
        cardNewsContainer = view.findViewById(R.id.cardNewsContainer);

        // 레이아웃 매니저 설정
        rvNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStockTip.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvTopicCard.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        // 뉴스 아이템 클릭 시 오디오 뉴스 화면으로 이동
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

        // 뉴스 탭 클릭 동작 설정
        setupNewsTabs();

        // 피드 데이터(뉴스/종목/토픽) 로드
        loadFeed();

        // 오늘의 맞춤 키워드 로드 (API 호출)
        loadUserKeywords();

        return view;
    }

    /**
     * 뉴스 카드 상단 탭(주요 뉴스 / 사용자 맞춤 뉴스) 설정
     */
    private void setupNewsTabs() {
        if (tvMainNewsTab == null || tvUserNewsTab == null) return;

        // 기본 모드는 "주요 뉴스"
        isPersonalMode = false;
        updateNewsTabUi();

        if (cardNewsContainer != null && isAdded()) {
            if (isPersonalMode) {
                // 사용자 맞춤 뉴스 모드: 연한 파랑
                cardNewsContainer.setCardBackgroundColor(Color.parseColor("#EEF4FF"));
            } else {
                // 주요 뉴스 모드: 흰색
                cardNewsContainer.setCardBackgroundColor(Color.WHITE);
            }
        }


        // 왼쪽 탭: 주요 뉴스
        tvMainNewsTab.setOnClickListener(v -> {
            if (!isPersonalMode) return;
            isPersonalMode = false;
            applyNewsMode();
        });

        // 오른쪽 탭: 사용자 맞춤 뉴스
        tvUserNewsTab.setOnClickListener(v -> {
            if (isPersonalMode) return;
            isPersonalMode = true;
            applyNewsMode();
        });
    }

    /**
     * FakeFeedRepository에서 오늘 피드 데이터 가져오기
     */
    private void loadFeed() {
        progressBar.setVisibility(View.VISIBLE);

        FeedResponse feed = FakeFeedRepository.getFeed();

        if (feed != null) {
            List<NewsItem> newsList = feed.getNews();
            List<StockTip> stockTips = feed.getStockTips();
            List<TopicCard> topicCards = feed.getTopics();

            allNews.clear();
            if (newsList != null) {
                allNews.addAll(newsList);
            }

            if (stockTips == null) stockTips = new ArrayList<>();
            if (topicCards == null) topicCards = new ArrayList<>();

            // 초기에는 전체 뉴스 + 나머지 카드 세팅
            newsAdapter.setItems(allNews);
            stockTipAdapter.setItems(stockTips);
            topicCardAdapter.setItems(topicCards);

            tvKeywordLine.setText("전체 주요 뉴스를 보고 있어요");

            // 데이터 로드 후 현재 모드에 맞게 다시 적용
            applyNewsMode();
        } else {
            tvKeywordLine.setText("데이터를 불러오지 못했습니다");
        }

        progressBar.setVisibility(View.GONE);
    }

    /**
     * 서버에서 오늘의 맞춤 키워드 가져오기
     */
    private void loadUserKeywords() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        Log.d("HomeFragment", "loadUserKeywords(), userId=" + userId);

        // 비로그인 상태면 키워드 없이 진행
        if (userId == -1) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            userKeywords.clear();
            updateKeywordHeader();
            applyNewsMode();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();

        apiService.getTodayKeywords(userId).enqueue(new Callback<KeywordResponse>() {
            @Override
            public void onResponse(@NonNull Call<KeywordResponse> call,
                                   @NonNull Response<KeywordResponse> response) {
                Log.d("HomeFragment", "getTodayKeywords onResponse, isSuccessful: " + response.isSuccessful());
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    userKeywords.clear();
                    List<String> serverKeywords = response.body().getKeywords();
                    if (serverKeywords != null) {
                        userKeywords.addAll(serverKeywords);
                        Log.d("HomeFragment", "성공! 키워드 개수 = " + userKeywords.size());
                    }

                    // 상단 해시태그 텍스트 갱신
                    updateKeywordHeader();

                    // 맞춤 키워드 기반으로 뉴스 필터 다시 적용
                    applyNewsMode();
                } else {
                    Log.e("HomeFragment", "getTodayKeywords 응답 실패, code: " + response.code());
                    Toast.makeText(requireContext(), "키워드 로딩에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<KeywordResponse> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "getTodayKeywords onFailure: " + t.getMessage(), t);
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 상단 "오늘의 키워드" 해시태그 라인 업데이트
     */
    private void updateKeywordHeader() {
        if (tvUserKeywords == null) return;

        if (userKeywords.isEmpty()) {
            tvUserKeywords.setVisibility(View.GONE);
            return;
        }

        tvUserKeywords.setVisibility(View.VISIBLE);
        StringBuilder sb = new StringBuilder();
        for (String k : userKeywords) {
            sb.append("#").append(k).append("  ");
        }
        tvUserKeywords.setText(sb.toString().trim());
    }

    /**
     * 현재 모드(isPersonalMode)와 키워드 리스트에 따라
     * 보여줄 뉴스 리스트 결정 + 탭 UI/설명 문구 갱신
     */
    private void applyNewsMode() {
        if (allNews.isEmpty()) {
            newsAdapter.setItems(new ArrayList<>());
            updateNewsTabUi();
            return;
        }

        // 사용자 맞춤 모드 + 키워드 존재
        if (isPersonalMode && !userKeywords.isEmpty()) {
            personalNews.clear();

            for (NewsItem item : allNews) {
                String title = (item.getTitle() != null)
                        ? item.getTitle().toLowerCase(Locale.KOREAN)
                        : "";
                for (String keyword : userKeywords) {
                    if (keyword != null &&
                            title.contains(keyword.toLowerCase(Locale.KOREAN))) {
                        personalNews.add(item);
                        break;
                    }
                }
            }

            if (personalNews.isEmpty()) {
                // 키워드와 매칭되는 뉴스가 없으면 전체 뉴스
                newsAdapter.setItems(allNews);
                tvKeywordLine.setText("맞춤 키워드에 해당하는 뉴스가 없어 전체 주요 뉴스를 보여드려요");
            } else {
                newsAdapter.setItems(personalNews);
                tvKeywordLine.setText("오늘의 맞춤 키워드 기반 추천 뉴스입니다");
            }
        } else {
            // 주요 뉴스 모드 또는 키워드가 없는 경우
            newsAdapter.setItems(allNews);
            if (isPersonalMode && userKeywords.isEmpty()) {
                tvKeywordLine.setText("맞춤 키워드가 없어 전체 주요 뉴스를 보여드려요");
            } else {
                tvKeywordLine.setText("전체 주요 뉴스를 보고 있어요");
            }
        }

        // 탭 비주얼도 현재 모드에 맞게 갱신
        updateNewsTabUi();
    }

    /**
     * 탭 텍스트뷰들의 배경/글자색을 현재 모드에 맞게 변경
     */
    private void updateNewsTabUi() {
        if (tvMainNewsTab == null || tvUserNewsTab == null || !isAdded()) return;

        if (isPersonalMode) {
            // 사용자 맞춤 뉴스 선택
            tvUserNewsTab.setBackgroundResource(R.drawable.bg_news_tab_selected);
            tvUserNewsTab.setTextColor(requireContext().getColor(android.R.color.white));

            tvMainNewsTab.setBackgroundResource(R.drawable.bg_news_tab_unselected);
            tvMainNewsTab.setTextColor(requireContext().getColor(R.color.gray_500));
        } else {
            // 주요 뉴스 선택
            tvMainNewsTab.setBackgroundResource(R.drawable.bg_news_tab_selected);
            tvMainNewsTab.setTextColor(requireContext().getColor(android.R.color.white));

            tvUserNewsTab.setBackgroundResource(R.drawable.bg_news_tab_unselected);
            tvUserNewsTab.setTextColor(requireContext().getColor(R.color.gray_500));
        }
    }
}
