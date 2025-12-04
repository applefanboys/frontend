package com.example.stocksapp.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.ResetPasswordRequest;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetNewPasswordActivity extends AppCompatActivity {

    private String token;
    private EditText etNewPw;
    private EditText etNewPwConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_new_password);

        etNewPw = findViewById(R.id.etNewPw);
        etNewPwConfirm = findViewById(R.id.etNewPwConfirm);

        // 1) 딥링크로 들어온 경우: shoteconomy://reset-password?token=...
        Uri data = getIntent().getData();
        if (data != null) {
            token = data.getQueryParameter("token");
        }

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "유효하지 않은 비밀번호 재설정 링크입니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        MaterialButton btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> {
            String pw = etNewPw.getText().toString().trim();
            String pw2 = etNewPwConfirm.getText().toString().trim();

            if (pw.isEmpty() || pw2.isEmpty()) {
                Toast.makeText(this, "새 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pw.equals(pw2)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pw.length() < 6) { // 예시: 최소 길이 체크
                Toast.makeText(this, "비밀번호는 6자 이상으로 설정해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 서버에 token + pw로 비번 변경 요청
            sendResetPasswordRequest(token, pw);
        });
    }

    private void sendResetPasswordRequest(String token, String newPassword) {
        ApiService api = RetrofitClient.getApiService();
        ResetPasswordRequest body = new ResetPasswordRequest(token, newPassword);

        api.resetPassword(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ResetNewPasswordActivity.this,
                            "비밀번호가 변경되었습니다. 다시 로그인해 주세요.",
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(ResetNewPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ResetNewPasswordActivity.this,
                            "비밀번호 변경 실패: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ResetNewPasswordActivity.this,
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
