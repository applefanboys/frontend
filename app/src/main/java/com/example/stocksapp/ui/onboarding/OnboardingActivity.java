package com.example.stocksapp.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 온보딩 화면
 * - 4단계 질문으로 사용자 프로필/관심 키워드를 수집하는 "UI 껍데기"만 구현
 * - API, 서버 연동, DB 저장 등은 전부 빠진 상태
 *   나중에 백엔드/네트워크 담당이 이 클래스 안의 선택 결과를 가져다 쓰면 됨.
 */
public class OnboardingActivity extends AppCompatActivity {

    // 단계 컨테이너
    private LinearLayout layoutStep1;
    private LinearLayout layoutStep2;
    private LinearLayout layoutStep3;
    private LinearLayout layoutStep4;

    // 상단 텍스트
    private TextView tvTitle;
    private TextView tvSubtitle;

    // 하단 버튼
    private Button btnPrev;
    private Button btnNext;

    // 현재 단계 (1~4)
    private int currentStep = 1;

    // 1단계: 관심 분야(대분류) 선택 결과 (복수 선택)
    private final List<String> selectedCategories = new ArrayList<>();

    // 2단계: 투자 성향 (단일 선택)
    private String selectedProfileType = null;

    // 3단계: 자주 보는 뉴스 유형 (복수 선택)
    private final List<String> selectedNewsTypes = new ArrayList<>();

    // 4단계: 관심 산업/테마 (복수 선택, 선택 안 해도 됨)
    private final List<String> selectedThemes = new ArrayList<>();

    // 1단계 버튼들
    private Button btnCategoryAllEconomy, btnCategoryStock, btnCategoryIndustry,
            btnCategoryRealEstate, btnCategoryFinance, btnCategoryGlobal,
            btnCategoryIt, btnCategoryCommodity;

    // 2단계 버튼들
    private Button btnProfileSafe, btnProfileNeutral, btnProfileAggressive, btnProfileNonInvestor;

    // 3단계 버튼들
    private Button btnNewsStock, btnNewsIndicator, btnNewsRealEstate,
            btnNewsGlobal, btnNewsTech, btnNewsIndustryTrend;

    // 4단계 버튼들
    private Button btnThemeSemiconductor, btnThemeAi, btnThemeBattery, btnThemeAuto,
            btnThemeBio, btnThemeBigTech, btnThemeFinance, btnThemeEnergy,
            btnThemeRetail, btnThemeTravel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // 뷰 초기화
        initViews();

        // 각 단계별 버튼 클릭 리스너 세팅
        setupStep1Buttons();
        setupStep2Buttons();
        setupStep3Buttons();
        setupStep4Buttons();

        // 하단 이전/다음 버튼 동작 설정
        setupNavigationButtons();

        // 처음에는 1단계 화면을 보여줌
        showStep(1);
    }

    /**
     * 레이아웃에 있는 뷰들을 findViewById 로 연결하는 부분
     */
    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);
        layoutStep4 = findViewById(R.id.layoutStep4);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        // 1단계 버튼들
        btnCategoryAllEconomy = findViewById(R.id.btnCategoryAllEconomy);
        btnCategoryStock = findViewById(R.id.btnCategoryStock);
        btnCategoryIndustry = findViewById(R.id.btnCategoryIndustry);
        btnCategoryRealEstate = findViewById(R.id.btnCategoryRealEstate);
        btnCategoryFinance = findViewById(R.id.btnCategoryFinance);
        btnCategoryGlobal = findViewById(R.id.btnCategoryGlobal);
        btnCategoryIt = findViewById(R.id.btnCategoryIt);
        btnCategoryCommodity = findViewById(R.id.btnCategoryCommodity);

        // 2단계 버튼들
        btnProfileSafe = findViewById(R.id.btnProfileSafe);
        btnProfileNeutral = findViewById(R.id.btnProfileNeutral);
        btnProfileAggressive = findViewById(R.id.btnProfileAggressive);
        btnProfileNonInvestor = findViewById(R.id.btnProfileNonInvestor);

        // 3단계 버튼들
        btnNewsStock = findViewById(R.id.btnNewsStock);
        btnNewsIndicator = findViewById(R.id.btnNewsIndicator);
        btnNewsRealEstate = findViewById(R.id.btnNewsRealEstate);
        btnNewsGlobal = findViewById(R.id.btnNewsGlobal);
        btnNewsTech = findViewById(R.id.btnNewsTech);
        btnNewsIndustryTrend = findViewById(R.id.btnNewsIndustryTrend);

        // 4단계 버튼들
        btnThemeSemiconductor = findViewById(R.id.btnThemeSemiconductor);
        btnThemeAi = findViewById(R.id.btnThemeAi);
        btnThemeBattery = findViewById(R.id.btnThemeBattery);
        btnThemeAuto = findViewById(R.id.btnThemeAuto);
        btnThemeBio = findViewById(R.id.btnThemeBio);
        btnThemeBigTech = findViewById(R.id.btnThemeBigTech);
        btnThemeFinance = findViewById(R.id.btnThemeFinance);
        btnThemeEnergy = findViewById(R.id.btnThemeEnergy);
        btnThemeRetail = findViewById(R.id.btnThemeRetail);
        btnThemeTravel = findViewById(R.id.btnThemeTravel);
    }

    /**
     * 1단계(관심 분야) 버튼 클릭 리스너
     * - 복수 선택 가능
     */
    private void setupStep1Buttons() {
        setupMultiSelectButton(btnCategoryAllEconomy, selectedCategories, "전체 경제");
        setupMultiSelectButton(btnCategoryStock, selectedCategories, "주식·증권");
        setupMultiSelectButton(btnCategoryIndustry, selectedCategories, "산업·기업");
        setupMultiSelectButton(btnCategoryRealEstate, selectedCategories, "부동산");
        setupMultiSelectButton(btnCategoryFinance, selectedCategories, "금융");
        setupMultiSelectButton(btnCategoryGlobal, selectedCategories, "국제·글로벌");
        setupMultiSelectButton(btnCategoryIt, selectedCategories, "기술·IT");
        setupMultiSelectButton(btnCategoryCommodity, selectedCategories, "원자재");
    }

    /**
     * 2단계(투자 성향) 버튼 클릭 리스너
     * - 단일 선택
     */
    private void setupStep2Buttons() {
        View.OnClickListener profileClickListener = v -> {
            clearProfileSelection();
            Button clicked = (Button) v;
            clicked.setSelected(true);

            // 어떤 버튼이 선택됐는지에 따라 문자열 설정
            if (clicked == btnProfileSafe) {
                selectedProfileType = "안정형";
            } else if (clicked == btnProfileNeutral) {
                selectedProfileType = "중립형";
            } else if (clicked == btnProfileAggressive) {
                selectedProfileType = "공격형";
            } else if (clicked == btnProfileNonInvestor) {
                selectedProfileType = "비투자형";
            }
        };

        btnProfileSafe.setOnClickListener(profileClickListener);
        btnProfileNeutral.setOnClickListener(profileClickListener);
        btnProfileAggressive.setOnClickListener(profileClickListener);
        btnProfileNonInvestor.setOnClickListener(profileClickListener);
    }

    /**
     * 3단계(자주 보는 뉴스 유형) 버튼 클릭 리스너
     * - 복수 선택 가능
     */
    private void setupStep3Buttons() {
        setupMultiSelectButton(btnNewsStock, selectedNewsTypes, "주식 종목 뉴스");
        setupMultiSelectButton(btnNewsIndicator, selectedNewsTypes, "경제 지표/정책");
        setupMultiSelectButton(btnNewsRealEstate, selectedNewsTypes, "부동산 시장 동향");
        setupMultiSelectButton(btnNewsGlobal, selectedNewsTypes, "글로벌 이슈");
        setupMultiSelectButton(btnNewsTech, selectedNewsTypes, "기술/미래산업");
        setupMultiSelectButton(btnNewsIndustryTrend, selectedNewsTypes, "산업 트렌드");
    }

    /**
     * 4단계(관심 테마) 버튼 클릭 리스너
     * - 복수 선택 가능, 선택은 옵션
     */
    private void setupStep4Buttons() {
        setupMultiSelectButton(btnThemeSemiconductor, selectedThemes, "반도체");
        setupMultiSelectButton(btnThemeAi, selectedThemes, "AI/클라우드");
        setupMultiSelectButton(btnThemeBattery, selectedThemes, "2차전지");
        setupMultiSelectButton(btnThemeAuto, selectedThemes, "자동차/전기차");
        setupMultiSelectButton(btnThemeBio, selectedThemes, "바이오/헬스케어");
        setupMultiSelectButton(btnThemeBigTech, selectedThemes, "빅테크");
        setupMultiSelectButton(btnThemeFinance, selectedThemes, "금융업");
        setupMultiSelectButton(btnThemeEnergy, selectedThemes, "에너지");
        setupMultiSelectButton(btnThemeRetail, selectedThemes, "유통/소비재");
        setupMultiSelectButton(btnThemeTravel, selectedThemes, "여행/항공");
    }

    /**
     * 복수 선택 가능한 버튼에 공통으로 사용하는 토글 로직
     * - 버튼 selected 상태만 변경해주면
     *   bg_chip + chip_text_color(셋트로 만들어 둔 selector)가 알아서 색 바꿔줌
     */
    private void setupMultiSelectButton(Button button, List<String> targetList, String value) {
        button.setOnClickListener(v -> {
            boolean nowSelected = !button.isSelected();
            button.setSelected(nowSelected);

            if (nowSelected) {
                if (!targetList.contains(value)) {
                    targetList.add(value);
                }
            } else {
                targetList.remove(value);
            }
        });
    }

    /**
     * 2단계 투자 성향 버튼들의 선택 상태를 전부 해제하는 메서드
     */
    private void clearProfileSelection() {
        btnProfileSafe.setSelected(false);
        btnProfileNeutral.setSelected(false);
        btnProfileAggressive.setSelected(false);
        btnProfileNonInvestor.setSelected(false);
    }

    /**
     * 하단의 이전 / 다음 버튼 동작 설정
     * - 이전: 한 단계 뒤로
     * - 다음: 마지막 단계면 메인화면으로 이동
     */
    private void setupNavigationButtons() {
        btnPrev.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                showStep(currentStep);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentStep < 4) {
                // 필요하다면 각 단계별로 "선택 안 했을 때 막기" 같은 검증 로직 추가 가능
                // 예) 1단계에서 아무것도 선택 안 하면 다음으로 못 넘어가게:
                // if (currentStep == 1 && selectedCategories.isEmpty()) { ... }

                currentStep++;
                showStep(currentStep);
            } else {
                // 마지막 단계에서 "완료" 버튼 역할
                completeOnboarding();
            }
        });
    }

    /**
     * 현재 단계에 따라 레이아웃 visibility / 상단 텍스트 / 버튼 상태를 바꾸는 메서드
     */
    private void showStep(int step) {
        currentStep = step;

        // 모든 단계 레이아웃을 우선 숨김
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.GONE);
        layoutStep3.setVisibility(View.GONE);
        layoutStep4.setVisibility(View.GONE);

        // 단계에 맞는 레이아웃만 보이게 처리
        switch (step) {
            case 1:
                layoutStep1.setVisibility(View.VISIBLE);
                tvSubtitle.setText("관심 분야를 선택하세요 (1개 이상)");
                btnPrev.setVisibility(View.INVISIBLE);
                btnNext.setText("다음");
                break;

            case 2:
                layoutStep2.setVisibility(View.VISIBLE);
                tvSubtitle.setText("투자/소비 성향을 선택하세요");
                btnPrev.setVisibility(View.VISIBLE);
                btnNext.setText("다음");
                break;

            case 3:
                layoutStep3.setVisibility(View.VISIBLE);
                tvSubtitle.setText("자주 보는 경제 뉴스 유형을 선택하세요");
                btnPrev.setVisibility(View.VISIBLE);
                btnNext.setText("다음");
                break;

            case 4:
                layoutStep4.setVisibility(View.VISIBLE);
                tvSubtitle.setText("관심 있는 산업/테마를 선택하세요 (건너뛰어도 됩니다)");
                btnPrev.setVisibility(View.VISIBLE);
                btnNext.setText("완료");
                break;
        }
    }

    /**
     * 온보딩 완료 처리
     * - 지금은 단순히 메인 화면으로 이동만 함
     * - 나중에 여기서 선택 결과를 서버로 보내거나, SharedPreferences에 저장하면 됨
     */
    private void completeOnboarding() {
        // 예시: 선택 결과를 잠깐 Toast로 보여줌 (임시 디버깅용)
        // 실제 서비스에서는 지워도 되는 부분
        String debugMessage = "카테고리: " + selectedCategories
                + "\n성향: " + selectedProfileType
                + "\n뉴스유형: " + selectedNewsTypes
                + "\n테마: " + selectedThemes;
        Toast.makeText(this, debugMessage, Toast.LENGTH_SHORT).show();

        // 메인 화면으로 이동
        Intent intent = new Intent(this, MainActivity.class);

        // 나중에 API/서버에서 사용할 수 있도록
        // intent 로 선택값들을 넘기고 싶다면 아래처럼 putExtra 를 추가하면 됨.
        // (지금은 껍데기만 필요하다 해서 예시만 주석으로 남겨둠)
        //
        // intent.putStringArrayListExtra("categories", new ArrayList<>(selectedCategories));
        // intent.putExtra("profileType", selectedProfileType);
        // intent.putStringArrayListExtra("newsTypes", new ArrayList<>(selectedNewsTypes));
        // intent.putStringArrayListExtra("themes", new ArrayList<>(selectedThemes));

        startActivity(intent);
        finish();
    }
}
