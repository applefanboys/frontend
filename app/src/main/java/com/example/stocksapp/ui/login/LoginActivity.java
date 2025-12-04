package com.example.stocksapp.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.LoginRequest;
import com.example.stocksapp.data.model.LoginResponse;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.main.MainActivity;
import com.example.stocksapp.ui.onboarding.OnboardingActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private TextView tvGoSignUp;
    private TextView tvGoResetPassword;
    private CheckBox cbAutoLogin; // 🔥 추가됨

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 자동 로그인 체크: 이미 로그인 유지 true면 바로 MainActivity로 이동
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean autoLogin = prefs.getBoolean("auto_login", false);
        if (autoLogin) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoSignUp = findViewById(R.id.tvGoSignUp);
        tvGoResetPassword = findViewById(R.id.tvGoResetPassword);
        cbAutoLogin = findViewById(R.id.cbAutoLogin); // 🔥 XML에서 추가해야 함

        // 🔥 로그인 버튼 → 서버로 로그인 요청
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest request = new LoginRequest(email, password);
            ApiService api = RetrofitClient.getApiService();

            api.login(request).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse body = response.body();

                        // 메시지 출력
                        String msg = body.getMessage();
                        Toast.makeText(LoginActivity.this,
                                msg != null ? msg : "로그인 성공!",
                                Toast.LENGTH_SHORT).show();

                        // 🔥 자동 로그인 체크 저장
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("email", email);
                        editor.putBoolean("auto_login", cbAutoLogin.isChecked());
                        editor.apply();

                        // 🔥 온보딩 또는 메인 화면으로 이동
                        Intent intent = new Intent(LoginActivity.this, OnboardingActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "로그인 실패: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this,
                            "네트워크 오류: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 회원가입 화면으로 이동
        tvGoSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // 비밀번호 재설정 화면으로 이동
        tvGoResetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
