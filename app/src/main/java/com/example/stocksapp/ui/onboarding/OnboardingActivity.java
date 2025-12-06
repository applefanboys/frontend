package com.example.stocksapp.ui.onboarding;import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksapp.R;
import com.example.stocksapp.data.model.Category;
import com.example.stocksapp.data.model.Q1AnswerRequest;
import com.example.stocksapp.data.model.Q1AnswerResponse;
import com.example.stocksapp.data.model.Q1CategoriesResponse;
import com.example.stocksapp.data.model.Q2AnswerRequest;
import com.example.stocksapp.data.model.Q2AnswerResponse;
import com.example.stocksapp.data.model.Q3AnswerRequest;
import com.example.stocksapp.data.model.Q3AnswerResponse;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.login.LoginActivity;
import com.example.stocksapp.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingActivity extends AppCompatActivity {

    private TextView tvSubtitle, tvStepIndicator, tvQuestion, tvDescription;
    private TextInputEditText etAnswer;
    private MaterialButton btnPrev, btnNext;
    private ChipGroup chipGroup;
    private ListView lvSuggestions;

    private int currentStep = 1;
    private int userId = -1; // -1로 초기화

    private ApiService apiService;

    // 저장 리스트
    private final List<Integer> selectedCategoryIds = new ArrayList<>();
    private final List<String> selectedKeywords = new ArrayList<>();
    private final List<String> excludedKeywords = new ArrayList<>();

    private List<Category> q1Categories = new ArrayList<>();
    private boolean categoriesLoaded = false;

    // 추천 단어
    private final List<String> SUGGESTIONS = Arrays.asList(
            "반도체", "AI", "2차전지", "메타버스", "부동산", "정치",
            "경제정책", "재테크", "자율주행", "친환경", "물가"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        apiService = RetrofitClient.getApiService();
        Intent intent = getIntent();

        // [수정됨] MyPageFragment와 LoginActivity에서 오는 user_id를 모두 처리
        // LoginActivity에서는 "user_id_extra", MyPageFragment에서는 "user_id" 키를 사용
        userId = intent.getIntExtra("user_id", -1); // MyPageFragment에서 오는 값
        if (userId == -1) {
            userId = intent.getIntExtra(LoginActivity.EXTRA_USER_ID, -1); // LoginActivity에서 오는 값
        }

        currentStep = intent.getIntExtra(LoginActivity.EXTRA_START_STEP, 1);

        // [수정됨] 유효한 user_id가 없으면 액티비티를 즉시 종료
        if (userId == -1) {
            Toast.makeText(this, "사용자 정보가 없어 설정을 진행할 수 없습니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        updateUI();

        // [수정됨] user_id가 확정된 후에 API를 호출
        loadQ1Categories();
    }

    private void initViews() {
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvStepIndicator = findViewById(R.id.tvStepIndicator);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvDescription = findViewById(R.id.tvDescription);
        etAnswer = findViewById(R.id.etAnswer);
        chipGroup = findViewById(R.id.chipGroupKeywords);
        lvSuggestions = findViewById(R.id.lvSuggestions);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        btnPrev.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                updateUI();
            }
        });

        btnNext.setOnClickListener(v -> handleNext());
    }

    // 다음 버튼 클릭 처리
    private void handleNext() {
        if (currentStep == 1) {
            submitQ1();
        } else if (currentStep == 2) {
            submitQ2();
        } else if (currentStep == 3) {
            submitQ3();
        }
    }


    // -----------------------------------------------------------
    // UI 갱신
    // -----------------------------------------------------------
    private void updateUI() {
        tvStepIndicator.setText(String.format("Step %d / 3", currentStep));

        chipGroup.removeAllViews();
        lvSuggestions.setVisibility(View.GONE);
        etAnswer.setVisibility(View.GONE);

        if (currentStep == 1) showStep1();
        else if (currentStep == 2) showStep2();
        else showStep3();
    }

    // -----------------------------------------------------------
    // Q1 카테고리 서버 한 번만 로딩
    // -----------------------------------------------------------
    private void loadQ1Categories() {
        if (categoriesLoaded) return;

        apiService.getQ1Categories().enqueue(new Callback<Q1CategoriesResponse>() {
            @Override
            public void onResponse(@NonNull Call<Q1CategoriesResponse> call, @NonNull Response<Q1CategoriesResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(OnboardingActivity.this, "카테고리 로드 실패", Toast.LENGTH_SHORT).show();
                    return;
                }

                q1Categories = response.body().getCategories();
                categoriesLoaded = true;

                if (currentStep == 1) {
                    renderQ1Chips();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Q1CategoriesResponse> call, @NonNull Throwable t) {
                Toast.makeText(OnboardingActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -----------------------------------------------------------
    // STEP 1: 카테고리 선택
    // -----------------------------------------------------------
    private void showStep1() {
        tvSubtitle.setText("관심 분야");
        tvQuestion.setText("어떤 분야에 관심이 있나요?");
        tvDescription.setText("하나 이상 선택하세요.");
        btnPrev.setVisibility(View.INVISIBLE); // 첫 단계에서는 이전 버튼 숨김
        btnNext.setText("다음");

        chipGroup.setVisibility(View.VISIBLE);

        if (categoriesLoaded) {
            renderQ1Chips();
        }
    }

    private void renderQ1Chips() {
        chipGroup.removeAllViews();

        for (Category c : q1Categories) {
            Chip chip = new Chip(this);
            chip.setText(c.getLabel());
            chip.setCheckable(true);

            int id = c.getId();

            if (selectedCategoryIds.contains(id)) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((button, checked) -> {
                if (checked) {
                    if (!selectedCategoryIds.contains(id)) {
                        selectedCategoryIds.add(id);
                    }
                } else {
                    selectedCategoryIds.remove(Integer.valueOf(id));
                }
            });

            chipGroup.addView(chip);
        }
    }


    private void submitQ1() {
        if (selectedCategoryIds.isEmpty()) {
            Toast.makeText(this, "관심 분야를 하나 이상 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Q1AnswerRequest req = new Q1AnswerRequest(selectedCategoryIds);

        apiService.postQ1Answer(userId, req).enqueue(new Callback<Q1AnswerResponse>() {
            @Override
            public void onResponse(@NonNull Call<Q1AnswerResponse> call, @NonNull Response<Q1AnswerResponse> response) {
                if (response.isSuccessful()) {
                    currentStep = 2;
                    updateUI();
                } else {
                    Toast.makeText(OnboardingActivity.this, "선택 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Q1AnswerResponse> call, @NonNull Throwable t) {
                Toast.makeText(OnboardingActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -----------------------------------------------------------
    // STEP 2: 포함 키워드
    // -----------------------------------------------------------
    private void showStep2() {
        tvSubtitle.setText("키워드 설정");
        tvQuestion.setText("관심 있는 키워드를 입력하세요");
        tvDescription.setText("선택한 분야와 관련된 뉴스를 추천해드려요.");
        btnPrev.setVisibility(View.VISIBLE);
        btnNext.setText("다음");

        chipGroup.setVisibility(View.VISIBLE);
        etAnswer.setVisibility(View.VISIBLE);
        lvSuggestions.setVisibility(View.VISIBLE);

        chipGroup.removeAllViews();
        for (String s : selectedKeywords) renderKeywordChip(s, selectedKeywords);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, SUGGESTIONS);
        lvSuggestions.setAdapter(adapter);

        lvSuggestions.setOnItemClickListener((parent, v, pos, id) -> addKeyword(SUGGESTIONS.get(pos)));

        etAnswer.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String text = etAnswer.getText().toString().trim();
                addKeyword(text);
                etAnswer.setText("");
                return true;
            }
            return false;
        });
    }

    private void addKeyword(String text) {
        if (text == null || text.isEmpty()) return;
        if (selectedKeywords.contains(text)) {
            Toast.makeText(this, "이미 추가된 키워드입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedKeywords.add(text);
        renderKeywordChip(text, selectedKeywords);
    }

    private void renderKeywordChip(String text, List<String> store) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            store.remove(text);
        });
        chipGroup.addView(chip);
    }

    private void submitQ2() {
        if (selectedKeywords.isEmpty()) {
            Toast.makeText(this, "관심 키워드를 하나 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Q2AnswerRequest req = new Q2AnswerRequest(selectedKeywords);

        apiService.postQ2Answer(userId, req).enqueue(new Callback<Q2AnswerResponse>() {
            @Override
            public void onResponse(@NonNull Call<Q2AnswerResponse> call, @NonNull Response<Q2AnswerResponse> response) {
                if (response.isSuccessful()) {
                    currentStep = 3;
                    updateUI();
                } else {
                    Toast.makeText(OnboardingActivity.this, "키워드 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Q2AnswerResponse> call, @NonNull Throwable t) {
                Toast.makeText(OnboardingActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -----------------------------------------------------------
    // STEP 3: 제외 키워드
    // -----------------------------------------------------------
    private void showStep3() {
        tvSubtitle.setText("제외 키워드");
        tvQuestion.setText("보고 싶지 않은 키워드가 있나요?");
        tvDescription.setText("해당 키워드가 포함된 뉴스는 추천에서 제외돼요.");
        btnPrev.setVisibility(View.VISIBLE);
        btnNext.setText("완료"); // 마지막 단계이므로 버튼 텍스트 변경

        chipGroup.setVisibility(View.VISIBLE);
        etAnswer.setVisibility(View.VISIBLE);
        lvSuggestions.setVisibility(View.VISIBLE);

        chipGroup.removeAllViews();
        for (String s : excludedKeywords) renderKeywordChip(s, excludedKeywords);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, SUGGESTIONS);
        lvSuggestions.setAdapter(adapter);

        lvSuggestions.setOnItemClickListener((parent, v, pos, id) -> addExcludeKeyword(SUGGESTIONS.get(pos)));

        etAnswer.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addExcludeKeyword(etAnswer.getText().toString().trim());
                etAnswer.setText("");
                return true;
            }
            return false;
        });
    }

    private void addExcludeKeyword(String text) {
        if (text == null || text.isEmpty()) return;
        if (excludedKeywords.contains(text)) {
            Toast.makeText(this, "이미 추가된 키워드입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        excludedKeywords.add(text);
        renderKeywordChip(text, excludedKeywords);
    }

    private void submitQ3() {
        // 제외 키워드는 선택 사항이므로 비어있어도 통과
        Q3AnswerRequest req = new Q3AnswerRequest(excludedKeywords);

        apiService.postQ3Answer(userId, req).enqueue(new Callback<Q3AnswerResponse>() {
            @Override
            public void onResponse(@NonNull Call<Q3AnswerResponse> call, @NonNull Response<Q3AnswerResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OnboardingActivity.this, "설정이 완료되었습니다!", Toast.LENGTH_SHORT).show();
                    goToMain();
                } else {
                    Toast.makeText(OnboardingActivity.this, "설정 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Q3AnswerResponse> call, @NonNull Throwable t) {
                Toast.makeText(OnboardingActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
