package com.example.stocksapp.network;

import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.data.model.LoginRequest;
import com.example.stocksapp.data.model.LoginResponse;
import com.example.stocksapp.data.model.SignupRequest;
import com.example.stocksapp.data.model.SignupResponse;
import com.example.stocksapp.data.model.ForgotPasswordRequest;
import com.example.stocksapp.data.model.ResetPasswordRequest;
import com.example.stocksapp.data.model.OnboardingStatusResponse;
import com.example.stocksapp.data.model.Q1CategoriesResponse;
import com.example.stocksapp.data.model.Q1AnswerResponse;
import com.example.stocksapp.data.model.Q1AnswerRequest;
import com.example.stocksapp.data.model.Q2AnswerResponse;
import com.example.stocksapp.data.model.Q2AnswerRequest;
import com.example.stocksapp.data.model.Q3AnswerResponse;
import com.example.stocksapp.data.model.Q3AnswerRequest;
import com.example.stocksapp.network.KeywordResponse;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.Query;

import okhttp3.ResponseBody;



import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("feed")
    Call<FeedResponse> getFeed(@Body FeedRequest request);
    @POST("/api/user/signup")
    Call<SignupResponse> signup(@Body SignupRequest request);
    @POST("/api/user/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @POST("/api/user/logout")
    Call<ResponseBody> logout();
    @POST("/api/user/forgot-password")
    Call<ResponseBody> forgotPassword(@Body ForgotPasswordRequest request);
    @POST("/api/user/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest request);
    @GET("/api/onboarding/status")
    Call<OnboardingStatusResponse> getOnboardingStatus(@Query("user_id") int userId);
    // Q1 카테고리 목록 조회
    @GET("/api/onboarding/q1/categories")
    Call<Q1CategoriesResponse> getQ1Categories();

    // Q1 답 저장
    @POST("/api/onboarding/q1/answer")
    Call<Q1AnswerResponse> postQ1Answer(
            @Query("user_id") int userId,
            @Body Q1AnswerRequest body
    );

    // Q2 답 저장
    @POST("/api/onboarding/q2/answer")
    Call<Q2AnswerResponse> postQ2Answer(
            @Query("user_id") int userId,
            @Body Q2AnswerRequest body
    );

    // Q3 답 저장
    @POST("/api/onboarding/q3/answer")
    Call<Q3AnswerResponse> postQ3Answer(
            @Query("user_id") int userId,
            @Body Q3AnswerRequest body
    );

    @GET("/api/keywords/today_keywords")
    Call<KeywordResponse> getTodayKeywords(@Query("user_id") int userId);


}
