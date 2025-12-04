package com.example.stocksapp.ui.main.mypage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // ★★★ 1. AlertDialog 임포트 추가
import androidx.fragment.app.Fragment;

import com.example.stocksapp.R;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.login.LoginActivity;
import com.example.stocksapp.ui.login.ResetRequestActivity;
import com.example.stocksapp.ui.onboarding.OnboardingActivity; // ★★★ 2. OnboardingActivity 임포트 추가

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        TextView tvResetPassword = view.findViewById(R.id.tvMenuResetPassword);
        TextView tvLogout = view.findViewById(R.id.tvMenuLogout);

        TextView tvMenuKeywordSetting = view.findViewById(R.id.tvMenuKeywordSetting);
        tvMenuKeywordSetting.setOnClickListener(v -> showResetKeywordsDialog());


        tvResetPassword.setOnClickListener(v -> {
            if (getActivity() == null) return;
            Intent intent = new Intent(getActivity(), ResetRequestActivity.class);
            startActivity(intent);
        });

        // 로그아웃 버튼: 서버 로그아웃 + 로컬 로그인 상태 정리
        tvLogout.setOnClickListener(v -> doLogout());

        return view;
    }

    private void doLogout() {
        ApiService api = RetrofitClient.getApiService();
        api.logout().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                clearLoginState();
                moveToLoginAndClear();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                clearLoginState();
                moveToLoginAndClear();
            }
        });
    }

    // 로그인 정보(user_prefs)만 지워서 자동로그인/이메일 상태만 초기화
    // 온보딩(onboarding_prefs)은 그대로 두기 때문에 키워드는 유지됨
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
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        // 🔥 네 앱에서 “키워드 설정 완료 여부” 체크에 실제 사용하는 key 로 바꿔야 함
        editor.putBoolean("has_keywords", false);

        // 🔥 혹시 키워드 목록을 따로 저장해두는 key 가 있다면 삭제
        editor.remove("include_keywords");
        editor.remove("exclude_keywords");

        editor.apply();

        // 온보딩 화면으로 이동 (키워드 재설정 화면)
        Intent intent = new Intent(requireContext(), OnboardingActivity.class);
        intent.putExtra("from_mypage", true);
        startActivity(intent);

        // 현재 Activity 종료 (뒤로가기 눌러도 온보딩만 남도록)
        requireActivity().finish();
    }
}
