package com.example.techasians_appmusic.api;

import com.example.techasians_appmusic.model.Music;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MusicService {
    @GET("musics")
    Call<List<Music>> getMusic();
}
