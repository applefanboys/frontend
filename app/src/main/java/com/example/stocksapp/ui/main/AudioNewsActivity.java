package com.example.stocksapp.ui.main;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.ContentRequest;
import com.example.stocksapp.data.model.NewsItem;
import com.example.stocksapp.data.model.ShortformTTSRequest;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AudioNewsActivity extends AppCompatActivity {

    private ImageButton backButton, prevButton, nextButton, playPauseButton;
    private SeekBar playbackSeekBar;
    private TextView tvNewsTitle, tvNewsSummary;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    private List<NewsItem> newsList = new ArrayList<>();
    private int currentIndex = 0;

    private int currentRequestSequence = 0;
    private Call<ResponseBody> currentTtsCall;
    private Call<ResponseBody> currentContentCall;

    private boolean isAudioReady = false; // 오디오 준비 상태 플래그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_news);

        backButton = findViewById(R.id.backButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        playbackSeekBar = findViewById(R.id.playbackSeekBar);
        playPauseButton = findViewById(R.id.playPauseButton);
        tvNewsTitle = findViewById(R.id.newsTitle);
        tvNewsSummary = findViewById(R.id.newsSummary);

        if (getIntent() != null) {
            newsList = (List<NewsItem>) getIntent().getSerializableExtra("news_list");
            currentIndex = getIntent().getIntExtra("current_index", 0);
        }

        setupListeners();
        loadCurrentNews();
    }

    private void loadCurrentNews() {
        if (newsList == null || newsList.isEmpty()) return;

        // [버그 방지] 요청 순서 증가 및 이전 요청 취소
        currentRequestSequence++;
        if (currentTtsCall != null && !currentTtsCall.isCanceled()) currentTtsCall.cancel();
        if (currentContentCall != null && !currentContentCall.isCanceled()) currentContentCall.cancel();

        stopPlayback();
        isAudioReady = false;
        playPauseButton.setImageResource(R.drawable.ic_play);
        playbackSeekBar.setProgress(0);

        NewsItem item = newsList.get(currentIndex);

        // 1. UI 업데이트 (새 뉴스 제목 즉시 표시)
        tvNewsTitle.setText(item.getTitle());

        // 2. 내용 처리: summary가 충분히 길다면 그대로 사용, 아니면 원문 크롤링 + AI 요약
        String summary = item.getSummary();

        if (summary != null && summary.length() > 100) {
            // 이미 충분한 요약문이 있는 경우 -> 그대로 사용 & TTS 요청
            tvNewsSummary.setText(item.getSummary());
            fetchTTS(item.getSummary(), currentRequestSequence);
        } else {
            // 요약문이 짧으면(네이버 Snippet) -> AI 요약본을 실시간으로 가져옴
            tvNewsSummary.setText("AI가 상세 내용을 분석/요약 중입니다...\n(약 3~5초 소요)");
            fetchFullContent(item.getNewsUrl(), currentRequestSequence);
        }

        updateNavigationButtons();
    }

    private void fetchFullContent(String url, final int sequence) {
        if (url == null || url.isEmpty()) {
            tvNewsSummary.setText("뉴스 원문 링크가 없습니다.");
            return;
        }

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        currentContentCall = api.getNewsContent(new ContentRequest(url));

        currentContentCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (sequence != currentRequestSequence) return; // 오래된 요청 무시

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonStr = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonStr);
                        String fullContent = jsonObject.getString("content");

                        // 화면 갱신 및 TTS 요청
                        tvNewsSummary.setText(fullContent);

                        // [캐시] 다음 번 재생을 위해 리스트 객체에 저장
                        newsList.get(currentIndex).setSummary(fullContent);

                        fetchTTS(fullContent, sequence);

                    } catch (Exception e) {
                        tvNewsSummary.setText("내용을 불러오지 못했습니다. (파싱 오류)");
                    }
                } else {
                    tvNewsSummary.setText("내용을 불러오지 못했습니다. (서버 오류)");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!call.isCanceled()) { // 취소된 요청 아니면
                    tvNewsSummary.setText("내용을 불러오지 못했습니다. (네트워크 오류)");
                }
            }
        });
    }

    private void fetchTTS(String text, final int sequence) {
        Toast.makeText(this, "음성 생성 중...", Toast.LENGTH_SHORT).show();
        playPauseButton.setEnabled(false);

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        String ttsText = text.length() > 500 ? text.substring(0, 500) : text;

        ShortformTTSRequest request = new ShortformTTSRequest(ttsText);

        currentTtsCall = apiService.getShortformTTS(request);

        currentTtsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (sequence != currentRequestSequence) return; // 오래된 요청 무시

                if (response.isSuccessful() && response.body() != null) {
                    ResponseBody body = response.body();
                    new Thread(() -> {
                        boolean saved = saveAudioFile(body);
                        runOnUiThread(() -> {
                            if (saved) prepareMediaPlayer();
                            else Toast.makeText(AudioNewsActivity.this, "오디오 저장 실패", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                } else {
                    Toast.makeText(AudioNewsActivity.this, "오디오 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!call.isCanceled()) { // 취소된 요청 아니면
                    Toast.makeText(AudioNewsActivity.this, "서버 연결 오류", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateNavigationButtons() {
        prevButton.setVisibility(currentIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        nextButton.setVisibility(currentIndex < newsList.size() - 1 ? View.VISIBLE : View.INVISIBLE);
    }

    private boolean saveAudioFile(ResponseBody body) {
        try {
            File audioFile = new File(getCacheDir(), "temp_tts.mp3");
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(audioFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) break;
                    outputStream.write(fileReader, 0, read);
                }
                outputStream.flush();
                return true;
            } catch (Exception e) {
                Log.e("AudioNewsActivity", "파일 저장 중 오류", e);
                return false;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (Exception e) {
            Log.e("AudioNewsActivity", "saveAudioFile 전체 오류", e);
            return false;
        }
    }

    private void prepareMediaPlayer() {
        try {
            if (mediaPlayer != null) mediaPlayer.release();

            mediaPlayer = new MediaPlayer();
            File audioFile = new File(getCacheDir(), "temp_tts.mp3");

            if (!audioFile.exists()) return;

            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            playbackSeekBar.setMax(mediaPlayer.getDuration());

            isAudioReady = true;
            playPauseButton.setEnabled(true);
            playPlayback();

            mediaPlayer.setOnCompletionListener(mp -> {
                playPauseButton.setImageResource(R.drawable.ic_play);
                playbackSeekBar.setProgress(0);
                handler.removeCallbacks(updateSeekBarAction);

                if (newsList != null && !newsList.isEmpty()
                        && currentIndex < newsList.size() - 1) {
                    // 다음 뉴스로 자동 이동
                    currentIndex++;
                    loadCurrentNews();
                } else {
                    // 마지막 뉴스이면 다음 기사로 이동하지 않고 정지 상태 유지
                }
            });

        } catch (Exception e) {
            Log.e("AudioNewsActivity", "prepareMediaPlayer 오류", e);
        }
    }

    private void playPlayback() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);

            handler.post(updateSeekBarAction);
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updateSeekBarAction);
        }
    }

    private void cancelRequestsAndStopPlayback() {
        // 더 이상 이전 응답을 처리하지 않도록 시퀀스 증가
        currentRequestSequence++;

        // 진행 중인 네트워크 요청 취소
        if (currentTtsCall != null && !currentTtsCall.isCanceled()) {
            currentTtsCall.cancel();
        }
        if (currentContentCall != null && !currentContentCall.isCanceled()) {
            currentContentCall.cancel();
        }

        // 오디오 재생 중지 및 리소스 해제
        stopPlayback();
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    private final Runnable updateSeekBarAction = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                playbackSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
            }
        }
    };

    private void setupListeners() {
        backButton.setOnClickListener(v -> {
            cancelRequestsAndStopPlayback();
            finish();
        });

        prevButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                loadCurrentNews(); // 이전 뉴스로 갱신
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentIndex < newsList.size() - 1) {
                currentIndex++;
                loadCurrentNews(); // 다음 뉴스로 갱신
            }
        });

        playPauseButton.setOnClickListener(v -> {
            if (!isAudioReady) return;
            if (mediaPlayer != null && mediaPlayer.isPlaying()) pausePlayback();
            else playPlayback();
        });

        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null && isAudioReady) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBarAction);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler.post(updateSeekBarAction);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        cancelRequestsAndStopPlayback();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        cancelRequestsAndStopPlayback();
        super.onDestroy();
    }
}