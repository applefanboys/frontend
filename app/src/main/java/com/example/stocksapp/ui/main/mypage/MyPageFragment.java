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
import com.example.stocksapp.ui.login.LoginActivity;
import com.example.stocksapp.ui.login.ResetPasswordActivity;

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
            Intent intent = new Intent(getActivity(), ResetPasswordActivity.class);
            startActivity(intent);
        });

        tvLogout.setOnClickListener(v -> {
            clearLoginState();
            moveToLoginAndClear();
        });

        return view;
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
