package com.example.stocksapp.ui.main.home;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.data.model.NewsItem;
import com.example.stocksapp.data.model.StockTip;
import com.example.stocksapp.data.model.TopicCard;
import com.example.stocksapp.data.repo.FakeFeedRepository;
import com.example.stocksapp.ui.main.AudioNewsActivity;
import com.example.stocksapp.ui.main.adapter.NewsAdapter;
import com.example.stocksapp.ui.main.adapter.StockTipAdapter;
import com.example.stocksapp.ui.main.adapter.TopicCardAdapter;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    // 슬라이드 스위치 + 레이블
    private SwitchMaterial swNewsMode;
    private TextView tvToggleMain;
    private TextView tvToggleUser;

    private NewsAdapter newsAdapter;
    private StockTipAdapter stockTipAdapter;
    private TopicCardAdapter topicCardAdapter;

    // 전체 뉴스 / 사용자 맞춤 뉴스 리스트
    private List<NewsItem> mainNewsList = new ArrayList<>();
    private List<NewsItem> personalizedNewsList = new ArrayList<>();

    // 온보딩에서 넘어온 키워드
    private List<String> includeKeywords = new ArrayList<>();
    private List<String> excludeKeywords = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityIntent = requireActivity().getIntent();
        String includeStr = activityIntent.getStringExtra("ONBOARD_INCLUDE");
        String excludeStr = activityIntent.getStringExtra("ONBOARD_EXCLUDE");

        if (includeStr != null && !includeStr.isEmpty()) {
            includeKeywords = new ArrayList<>(Arrays.asList(includeStr.split(",")));
        }
        if (excludeStr != null && !excludeStr.isEmpty()) {
            excludeKeywords = new ArrayList<>(Arrays.asList(excludeStr.split(",")));
        }
    }

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

        swNewsMode = view.findViewById(R.id.swNewsMode);
        tvToggleMain = view.findViewById(R.id.tvToggleMain);
        tvToggleUser = view.findViewById(R.id.tvToggleUser);

        // 레이아웃 매니저
        rvNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNews.setNestedScrollingEnabled(false); // 화면 전체 스크롤에 합치기

        rvStockTip.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvTopicCard.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        // 어댑터
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

        setupNewsSwitch();
        loadFeed();

        return view;
    }

    private void setupNewsSwitch() {
        // 기본은 주요 뉴스 모드 (checked = false)
        swNewsMode.setChecked(false);
        updateNewsToggleUi(false);

        swNewsMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showPersonalizedNews();
            } else {
                showMainNews();
            }
            updateNewsToggleUi(isChecked);
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

            mainNewsList = new ArrayList<>(newsList);
            stockTipAdapter.setItems(stockTips);
            topicCardAdapter.setItems(topicCards);

            personalizedNewsList = buildPersonalizedNews(mainNewsList);

            // 기본은 주요 뉴스 표시
            showMainNews();
            updateNewsToggleUi(swNewsMode.isChecked());

            tvKeywordLine.setText("선호 키워드 기반으로 추천된 뉴스입니다");
        } else {
            tvKeywordLine.setText("데이터를 불러오지 못했습니다");
        }

        progressBar.setVisibility(View.GONE);
    }

    private void showMainNews() {
        newsAdapter.setItems(mainNewsList);
        tvSectionNews.setText("뉴스 - 주요 뉴스");
    }

    private void showPersonalizedNews() {
        newsAdapter.setItems(personalizedNewsList);
        tvSectionNews.setText("뉴스 - 사용자 맞춤 뉴스");
    }

    // 스위치 위치에 따라 레이블 스타일 바꾸기
    private void updateNewsToggleUi(boolean personalizedSelected) {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.black);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.gray_500);

        if (!personalizedSelected) {
            tvToggleMain.setTextColor(activeColor);
            tvToggleMain.setTypeface(tvToggleMain.getTypeface(), Typeface.BOLD);

            tvToggleUser.setTextColor(inactiveColor);
            tvToggleUser.setTypeface(tvToggleUser.getTypeface(), Typeface.NORMAL);
        } else {
            tvToggleMain.setTextColor(inactiveColor);
            tvToggleMain.setTypeface(tvToggleMain.getTypeface(), Typeface.NORMAL);

            tvToggleUser.setTextColor(activeColor);
            tvToggleUser.setTypeface(tvToggleUser.getTypeface(), Typeface.BOLD);
        }
    }

    // 키워드 기반 사용자 맞춤 리스트 생성
    private List<NewsItem> buildPersonalizedNews(List<NewsItem> allNews) {
        List<NewsItem> result = new ArrayList<>();

        if (includeKeywords.isEmpty()) {
            int limit = Math.min(5, allNews.size());
            for (int i = 0; i < limit; i++) {
                result.add(allNews.get(i));
            }
            return result;
        }

        for (NewsItem item : allNews) {
            String title = item.getTitle() != null ? item.getTitle() : "";

            boolean matchedInclude = false;
            for (String kw : includeKeywords) {
                if (kw == null) continue;
                String trimmed = kw.trim();
                if (trimmed.isEmpty()) continue;
                if (title.contains(trimmed)) {
                    matchedInclude = true;
                    break;
                }
            }
            if (!matchedInclude) continue;

            boolean excluded = false;
            for (String ex : excludeKeywords) {
                if (ex == null) continue;
                String trimmedEx = ex.trim();
                if (trimmedEx.isEmpty()) continue;
                if (title.contains(trimmedEx)) {
                    excluded = true;
                    break;
                }
            }
            if (excluded) continue;

            result.add(item);
        }

        if (result.isEmpty()) {
            int limit = Math.min(5, allNews.size());
            for (int i = 0; i < limit; i++) {
                result.add(allNews.get(i));
            }
        }

        return result;
    }
}
