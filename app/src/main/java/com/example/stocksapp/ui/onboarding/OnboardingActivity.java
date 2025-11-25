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

/**
 * 온보딩 화면
 *
 * 이 화면의 흐름:
 * 1) 처음에는 "시작하기" 버튼만 보임
 * 2) 시작하기 누르면 키워드/종목 선택 레이아웃이 나타남
 * 3) 사용자가 키워드 1개 + 종목 1개 선택해야 다음 화면(MainActivity)로 이동
 *
 * 선택 UI는 칩 버튼(bg_chip) + selected 상태로 디자인 변경되도록 구성됨
 */
public class OnboardingActivity extends AppCompatActivity {

    // "시작하기" 버튼
    private Button btnStart;

    // 키워드/종목 선택 UI가 들어있는 전체 영역 (처음에는 숨겨짐)
    private LinearLayout layoutSelect;

    // 키워드 버튼들
    private Button btnKeywordIt, btnKeywordEco, btnKeywordFinance, btnKeywordGlobal, btnKeywordEtc;

    // 종목 버튼들
    private Button btnStockSamsung, btnStockHyundai, btnStockKakao, btnStockNaver;

    // 선택 완료 버튼
    private Button btnDone;

    // 사용자가 선택한 값 저장
    private String selectedKeyword = null;
    private String selectedStock = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // 버튼/레이아웃 id 연결
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

        /**
         * 1. "시작하기" 버튼 클릭 시
         *    - 숨겨져 있던 키워드/종목 선택 영역 layoutSelect를 보여줌
         */
        btnStart.setOnClickListener(v -> {
            layoutSelect.setVisibility(View.VISIBLE);
        });

        /**
         * 2. 키워드/종목 버튼 클릭 시 상태 변경해주는 함수 호출
         *    (토스 느낌 칩 버튼: selected 값을 true/false로 바꾸면 배경 자동 변경됨)
         */
        setupKeywordButton(btnKeywordIt, "IT");
        setupKeywordButton(btnKeywordEco, "경제");
        setupKeywordButton(btnKeywordFinance, "금융");
        setupKeywordButton(btnKeywordGlobal, "글로벌");
        setupKeywordButton(btnKeywordEtc, "기타");

        setupStockButton(btnStockSamsung, "삼성전자");
        setupStockButton(btnStockHyundai, "현대차");
        setupStockButton(btnStockKakao, "카카오");
        setupStockButton(btnStockNaver, "네이버");

        /**
         * 3. 선택 완료 버튼 클릭
         *    - 키워드와 종목을 각각 하나씩 선택했는지 검사
         *    - 선택 안 했으면 Toast 메시지 출력
         *    - 정상 선택 시 MainActivity로 이동 + 선택값 전달
         */
        btnDone.setOnClickListener(v -> {

            // 키워드 선택 안 된 경우
            if (selectedKeyword == null) {
                Toast.makeText(this, "키워드를 선택하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // 종목 선택 안 된 경우
            if (selectedStock == null) {
                Toast.makeText(this, "종목을 선택하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // 선택된 내용 MainActivity로 전달하며 이동
            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            intent.putExtra("keyword", selectedKeyword);
            intent.putExtra("stock", selectedStock);
            startActivity(intent);

            // 온보딩 화면 종료 (뒤로가기 시 돌아오지 않도록)
            finish();
        });
    }

    /**
     * 키워드 버튼 클릭 설정
     * - selectedKeyword 값을 바꿔줌
     * - 모든 키워드 버튼 중 오직 하나만 선택되도록 제어
     */
    private void setupKeywordButton(Button button, String keywordValue) {

        button.setOnClickListener(v -> {

            // 모든 버튼 선택 해제
            clearKeywordSelection();

            // 현재 버튼 선택 설정
            button.setSelected(true);
            selectedKeyword = keywordValue;
        });
    }

    /**
     * 종목 버튼 클릭 설정
     * - selectedStock 값을 바꿔줌
     * - 종목 버튼도 하나만 선택되도록 제어
     */
    private void setupStockButton(Button button, String stockValue) {

        button.setOnClickListener(v -> {

            // 모든 종목 버튼 선택 해제
            clearStockSelection();

            // 현재 버튼 선택 설정
            button.setSelected(true);
            selectedStock = stockValue;
        });
    }

    /**
     * 모든 키워드 버튼 선택 해제 (selected=false)
     */
    private void clearKeywordSelection() {
        btnKeywordIt.setSelected(false);
        btnKeywordEco.setSelected(false);
        btnKeywordFinance.setSelected(false);
        btnKeywordGlobal.setSelected(false);
        btnKeywordEtc.setSelected(false);
    }

    /**
     * 모든 종목 버튼 선택 해제
     */
    private void clearStockSelection() {
        btnStockSamsung.setSelected(false);
        btnStockHyundai.setSelected(false);
        btnStockKakao.setSelected(false);
        btnStockNaver.setSelected(false);
    }
}
