package com.example.stocksapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.LoginRequest;
import com.example.stocksapp.data.model.LoginResponse;
import com.example.stocksapp.data.model.OnboardingStatusResponse;
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

    // OnboardingActivity에 넘길 때 쓸 키 값들 (필요하면 OnboardingActivity 쪽에도 동일하게 선언)
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_START_STEP = "extra_start_step"; // 1, 2, 3 중 하나

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoSignUp = findViewById(R.id.tvGoSignUp);
        tvGoResetPassword = findViewById(R.id.tvGoResetPassword);

        // 🔥 로그인 버튼 → 서버로 로그인 요청
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

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

                        // 서버에서 내려주는 메시지/유저 정보 확인
                        String msg = body.getMessage();
                        Toast.makeText(LoginActivity.this,
                                msg != null ? msg : "로그인 성공!",
                                Toast.LENGTH_SHORT).show();

                        // 🔑 user_id 꺼내기 (LoginResponse.User 안에 있다고 가정)
                        LoginResponse.User user = body.getUser();
                        if (user == null) {
                            // user 정보가 없으면 그냥 온보딩 처음부터
                            goOnboardingFromStep(1, -1);
                            return;
                        }

                        int userId = user.getId();  // getId() 이름은 실제 모델에 맞게 수정

                        // user_id를 SharedPreferences에 저장
                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                                .edit()
                                .putInt("user_id", userId)
                                .apply();

                        // 🔥 토큰 없이, user_id로 /api/onboarding/status 호출
                        fetchOnboardingStatusAndNavigate(userId);
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
            Intent intent = new Intent(LoginActivity.this, ResetRequestActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    /**
     * user_id로 /api/onboarding/status를 호출해서
     * 온보딩 어디까지 했는지 보고 다음 화면 결정
     */
    private void fetchOnboardingStatusAndNavigate(int userId) {
        ApiService api = RetrofitClient.getApiService();

        api.getOnboardingStatus(userId).enqueue(new Callback<OnboardingStatusResponse>() {
            @Override
            public void onResponse(Call<OnboardingStatusResponse> call,
                                   Response<OnboardingStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OnboardingStatusResponse status = response.body();

                    boolean q1 = status.isQ1Completed();
                    boolean q2 = status.isQ2Completed();
                    boolean q3 = status.isQ3Completed();

                    // 🔁 어디까지 했는지 보고 분기
                    if (!q1) {
                        // Q1을 아직 안 했으면 온보딩 1단계부터
                        goOnboardingFromStep(1, userId);
                    } else if (!q2) {
                        // Q1은 했고 Q2는 안 했으면 2단계부터
                        goOnboardingFromStep(2, userId);
                    } else if (!q3) {
                        // Q1, Q2는 했고 Q3는 안 했으면 3단계부터
                        goOnboardingFromStep(3, userId);
                    } else {
                        // 세 단계 다 끝났으면 메인 화면으로
                        goMain(userId);
                    }
                } else {
                    // 상태 조회 실패하면 그냥 온보딩 처음부터
                    Toast.makeText(LoginActivity.this,
                            "온보딩 상태 조회 실패: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    goOnboardingFromStep(1, userId);
                }
            }

            @Override
            public void onFailure(Call<OnboardingStatusResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "온보딩 상태 조회 네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                // 실패 시에도 최소 기능은 되게 온보딩 1단계로
                goOnboardingFromStep(1, userId);
            }
        });
    }

    private void goOnboardingFromStep(int step, int userId) {
        Intent intent = new Intent(LoginActivity.this, OnboardingActivity.class);
        intent.putExtra(EXTRA_START_STEP, step);
        intent.putExtra(EXTRA_USER_ID, userId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void goMain(int userId) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
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
