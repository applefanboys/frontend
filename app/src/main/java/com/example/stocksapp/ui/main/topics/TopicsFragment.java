package com.example.stocksapp.ui.main.topics;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.NewsItem;
import com.example.stocksapp.data.model.PersonalizedNewsResponse;
import com.example.stocksapp.data.model.TodayNewsResponse;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// Base64 import
import android.util.Base64;

public class TopicsFragment extends Fragment {

    private static final String TAG = "TopicsFragment";

    // TTS 엔드포인트
    private static final String TTS_URL =
            "https://api.short-economy.store/api/tts/shortform";
    private static final String TTS_PERSONALIZED_URL =
            "https://api.short-economy.store/api/tts/shortform/personalized";

    // 로그인 붙기 전 임시 user_id
    private static final int TEMP_USER_ID = 1;

    private ViewPager2 vpNewsReels;
    private NewsReelsAdapter adapter;

    /** 실제로 화면에 보여줄 카드 리스트 (메인/맞춤 섞인 결과) */
    private final List<NewsItem> newsList = new ArrayList<>();

    /** 각각의 API 결과 */
    private final List<NewsItem> todayNews = new ArrayList<>();
    private final List<NewsItem> personalizedNews = new ArrayList<>();

    /** 둘 다 로딩 끝났는지 플래그 */
    private boolean todayLoaded = false;
    private boolean personalizedLoaded = false;

    private ApiService apiService;
    private final OkHttpClient httpClient = new OkHttpClient();

    private MediaPlayer mediaPlayer;
    private int currentPosition = 0;

    /** position -> mp3 캐시 파일 */
    private final Map<Integer, File> ttsCache = new HashMap<>();
    /** position -> 이 카드가 어떤 TTS 모드인지 (false: 일반, true: 개인화) */
    private final Map<Integer, Boolean> ttsModeMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_topics, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        apiService = RetrofitClient.getApiService();

        vpNewsReels = view.findViewById(R.id.vpNewsReels);
        vpNewsReels.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        // 처음엔 빈 리스트로 어댑터 연결
        adapter = new NewsReelsAdapter(newsList);
        vpNewsReels.setAdapter(adapter);

        // 메인 뉴스 + 맞춤 뉴스 불러오기 시작
        loadTodayNews();
        loadPersonalizedNews();

        // 페이지 바뀔 때마다 해당 카드 TTS 재생
        vpNewsReels.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                startTtsForPosition(position);
            }
        });
    }

    // --------------------------------------------------------------------
    // 1. 뉴스 API 호출
    // --------------------------------------------------------------------

    // GET /api/news/today
    private void loadTodayNews() {
        apiService.getTodayNews().enqueue(new retrofit2.Callback<TodayNewsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<TodayNewsResponse> call,
                                   retrofit2.Response<TodayNewsResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> data = response.body().getData();
                    todayNews.clear();
                    if (data != null) todayNews.addAll(data);
                    Log.d(TAG, "todayNews size=" + todayNews.size());
                } else {
                    Log.e(TAG, "loadTodayNews 실패 code=" + response.code());
                }

                todayLoaded = true;
                buildMixedFeedIfReady();
            }

            @Override
            public void onFailure(retrofit2.Call<TodayNewsResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "loadTodayNews onFailure", t);

                todayLoaded = true;
                buildMixedFeedIfReady();
            }
        });
    }

    // GET /api/news/personalized
    private void loadPersonalizedNews() {
        apiService.getPersonalizedNews(TEMP_USER_ID, 3, 20)
                .enqueue(new retrofit2.Callback<PersonalizedNewsResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<PersonalizedNewsResponse> call,
                                           retrofit2.Response<PersonalizedNewsResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            List<NewsItem> data = response.body().getArticles();
                            personalizedNews.clear();
                            if (data != null) personalizedNews.addAll(data);
                            Log.d(TAG, "personalizedNews size=" + personalizedNews.size());
                        } else {
                            Log.e(TAG, "loadPersonalizedNews 실패 code=" + response.code());
                        }

                        personalizedLoaded = true;
                        buildMixedFeedIfReady();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<PersonalizedNewsResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        Log.e(TAG, "loadPersonalizedNews onFailure", t);

                        personalizedLoaded = true;
                        buildMixedFeedIfReady();
                    }
                });
    }

    /**
     * 두 API 응답이 모두 끝나면,
     * newsList를 "메인 1개 → 맞춤 1개 → 메인 1개 → 맞춤 1개 ..." 순서로 섞어서 만든다.
     * - 메인카드: ttsModeMap[pos] = false  (일반 TTS)
     * - 맞춤카드: ttsModeMap[pos] = true   (개인화 TTS)
     */
    private void buildMixedFeedIfReady() {
        if (!todayLoaded || !personalizedLoaded) return;

        newsList.clear();
        ttsCache.clear();
        ttsModeMap.clear();

        if (todayNews.isEmpty() && personalizedNews.isEmpty()) {
            Log.w(TAG, "두 뉴스 리스트가 모두 비어 있음");
            adapter.setItems(newsList);
            return;
        }

        int maxLen = Math.max(todayNews.size(), personalizedNews.size());
        int maxCards = 50;
        int count = 0;

        for (int i = 0; i < maxLen && count < maxCards; i++) {
            // 메인 뉴스
            if (i < todayNews.size() && count < maxCards) {
                int pos = newsList.size();
                newsList.add(todayNews.get(i));
                ttsModeMap.put(pos, false);   // 일반 TTS
                count++;
            }
            // 맞춤 뉴스
            if (i < personalizedNews.size() && count < maxCards) {
                int pos = newsList.size();
                newsList.add(personalizedNews.get(i));
                ttsModeMap.put(pos, true);    // 개인화 TTS
                count++;
            }
        }

        adapter.setItems(newsList);

        // 첫 카드 자동 TTS
        if (!newsList.isEmpty()) {
            currentPosition = 0;
            vpNewsReels.post(() -> startTtsForPosition(0));
        }
    }

    // --------------------------------------------------------------------
    // 2. TTS 재생 (단일 카드, 프리페치 없이) + 무한 루프
    // --------------------------------------------------------------------

    private void startTtsForPosition(int position) {
        if (position < 0 || position >= newsList.size()) return;

        stopCurrentAudio();

        NewsItem item = newsList.get(position);
        String text = buildTtsTextFromItem(item);

        // 이 카드에 대해 사용할 TTS 모드 (true: personalized, false: normal)
        boolean usePersonalized = Boolean.TRUE.equals(ttsModeMap.get(position));

        Log.d(TAG, "startTtsForPosition pos=" + position +
                ", personalized=" + usePersonalized);

        // 캐시를 쓰고 싶으면 여기서 ttsCache 확인해서 쓰고,
        // 지금은 단순화를 위해 매번 서버 호출
        requestTtsFromServer(text, position, true, usePersonalized);
    }

    /** summary → title 순으로 TTS용 텍스트 구성 + 180자 제한 */
    private String buildTtsTextFromItem(NewsItem item) {
        if (item == null) return "오늘의 경제 뉴스입니다.";

        String text = item.getSummary();
        if (text == null || text.trim().isEmpty()) {
            text = item.getTitle();
        }
        if (text == null || text.trim().isEmpty()) {
            text = "오늘의 경제 뉴스입니다.";
        }

        text = text.trim();
        if (text.length() > 180) {
            text = text.substring(0, 180);
        }
        return text;
    }

    /**
     * TTS 호출
     *
     * @param autoPlay        true  → 이 카드용 음성: 다운로드 후 바로 재생 시도
     *                        false → (지금은 사용 안 함)
     * @param usePersonalized true  → /api/tts/shortform/personalized
     *                        false → /api/tts/shortform
     */
    private void requestTtsFromServer(String text,
                                      int positionForThisRequest,
                                      boolean autoPlay,
                                      boolean usePersonalized) {
        try {
            if (positionForThisRequest < 0 || positionForThisRequest >= newsList.size()) {
                return;
            }

            // 이 요청에 대응되는 뉴스
            NewsItem newsItem = newsList.get(positionForThisRequest);

            // 🔹 1) 공통 텍스트 전처리
            if (text == null) text = "";
            text = text.trim();
            if (text.isEmpty()) {
                text = "오늘의 경제 뉴스입니다.";
            }
            if (text.length() > 180) {
                text = text.substring(0, 180);
            }

            // 🔹 2) JSON 바디 구성
            JSONObject json = new JSONObject();
            json.put("max_chars", 180);

            // origin_url / image_url 로 쓸 값 (없으면 url을 대신 사용)
            String originUrlToSend = null;
            if (newsItem.getOriginUrl() != null && !newsItem.getOriginUrl().isEmpty()) {
                originUrlToSend = newsItem.getOriginUrl();
            } else if (newsItem.getUrl() != null && !newsItem.getUrl().isEmpty()) {
                originUrlToSend = newsItem.getUrl();
            }

            if (originUrlToSend != null) {
                json.put("origin_url", originUrlToSend);
                json.put("image_url", originUrlToSend);   // 이미지 URL이 별도 없으니 일단 동일하게
            }

            String url;
            if (usePersonalized) {
                // ✅ 맞춤형 TTS: user_id + max_chars (+ origin/image)
                json.put("user_id", TEMP_USER_ID);
                url = TTS_PERSONALIZED_URL;
            } else {
                // ✅ 일반 TTS: text + max_chars (+ origin/image)
                json.put("text", text);
                url = TTS_URL;
            }

            Log.d(TAG, "TTS 요청 (" + (usePersonalized ? "personalized" : "normal")
                    + ") pos=" + positionForThisRequest + " body=" + json.toString());

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    Log.e(TAG, "TTS 요청 실패 position=" + positionForThisRequest, e);
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) {
                    if (!response.isSuccessful()) {
                        String errorBody = "";
                        try {
                            if (response.body() != null) {
                                errorBody = response.body().string();
                            }
                        } catch (Exception ignored) {}

                        Log.e(TAG, "TTS 응답 실패: code=" + response.code()
                                + " body=" + errorBody);
                        return;
                    }

                    try {
                        // 🔹 3) 헤더에서 실제 읽힌 스크립트/이미지 URL 꺼내기
                        String ttsText = response.header("X-TTS-Text");
                        String imageUrlFromHeader = response.header("X-Image-Url");

                        // TTS가 실제로 읽은 문장을 summary에 반영 → 화면과 음성 일치
                        if (ttsText != null && !ttsText.trim().isEmpty()) {
                            String decoded = ttsText;
                            try {
                                // 서버가 Base64로 보내는 경우 디코딩
                                byte[] bytes = Base64.decode(ttsText, Base64.DEFAULT);
                                decoded = new String(bytes, StandardCharsets.UTF_8);
                            } catch (IllegalArgumentException e) {
                                // Base64가 아니면 그냥 원문 사용
                                Log.w(TAG, "X-TTS-Text Base64 decode 실패, raw 사용", e);
                            }
                            newsItem.setSummary(decoded);
                        }

                        // originUrl이 비어 있을 때만, 헤더의 이미지 URL로 채워 넣기
                        String oldOrigin = newsItem.getOriginUrl();
                        if ((oldOrigin == null || oldOrigin.trim().isEmpty())
                                && imageUrlFromHeader != null
                                && !imageUrlFromHeader.trim().isEmpty()) {
                            newsItem.setOriginUrl(imageUrlFromHeader);
                        }

                        // UI 갱신
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                try {
                                    adapter.notifyItemChanged(positionForThisRequest);
                                } catch (Exception ignored) {}
                            });
                        }

                        // 🔹 4) 오디오 파일 저장
                        InputStream is = response.body().byteStream();
                        File cacheDir = requireContext().getCacheDir();
                        File file = new File(cacheDir,
                                "reel_tts_" + positionForThisRequest + ".mp3");

                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[8 * 1024];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        fos.flush();
                        fos.close();
                        is.close();

                        long fileSize = file.length();
                        Log.d(TAG, "TTS 파일 저장 완료 pos=" + positionForThisRequest +
                                ", size=" + fileSize + " bytes");

                        if (fileSize == 0) {
                            Log.e(TAG, "TTS 파일 크기가 0입니다. 재생하지 않음.");
                            return;
                        }

                        // 캐시에 저장 (지금은 안 쓰지만 남겨둠)
                        ttsCache.put(positionForThisRequest, file);

                        if (!autoPlay) return;
                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {
                            if (positionForThisRequest == currentPosition) {
                                initMediaPlayerWithFile(file, positionForThisRequest);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "오디오 파일 처리 / 헤더 반영 중 오류", e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "TTS 요청 JSON 구성 오류", e);
        }
    }

    // --------------------------------------------------------------------
    // 3. MediaPlayer 관리 (무한 루프)
    // --------------------------------------------------------------------

    private void initMediaPlayerWithFile(File file, int positionForThisAudio) {
        stopMediaPlayerOnly();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());

            mediaPlayer.setOnPreparedListener(MediaPlayer::start);

            // 🔁 재생 끝나면 다음 카드로, 마지막이면 0번으로 루프
            mediaPlayer.setOnCompletionListener(mp -> {
                if (newsList.isEmpty()) return;

                if (positionForThisAudio == currentPosition) {
                    int next = (positionForThisAudio + 1) % newsList.size();
                    vpNewsReels.setCurrentItem(next, true);
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error what=" + what + ", extra=" + extra);
                return false;
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "MediaPlayer 초기화 실패", e);
        }
    }

    private void stopMediaPlayerOnly() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            } catch (IllegalStateException ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void stopCurrentAudio() {
        stopMediaPlayerOnly();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCurrentAudio();
    }
}
