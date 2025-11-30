package com.example.stocksapp.ui.login;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.google.android.material.button.MaterialButton;

public class ResetRequestActivity extends AppCompatActivity {

    private EditText etEmail;
    private MaterialButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_request);

        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSendEmail);

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: 백엔드에게 재설정 요청(email 넘기기) → 이메일 발송
            Toast.makeText(this, "인증 메일을 전송했습니다.", Toast.LENGTH_SHORT).show();
        });
    }
}
