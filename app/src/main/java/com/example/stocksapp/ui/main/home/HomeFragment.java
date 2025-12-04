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
import com.example.stocksapp.network.KeywordResponse; // network 패키지에 있는 것이 맞다고 가정
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.main.adapter.NewsAdapter;
import com.example.stocksapp.ui.main.adapter.StockTipAdapter;
import com.example.stocksapp.ui.main.adapter.TopicCardAdapter;
import com.example.stocksapp.ui.main.AudioNewsActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    // 상단 텍스트들
    private TextView tvMainTitle;
    private TextView tvKeywordLine;      // "선호 키워드 기반으로 추천된 뉴스입니다" 같은 설명
    private TextView tvSectionTopic;
    private TextView tvSectionStock;
    private TextView tvSectionNews;

    // 맞춤 키워드 해시태그 표시용
    private TextView tvUserKeywords;

    // 뉴스 모드 전환 스위치 (주요뉴스 / 사용자 맞춤 뉴스)
    private SwitchMaterial switchNewsMode;

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

    // 현재 모드: false = 주요뉴스, true = 맞춤뉴스
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
        switchNewsMode = view.findViewById(R.id.switchNewsMode);

        rvNews = view.findViewById(R.id.rvNews);
        rvStockTip = view.findViewById(R.id.rvStockTip);
        rvTopicCard = view.findViewById(R.id.rvTopicCard);
        progressBar = view.findViewById(R.id.progressBar);

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

        // 스위치 동작 설정
        setupSwitch();

        // 피드 데이터(뉴스/종목/토픽) 로드
        loadFeed();

        // 오늘의 맞춤 키워드 로드 (★★★ API 호출 시작점)
        loadUserKeywords();

        return view;
    }

    /**
     * 상단 스위치: 주요뉴스 / 사용자 맞춤 뉴스 전환
     */
    private void setupSwitch() {
        if (switchNewsMode == null) return;

        // 초기 텍스트
        switchNewsMode.setText("주요 뉴스");

        switchNewsMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isPersonalMode = isChecked;
            applyNewsMode();
        });
    }

    /**
     * FakeFeedRepository 에서 오늘 피드 데이터 가져오기
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

            // 초기에는 전체 뉴스를 보여줌
            newsAdapter.setItems(allNews);
            stockTipAdapter.setItems(stockTips);
            topicCardAdapter.setItems(topicCards);

            tvKeywordLine.setText("선호 키워드 기반으로 추천된 뉴스입니다");
            applyNewsMode(); // 데이터 로드 후 모드에 맞게 UI 갱신
        } else {
            tvKeywordLine.setText("데이터를 불러오지 못했습니다");
        }

        progressBar.setVisibility(View.GONE);
    }

    /**
     * 서버에서 오늘의 맞춤 키워드 가져오기
     */
    private void loadUserKeywords() {
        // 로그인 시 저장해둔 user_id 읽기
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        Log.d("HomeFragment", "loadUserKeywords(), userId=" + userId);

        // 비로그인 상태면 그냥 리턴 (또는 UI 처리)
        if (userId == -1) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            // 비로그인 시 UI 초기화
            userKeywords.clear();
            updateKeywordHeader();
            applyNewsMode();
            return;
        }

        // Retrofit으로 API 호출
        ApiService apiService = RetrofitClient.getApiService();

        apiService.getTodayKeywords(userId).enqueue(new Callback<KeywordResponse>() {
            @Override
            public void onResponse(@NonNull Call<KeywordResponse> call, @NonNull Response<KeywordResponse> response) {
                Log.d("HomeFragment", "getTodayKeywords onResponse, isSuccessful: " + response.isSuccessful());
                if (!isAdded()) return; // Fragment가 Activity에 붙어있지 않으면 아무것도 하지 않음

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
                    // 응답은 왔지만 성공이 아닐 때 (예: 404, 500 에러)
                    Toast.makeText(requireContext(), "키워드 로딩에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<KeywordResponse> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "getTodayKeywords onFailure: " + t.getMessage(), t);
                if (!isAdded()) return;

                // 네트워크 오류 등 통신 자체 실패
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
            tvUserKeywords.setVisibility(View.GONE); // 키워드 없으면 숨기기
            return;
        }

        tvUserKeywords.setVisibility(View.VISIBLE); // 키워드 있으면 보여주기
        StringBuilder sb = new StringBuilder();
        for (String k : userKeywords) {
            sb.append("#").append(k).append("  ");
        }
        tvUserKeywords.setText(sb.toString().trim());
    }

    /**
     * 현재 모드와 키워드 리스트에 따라 보여줄 뉴스 리스트를 결정
     */
    private void applyNewsMode() {
        if (allNews.isEmpty()) {
            newsAdapter.setItems(new ArrayList<>());
            return;
        }

        // 스위치가 "맞춤 뉴스"이고, 키워드도 있을 때: 필터링
        if (isPersonalMode && !userKeywords.isEmpty()) {
            personalNews.clear();

            for (NewsItem item : allNews) {
                String title = (item.getTitle() != null) ? item.getTitle().toLowerCase(Locale.KOREAN) : "";
                for (String keyword : userKeywords) {
                    if (keyword != null && title.contains(keyword.toLowerCase(Locale.KOREAN))) {
                        personalNews.add(item);
                        break;
                    }
                }
            }

            if (personalNews.isEmpty()) {
                newsAdapter.setItems(allNews);
                tvKeywordLine.setText("맞춤 키워드에 해당하는 뉴스가 없어 전체 주요 뉴스를 보여드려요");
            } else {
                newsAdapter.setItems(personalNews);
                tvKeywordLine.setText("오늘의 맞춤 키워드 기반 추천 뉴스입니다");
            }
        } else {
            // 주요뉴스 모드 또는 키워드가 없는 경우: 전체 뉴스
            newsAdapter.setItems(allNews);
            if (isPersonalMode && userKeywords.isEmpty()) {
                tvKeywordLine.setText("맞춤 키워드가 없어 전체 주요 뉴스를 보여드려요");
            } else {
                tvKeywordLine.setText("전체 주요 뉴스를 보고 있어요");
            }
        }

        // 스위치 텍스트도 현재 모드에 맞게 변경
        if (switchNewsMode != null) {
            switchNewsMode.setText(isPersonalMode ? "사용자 맞춤 뉴스" : "주요 뉴스");
        }
    }
}
