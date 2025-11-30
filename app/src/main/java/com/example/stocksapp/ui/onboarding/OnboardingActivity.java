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

    private int currentStep = 1;

    private String answerQ1 = "";
    private List<String> includeKeywords = new ArrayList<>();
    private List<String> excludeKeywords = new ArrayList<>();

    private final List<String> baseKeywordList = Arrays.asList(
            "삼성전자", "삼성SDI", "SK하이닉스", "현대로템", "현대차", "기아", "네이버", "카카오",
            "반도체", "2차전지", "전기차", "친환경차", "환율", "나스닥", "금리", "부동산",
            "유가", "달러 인덱스"
    );

    private ArrayAdapter<String> suggestionAdapter;
    private List<String> filteredList = new ArrayList<>();

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

    private void setupStep(int step) {
        tvStepIndicator.setText(step + " / 3");
        lvSuggestions.setVisibility(View.GONE);

        if (step == 1) {
            chipGroupKeywords.setVisibility(View.GONE);
            chipGroupKeywords.removeAllViews();
            etAnswer.setText(answerQ1);
            etAnswer.setHint("선호하는 뉴스 유형을 입력해주세요.");

            tvSubtitle.setText("선호하는 경제 뉴스 유형을 알려주세요.");
            tvQuestion.setText("어떤 경제 뉴스를 주로 보고 싶으세요?");
            tvDescription.setText("예: 전체 경제, 주식·증권, 산업·기업, 부동산 등");

            btnPrev.setEnabled(false);
            btnNext.setText("다음");
        }

        if (step == 2) {
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

    // ★ 한국어 조합 중 엔터를 무시하여 "엔터 1번만 반응" 만들기
    private boolean isComposing() {
        Editable editable = etAnswer.getText();
        if (editable == null) return false;

        Object[] composingSpans = editable.getSpans(0, editable.length(), Object.class);
        for (Object span : composingSpans) {
            if ((editable.getSpanFlags(span) & Spanned.SPAN_COMPOSING) != 0) {
                return true; // 한글 조합 중
            }
        }
        return false;
    }

    private void setupEnterListener() {
        etAnswer.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                // ★ 조합 중이면 엔터 무시 → 한국어 1번 엔터 버그 해결
                if (isComposing()) return true;

                String text = etAnswer.getText().toString().trim();
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

    private void setupAutoComplete() {
        etAnswer.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                String input = s.toString().trim();

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

    private void setupSuggestionClick() {
        lvSuggestions.setOnItemClickListener((p, v, pos, id) -> {
            String keyword = filteredList.get(pos);

            if (currentStep == 2) addKeyword(includeKeywords, keyword);
            else if (currentStep == 3) addKeyword(excludeKeywords, keyword);

            etAnswer.setText("");
            lvSuggestions.setVisibility(View.GONE);
        });
    }

    private void addKeyword(List<String> target, String keyword) {
        if (target.contains(keyword)) {
            Toast.makeText(this, "이미 추가된 키워드입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        target.add(keyword);
        addChip(keyword, target);
    }

    private void reloadChips(List<String> list) {
        chipGroupKeywords.removeAllViews();
        for (String k : list) addChip(k, list);
    }

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

    private boolean validateAndSave() {
        if (currentStep == 1) {
            answerQ1 = etAnswer.getText().toString().trim();
            if (answerQ1.isEmpty()) {
                Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
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

        return true;
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ONBOARD_Q1", answerQ1);
        intent.putExtra("ONBOARD_INCLUDE", TextUtils.join(",", includeKeywords));
        intent.putExtra("ONBOARD_EXCLUDE", TextUtils.join(",", excludeKeywords));

        startActivity(intent);
        finish();
    }
}
