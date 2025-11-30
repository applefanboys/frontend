package com.example.stocksapp.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnResetConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetConfirm = findViewById(R.id.btnResetConfirm);

        btnResetConfirm.setOnClickListener(v -> onResetClicked());
    }

    private void onResetClicked() {
        String email = etEmail.getText().toString().trim();
        String newPw = etNewPassword.getText().toString().trim();
        String confirmPw = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newPw) || TextUtils.isEmpty(confirmPw)) {
            Toast.makeText(this, "새 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPw.equals(confirmPw)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        saveTempPassword(email, newPw);

        Toast.makeText(this, "비밀번호가 변경되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
        moveToLoginAndClear();
    }

    private void saveTempPassword(String email, String password) {
        SharedPreferences prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("email", email)
                .putString("password", password)
                .apply();
    }

    private void moveToLoginAndClear() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
