package com.example.stocksapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.SignupRequest;
import com.example.stocksapp.data.model.SignupResponse;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etNickname;
    private TextInputEditText etPassword;
    private TextInputEditText etPasswordConfirm;
    private MaterialButton btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etEmail = findViewById(R.id.etEmailSignUp);
        etNickname = findViewById(R.id.etNickname);
        etPassword = findViewById(R.id.etPasswordSignUp);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String nickname = etNickname.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String passwordConfirm = etPasswordConfirm.getText().toString().trim();

            // 🔹 간단한 입력 검증
            if (email.isEmpty() || nickname.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일, 닉네임, 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(passwordConfirm)) {
                Toast.makeText(this, "비밀번호가 서로 다릅니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 요청 바디 만들기 (email, username, password)
            SignupRequest request = new SignupRequest(email, nickname, password);

            ApiService api = RetrofitClient.getApiService();
            api.signup(request).enqueue(new Callback<SignupResponse>() {
                @Override
                public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SignupResponse body = response.body();

                        // 서버에서 내려주는 정보 확인용 (원하면 SharedPreferences 등에 저장 가능)
                        String registeredEmail = body.getEmail();
                        String registeredUsername = body.getUsername();

                        Toast.makeText(SignUpActivity.this,
                                "회원가입 성공! 이메일: " + registeredEmail,
                                Toast.LENGTH_SHORT).show();

                        // 🔹 회원가입 성공 후 → 로그인 화면으로 복귀
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this,
                                "회원가입 실패: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SignupResponse> call, Throwable t) {
                    Toast.makeText(SignUpActivity.this,
                            "네트워크 오류: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
