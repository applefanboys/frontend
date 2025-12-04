package com.example.stocksapp.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.LinearLayout;
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
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private static final int MAX_CATEGORIES = 3;
    private static final int MAX_KEYWORDS = 10;
    private static final String PREF_ONBOARDING = "onboarding_prefs";

    private static final String[] ALL_KEYWORDS = new String[]{
            "주식", "코스피", "코스닥", "나스닥", "달러 인덱스", "환율",
            "금리", "국채", "반도체", "2차전지", "전기차", "바이오",
            "엔비디아", "테슬라", "빅테크", "원유", "금", "천연가스",
            "부동산", "주택", "물가", "인플레이션", "실업률", "GDP"
    };

    private LinearLayout layoutStep1;
    private LinearLayout layoutStep2;
    private LinearLayout layoutStep3;

    private TextView tvTitle;
    private TextView tvSubtitle;

    private ChipGroup chipGroupCategory;
    private ChipGroup chipGroupInclude;
    private ChipGroup chipGroupExclude;

    private TextInputEditText etKeywordInclude;
    private TextInputEditText etKeywordExclude;

    private TextView tvSkipExclude;

    private ListView lvIncludeSuggestions;
    private ListView lvExcludeSuggestions;

    private final List<String> selectedCategories = new ArrayList<>();
    private final List<String> includeKeywords = new ArrayList<>();
    private final List<String> excludeKeywords = new ArrayList<>();

    private final List<String> filteredInclude = new ArrayList<>();
    private final List<String> filteredExclude = new ArrayList<>();

    private ArrayAdapter<String> includeAdapter;
    private ArrayAdapter<String> excludeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        chipGroupInclude = findViewById(R.id.chipGroupInclude);
        chipGroupExclude = findViewById(R.id.chipGroupExclude);

        etKeywordInclude = findViewById(R.id.etKeywordInclude);
        etKeywordExclude = findViewById(R.id.etKeywordExclude);

        tvSkipExclude = findViewById(R.id.tvSkipExclude);

        lvIncludeSuggestions = findViewById(R.id.lvIncludeSuggestions);
        lvExcludeSuggestions = findViewById(R.id.lvExcludeSuggestions);

        MaterialButton btnPrev = findViewById(R.id.btnPrev);
        MaterialButton btnNext = findViewById(R.id.btnNext);

        includeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredInclude);
        excludeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredExclude);

        lvIncludeSuggestions.setAdapter(includeAdapter);
        lvExcludeSuggestions.setAdapter(excludeAdapter);

        setupCategoryChips();
        setupKeywordInputs();
        setupSuggestionClicks();

        btnPrev.setOnClickListener(v -> moveStep(-1));
        btnNext.setOnClickListener(v -> {
            if (currentStep == 1) {
                if (!validateStep1()) return;
                moveStep(1);
            } else if (currentStep == 2) {
                if (!validateStep2()) return;
                moveStep(1);
            } else {
                completeOnboarding();
            }
        });

        tvSkipExclude.setOnClickListener(v -> completeOnboarding());

        updateStepUi();
    }

    private int currentStep = 1;

    private void moveStep(int diff) {
        currentStep += diff;
        if (currentStep < 1) currentStep = 1;
        if (currentStep > 3) currentStep = 3;
        updateStepUi();
    }

    private void updateStepUi() {
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.GONE);
        layoutStep3.setVisibility(View.GONE);

        if (currentStep == 1) {
            layoutStep1.setVisibility(View.VISIBLE);
            tvTitle.setText("선호하는 뉴스 카테고리");
            tvSubtitle.setText("관심 있는 경제 뉴스 유형을 선택해 주세요. (최대 3개)");
        } else if (currentStep == 2) {
            layoutStep2.setVisibility(View.VISIBLE);
            tvTitle.setText("관심 키워드");
            tvSubtitle.setText("특히 관심 있는 키워드를 추가해 주세요. (최대 10개)");
        } else if (currentStep == 3) {
            layoutStep3.setVisibility(View.VISIBLE);
            tvTitle.setText("제외 키워드");
            tvSubtitle.setText("보고 싶지 않은 주제가 있다면 제외 키워드를 적어주세요.");
        }
    }

    private void setupCategoryChips() {
        String[] categories = new String[]{
                "전체 경제", "주식·증권", "산업·기업", "부동산",
                "해외 증시", "환율·금리", "원자재·유가"
        };

        for (String c : categories) {
            Chip chip = new Chip(this);
            chip.setText(c);
            chip.setCheckable(true);

            chip.setOnClickListener(v -> {
                if (chip.isChecked()) {
                    if (selectedCategories.size() >= MAX_CATEGORIES) {
                        chip.setChecked(false);
                        Toast.makeText(this,
                                "최대 " + MAX_CATEGORIES + "개까지 선택할 수 있어요.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        selectedCategories.add(c);
                    }
                } else {
                    selectedCategories.remove(c);
                }
            });

            chipGroupCategory.addView(chip);
        }
    }

    private void setupKeywordInputs() {
        etKeywordInclude.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String text = etKeywordInclude.getText() != null
                        ? etKeywordInclude.getText().toString().trim()
                        : "";
                if (!text.isEmpty()) {
                    addKeyword(includeKeywords, chipGroupInclude, text, true);
                    etKeywordInclude.setText("");
                    lvIncludeSuggestions.setVisibility(View.GONE);
                }
                return true;
            }
            return false;
        });

        etKeywordInclude.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                filterSuggestions(s.toString(), filteredInclude, includeAdapter, lvIncludeSuggestions);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etKeywordExclude.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String text = etKeywordExclude.getText() != null
                        ? etKeywordExclude.getText().toString().trim()
                        : "";
                if (!text.isEmpty()) {
                    addKeyword(excludeKeywords, chipGroupExclude, text, false);
                    etKeywordExclude.setText("");
                    lvExcludeSuggestions.setVisibility(View.GONE);
                }
                return true;
            }
            return false;
        });

        etKeywordExclude.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                filterSuggestions(s.toString(), filteredExclude, excludeAdapter, lvExcludeSuggestions);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterSuggestions(String input,
                                   List<String> targetList,
                                   ArrayAdapter<String> adapter,
                                   ListView listView) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            listView.setVisibility(View.GONE);
            return;
        }
        targetList.clear();
        for (String k : ALL_KEYWORDS) {
            if (k.contains(trimmed)) {
                targetList.add(k);
            }
        }
        if (targetList.isEmpty()) {
            listView.setVisibility(View.GONE);
        } else {
            adapter.notifyDataSetChanged();
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void setupSuggestionClicks() {
        lvIncludeSuggestions.setOnItemClickListener((parent, view, position, id) -> {
            String keyword = filteredInclude.get(position);
            addKeyword(includeKeywords, chipGroupInclude, keyword, true);
            etKeywordInclude.setText("");
            lvIncludeSuggestions.setVisibility(View.GONE);
        });

        lvExcludeSuggestions.setOnItemClickListener((parent, view, position, id) -> {
            String keyword = filteredExclude.get(position);
            addKeyword(excludeKeywords, chipGroupExclude, keyword, false);
            etKeywordExclude.setText("");
            lvExcludeSuggestions.setVisibility(View.GONE);
        });
    }

    private void addKeyword(List<String> targetList, ChipGroup group, String keyword, boolean isInclude) {
        if (targetList.contains(keyword)) {
            Toast.makeText(this, "이미 추가된 키워드입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (targetList.size() >= MAX_KEYWORDS) {
            Toast.makeText(this,
                    "키워드는 최대 " + MAX_KEYWORDS + "개까지 추가할 수 있어요.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        targetList.add(keyword);
        Chip chip = new Chip(this);
        chip.setText(keyword);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setOnCloseIconClickListener(v -> {
            group.removeView(chip);
            targetList.remove(keyword);
        });
        group.addView(chip);
    }

    private boolean validateStep1() {
        if (selectedCategories.isEmpty()) {
            Toast.makeText(this, "카테고리를 1개 이상 선택해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (includeKeywords.isEmpty()) {
            Toast.makeText(this, "관심 키워드를 1개 이상 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void completeOnboarding() {
        // 온보딩 완료 정보와 키워드를 SharedPreferences에 저장
        SharedPreferences prefs = getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE);
        prefs.edit()
                .putBoolean("completed", true)
                .putString("categories", TextUtils.join(",", selectedCategories))
                .putString("include", TextUtils.join(",", includeKeywords))
                .putString("exclude", TextUtils.join(",", excludeKeywords))
                .apply();

        // 메인 화면으로 이동 (기존 동작 유지)
        Intent intent = new Intent(this, MainActivity.class);
        intent.putStringArrayListExtra("categories", new ArrayList<>(selectedCategories));
        intent.putStringArrayListExtra("includeKeywords", new ArrayList<>(includeKeywords));
        intent.putStringArrayListExtra("excludeKeywords", new ArrayList<>(excludeKeywords));
        startActivity(intent);
        finish();
    }
}
