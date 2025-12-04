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

    // API 명세서: POST /api/tts/shortform
    private static final String TTS_URL =
            "https://api.short-economy.store/api/tts/shortform";

    private ImageButton backButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ImageButton playPauseButton;
    private SeekBar playbackSeekBar;

    private TextView newsTitle;
    private TextView newsSummary;
    private ImageView newsImage;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private File audioFile;
    private boolean isAudioReady = false;
    private boolean isLoading = false;

    private OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_news);

        backButton = findViewById(R.id.backButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        playbackSeekBar = findViewById(R.id.playbackSeekBar);

        newsTitle = findViewById(R.id.newsTitle);
        newsSummary = findViewById(R.id.newsSummary);
        newsImage = findViewById(R.id.newsImage);

        playPauseButton.setEnabled(false);

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

        // 요약 텍스트를 그대로 text 필드로 보내서 TTS 생성
        String summaryText = newsSummary.getText().toString();
        int maxChars = 180;
        requestTtsFromServer(summaryText, maxChars);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        prevButton.setOnClickListener(v ->
                Toast.makeText(this, "이전 뉴스 이동 기능은 아직 미구현입니다.", Toast.LENGTH_SHORT).show()
        );

        nextButton.setOnClickListener(v ->
                Toast.makeText(this, "다음 뉴스 이동 기능은 아직 미구현입니다.", Toast.LENGTH_SHORT).show()
        );

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

    // text + max_chars 로 /api/tts/shortform 호출
    private void requestTtsFromServer(String text, int maxChars) {
        if (isLoading) return;
        isLoading = true;
        playPauseButton.setEnabled(false);
        Toast.makeText(this, "뉴스 음성을 생성 중입니다...", Toast.LENGTH_SHORT).show();

        try {
            JSONObject json = new JSONObject();
            json.put("text", text);
            json.put("max_chars", maxChars);

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
                        audioFile = new File(getCacheDir(), "shortform_news.mp3");

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
