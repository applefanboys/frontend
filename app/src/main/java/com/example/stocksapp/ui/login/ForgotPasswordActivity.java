package com.example.stocksapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etForgotEmail;
    private Button btnSendResetEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etForgotEmail = findViewById(R.id.etForgotEmail);
        btnSendResetEmail = findViewById(R.id.btnSendResetEmail);

        btnSendResetEmail.setOnClickListener(v -> {
            String email = etForgotEmail.getText() != null ? etForgotEmail.getText().toString().trim() : "";

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "재설정 메일 발송 요청", Toast.LENGTH_SHORT).show();

            // 나중에 친구가 여기서 실제 API 호출 + 메일 링크 딥링크 처리해주면 됨
        });
    }
}
