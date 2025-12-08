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
import com.example.stocksapp.data.model.TodayNewsResponse;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopicsFragment extends Fragment {

    private static final String TAG = "TopicsFragment";

    private static final String TTS_URL =
            "https://api.short-economy.store/api/tts/shortform";
    private static final String TTS_PERSONALIZED_URL =
            "https://api.short-economy.store/api/tts/shortform/personalized";

    private static final int TEMP_USER_ID = 1;

    private ViewPager2 vpNewsReels;
    private NewsReelsAdapter adapter;

    private final List<NewsItem> newsList = new ArrayList<>();

    private final OkHttpClient httpClient = new OkHttpClient();

    private MediaPlayer mediaPlayer;
    private int currentPosition = 0;

    private final Random random = new Random();
    private final Map<Integer, File> ttsCache = new HashMap<>();
    private final Map<Integer, Boolean> ttsModeMap = new HashMap<>();

    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_topics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = RetrofitClient.getApiService();

        vpNewsReels = view.findViewById(R.id.vpNewsReels);
        vpNewsReels.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        adapter = new NewsReelsAdapter(newsList);
        vpNewsReels.setAdapter(adapter);

        vpNewsReels.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                startTtsForPosition(position);
            }
        });

        loadTodayNews();
    }

    private void loadTodayNews() {
        apiService.getTodayNews()
                .enqueue(new Callback<TodayNewsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TodayNewsResponse> call,
                                           @NonNull Response<TodayNewsResponse> response) {
                        if (!isAdded()) return;

                        if (!response.isSuccessful() || response.body() == null) {
                            Log.e(TAG, "뉴스 로드 실패: " + response.code());
                            return;
                        }

                        List<NewsItem> apiNews = response.body().getData();
                        if (apiNews == null || apiNews.isEmpty()) {
                            Log.e(TAG, "뉴스 없음");
                            return;
                        }

                        newsList.clear();
                        newsList.addAll(apiNews);

                        int baseSize = apiNews.size();
                        while (newsList.size() < 50) {
                            newsList.add(apiNews.get(newsList.size() % baseSize));
                        }

                        adapter.notifyDataSetChanged();

                        if (!newsList.isEmpty()) {
                            vpNewsReels.post(() -> startTtsForPosition(0));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TodayNewsResponse> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Log.e(TAG, "뉴스 로드 실패", t);
                    }
                });
    }

    private void startTtsForPosition(int position) {
        if (position < 0 || position >= newsList.size()) return;

        stopCurrentAudio();

        NewsItem item = newsList.get(position);
        String text = buildTtsText(item);

        boolean usePersonalized = getOrAssignTtsMode(position);

        File cached = ttsCache.get(position);
        Boolean cachedMode = ttsModeMap.get(position);

        if (cached != null && cached.exists()
                && cachedMode != null && cachedMode == usePersonalized) {
            initMediaPlayerWithFile(cached, position);
        } else {
            requestTtsFromServer(text, position, true, usePersonalized);
        }

        int prev = position - 1;
        int next = position + 1;

        if (prev >= 0 && !ttsCache.containsKey(prev)) {
            String prevText = buildTtsText(newsList.get(prev));
            boolean prevPersonalized = getOrAssignTtsMode(prev);
            requestTtsFromServer(prevText, prev, false, prevPersonalized);
        }

        if (next < newsList.size() && !ttsCache.containsKey(next)) {
            String nextText = buildTtsText(newsList.get(next));
            boolean nextPersonalized = getOrAssignTtsMode(next);
            requestTtsFromServer(nextText, next, false, nextPersonalized);
        }
    }

    private String buildTtsText(NewsItem item) {
        if (item == null) return "오늘의 경제 뉴스입니다.";

        String text = item.getSummary();
        if (text == null || text.trim().isEmpty()) {
            text = item.getTitle();
        }
        if (text == null || text.trim().isEmpty()) {
            text = "오늘의 경제 뉴스입니다.";
        }
        return text;
    }

    private boolean getOrAssignTtsMode(int position) {
        Boolean mode = ttsModeMap.get(position);
        if (mode != null) return mode;
        boolean newMode = random.nextBoolean();
        ttsModeMap.put(position, newMode);
        return newMode;
    }

    private void requestTtsFromServer(String text,
                                      int positionForThisRequest,
                                      boolean autoPlay,
                                      boolean usePersonalized) {
        try {
            if (text == null) text = "";
            text = text.trim();
            if (text.isEmpty()) {
                text = "오늘의 경제 뉴스입니다.";
            }
            if (text.length() > 150) {
                text = text.substring(0, 150);
            }

            JSONObject json = new JSONObject();
            json.put("max_chars", 120);

            String url;
            if (usePersonalized) {
                json.put("user_id", TEMP_USER_ID);
                json.put("text", text);
                url = TTS_PERSONALIZED_URL;
            } else {
                json.put("text", text);
                url = TTS_URL;
            }

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call,
                                      @NonNull IOException e) {
                    Log.e(TAG, "TTS 요청 실패 position=" + positionForThisRequest, e);
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call,
                                       @NonNull okhttp3.Response response) {
                    if (!response.isSuccessful()) {
                        String errorBody = "";
                        try {
                            okhttp3.ResponseBody body = response.body();
                            if (body != null) {
                                errorBody = body.string();
                            }
                        } catch (Exception ignored) {
                        }
                        Log.e(TAG, "TTS 응답 실패: code=" + response.code()
                                + " body=" + errorBody);
                        return;
                    }

                    try {
                        okhttp3.ResponseBody body = response.body();
                        if (body == null) {
                            Log.e(TAG, "TTS body null");
                            return;
                        }
                        InputStream is = body.byteStream();

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

                        ttsCache.put(positionForThisRequest, file);
                        ttsModeMap.put(positionForThisRequest, usePersonalized);

                        if (!autoPlay) return;
                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {
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

    private void initMediaPlayerWithFile(File file, int positionForThisAudio) {
        stopMediaPlayerOnly();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());

            mediaPlayer.setOnPreparedListener(MediaPlayer::start);

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

    private void stopCurrentAudio() {
        stopMediaPlayerOnly();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCurrentAudio();
    }
}
