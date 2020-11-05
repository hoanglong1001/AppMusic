package com.example.techasians_appmusic.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitSingleton {
    private static final RetrofitSingleton ourInstance = new RetrofitSingleton();
    private Retrofit mRetrofit;

    public static RetrofitSingleton getOurInstance() {
        return ourInstance;
    }

    private RetrofitSingleton() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.solarjsc.vn/commonservice/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public Retrofit getmRetrofit() {
        return mRetrofit;
    }
}
