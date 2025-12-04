package com.example.stocksapp.network;

import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.data.model.LoginRequest;
import com.example.stocksapp.data.model.LoginResponse;
import com.example.stocksapp.data.model.NewsItem;
import com.example.stocksapp.data.model.SignupRequest;
import com.example.stocksapp.data.model.SignupResponse;
import com.example.stocksapp.data.model.ForgotPasswordRequest;
import com.example.stocksapp.data.model.ResetPasswordRequest;

import java.util.List;

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

    @GET("/api/news/today")
    Call<List<NewsItem>> getTodayNews();

}
