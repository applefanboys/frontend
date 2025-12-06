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
import com.example.stocksapp.data.model.FortuneDetail;
import com.example.stocksapp.data.model.FortuneResponse;
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

    // (필드 선언... 생략)
    private TextView tvMenuKeywordSetting, tvMenuResetPassword, tvMenuLogout, tvFortuneOverall;
    private MaterialButton btnLoadFortune;
    private ApiService apiService;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // (onCreateView 내용... 이전과 동일하므로 생략)
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        tvMenuKeywordSetting = view.findViewById(R.id.tvMenuKeywordSetting);
        tvMenuResetPassword = view.findViewById(R.id.tvMenuResetPassword);
        tvMenuLogout = view.findViewById(R.id.tvMenuLogout);
        tvFortuneOverall = view.findViewById(R.id.tvFortuneOverall);
        btnLoadFortune = view.findViewById(R.id.btnLoadFortune);

        apiService = RetrofitClient.getApiService();

        tvMenuResetPassword.setOnClickListener(v -> {
            if (getActivity() == null) return;
            Intent intent = new Intent(getActivity(), ResetRequestActivity.class);
            startActivity(intent);
        });

        tvMenuKeywordSetting.setOnClickListener(v -> showResetKeywordsDialog());
        tvMenuLogout.setOnClickListener(v -> doLogout());

        loadTodayFortune();

        if (btnLoadFortune != null) {
            btnLoadFortune.setOnClickListener(v -> loadTodayFortune());
        }

        return view;
    }

    // (loadTodayFortune, showResetKeywordsDialog 메서드... 생략)
    private void loadTodayFortune() {
        if (!isAdded()) return;

        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        String name = prefs.getString("user_name", "사용자");
        String birthdate = prefs.getString("user_birthdate", "1999-01-01");
        String sign = prefs.getString("user_sign", "물병자리");

        if (tvFortuneOverall != null) {
            tvFortuneOverall.setText("오늘의 운세를 불러오는 중입니다...");
        }

        apiService.getTodayFortune(name, birthdate, sign)
                .enqueue(new Callback<FortuneResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<FortuneResponse> call,
                                           @NonNull Response<FortuneResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            FortuneResponse body = response.body();
                            FortuneDetail fortune = body.getFortune();

                            if (fortune != null && tvFortuneOverall != null) {
                                tvFortuneOverall.setText(fortune.getOverall());
                            }
                        } else {
                            if (tvFortuneOverall != null) {
                                tvFortuneOverall.setText("오늘의 운세를 불러오지 못했습니다.");
                            }
                            Toast.makeText(requireContext(),
                                    "오늘의 운세 조회 실패 (" + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<FortuneResponse> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        if (tvFortuneOverall != null) {
                            tvFortuneOverall.setText("오늘의 운세를 불러오지 못했습니다.");
                        }
                        Toast.makeText(requireContext(),
                                "서버 오류: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showResetKeywordsDialog() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("선호 키워드 초기화")
                .setMessage("선호 키워드를 초기화하고\n다시 설정하시겠어요?")
                .setPositiveButton("다시 설정", (dialog, which) -> resetKeywordsAndGoOnboarding())
                .setNegativeButton("취소", null)
                .show();
    }


    // [수정됨] 키워드를 초기화하고 온보딩 화면으로 이동 (user_id 전달)
    private void resetKeywordsAndGoOnboarding() {
        if (getContext() == null) return;

        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // SharedPreferences에서 현재 user_id를 가져옵니다.
        int userId = prefs.getInt("user_id", -1); // 기본값 -1 (유효하지 않은 ID)

        // --- [핵심 수정] ---
        // 만약 이 시점에 user_id가 없다면, 비정상적인 상태로 간주하고 로그아웃 처리
        if (userId == -1) {
            Toast.makeText(getContext(), "사용자 정보가 유효하지 않아 로그아웃합니다.", Toast.LENGTH_LONG).show();
            doLogout(); // 강제 로그아웃
            return; // 더 이상 진행하지 않음
        }

        // 키워드 관련 정보만 삭제
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("has_keywords", false);
        editor.remove("include_keywords");
        editor.remove("exclude_keywords");
        editor.apply();

        Intent intent = new Intent(requireContext(), OnboardingActivity.class);

        // Intent에 user_id와 마이페이지에서 왔다는 플래그를 함께 담아 전달합니다.
        intent.putExtra("from_mypage", true);
        intent.putExtra("user_id", userId);
        startActivity(intent);

        // 현재 액티비티(MainActivity)를 종료하여 뒤로가기 시 다시 돌아오지 않도록 합니다.
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    // (doLogout, clearLoginState, moveToLoginAndClear 메서드... 생략)
    private void doLogout() {
        if (getContext() == null) {
            clearLoginState();
            moveToLoginAndClear();
            return;
        }

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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
