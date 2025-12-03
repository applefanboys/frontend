package com.example.stocksapp.network;

import com.example.stocksapp.data.model.ContentRequest;
import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.data.model.LoginRequest;
import com.example.stocksapp.data.model.LoginResponse;
import com.example.stocksapp.data.model.OnboardingStatus;
import com.example.stocksapp.data.model.PreferenceRequest;
import com.example.stocksapp.data.model.ShortformTTSRequest;
import com.example.stocksapp.data.model.SignUpRequest;
import com.example.stocksapp.data.model.UserRead;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ApiService {

    @POST("api/user/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/user/signup")
    Call<UserRead> signUp(@Body SignUpRequest request);

    @POST("api/onboarding/complete")
    Call<ResponseBody> completeOnboarding(@Query("user_id") int userId, @Body PreferenceRequest request);

    @GET("api/onboarding/status")
    Call<OnboardingStatus> getOnboardingStatus(@Query("user_id") int userId);

    @GET("api/feed/home")
    Call<FeedResponse> getHomeFeed(@Query("user_id") int userId);

    @Streaming
    @POST("api/tts/shortform")
    Call<ResponseBody> getShortformTTS(@Body ShortformTTSRequest request);

    // [NEW] 실시간 본문 크롤링 요청 API
    @POST("api/feed/content")
    Call<ResponseBody> getNewsContent(@Body ContentRequest request);
}