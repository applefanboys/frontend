package com.example.stocksapp.ui.main.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.data.model.NewsItem;
import com.example.stocksapp.data.model.StockTip;
import com.example.stocksapp.data.model.TopicCard;
import com.example.stocksapp.data.model.PersonalizedNewsResponse;
import com.example.stocksapp.data.repo.FakeFeedRepository;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.main.adapter.NewsAdapter;
import com.example.stocksapp.ui.main.adapter.StockTipAdapter;
import com.example.stocksapp.ui.main.adapter.TopicCardAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    // AI 추천 종목 / 토픽
    private RecyclerView rvStockTips;
    private RecyclerView rvTopics;

    // 뉴스 (메인 / 선호 키워드)
    private RecyclerView rvMainNews;
    private RecyclerView rvKeywordNews;
    private MaterialButtonToggleGroup groupNewsTabs;
    private MaterialButton btnMainNews;
    private MaterialButton btnKeywordNews;

    // 설명 / 키워드 텍스트
    private TextView tvKeywordLine;
    private TextView tvKeywordList;

    private ProgressBar progressBar;

    private StockTipAdapter stockTipAdapter;
    private TopicCardAdapter topicCardAdapter;
    private NewsAdapter mainNewsAdapter;
    private NewsAdapter keywordNewsAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 뷰 바인딩
        rvStockTips = view.findViewById(R.id.rvStockTips);
        rvTopics = view.findViewById(R.id.rvTopics);
        rvMainNews = view.findViewById(R.id.rvMainNews);
        rvKeywordNews = view.findViewById(R.id.rvKeywordNews);

        groupNewsTabs = view.findViewById(R.id.groupNewsTabs);
        btnMainNews = view.findViewById(R.id.btnMainNews);
        btnKeywordNews = view.findViewById(R.id.btnKeywordNews);

        tvKeywordLine = view.findViewById(R.id.tvKeywordLine);
        tvKeywordList = view.findViewById(R.id.tvKeywordList);

        progressBar = view.findViewById(R.id.progressBar);
        apiService = RetrofitClient.getApiService();

        // 리스트, 탭 설정
        setupRecyclerViews();
        setupNewsTabToggle();

        // 데이터 로딩
        loadFeed();
        updateKeywordList();
        loadNewsFromServer();   // 실제 뉴스 API
        loadPersonalizedNewsFromServer();   // 선호 키워드 기반 맞춤 뉴스

        return view;
    }

    // RecyclerView 설정 (레이아웃, 어댑터, 스냅 등)
    private void setupRecyclerViews() {
        // AI 추천 종목 - 가로, 페이지 스냅
        // TODO: StockTipAdapter에 클릭 리스너를 받는 생성자 추가 필요
        stockTipAdapter = new StockTipAdapter(requireContext(), null);// 기본 생성자 사용
        LinearLayoutManager stockLayoutManager =
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvStockTips.setLayoutManager(stockLayoutManager);
        rvStockTips.setAdapter(stockTipAdapter);
        rvStockTips.setHasFixedSize(true);
        rvStockTips.setItemViewCacheSize(10);
        SnapHelper stockSnap = new PagerSnapHelper();
        stockSnap.attachToRecyclerView(rvStockTips);

        // 오늘의 토픽 - 가로, 페이지 스냅
        // TODO: TopicCardAdapter에 클릭 리스너를 받는 생성자 추가 필요
        topicCardAdapter = new TopicCardAdapter(requireContext(), null);
        LinearLayoutManager topicLayoutManager =
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvTopics.setLayoutManager(topicLayoutManager);
        rvTopics.setAdapter(topicCardAdapter);
        rvTopics.setHasFixedSize(true);
        rvTopics.setItemViewCacheSize(10);
        SnapHelper topicSnap = new PagerSnapHelper();
        topicSnap.attachToRecyclerView(rvTopics);

        // 메인 뉴스 (세로)
        mainNewsAdapter = new NewsAdapter(requireContext(), null);
        rvMainNews.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        rvMainNews.setAdapter(mainNewsAdapter);
        rvMainNews.setHasFixedSize(true);
        rvMainNews.setItemViewCacheSize(10);

        // 선호 키워드 기반 뉴스 (세로)
        keywordNewsAdapter = new NewsAdapter(requireContext(), null);
        rvKeywordNews.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        rvKeywordNews.setAdapter(keywordNewsAdapter);
        rvKeywordNews.setHasFixedSize(true);
        rvKeywordNews.setItemViewCacheSize(10);
    }

    // 뉴스 탭 전환 (메인 / 선호 키워드)
    private void setupNewsTabToggle() {
        // 기본 선택: 메인 뉴스
        groupNewsTabs.check(R.id.btnMainNews);

        groupNewsTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnMainNews) {
                switchNewsTab(true);
                tvKeywordLine.setText("오늘 시장에서 꼭 봐야 할 메인 뉴스");
            } else if (checkedId == R.id.btnKeywordNews) {
                switchNewsTab(false);
                tvKeywordLine.setText("내가 선택한 선호 키워드 기반 맞춤 뉴스");
            }
        });
    }

    // 탭 전환 애니메이션
    private void switchNewsTab(boolean showMain) {
        RecyclerView toShow = showMain ? rvMainNews : rvKeywordNews;
        RecyclerView toHide = showMain ? rvKeywordNews : rvMainNews;

        toHide.animate()
                .alpha(0f)
                .translationX(showMain ? 20f : -20f)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    toHide.setVisibility(View.GONE);
                    toShow.setVisibility(View.VISIBLE);
                    toShow.setAlpha(0f);
                    toShow.setTranslationX(showMain ? -20f : 20f);
                    toShow.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setDuration(180)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                })
                .start();
    }

    // 메인 뉴스
    private void loadNewsFromServer() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getTodayNews().enqueue(new Callback<List<NewsItem>>() {
            @Override
            public void onResponse(Call<List<NewsItem>> call, Response<List<NewsItem>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> newsList = response.body();
                    if (newsList == null) newsList = new ArrayList<>();

                    // 메인 뉴스 / 키워드 뉴스 둘 다 같은 리스트로 채워두고
                    // 나중에 개인화 API 붙이면 keywordNewsAdapter만 교체
                    mainNewsAdapter.setItems(newsList);
                    keywordNewsAdapter.setItems(newsList);
                } else {
                    // ❗ 서버 응답 실패 시 FakeFeed로 fallback
                    loadFeed();
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<NewsItem>> call, Throwable t) {
                if (!isAdded()) return;

                // ❗ 네트워크 실패 시에도 FakeFeed로 fallback
                loadFeed();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // 선호 키워드 기반 맞춤 뉴스
    private void loadPersonalizedNewsFromServer() {
        // progressBar는 메인 뉴스 기준으로만 돌리고 싶다면 여기서는 안 건드려도 됨
        apiService.getPersonalizedNews(3, 20)    // ★ days=3, limit=20
                .enqueue(new Callback<PersonalizedNewsResponse>() {
                    @Override
                    public void onResponse(Call<PersonalizedNewsResponse> call,
                                           Response<PersonalizedNewsResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            List<NewsItem> articles = response.body().getArticles();
                            if (articles == null) articles = new ArrayList<>();

                            // 🟡 선호 키워드 뉴스 어댑터에만 세팅
                            keywordNewsAdapter.setItems(articles);
                        } else {
                            // 실패 시: 이미 todayNews나 FakeFeed로 채워져 있을 수 있으므로 추가 처리 X
                        }
                    }

                    @Override
                    public void onFailure(Call<PersonalizedNewsResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        // 실패해도 기존 keywordNews 리스트(FakeFeed or todayNews) 유지
                    }
                });
    }

    // 피드(뉴스/종목/토픽) 로딩
    private void loadFeed() {
        // progressBar는 서버 호출 기준으로 관리하고 싶으면 여기서는 안 건드려도 됨
        FeedResponse feed = FakeFeedRepository.getFeed();

        if (feed != null) {
            List<NewsItem> newsList = feed.getNews();
            List<StockTip> stockTips = feed.getStockTips();
            List<TopicCard> topicCards = feed.getTopics();

            if (newsList == null) newsList = new ArrayList<>();
            if (stockTips == null) stockTips = new ArrayList<>();
            if (topicCards == null) topicCards = new ArrayList<>();

            // 🟢 토픽/종목은 항상 FakeFeed 기준으로 세팅
            stockTipAdapter.setItems(stockTips);
            topicCardAdapter.setItems(topicCards);

            // 🟡 뉴스는 "어댑터가 아직 비어 있는 경우"에만 채움 (fallback 역할)
            if (mainNewsAdapter.getItemCount() == 0) {
                mainNewsAdapter.setItems(newsList);
            }
            if (keywordNewsAdapter.getItemCount() == 0) {
                keywordNewsAdapter.setItems(newsList);
            }
        }
    }

    // 선호 키워드 텍스트 업데이트 (SharedPreferences 에서 가져오기)
    private void updateKeywordList() {
        if (!isAdded() || tvKeywordList == null) return;

        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // 예시: "반도체, 2차전지, 달러" 이런 식으로 저장돼 있다고 가정
        String raw = prefs.getString("include_keywords", "");

        if (raw == null || raw.trim().isEmpty()) {
            tvKeywordList.setText("키워드를 설정하면 맞춤 뉴스가 제공돼요");
            return;
        }

        // "반도체, 2차전지, 달러" → "반도체 · 2차전지 · 달러"
        String pretty = raw.replace(",", " · ").replace(" ,", " · ").trim();
        tvKeywordList.setText(pretty);
    }
}
