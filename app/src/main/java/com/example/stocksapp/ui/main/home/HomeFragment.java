package com.example.stocksapp.ui.main.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.AiResult;
import com.example.stocksapp.data.model.AiStockResponse;
import com.example.stocksapp.data.model.NewsItem;
import com.example.stocksapp.data.model.PersonalizedNewsResponse;
import com.example.stocksapp.data.model.StockTip;
import com.example.stocksapp.data.model.TodayNewsResponse;
import com.example.stocksapp.data.model.TopicCard;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.main.adapter.NewsAdapter;
import com.example.stocksapp.ui.main.adapter.StockTipAdapter;
import com.example.stocksapp.ui.main.adapter.TopicCardAdapter;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // AI 추천 종목 / 토픽
    private RecyclerView rvStockTips;
    private RecyclerView rvTopics;

    // 뉴스 (메인 / 선호 키워드)
    private RecyclerView rvMainNews;
    private RecyclerView rvKeywordNews;
    private MaterialButtonToggleGroup groupNewsTabs;

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

        // UI binding
        rvStockTips = view.findViewById(R.id.rvStockTips);
        rvTopics = view.findViewById(R.id.rvTopics);
        rvMainNews = view.findViewById(R.id.rvMainNews);
        rvKeywordNews = view.findViewById(R.id.rvKeywordNews);
        groupNewsTabs = view.findViewById(R.id.groupNewsTabs);
        tvKeywordLine = view.findViewById(R.id.tvKeywordLine);
        tvKeywordList = view.findViewById(R.id.tvKeywordList);
        progressBar = view.findViewById(R.id.progressBar);

        apiService = RetrofitClient.getApiService();

        setupRecyclerViews();
        setupNewsTabToggle();

        updateKeywordList();
        loadTodayNews();
        loadPersonalizedNews();
        loadAiPersonalStockRecommendations();

        return view;
    }

    /** RecyclerView 세팅 */
    private void setupRecyclerViews() {
        // 종목
        stockTipAdapter = new StockTipAdapter(requireContext(), null);
        rvStockTips.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvStockTips.setAdapter(stockTipAdapter);
        new PagerSnapHelper().attachToRecyclerView(rvStockTips);

        // 토픽 (API 없음)
        topicCardAdapter = new TopicCardAdapter(requireContext(), null);
        rvTopics.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvTopics.setAdapter(topicCardAdapter);
        new PagerSnapHelper().attachToRecyclerView(rvTopics);

        // 메인 뉴스
        mainNewsAdapter = new NewsAdapter(requireContext(), null);
        rvMainNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMainNews.setAdapter(mainNewsAdapter);

        // 개인화 뉴스
        keywordNewsAdapter = new NewsAdapter(requireContext(), null);
        rvKeywordNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvKeywordNews.setAdapter(keywordNewsAdapter);
    }

    /** 탭 전환 */
    private void setupNewsTabToggle() {
        groupNewsTabs.check(R.id.btnMainNews);
        groupNewsTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            boolean showMain = (checkedId == R.id.btnMainNews);

            rvMainNews.setVisibility(showMain ? View.VISIBLE : View.GONE);
            rvKeywordNews.setVisibility(showMain ? View.GONE : View.VISIBLE);

            tvKeywordLine.setText(showMain
                    ? "오늘 시장에서 꼭 봐야 할 메인 뉴스"
                    : "내가 선택한 선호 키워드 기반 맞춤 뉴스");
        });
    }

    /** 오늘 메인 뉴스 */
    private void loadTodayNews() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getTodayNews().enqueue(new Callback<TodayNewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<TodayNewsResponse> call, @NonNull Response<TodayNewsResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> list = response.body().getData();
                    if (list == null) list = new ArrayList<>();
                    mainNewsAdapter.setItems(list);
                } else {
                    mainNewsAdapter.setItems(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TodayNewsResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;
                Log.e(TAG, "loadTodayNews failed: ", t);
                mainNewsAdapter.setItems(new ArrayList<>());
            }
        });
    }

    /** 개인화 뉴스 */
    private void loadPersonalizedNews() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId <= 0) {
            keywordNewsAdapter.setItems(new ArrayList<>());
            return;
        }

        apiService.getPersonalizedNews(userId, 3, 20)
                .enqueue(new Callback<PersonalizedNewsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<PersonalizedNewsResponse> call, @NonNull Response<PersonalizedNewsResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            List<NewsItem> list = response.body().getArticles();
                            if (list == null) list = new ArrayList<>();
                            keywordNewsAdapter.setItems(list);
                        } else {
                            keywordNewsAdapter.setItems(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PersonalizedNewsResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Log.e(TAG, "loadPersonalizedNews failed: ", t);
                        keywordNewsAdapter.setItems(new ArrayList<>());
                    }
                });
    }

    // --- 수정사항: 요청하신 코드로 메서드 전체 교체 ---
    /** AI 개인 맞춤 추천 종목 */
    private void loadAiPersonalStockRecommendations() {
        if (!isAdded()) return;

        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId <= 0) {
            stockTipAdapter.setItems(new ArrayList<>());
            return;
        }

        apiService.getPersonalStockRecommend(userId)
                .enqueue(new Callback<AiStockResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AiStockResponse> call,
                                           @NonNull Response<AiStockResponse> response) {
                        if (!isAdded()) return;

                        if (!response.isSuccessful() || response.body() == null) {
                            android.util.Log.e("AiDebug",
                                    "AI recommend fail: code=" + response.code());
                            stockTipAdapter.setItems(new ArrayList<>());
                            return;
                        }

                        AiStockResponse body = response.body();
                        AiResult ai = body.getAiResult();

                        List<StockTip> tips = new ArrayList<>();

                        // 1) AI가 최종 추천한 종목 1개
                        if (ai != null &&
                                ai.getRecommendedStock() != null &&
                                ai.getReason() != null) {

                            tips.add(new StockTip(
                                    ai.getRecommendedStock(),
                                    ai.getReason()
                            ));
                        }

                        // 2) candidates_found 를 같이 보여주고 싶으면 여기서 추가
                        if (body.getCandidatesFound() != null) {
                            for (String name : body.getCandidatesFound()) {
                                // 중복 방지: 이미 추천 종목으로 들어간 건 건너뛰기
                                if (ai != null && name.equals(ai.getRecommendedStock())) continue;

                                tips.add(new StockTip(
                                        name,
                                        body.getUserInterest() + " 관련 후보 종목"
                                ));
                            }
                        }

                        stockTipAdapter.setItems(tips);
                    }

                    @Override
                    public void onFailure(@NonNull Call<AiStockResponse> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;

                        android.util.Log.e("AiDebug",
                                "AI recommend network error", t);
                        stockTipAdapter.setItems(new ArrayList<>());
                    }
                });
    }

    /** 키워드 라벨 업데이트 */
    private void updateKeywordList() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String raw = prefs.getString("include_keywords", "");

        if (raw == null || raw.trim().isEmpty()) {
            tvKeywordList.setText("키워드를 설정하면 맞춤 뉴스가 제공돼요");
        } else {
            tvKeywordList.setText(raw.replace(",", " · "));
        }
    }
}
