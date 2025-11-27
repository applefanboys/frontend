package com.example.stocksapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.ui.onboarding.OnboardingActivity;

public class LoginActivity extends AppCompatActivity {

    // 아이디, 비밀번호 입력창
    private EditText etUsername;
    private EditText etPassword;

    // 로그인 버튼
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 레이아웃의 UI 요소들과 연결
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // 로그인 버튼 클릭 시 실행
        // 지금은 API 호출 없이 단순히 온보딩으로 넘어가기만 함
        // 나중에 백엔드 API 완성되면 이 부분에서 Retrofit으로 로그인 요청 보내면 됨
        btnLogin.setOnClickListener(v -> {

            // 나중에 이 부분에 친구가 작성한 로그인 API 연동 들어갈 예정
            // ex) login(username, password) → 토큰 저장 → 다음 화면 이동

            // 임시: 버튼 누르면 그냥 OnboardingActivity로 이동
            Intent intent = new Intent(LoginActivity.this, OnboardingActivity.class);
            startActivity(intent);

            // 로그인 후 뒤로가기 누르면 다시 로그인 화면이 뜨지 않도록 finish()
            finish();
        });
    }
}
