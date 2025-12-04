package com.example.stocksapp.ui.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        btnResetPassword.setOnClickListener(v -> {
            String pw = etNewPassword.getText() != null ? etNewPassword.getText().toString() : "";
            String pwConfirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

            if (pw.isEmpty() || pwConfirm.isEmpty()) {
                Toast.makeText(this, "비밀번호를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pw.equals(pwConfirm)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "비밀번호 변경 요청", Toast.LENGTH_SHORT).show();

            // 여기서 실제 비밀번호 변경 API 호출은 친구가 채워넣으면 됨
        });
    }
}
