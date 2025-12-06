package com.example.stocksapp.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

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

    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_START_STEP = "extra_start_step";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoSignUp = findViewById(R.id.tvGoSignUp);
        tvGoResetPassword = findViewById(R.id.tvGoResetPassword);

        // 로그인 버튼 → 서버로 로그인 요청
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
                public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse body = response.body();

                        LoginResponse.User user = body.getUser();
                        if (user == null) {
                            Toast.makeText(LoginActivity.this, "사용자 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // [핵심 수정] LoginResponse의 실제 필드에 맞게 SharedPreferences 저장
                        // body.getToken() -> body.getMessage() (토큰이 message 필드에 담겨온다고 가정)
                        // user.getName() -> user.getUsername()
                        saveUserInfo(user, body.getMessage());

                        // [핵심 수정] user.getName() -> user.getUsername()
                        Toast.makeText(LoginActivity.this,
                                user.getUsername() + "님, 환영합니다!",
                                Toast.LENGTH_SHORT).show();

                        // user_id로 온보딩 상태 확인 후 화면 이동
                        fetchOnboardingStatusAndNavigate(user.getId());
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "로그인 실패: 이메일 또는 비밀번호를 확인해주세요.",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
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
     * [핵심 수정] 실제 LoginResponse.User 구조에 맞게 메서드 수정
     */
    private void saveUserInfo(LoginResponse.User user, String token) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("user_id", user.getId());
        editor.putString("token", token); // 토큰이 message 필드에 담겨온다고 가정합니다.
        editor.putString("user_name", user.getUsername()); // name 대신 username
        editor.putString("user_email", user.getEmail());

        // birthdate, sign 필드는 현재 LoginResponse.User 클래스에 없으므로 관련 코드는 삭제합니다.
        // editor.putString("user_birthdate", user.getBirthdate());
        // editor.putString("user_sign", user.getSign());

        editor.apply();
    }

    /**
     * user_id로 /api/onboarding/status를 호출해서
     * 온보딩 어디까지 했는지 보고 다음 화면 결정
     */
    private void fetchOnboardingStatusAndNavigate(int userId) {
        ApiService api = RetrofitClient.getApiService();

        api.getOnboardingStatus(userId).enqueue(new Callback<OnboardingStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<OnboardingStatusResponse> call,
                                   @NonNull Response<OnboardingStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OnboardingStatusResponse status = response.body();

                    if (!status.isQ1Completed()) {
                        goOnboardingFromStep(1, userId);
                    } else if (!status.isQ2Completed()) {
                        goOnboardingFromStep(2, userId);
                    } else if (!status.isQ3Completed()) {
                        goOnboardingFromStep(3, userId);
                    } else {
                        goMain();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            "온보딩 상태 조회 실패: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    goOnboardingFromStep(1, userId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OnboardingStatusResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "온보딩 상태 조회 네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
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

    private void goMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
