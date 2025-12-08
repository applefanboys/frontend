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

        return view;
    }

    /** RecyclerView 세팅 */
    private void setupRecyclerViews() {

        // 종목 (현재 API 없음 → 일단 어댑터만 준비)
        stockTipAdapter = new StockTipAdapter(requireContext(), null);
        rvStockTips.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvStockTips.setAdapter(stockTipAdapter);
        new PagerSnapHelper().attachToRecyclerView(rvStockTips);

        // 토픽 (현재 API 없음 → 일단 어댑터만 준비)
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
            public void onResponse(Call<TodayNewsResponse> call, Response<TodayNewsResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> list = response.body().getData();
                    if (list == null) list = new ArrayList<>();

                    mainNewsAdapter.setItems(list);

                } else {
                    mainNewsAdapter.setItems(new ArrayList<>());  // 빈 리스트
                }
            }

            @Override
            public void onFailure(Call<TodayNewsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;

                mainNewsAdapter.setItems(new ArrayList<>());
            }
        });
    }

    /** 개인화 뉴스 */
    private void loadPersonalizedNews() {
        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        int userId = prefs.getInt("user_id", -1);
        if (userId <= 0) return;

        apiService.getPersonalizedNews(userId, 3, 20)
                .enqueue(new Callback<PersonalizedNewsResponse>() {
                    @Override
                    public void onResponse(Call<PersonalizedNewsResponse> call,
                                           Response<PersonalizedNewsResponse> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            List<NewsItem> list = response.body().getArticles();
                            if (list == null) list = new ArrayList<>();

                            keywordNewsAdapter.setItems(list);
                        }
                    }

                    @Override
                    public void onFailure(Call<PersonalizedNewsResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        keywordNewsAdapter.setItems(new ArrayList<>());
                    }
                });
    }

    /** 키워드 라벨 업데이트 */
    private void updateKeywordList() {
        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        String raw = prefs.getString("include_keywords", "");

        if (raw == null || raw.trim().isEmpty()) {
            tvKeywordList.setText("키워드를 설정하면 맞춤 뉴스가 제공돼요");
        } else {
            tvKeywordList.setText(raw.replace(",", " · "));
        }
    }
}
