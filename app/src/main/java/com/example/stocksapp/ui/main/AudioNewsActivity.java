package com.example.stocksapp.ui.main;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AudioNewsActivity extends AppCompatActivity {

    private static final String TAG = "AudioNewsActivity";

    // 🔹 실제 API 명세서와 동일하게 수정
    private static final String TTS_URL =
            "https://api.short-economy.store/api/tts/shortform/personalized";

    // ---------- UI 요소 ----------
    private ImageButton backButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ImageButton playPauseButton;
    private SeekBar playbackSeekBar;

    private TextView newsTitle;
    private TextView newsSummary;
    private ImageView newsImage;

    // ---------- 오디오 / TTS ----------
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private File audioFile;
    private boolean isAudioReady = false;
    private boolean isLoading = false;

    private OkHttpClient httpClient = new OkHttpClient();

    // (예시) 로그인 시 저장해둔 userId를 불러온다고 가정
    private int getUserId() {
        // TODO: 실제론 SharedPreferences 등에서 가져와야 함
        // 예: return getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", 0);
        return 1; // 일단 하드코딩 (테스트용)
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_news);

        // UI 바인딩
        backButton = findViewById(R.id.backButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        // playPauseButton = findViewById(R.id.playPauseButton);
        playbackSeekBar = findViewById(R.id.playbackSeekBar);

        newsTitle = findViewById(R.id.newsTitle);
        newsSummary = findViewById(R.id.newsSummary);
        newsImage = findViewById(R.id.newsImage);

        // 처음에는 재생 버튼 비활성화 (TTS 내려받기 전)
        playPauseButton.setEnabled(false);

        // 인텐트로부터 뉴스 정보 전달받기 (없으면 XML 기본 텍스트 사용)
        String title = getIntent().getStringExtra("NEWS_TITLE");
        String summary = getIntent().getStringExtra("NEWS_SUMMARY");
        int imageResId = getIntent().getIntExtra("NEWS_IMAGE_RES_ID", 0);

        if (title != null && !title.trim().isEmpty()) {
            newsTitle.setText(title);
        }
        if (summary != null && !summary.trim().isEmpty()) {
            newsSummary.setText(summary);
        }
        if (imageResId != 0) {
            newsImage.setImageResource(imageResId);
        }

        setupListeners();

        // 🔹 API 명세서상 text를 보낼 필요가 없으므로, 여기서는 user_id 기준으로만 요청
        int userId = getUserId();
        int maxChars = 180;

        // 액티비티 진입 시 TTS 요청
        requestTtsFromServer(userId, maxChars);
    }

    private void setupListeners() {
        // 뒤로가기
        backButton.setOnClickListener(v -> finish());

        // 이전/다음 뉴스 (지금은 자리만 만들어 둠)
        prevButton.setOnClickListener(v ->
                Toast.makeText(this, "이전 뉴스 이동 기능은 아직 미구현입니다.", Toast.LENGTH_SHORT).show()
        );

        nextButton.setOnClickListener(v ->
                Toast.makeText(this, "다음 뉴스 이동 기능은 아직 미구현입니다.", Toast.LENGTH_SHORT).show()
        );

        // 재생/일시정지
        playPauseButton.setOnClickListener(v -> {
            if (!isAudioReady) {
                Toast.makeText(this, "음성을 준비 중입니다. 잠시만 기다려 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pausePlayback();
            } else {
                playPlayback();
            }
        });

        // 시크바 조작
        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
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

    // ---------- 재생/일시정지 ----------

    private void playPlayback() {
        if (mediaPlayer != null) {
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

    private final Runnable updateSeekBarAction = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                playbackSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
            }
        }
    };

    // ---------- 백엔드 TTS 요청 (API 명세서에 맞게 수정된 부분) ----------

    private void requestTtsFromServer(int userId, int maxChars) {
        if (isLoading) return;
        isLoading = true;
        playPauseButton.setEnabled(false);
        Toast.makeText(this, "뉴스 음성을 생성 중입니다...", Toast.LENGTH_SHORT).show();

        try {
            JSONObject json = new JSONObject();
            json.put("user_id", userId);     // 🔹 명세서에 맞게 user_id 사용
            json.put("max_chars", maxChars); // 🔹 max_chars 그대로 사용

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(TTS_URL)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "TTS 요청 실패", e);
                    runOnUiThread(() -> {
                        isLoading = false;
                        Toast.makeText(AudioNewsActivity.this,
                                "TTS 생성에 실패했습니다. 네트워크를 확인해 주세요.",
                                Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) {
                    isLoading = false;
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "TTS 응답 실패: " + response.code());
                        runOnUiThread(() ->
                                Toast.makeText(AudioNewsActivity.this,
                                        "서버 오류: " + response.code(),
                                        Toast.LENGTH_LONG).show()
                        );
                        return;
                    }

                    try {
                        InputStream is = response.body().byteStream();
                        audioFile = new File(getCacheDir(), "shortform_news.mp3"); // 🔹 파일명도 맞춰줌

                        FileOutputStream fos = new FileOutputStream(audioFile);
                        byte[] buffer = new byte[8 * 1024];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        fos.flush();
                        fos.close();
                        is.close();

                        runOnUiThread(() -> initMediaPlayerWithFile(audioFile));
                    } catch (Exception e) {
                        Log.e(TAG, "오디오 파일 저장 중 오류", e);
                        runOnUiThread(() ->
                                Toast.makeText(AudioNewsActivity.this,
                                        "오디오 파일 처리 중 오류가 발생했습니다.",
                                        Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });
        } catch (Exception e) {
            isLoading = false;
            Log.e(TAG, "TTS 요청 JSON 구성 오류", e);
            Toast.makeText(this,
                    "요청 데이터 구성 중 오류가 발생했습니다.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ---------- 다운로드된 파일로 MediaPlayer 초기화 ----------

    private void initMediaPlayerWithFile(File file) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());

            mediaPlayer.setOnPreparedListener(mp -> {
                isAudioReady = true;
                playbackSeekBar.setMax(mp.getDuration());
                playPauseButton.setEnabled(true);
                playPauseButton.setImageResource(R.drawable.ic_play);
                Toast.makeText(AudioNewsActivity.this,
                        "뉴스 음성 준비 완료!",
                        Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                playPauseButton.setImageResource(R.drawable.ic_play);
                playbackSeekBar.setProgress(0);
                handler.removeCallbacks(updateSeekBarAction);
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "MediaPlayer 초기화 실패", e);
            Toast.makeText(this,
                    "오디오 재생 준비 중 오류가 발생했습니다.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateSeekBarAction);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (audioFile != null && audioFile.exists()) {
            audioFile.delete();
        }
    }
}