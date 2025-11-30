package com.example.stocksapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.google.android.material.button.MaterialButton;

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

        token = getIntent().getStringExtra("token");

        MaterialButton btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> {
            String pw = etNewPw.getText().toString().trim();
            String pw2 = etNewPwConfirm.getText().toString().trim();

            if (!pw.equals(pw2)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: 서버에 token + pw로 비번 변경 요청
            Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
