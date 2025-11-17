package com.example.stocksapp.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.ui.main.MainActivity;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
