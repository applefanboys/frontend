package com.example.stocksapp.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.ui.main.MainActivity;

public class OnboardingActivity extends AppCompatActivity {

    private Button btnStart;
    private LinearLayout layoutSelect;

    private Button btnKeywordIt;
    private Button btnKeywordEco;
    private Button btnKeywordFinance;
    private Button btnKeywordGlobal;
    private Button btnKeywordEtc;

    private Button btnStockSamsung;
    private Button btnStockHyundai;
    private Button btnStockKakao;
    private Button btnStockNaver;

    private Button btnDone;

    private String selectedKeyword = null;
    private String selectedStock = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        btnStart = findViewById(R.id.btnStart);
        layoutSelect = findViewById(R.id.layoutSelect);

        btnKeywordIt = findViewById(R.id.btnKeywordIt);
        btnKeywordEco = findViewById(R.id.btnKeywordEco);
        btnKeywordFinance = findViewById(R.id.btnKeywordFinance);
        btnKeywordGlobal = findViewById(R.id.btnKeywordGlobal);
        btnKeywordEtc = findViewById(R.id.btnKeywordEtc);

        btnStockSamsung = findViewById(R.id.btnStockSamsung);
        btnStockHyundai = findViewById(R.id.btnStockHyundai);
        btnStockKakao = findViewById(R.id.btnStockKakao);
        btnStockNaver = findViewById(R.id.btnStockNaver);

        btnDone = findViewById(R.id.btnDone);

        btnStart.setOnClickListener(v -> {
            btnStart.setVisibility(View.GONE);
            layoutSelect.setVisibility(View.VISIBLE);
        });

        btnKeywordIt.setOnClickListener(v -> {
            selectedKeyword = "IT";
            Toast.makeText(this, "키워드: IT 선택", Toast.LENGTH_SHORT).show();
        });

        btnKeywordEco.setOnClickListener(v -> {
            selectedKeyword = "경제";
            Toast.makeText(this, "키워드: 경제 선택", Toast.LENGTH_SHORT).show();
        });

        btnKeywordFinance.setOnClickListener(v -> {
            selectedKeyword = "금융";
            Toast.makeText(this, "키워드: 금융 선택", Toast.LENGTH_SHORT).show();
        });

        btnKeywordGlobal.setOnClickListener(v -> {
            selectedKeyword = "글로벌";
            Toast.makeText(this, "키워드: 글로벌 선택", Toast.LENGTH_SHORT).show();
        });

        btnKeywordEtc.setOnClickListener(v -> {
            selectedKeyword = "기타";
            Toast.makeText(this, "키워드: 기타 선택", Toast.LENGTH_SHORT).show();
        });

        btnStockSamsung.setOnClickListener(v -> {
            selectedStock = "삼성전자";
            Toast.makeText(this, "종목: 삼성전자 선택", Toast.LENGTH_SHORT).show();
        });

        btnStockHyundai.setOnClickListener(v -> {
            selectedStock = "현대차";
            Toast.makeText(this, "종목: 현대차 선택", Toast.LENGTH_SHORT).show();
        });

        btnStockKakao.setOnClickListener(v -> {
            selectedStock = "카카오";
            Toast.makeText(this, "종목: 카카오 선택", Toast.LENGTH_SHORT).show();
        });

        btnStockNaver.setOnClickListener(v -> {
            selectedStock = "네이버";
            Toast.makeText(this, "종목: 네이버 선택", Toast.LENGTH_SHORT).show();
        });

        btnDone.setOnClickListener(v -> {
            if (selectedKeyword == null) {
                Toast.makeText(this, "키워드를 선택하세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedStock == null) {
                Toast.makeText(this, "종목을 선택하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            intent.putExtra("keyword", selectedKeyword);
            intent.putExtra("stock", selectedStock);
            startActivity(intent);
            finish();
        });
    }
}
