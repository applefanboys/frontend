package com.example.stocksapp.ui.main.mypage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.stocksapp.R;
import com.example.stocksapp.ui.login.FortuneTodayResponse;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.login.LoginActivity;
import com.example.stocksapp.ui.login.ResetRequestActivity;
import com.example.stocksapp.ui.onboarding.OnboardingActivity;
import com.google.android.material.button.MaterialButton;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageFragment extends Fragment {

    // 설정 메뉴
    private TextView tvMenuKeywordSetting;
    private TextView tvMenuResetPassword;
    private TextView tvMenuLogout;

    // 오늘의 운세 표시 텍스트뷰 (설명 문구가 있던 그 TextView)
    private TextView tvFortuneOverall;

    // (운세 새로고침 버튼이 fragment_mypage.xml에 있으면 연결, 없으면 null)
    private MaterialButton btnLoadFortune;

    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        // 설정 메뉴 findViewById
        tvMenuKeywordSetting = view.findViewById(R.id.tvMenuKeywordSetting);
        tvMenuResetPassword = view.findViewById(R.id.tvMenuResetPassword);
        tvMenuLogout = view.findViewById(R.id.tvMenuLogout);

        // 오늘의 운세 TextView (레이아웃에서 id를 꼭 tvFortuneOverall로 달아야 함)
        tvFortuneOverall = view.findViewById(R.id.tvFortuneOverall);

        // 운세 새로고침 버튼이 있으면 연결 (없으면 무시)
        btnLoadFortune = view.findViewById(R.id.btnLoadFortune);

        // Retrofit ApiService 생성
        apiService = RetrofitClient.getApiService();

        // 비밀번호 재설정
        tvMenuResetPassword.setOnClickListener(v -> {
            if (getActivity() == null) return;
            Intent intent = new Intent(getActivity(), ResetRequestActivity.class);
            startActivity(intent);
        });

        // 선호 키워드 다시 설정
        tvMenuKeywordSetting.setOnClickListener(v -> showResetKeywordsDialog());

        // 로그아웃
        tvMenuLogout.setOnClickListener(v -> doLogout());

        // 마이페이지 들어오면 오늘의 운세 자동으로 한 번 가져오기
        loadTodayFortune();

        // 버튼이 있으면 버튼으로도 새로고침 가능
        if (btnLoadFortune != null) {
            btnLoadFortune.setOnClickListener(v -> loadTodayFortune());
        }

        return view;
    }

    // =========================
    //  오늘의 운세 API 호출
    // =========================
    private void loadTodayFortune() {
        if (!isAdded()) return;

        // 유저 정보는 SharedPreferences에 저장되어 있다고 가정
        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        String name = prefs.getString("user_name", "사용자");
        String birthdate = prefs.getString("user_birthdate", "1999-01-01"); // yyyy-MM-dd
        String sign = prefs.getString("user_sign", "물병자리");
        String interests = prefs.getString("user_interests", "주식, 경제");

        apiService.getTodayFortune(name, birthdate, sign, interests)
                .enqueue(new Callback<FortuneTodayResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<FortuneTodayResponse> call,
                                           @NonNull Response<FortuneTodayResponse> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            FortuneTodayResponse.Fortune fortune = response.body().getFortune();
                            if (fortune != null && tvFortuneOverall != null) {
                                // 카드 안의 문구를 서버에서 받은 전체 운세로 교체
                                tvFortuneOverall.setText(fortune.getOverall());
                            }
                        } else {
                            Toast.makeText(requireContext(),
                                    "오늘의 운세를 불러오지 못했어요.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FortuneTodayResponse> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;

                        Toast.makeText(requireContext(),
                                "서버 오류: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // =========================
    //  선호 키워드 재설정
    // =========================
    private void showResetKeywordsDialog() {
        if (!isAdded()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("선호 키워드 초기화")
                .setMessage("선호 키워드를 초기화하고\n다시 설정하시겠어요?")
                .setPositiveButton("다시 설정", (dialog, which) -> resetKeywordsAndGoOnboarding())
                .setNegativeButton("취소", null)
                .show();
    }

    private void resetKeywordsAndGoOnboarding() {
        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("has_keywords", false);
        editor.remove("include_keywords");
        editor.remove("exclude_keywords");
        editor.apply();

        Intent intent = new Intent(requireContext(), OnboardingActivity.class);
        intent.putExtra("from_mypage", true);
        startActivity(intent);

        requireActivity().finish();
    }

    // =========================
    //  로그아웃 처리
    // =========================
    private void doLogout() {
        // 서버 로그아웃 API가 있으면 여기서 호출
        ApiService api = RetrofitClient.getApiService();
        api.logout().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                clearLoginState();
                moveToLoginAndClear();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,
                                  @NonNull Throwable t) {
                clearLoginState();
                moveToLoginAndClear();
            }
        });
    }

    private void clearLoginState() {
        if (getContext() == null) return;

        SharedPreferences prefs =
                requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private void moveToLoginAndClear() {
        if (getActivity() == null) return;

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
