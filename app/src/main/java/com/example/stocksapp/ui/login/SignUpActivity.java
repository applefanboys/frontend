package com.example.stocksapp.ui.login;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
