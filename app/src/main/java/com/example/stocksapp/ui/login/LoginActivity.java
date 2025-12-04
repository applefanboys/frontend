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
    private CheckBox cbAutoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 자동 로그인 여부 체크 → 온보딩 완료 여부에 따라 분기
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences onboardingPrefs = getSharedPreferences("onboarding_prefs", MODE_PRIVATE);
        boolean autoLogin = prefs.getBoolean("auto_login", false);
        if (autoLogin) {
            boolean completed = onboardingPrefs.getBoolean("completed", false);
            Intent intent = completed
                    ? new Intent(LoginActivity.this, MainActivity.class)
                    : new Intent(LoginActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoSignUp = findViewById(R.id.tvGoSignUp);
        tvGoResetPassword = findViewById(R.id.tvGoResetPassword);
        cbAutoLogin = findViewById(R.id.cbAutoLogin);

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

                        String msg = body.getMessage();
                        Toast.makeText(LoginActivity.this,
                                msg != null ? msg : "로그인 성공!",
                                Toast.LENGTH_SHORT).show();

                        // 로그인 정보 + 자동로그인 옵션 저장
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("email", email);
                        editor.putBoolean("auto_login", cbAutoLogin.isChecked());
                        editor.apply();

                        // 온보딩 완료 여부에 따라 다음 화면 결정
                        SharedPreferences onboardingPrefs = getSharedPreferences("onboarding_prefs", MODE_PRIVATE);
                        boolean completed = onboardingPrefs.getBoolean("completed", false);
                        Intent intent = completed
                                ? new Intent(LoginActivity.this, MainActivity.class)
                                : new Intent(LoginActivity.this, OnboardingActivity.class);
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

        tvGoSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

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
