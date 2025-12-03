package com.example.stocksapp.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.SignUpRequest;
import com.example.stocksapp.data.model.UserRead;
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
            attemptSignUp();
        });
    }

    private void attemptSignUp() {
        String email = etEmail.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();

        if (email.isEmpty() || nickname.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        SignUpRequest request = new SignUpRequest(email, nickname, password);
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        apiService.signUp(request).enqueue(new Callback<UserRead>() {
            @Override
            public void onResponse(Call<UserRead> call, Response<UserRead> response) {
                if (response.isSuccessful()) {
                    // 성공 시 메시지 출력
                    Toast.makeText(SignUpActivity.this, "회원가입 성공! 로그인 해주세요.", Toast.LENGTH_LONG).show();

                    // [핵심 변경] ID 저장 없이, 현재 화면을 닫아서 로그인 화면으로 복귀
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                } else {
                    Log.e("SignUp", "실패 코드: " + response.code());
                    Toast.makeText(SignUpActivity.this, "회원가입 실패 (이미 존재하는 이메일 등)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserRead> call, Throwable t) {
                Log.e("SignUp", "통신 에러: " + t.getMessage());
                Toast.makeText(SignUpActivity.this, "서버 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}