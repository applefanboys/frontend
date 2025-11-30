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

public class OnboardingActivity extends AppCompatActivity {

    private LinearLayout layoutStep1;
    private LinearLayout layoutStep2;
    private LinearLayout layoutStep3;
    private LinearLayout layoutStep4;

    private TextView tvTitle;
    private TextView tvSubtitle;

    private Button btnPrev;
    private Button btnNext;

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

        initViews();
        setupStep1Buttons();
        setupStep2Buttons();
        setupStep3Buttons();
        setupStep4Buttons();
        setupNavigationButtons();
        showStep(1);
    }

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
     * 1단계(관심 분야)
     * - 전체 경제 선택 시 나머지 카테고리 해제 + 비활성화
     * - 다른 카테고리 선택 시 전체 경제 해제
     */
    private void setupStep1Buttons() {
        // 전체 경제
        btnCategoryAllEconomy.setOnClickListener(v -> {
            boolean nowSelected = !btnCategoryAllEconomy.isSelected();
            btnCategoryAllEconomy.setSelected(nowSelected);

            selectedCategories.clear();

            if (nowSelected) {
                selectedCategories.add("전체 경제");
                clearStep1OtherSelections();
                setStep1OtherEnabled(false);
            } else {
                setStep1OtherEnabled(true);
            }
        });

        // 나머지 카테고리들
        setupCategoryButton(btnCategoryStock, "주식·증권");
        setupCategoryButton(btnCategoryIndustry, "산업·기업");
        setupCategoryButton(btnCategoryRealEstate, "부동산");
        setupCategoryButton(btnCategoryFinance, "금융");
        setupCategoryButton(btnCategoryGlobal, "국제·글로벌");
        setupCategoryButton(btnCategoryIt, "기술·IT");
        setupCategoryButton(btnCategoryCommodity, "원자재");
    }

    private void setupCategoryButton(Button button, String value) {
        button.setOnClickListener(v -> {
            boolean nowSelected = !button.isSelected();
            button.setSelected(nowSelected);

            if (nowSelected) {
                if (!selectedCategories.contains(value)) {
                    selectedCategories.add(value);
                }
                // 전체 경제는 자동 해제 + 다시 사용 가능
                btnCategoryAllEconomy.setSelected(false);
                selectedCategories.remove("전체 경제");
                setStep1OtherEnabled(true);
            } else {
                selectedCategories.remove(value);
            }
        });
    }

    private void clearStep1OtherSelections() {
        btnCategoryStock.setSelected(false);
        btnCategoryIndustry.setSelected(false);
        btnCategoryRealEstate.setSelected(false);
        btnCategoryFinance.setSelected(false);
        btnCategoryGlobal.setSelected(false);
        btnCategoryIt.setSelected(false);
        btnCategoryCommodity.setSelected(false);

        selectedCategories.remove("주식·증권");
        selectedCategories.remove("산업·기업");
        selectedCategories.remove("부동산");
        selectedCategories.remove("금융");
        selectedCategories.remove("국제·글로벌");
        selectedCategories.remove("기술·IT");
        selectedCategories.remove("원자재");
    }

    private void setStep1OtherEnabled(boolean enabled) {
        btnCategoryStock.setEnabled(enabled);
        btnCategoryIndustry.setEnabled(enabled);
        btnCategoryRealEstate.setEnabled(enabled);
        btnCategoryFinance.setEnabled(enabled);
        btnCategoryGlobal.setEnabled(enabled);
        btnCategoryIt.setEnabled(enabled);
        btnCategoryCommodity.setEnabled(enabled);
    }

    /**
     * 2단계(투자 성향) 단일 선택
     */
    private void setupStep2Buttons() {
        View.OnClickListener profileClickListener = v -> {
            clearProfileSelection();
            Button clicked = (Button) v;
            clicked.setSelected(true);

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
     * 3단계(뉴스 유형) 복수 선택
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
     * 4단계(테마) 복수 선택 (옵션)
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

    private void clearProfileSelection() {
        btnProfileSafe.setSelected(false);
        btnProfileNeutral.setSelected(false);
        btnProfileAggressive.setSelected(false);
        btnProfileNonInvestor.setSelected(false);
    }

    /**
     * 이전 / 다음 버튼
     * - 다음 클릭 시 각 단계별 검증 추가
     */
    private void setupNavigationButtons() {
        btnPrev.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                showStep(currentStep);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (!validateCurrentStep()) {
                return;
            }

            if (currentStep < 4) {
                currentStep++;
                showStep(currentStep);
            } else {
                completeOnboarding();
            }
        });
    }

    /**
     * 단계별 선택 검증
     */
    private boolean validateCurrentStep() {
        if (currentStep == 1) {
            if (selectedCategories.isEmpty()) {
                Toast.makeText(this, "관심 분야를 하나 이상 선택해 주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (currentStep == 2) {
            if (selectedProfileType == null) {
                Toast.makeText(this, "투자/소비 성향을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (currentStep == 3) {
            if (selectedNewsTypes.isEmpty()) {
                Toast.makeText(this, "자주 보는 뉴스 유형을 하나 이상 선택해 주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        // 4단계는 선택 옵션이라 검증 없음
        return true;
    }

    private void showStep(int step) {
        currentStep = step;

        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.GONE);
        layoutStep3.setVisibility(View.GONE);
        layoutStep4.setVisibility(View.GONE);

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
     * 온보딩 완료
     * - 토스트는 제거 (중복 방지)
     * - 선택 결과를 메인 액티비티로 전달
     */
    private void completeOnboarding() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putStringArrayListExtra("categories", new ArrayList<>(selectedCategories));
        intent.putExtra("profileType", selectedProfileType);
        intent.putStringArrayListExtra("newsTypes", new ArrayList<>(selectedNewsTypes));
        intent.putStringArrayListExtra("themes", new ArrayList<>(selectedThemes));
        startActivity(intent);
        finish();
    }
}
