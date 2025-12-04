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
import androidx.fragment.app.Fragment;

import com.example.stocksapp.R;
import com.example.stocksapp.network.ApiService;
import com.example.stocksapp.network.RetrofitClient;
import com.example.stocksapp.ui.login.LoginActivity;
import com.example.stocksapp.ui.login.ResetRequestActivity;

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
}
