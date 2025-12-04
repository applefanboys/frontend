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

        // 🔥 로그아웃 버튼 클릭 시 서버 + 로컬 둘 다 정리
        tvLogout.setOnClickListener(v -> doLogout());

        return view;
    }

    private void doLogout() {
        ApiService api = RetrofitClient.getApiService();
        api.logout().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                // 성공/실패와 상관없이 로컬 상태 정리하고 로그인 화면으로
                clearLoginState();
                moveToLoginAndClear();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 네트워크가 좀 꼬여도, 일단 앱 쪽 로그아웃은 시켜줌
                clearLoginState();
                moveToLoginAndClear();
            }
        });
    }

    private void clearLoginState() {
        if (getContext() == null) return;
        SharedPreferences prefs =
                requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
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
