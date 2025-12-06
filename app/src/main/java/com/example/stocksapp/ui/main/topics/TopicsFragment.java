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
import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.data.model.NewsItem;
import com.example.stocksapp.data.repo.FakeFeedRepository;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 토픽 탭 – 인스타 릴스 / 유튜브 쇼츠 스타일
 * - 세로 ViewPager2
 * - 페이지 진입 시 자동 TTS 재생
 * - 음성 끝나면 다음 카드로 자동 이동
 * - 위/아래로 스와이프하면 이전 음성 stop + 새 카드 자동 재생
 * - 현재/위/아래 카드 TTS mp3 캐싱 → 최대한 빠르게 재생
 */
public class TopicsFragment extends Fragment {

    private static final String TAG = "TopicsFragment";

    // API 명세: POST /api/tts/shortform
    private static final String TTS_URL =
            "https://api.short-economy.store/api/tts/shortform";

    // 사용자 맞춤형 숏폼 TTS
    private static final String TTS_PERSONALIZED_URL =
            "https://api.short-economy.store/api/tts/shortform/personalized";

    // 로그인 연동 전 임시 user_id (TODO: 실제 로그인 연동 시 교체)
    private static final int TEMP_USER_ID = 1;

    private ViewPager2 vpNewsReels;
    private NewsReelsAdapter adapter;
    private final List<NewsItem> newsList = new ArrayList<>();

    private final OkHttpClient httpClient = new OkHttpClient();

    private MediaPlayer mediaPlayer;
    private int currentPosition = 0;

    private final Random random = new Random();

    // position -> 캐싱된 mp3 파일
    private final Map<Integer, File> ttsCache = new HashMap<>();

    // position -> 이 카드에 할당된 TTS 모드 (true: 개인화, false: 일반)
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

        vpNewsReels = view.findViewById(R.id.vpNewsReels);
        vpNewsReels.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        // 피드 데이터 로딩 (기존 FakeFeedRepository 사용)
        FeedResponse feed = FakeFeedRepository.getFeed();
        if (feed != null && feed.getNews() != null && !feed.getNews().isEmpty()) {
            newsList.clear();

            List<NewsItem> base = feed.getNews();
            int targetCount = 50;

            for (int i = 0; i < targetCount; i++) {
                NewsItem src = base.get(i % base.size());
                newsList.add(src);
            }
        }




        adapter = new NewsReelsAdapter(newsList);
        vpNewsReels.setAdapter(adapter);

        // 첫 카드 자동 재생
        if (!newsList.isEmpty()) {
            vpNewsReels.post(() -> startTtsForPosition(0));
        }

        // 페이지 바뀔 때마다 자동 재생
        vpNewsReels.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                startTtsForPosition(position);
            }
        });
    }

    /**
     * position 번째 뉴스에 대해 TTS 시작
     * - 이미 캐시되어 있으면 바로 재생
     * - 없으면 서버 요청
     * - 위/아래 카드도 미리 요청해서 캐시
     */
    private void startTtsForPosition(int position) {
        if (position < 0 || position >= newsList.size()) return;

        // 이전 카드 음성 정리
        stopCurrentAudio();

        // ===== 1) 현재 카드 텍스트 뽑기 =====
        NewsItem item = newsList.get(position);
        String text = buildTtsTextFromItem(item);

        // 이 카드에 대해 사용할 TTS 모드 결정 (true: 개인화, false: 일반)
        boolean usePersonalized = getOrAssignTtsMode(position);

        // 2) 현재 카드: 캐시 + 모드가 일치하면 바로 재생, 아니면 새로 요청
        File cached = ttsCache.get(position);
        Boolean cachedMode = ttsModeMap.get(position);
        if (cached != null && cached.exists()
                && cachedMode != null && cachedMode == usePersonalized) {
            initMediaPlayerWithFile(cached, position);
        } else {
            requestTtsFromServer(text, position, true, usePersonalized);
        }

        // ===== 3) 위/아래 카드 프리페치 =====
        int prev = position - 1;
        int next = position + 1;

        if (prev >= 0 && !ttsCache.containsKey(prev)) {
            NewsItem prevItem = newsList.get(prev);
            String prevText = buildTtsTextFromItem(prevItem);
            boolean prevPersonalized = getOrAssignTtsMode(prev);
            requestTtsFromServer(prevText, prev, false, prevPersonalized);
        }

        if (next < newsList.size() && !ttsCache.containsKey(next)) {
            NewsItem nextItem = newsList.get(next);
            String nextText = buildTtsTextFromItem(nextItem);
            boolean nextPersonalized = getOrAssignTtsMode(next);
            requestTtsFromServer(nextText, next, false, nextPersonalized);
        }
    }

    /**
     * 한 뉴스 카드에서 TTS에 쓸 텍스트 뽑기
     * - summary가 있으면 summary 우선
     * - 없으면 title
     * - 둘 다 없으면 기본 문구
     */
    private String buildTtsTextFromItem(NewsItem item) {
        if (item == null) return "오늘의 경제 뉴스입니다.";

        String text = null;

        // 요약 필드가 있으면 이걸 먼저 사용
        try {
            text = item.getSummary();
        } catch (Exception ignored) {}

        if (text == null || text.trim().isEmpty()) {
            try {
                text = item.getTitle();
            } catch (Exception ignored) {}
        }

        if (text == null || text.trim().isEmpty()) {
            text = "오늘의 경제 뉴스입니다.";
        }

        return text;
    }


    /**
     * 주어진 position에 대해 TTS 모드를 가져오거나(이미 있으면) 새로 50% 확률로 할당한다.
     * true  → 사용자 맞춤형 경제 뉴스 (/tts/shortform/personalized)
     * false → 일반 경제 뉴스 (/tts/shortform)
     */
    private boolean getOrAssignTtsMode(int position) {
        Boolean mode = ttsModeMap.get(position);
        if (mode != null) {
            return mode;
        }
        boolean newMode = random.nextBoolean();
        ttsModeMap.put(position, newMode);
        return newMode;
    }

    /**
     * TTS 호출
     *
     * @param autoPlay        true  → 이 카드용 음성: 다운로드 후 바로 재생 시도
     *                        false → 프리페치용: 파일만 캐시에 저장
     * @param usePersonalized true  → 사용자 맞춤형 경제 뉴스 (/tts/shortform/personalized)
     *                        false → 일반 경제 뉴스 (/tts/shortform)
     */
    private void requestTtsFromServer(String text,
                                      int positionForThisRequest,
                                      boolean autoPlay,
                                      boolean usePersonalized) {
        try {
            // 🔹 1) 공통 텍스트 전처리 (여기서 한 번에!)
            if (text == null) text = "";
            text = text.trim();
            if (text.isEmpty()) {
                text = "오늘의 경제 뉴스입니다.";
            }
            if (text.length() > 150) {
                text = text.substring(0, 150);
            }

            JSONObject json = new JSONObject();
            String url;

            // 둘 다 공통
            json.put("max_chars", 120);

            if (usePersonalized) {
                // ✅ 개인화도 text를 같이 보냄
                json.put("user_id", TEMP_USER_ID);
                json.put("text", text);
                url = TTS_PERSONALIZED_URL;
            } else {
                json.put("text", text);
                url = TTS_URL;
            }

            // 디버깅용 로그
            Log.d(TAG, "TTS 요청 (" + (usePersonalized ? "personalized" : "normal")
                    + ") pos=" + positionForThisRequest + " body=" + json.toString());

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
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

                        // 캐시에 저장
                        ttsCache.put(positionForThisRequest, file);
                        ttsModeMap.put(positionForThisRequest, usePersonalized);

                        if (!autoPlay) {
                            return; // 프리페치면 여기서 끝
                        }

                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {
                            // 사용자가 이미 다른 카드로 넘어갔으면 재생 안 함
                            if (positionForThisRequest == currentPosition) {
                                initMediaPlayerWithFile(file, positionForThisRequest);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "오디오 파일 저장 중 오류", e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "TTS 요청 JSON 구성 오류", e);
        }
    }



    /**
     * mp3 파일로 MediaPlayer 초기화 + 자동 재생 + 끝나면 다음 카드로 이동
     */
    private void initMediaPlayerWithFile(File file, int positionForThisAudio) {
        stopMediaPlayerOnly();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());

            // 준비되면 바로 재생
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);

            // 이 카드 읽기 끝나면 → 여전히 같은 카드 보고 있을 때만 다음 카드로
            mediaPlayer.setOnCompletionListener(mp -> {
                if (positionForThisAudio == currentPosition) {
                    int next = positionForThisAudio + 1;
                    if (next < newsList.size()) {
                        vpNewsReels.setCurrentItem(next, true);
                    }
                }
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "MediaPlayer 초기화 실패", e);
        }
    }

    /**
     * 현재 재생 중인 오디오만 stop/release (캐시는 유지)
     */
    private void stopMediaPlayerOnly() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 현재 재생 중인 오디오 stop + position 인덱스의 캐시도 제거
     */
    private void stopCurrentAudio() {
        stopMediaPlayerOnly();
        File cached = ttsCache.get(currentPosition);
        // 캐시를 완전히 지우고 싶으면 여기에서 삭제할 수도 있음
        // if (cached != null && cached.exists()) cached.delete();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCurrentAudio();

        // 화면 완전히 나갈 때 캐시까지 지우고 싶으면 여기 주석 해제
        /*
        for (File f : ttsCache.values()) {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
        ttsCache.clear();
        */
    }
}
