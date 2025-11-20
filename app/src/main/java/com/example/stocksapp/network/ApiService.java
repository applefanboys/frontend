package com.example.stocksapp.network;

import com.example.stocksapp.data.model.FeedResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("feed")
    Call<FeedResponse> getFeed(@Body FeedRequest request);
}
