package com.example.stocksapp.ui.main.mypage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.stocksapp.ui.main.MainActivity;
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
    private TextView tvMenuNewsRoutine;

    // 오늘의 운세 텍스트뷰
    private TextView tvFortuneOverall;

    // 운세 새로고침 버튼(있으면)
    private MaterialButton btnLoadFortune;

    // 닉네임 / 이메일 / 프로필 아이콘
    private TextView tvNickname;
    private TextView tvEmail;
    private ImageView ivProfile;

    private ApiService apiService;

    // 프로필 사진 선택용 런처
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 갤러리에서 이미지 선택 결과 콜백 등록
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null && isAdded()) {
                            // ImageView 에 표시
                            ivProfile.setImageURI(uri);

                            // SharedPreferences 에 URI 저장해서 다음에도 유지
                            SharedPreferences prefs =
                                    requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                            prefs.edit()
                                    .putString("user_profile_uri", uri.toString())
                                    .apply();
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        tvMenuKeywordSetting = view.findViewById(R.id.tvMenuKeywordSetting);
        tvMenuResetPassword = view.findViewById(R.id.tvMenuResetPassword);
        tvMenuLogout = view.findViewById(R.id.tvMenuLogout);
        tvFortuneOverall = view.findViewById(R.id.tvFortuneOverall);
        btnLoadFortune = view.findViewById(R.id.btnLoadFortune);
        tvMenuNewsRoutine = view.findViewById(R.id.tvMenuNewsRoutine);

        tvNickname = view.findViewById(R.id.tvNickname);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfile = view.findViewById(R.id.ivProfile);

        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        String nickname = prefs.getString("user_name", "사용자");
        String email = prefs.getString("user_email", "example@email.com");
        String profileUri = prefs.getString("user_profile_uri", null);

        tvNickname.setText(nickname);
        tvEmail.setText(email);

        // 저장된 프로필 사진 있으면 불러오기
        if (profileUri != null) {
            try {
                ivProfile.setImageURI(Uri.parse(profileUri));
            } catch (Exception e) {
                Log.e("MyPage", "failed to load profile image", e);
            }
        }

        // 프로필 아이콘 클릭 시 갤러리 열기
        ivProfile.setOnClickListener(v -> openImagePicker());

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

        // 뉴스 알림 시간 설정 메뉴 클릭 리스너
        tvMenuNewsRoutine.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openNewsTimeDialogFromMyPage();
            }
        });

        // 진입 시 오늘의 운세 자동 호출
        loadTodayFortune();

        // 버튼이 있으면 버튼으로도 새로고침
        if (btnLoadFortune != null) {
            btnLoadFortune.setOnClickListener(v -> loadTodayFortune());
        }

        return view;
    }

    // 프로필 사진 선택 인텐트
    private void openImagePicker() {
        if (!isAdded()) return;

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // =========================
    //  오늘의 운세 API 호출
    // =========================
    private void loadTodayFortune() {
        if (!isAdded()) return;

        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        String name = prefs.getString("user_name", null);
        String birthdate = prefs.getString("user_birthdate", null);
        String sign = prefs.getString("user_sign", null);

        if (tvFortuneOverall != null) {
            tvFortuneOverall.setText("오늘의 운세를 불러오는 중입니다...");
        }

        Log.d("FortuneDebug", "name=" + name
                + ", birthdate=" + birthdate + ", sign=" + sign);

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
                            String errorMsg = "unknown";
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (Exception ignored) { }

                            Log.e("FortuneDebug",
                                    "HTTP " + response.code() + " errorBody=" + errorMsg);

                            if (tvFortuneOverall != null) {
                                tvFortuneOverall.setText("오늘의 운세를 불러오지 못했습니다.");
                            }
                            Toast.makeText(requireContext(),
                                    "오늘의 운세 조회 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FortuneResponse> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;

                        Log.e("FortuneDebug", "network failure", t);

                        if (tvFortuneOverall != null) {
                            tvFortuneOverall.setText("오늘의 운세를 불러오지 못했습니다.");
                        }
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
                .setPositiveButton("다시 설정",
                        (dialog, which) -> resetKeywordsAndGoOnboarding())
                .setNegativeButton("취소", null)
                .show();
    }

    // 키워드를 초기화하고 온보딩 화면으로 이동 (user_id 유지)
    private void resetKeywordsAndGoOnboarding() {
        if (getContext() == null) return;

        SharedPreferences prefs =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(getContext(),
                    "사용자 정보가 유효하지 않아 로그아웃합니다.",
                    Toast.LENGTH_LONG).show();
            doLogout();
            return;
        }

        // 키워드 관련 정보만 삭제
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("has_keywords", false);
        editor.remove("include_keywords");
        editor.remove("exclude_keywords");
        editor.apply();

        Intent intent = new Intent(requireContext(), OnboardingActivity.class);
        intent.putExtra("from_mypage", true);
        intent.putExtra("user_id", userId);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    // =========================
    //  로그아웃 처리
    // =========================
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
