package com.example.stocksapp.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private TextView tvSubtitle;
    private TextView tvStepIndicator;
    private TextView tvQuestion;
    private TextView tvDescription;
    private TextInputEditText etAnswer;
    private MaterialButton btnPrev;
    private MaterialButton btnNext;
    private ChipGroup chipGroupKeywords;
    private ListView lvSuggestions;

    // 현재 단계 (1~3)
    private int currentStep = 1;

    // 1단계: 선택된 카테고리들 (텍스트 입력 대신 Chip 선택)
    private final List<String> selectedCategories = new ArrayList<>();

    // 2,3단계: 포함/제외 키워드
    private final List<String> includeKeywords = new ArrayList<>();
    private final List<String> excludeKeywords = new ArrayList<>();

    // 자동완성 기본 키워드 리스트
    private final List<String> baseKeywordList = Arrays.asList(
            "삼성전자", "삼성SDI", "SK하이닉스", "현대로템", "현대차", "기아", "네이버", "카카오",
            "반도체", "2차전지", "전기차", "친환경차", "환율", "나스닥", "금리", "부동산",
            "유가", "달러 인덱스"
    );

    private ArrayAdapter<String> suggestionAdapter;
    private final List<String> filteredList = new ArrayList<>();

    // 1단계 카테고리 최대 선택 개수
    private static final int MAX_CATEGORY_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvStepIndicator = findViewById(R.id.tvStepIndicator);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvDescription = findViewById(R.id.tvDescription);
        etAnswer = findViewById(R.id.etAnswer);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        chipGroupKeywords = findViewById(R.id.chipGroupKeywords);
        lvSuggestions = findViewById(R.id.lvSuggestions);

        suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredList);
        lvSuggestions.setAdapter(suggestionAdapter);

        setupStep(currentStep);
        setupEnterListener();
        setupAutoComplete();
        setupSuggestionClick();

        btnPrev.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                setupStep(currentStep);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (!validateAndSave()) return;

            if (currentStep < 3) {
                currentStep++;
                setupStep(currentStep);
            } else {
                goToMain();
            }
        });
    }

    /**
     * 단계에 따라 화면 구성 변경
     */
    private void setupStep(int step) {
        tvStepIndicator.setText(step + " / 3");
        lvSuggestions.setVisibility(View.GONE);

        if (step == 1) {
            // 1단계: 텍스트 입력 대신 카테고리 Chip 선택
            etAnswer.setVisibility(View.GONE);
            chipGroupKeywords.setVisibility(View.VISIBLE);
            setupCategoryChipsForStep1();

            tvSubtitle.setText("선호하는 경제 뉴스 유형을 알려주세요.");
            tvQuestion.setText("어떤 경제 뉴스 카테고리를 주로 보고 싶으세요?");
            tvDescription.setText("예: 전체 경제, 주식·증권, 산업·기업, 부동산 등 (최대 3개 선택)");

            btnPrev.setEnabled(false);
            btnNext.setText("다음");
        }

        if (step == 2) {
            // 2단계: 포함 키워드 입력 + Chip
            etAnswer.setVisibility(View.VISIBLE);
            chipGroupKeywords.setVisibility(View.VISIBLE);

            reloadChips(includeKeywords);
            etAnswer.setText("");
            etAnswer.setHint("키워드를 입력하면 자동완성이 나타나요");

            tvSubtitle.setText("특히 관심 있는 키워드를 알려주세요.");
            tvQuestion.setText("특히 관심 있는 키워드가 있나요?");
            tvDescription.setText("자동완성 추천을 눌러도 되고, 직접 입력 후 엔터로 추가할 수도 있어요.");

            btnPrev.setEnabled(true);
            btnNext.setText("다음");
        }

        if (step == 3) {
            // 3단계: 제외 키워드 입력 + Chip
            etAnswer.setVisibility(View.VISIBLE);
            chipGroupKeywords.setVisibility(View.VISIBLE);

            reloadChips(excludeKeywords);
            etAnswer.setText("");
            etAnswer.setHint("제외할 키워드를 입력해주세요");

            tvSubtitle.setText("보고 싶지 않은 키워드가 있나요?");
            tvQuestion.setText("제외하고 싶은 키워드를 입력해주세요.");
            tvDescription.setText("엔터 또는 자동완성으로 추가할 수 있어요.");

            btnPrev.setEnabled(true);
            btnNext.setText("완료");
        }
    }

    /**
     * 1단계 카테고리 Chip 생성 (토글형, 최대 3개)
     */
    private void setupCategoryChipsForStep1() {
        chipGroupKeywords.removeAllViews();

        List<String> categories = Arrays.asList(
                "전체 경제",
                "주식·증권",
                "산업·기업",
                "부동산",
                "해외 증시",
                "환율·금리",
                "원자재·유가"
        );

        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);

            // 이미 선택된 것들은 체크 상태 유지
            chip.setChecked(selectedCategories.contains(category));

            chip.setOnClickListener(v -> {
                if (chip.isChecked()) {
                    // 새로 선택
                    if (selectedCategories.size() >= MAX_CATEGORY_COUNT) {
                        chip.setChecked(false);
                        Toast.makeText(
                                this,
                                "카테고리는 최대 " + MAX_CATEGORY_COUNT + "개까지 선택할 수 있어요.",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        selectedCategories.add(category);
                    }
                } else {
                    // 선택 해제
                    selectedCategories.remove(category);
                }
            });

            chipGroupKeywords.addView(chip);
        }
    }

    /**
     * 한글 조합 중인지 체크 (엔터 두 번 문제 방지)
     */
    private boolean isComposing() {
        Editable editable = etAnswer.getText();
        if (editable == null) return false;

        Object[] composingSpans = editable.getSpans(0, editable.length(), Object.class);
        for (Object span : composingSpans) {
            if ((editable.getSpanFlags(span) & Spanned.SPAN_COMPOSING) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 엔터 입력 처리 (2,3단계에서만 의미 있음)
     */
    private void setupEnterListener() {
        etAnswer.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 한글 조합 중에는 엔터 무시
                if (isComposing()) return true;

                String text = etAnswer.getText() == null
                        ? ""
                        : etAnswer.getText().toString().trim();
                if (text.isEmpty()) return true;

                if (currentStep == 2) addKeyword(includeKeywords, text);
                else if (currentStep == 3) addKeyword(excludeKeywords, text);

                etAnswer.setText("");
                lvSuggestions.setVisibility(View.GONE);
                return true;
            }
            return false;
        });
    }

    /**
     * 자동완성 필터링
     */
    private void setupAutoComplete() {
        etAnswer.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                String input = s.toString().trim();

                // 1단계는 텍스트 입력 안 쓰므로 자동완성 숨김
                if (currentStep == 1 || input.isEmpty()) {
                    lvSuggestions.setVisibility(View.GONE);
                    return;
                }

                filteredList.clear();
                for (String k : baseKeywordList) {
                    if (k.contains(input)) filteredList.add(k);
                }

                if (filteredList.isEmpty()) {
                    lvSuggestions.setVisibility(View.GONE);
                } else {
                    suggestionAdapter.notifyDataSetChanged();
                    lvSuggestions.setVisibility(View.VISIBLE);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * 자동완성 리스트 클릭 시 키워드 추가
     */
    private void setupSuggestionClick() {
        lvSuggestions.setOnItemClickListener((p, v, pos, id) -> {
            String keyword = filteredList.get(pos);

            if (currentStep == 2) addKeyword(includeKeywords, keyword);
            else if (currentStep == 3) addKeyword(excludeKeywords, keyword);

            etAnswer.setText("");
            lvSuggestions.setVisibility(View.GONE);
        });
    }

    /**
     * 키워드 리스트에 추가 + Chip 생성
     */
    private void addKeyword(List<String> target, String keyword) {
        if (target.contains(keyword)) {
            Toast.makeText(this, "이미 추가된 키워드입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        target.add(keyword);
        addChip(keyword, target);
    }

    /**
     * 현재 리스트 기반으로 ChipGroup 다시 그림
     */
    private void reloadChips(List<String> list) {
        chipGroupKeywords.removeAllViews();
        for (String k : list) addChip(k, list);
    }

    /**
     * 하나의 Chip 생성 (포함/제외 키워드용, 삭제 아이콘 포함)
     */
    private void addChip(String text, List<String> targetList) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);

        chip.setOnCloseIconClickListener(v -> {
            chipGroupKeywords.removeView(chip);
            targetList.remove(text);
        });

        chipGroupKeywords.addView(chip);
    }

    /**
     * 단계별 유효성 검사
     */
    private boolean validateAndSave() {
        if (currentStep == 1) {
            if (selectedCategories.isEmpty()) {
                Toast.makeText(this, "카테고리를 1개 이상 선택해주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

        if (currentStep == 2) {
            if (includeKeywords.isEmpty()) {
                Toast.makeText(this, "키워드를 1개 이상 추가해주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

        // 3단계는 선택 사항 (제외 키워드 없으면 그냥 넘어감)
        return true;
    }

    /**
     * 온보딩 완료 후 메인으로 이동
     */
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);

        // 1단계: 선택 카테고리 문자열로 묶어서 전달
        intent.putExtra("ONBOARD_Q1", TextUtils.join(",", selectedCategories));
        intent.putExtra("ONBOARD_INCLUDE", TextUtils.join(",", includeKeywords));
        intent.putExtra("ONBOARD_EXCLUDE", TextUtils.join(",", excludeKeywords));

        startActivity(intent);
        finish();
    }
}
