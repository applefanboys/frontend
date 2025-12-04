package com.example.stocksapp.ui.login;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.ForgotPasswordRequest;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetRequestActivity extends AppCompatActivity {

    private EditText etEmail;
    private MaterialButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_request);

        etEmail = findViewById(R.id.etEmail);      // XML id에 맞춰 조정해줘
        btnSend = findViewById(R.id.btnSendEmail);      // XML id 맞춰 조정

        btnSend.setOnClickListener(v -> requestResetMail());
    }

    private void requestResetMail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "가입한 이메일(아이디)을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        ForgotPasswordRequest body = new ForgotPasswordRequest(email);
        ApiService api = RetrofitClient.getApiService();

        api.forgotPassword(body).enqueue(new Callback<ResponseBody>() {
            @Override

            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    // 🔥 화면 전환 없음 — 그냥 토스트만 띄움
                    Toast.makeText(ResetRequestActivity.this,
                            "비밀번호 재설정 이메일을 전송했습니다.\n메일함을 확인해 주세요.",
                            Toast.LENGTH_LONG).show();

                    // 원하면 입력창 비우기
                    // etEmail.setText("");

                } else {
                    Toast.makeText(ResetRequestActivity.this,
                            "요청 실패: " + response.code(),

                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ResetRequestActivity.this,
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
