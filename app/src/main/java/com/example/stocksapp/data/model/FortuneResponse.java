package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;

public class FortuneResponse {

    @SerializedName("name")
    private String name;

    @SerializedName("birthdate")
    private String birthdate;

    @SerializedName("sign")
    private String sign;

    @SerializedName("fortune")
    private FortuneDetail fortune;

    public String getName() {
        return name;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getSign() {
        return sign;
    }

    public FortuneDetail getFortune() {
        return fortune;
    }
}
