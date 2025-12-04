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

    private ViewPager2 vpNewsReels;
    private NewsReelsAdapter adapter;
    private final List<NewsItem> newsList = new ArrayList<>();

    private final OkHttpClient httpClient = new OkHttpClient();

    private MediaPlayer mediaPlayer;
    private int currentPosition = 0;

    // position -> 캐싱된 mp3 파일
    private final Map<Integer, File> ttsCache = new HashMap<>();

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
        if (feed != null && feed.getNews() != null) {
            newsList.clear();
            newsList.addAll(feed.getNews());
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

        NewsItem item = newsList.get(position);

        // 일단 제목 기준으로 TTS (요약 필드 있으면 여기 변경)
        String text = item.getTitle();
        if (text == null) text = "";
        text = text.trim();

        // TTS 속도 위해 길이 제한
        if (text.length() > 150) {
            text = text.substring(0, 150);
        }

        // 1) 현재 카드용: 캐시 있으면 바로 재생, 없으면 요청 + 자동재생
        File cached = ttsCache.get(position);
        if (cached != null && cached.exists()) {
            initMediaPlayerWithFile(cached, position);
        } else {
            requestTtsFromServer(text, position, true);
        }

        // 2) 위/아래 카드 프리페치 (최대한 빨리 재생되도록)
        int prev = position - 1;
        int next = position + 1;

        if (prev >= 0 && !ttsCache.containsKey(prev)) {
            NewsItem prevItem = newsList.get(prev);
            String prevText = prevItem.getTitle();
            if (prevText == null) prevText = "";
            prevText = prevText.trim();
            if (prevText.length() > 150) {
                prevText = prevText.substring(0, 150);
            }
            requestTtsFromServer(prevText, prev, false);
        }

        if (next < newsList.size() && !ttsCache.containsKey(next)) {
            NewsItem nextItem = newsList.get(next);
            String nextText = nextItem.getTitle();
            if (nextText == null) nextText = "";
            nextText = nextText.trim();
            if (nextText.length() > 150) {
                nextText = nextText.substring(0, 150);
            }
            requestTtsFromServer(nextText, next, false);
        }
    }

    /**
     * /api/tts/shortform 호출
     *
     * @param autoPlay true  → 이 카드용 음성: 다운로드 후 바로 재생 시도
     *                 false → 프리페치용: 파일만 캐시에 저장
     */
    private void requestTtsFromServer(String text, int positionForThisRequest, boolean autoPlay) {
        try {
            JSONObject json = new JSONObject();
            json.put("text", text);
            // TTS 생성 속도 위해 max_chars 살짝 줄임
            json.put("max_chars", 120);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(TTS_URL)
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
                        Log.e(TAG, "TTS 응답 실패: " + response.code());
                        return;
                    }

                    try {
                        InputStream is = response.body().byteStream();
                        File cacheDir = requireContext().getCacheDir();
                        File file = new File(cacheDir, "reel_tts_" + positionForThisRequest + ".mp3");

                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[8 * 1024];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        fos.flush();
                        fos.close();
                        is.close();

                        // 캐시에 저장 (이미 있으면 덮어쓰기)
                        ttsCache.put(positionForThisRequest, file);

                        if (!autoPlay) {
                            // 프리페치 요청은 여기서 끝
                            return;
                        }

                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {
                            // 사용자가 스와이프해서 이미 다른 카드로 이동했다면 재생하지 않음
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
            } catch (Exception ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 페이지 전환 등에서 호출
     * - 재생 중인 음성만 끊고, 캐시는 남겨두어
     *   다시 위로 올려도 즉시 재생 가능하게 유지
     */
    private void stopCurrentAudio() {
        stopMediaPlayerOnly();
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
