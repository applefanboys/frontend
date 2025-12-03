package com.example.stocksapp.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.LoginRequest;
import com.example.stocksapp.data.model.LoginResponse;
import com.example.stocksapp.data.model.OnboardingStatus;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // View 연결
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoSignUp = findViewById(R.id.tvGoSignUp);
        tvGoResetPassword = findViewById(R.id.tvGoResetPassword);

        // 로그인 버튼 리스너
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            doLogin(email, password);
        });

        // 회원가입 화면 이동
        tvGoSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // 비밀번호 재설정 화면 이동
        tvGoResetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void doLogin(String email, String password) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        LoginRequest request = new LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse result = response.body();
                    int userId = result.getUser().getId();

                    // 1. 유저 ID 저장 (필수)
                    SharedPreferences sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("USER_ID", userId);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                    // 2. 온보딩 상태 확인 후 이동 (바로 이동 X)
                    checkOnboardingAndMove(userId);

                } else {
                    Toast.makeText(LoginActivity.this, "이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LOGIN", "Error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkOnboardingAndMove(int userId) {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        apiService.getOnboardingStatus(userId).enqueue(new Callback<OnboardingStatus>() {
            @Override
            public void onResponse(Call<OnboardingStatus> call, Response<OnboardingStatus> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OnboardingStatus status = response.body();

                    // 필수 단계인 Q2(선호 키워드)까지 완료했으면 메인으로 이동
                    if (status.isQ2_completed()) {
                        Log.d("Login", "필수 온보딩 완료 -> MainActivity");
                        goToActivity(MainActivity.class);
                    } else {
                        // 미완료 시 온보딩으로 이동
                        Log.d("Login", "온보딩 미완료 -> OnboardingActivity");
                        goToActivity(OnboardingActivity.class);
                    }
                } else {
                    // 서버 응답 오류 시 안전하게 온보딩으로 이동
                    Log.e("Login", "상태 확인 실패 code: " + response.code());
                    goToActivity(OnboardingActivity.class);
                }
            }

            @Override
            public void onFailure(Call<OnboardingStatus> call, Throwable t) {
                // 통신 실패 시 안전하게 온보딩으로 이동
                Log.e("Login", "통신 에러: " + t.getMessage());
                goToActivity(OnboardingActivity.class);
            }
        });
    }

    private void goToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(LoginActivity.this, targetActivity);
        // 뒤로가기 시 로그인 화면이 다시 나오지 않도록 플래그 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}