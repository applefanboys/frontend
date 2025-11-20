package com.example.stocksapp.ui.main; // 🟢 패키지 이름 수정됨

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R; // 🟢 R 클래스 import (리소스 접근용)

public class AudioNewsActivity extends AppCompatActivity {

    // UI 요소 변수 선언
    private ImageButton backButton;
    private SeekBar playbackSeekBar;
    private ImageButton playPauseButton;

    // 오디오 재생 관련 변수
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(); // SeekBar 업데이트용 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_news); // 레이아웃 파일 연결

        // UI 요소 초기화 (ID 연결)
        backButton = findViewById(R.id.backButton);
        playbackSeekBar = findViewById(R.id.playbackSeekBar);
        playPauseButton = findViewById(R.id.playPauseButton);

        // --- MediaPlayer 초기화 ---
        // res/raw/news_audio.m4a 파일을 로드합니다.
        // ⚠️ 주의: res/raw 폴더에 'news_audio'라는 이름의 파일이 있어야 합니다.
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.news_audio);
        }

        // 오디오 파일의 총 길이를 가져와서 SeekBar의 최대값으로 설정
        if (mediaPlayer != null) {
            playbackSeekBar.setMax(mediaPlayer.getDuration());
        }

        // 버튼 및 리스너 설정 함수 호출
        setupListeners();
    }

    private void setupListeners() {
        // 뒤로가기 버튼 클릭 시 액티비티 종료
        backButton.setOnClickListener(v -> finish());

        // 재생/일시정지 버튼 클릭 이벤트
        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pausePlayback(); // 재생 중이면 일시정지
            } else {
                playPlayback(); // 멈춰있으면 재생
            }
        });

        // SeekBar 조작 리스너
        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 사용자가 직접 SeekBar를 움직였을 때만 오디오 위치 이동
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 사용자가 터치를 시작하면 SeekBar 자동 업데이트 중지 (충돌 방지)
                handler.removeCallbacks(updateSeekBarAction);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 사용자가 손을 떼면, 재생 중일 경우 다시 업데이트 시작
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler.post(updateSeekBarAction);
                }
            }
        });

        // 오디오 재생이 끝까지 다 돌았을 때 처리
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> {
                playPauseButton.setImageResource(R.drawable.ic_play); // 아이콘을 재생 모양으로 변경
                playbackSeekBar.setProgress(0); // 진행바 초기화
                handler.removeCallbacks(updateSeekBarAction); // 핸들러 중지
            });
        }
    }

    // 재생 시작 메서드
    private void playPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_pause); // 일시정지 아이콘으로 변경
            handler.post(updateSeekBarAction); // SeekBar 업데이트 시작
        }
    }

    // 일시정지 메서드
    private void pausePlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.ic_play); // 재생 아이콘으로 변경
            handler.removeCallbacks(updateSeekBarAction); // SeekBar 업데이트 중지
        }
    }

    // 0.5초마다 SeekBar 위치를 업데이트하는 Runnable
    private final Runnable updateSeekBarAction = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                playbackSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500); // 0.5초 뒤에 다시 실행
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티가 꺼질 때 메모리 누수 방지 및 리소스 해제
        handler.removeCallbacks(updateSeekBarAction);
        if (mediaPlayer != null) {
            mediaPlayer.release(); // MediaPlayer 해제
            mediaPlayer = null;
        }
    }
}