package com.example.techasians_appmusic.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techasians_appmusic.R;
import com.example.techasians_appmusic.adapter.OnlineMusicAdapter;
import com.example.techasians_appmusic.api.MusicService;
import com.example.techasians_appmusic.api.RetrofitSingleton;
import com.example.techasians_appmusic.model.Music;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.techasians_appmusic.activity.MainActivity.onlineMusic;

/**
 * A simple {@link Fragment} subclass.
 */
public class OnlineMusicFragment extends Fragment {

    private RecyclerView rvOnlineMusic;
    private OnlineMusicAdapter adapter;
    private EditText onlSearch;
    private ArrayList<Music> listOnlSearch;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online_music, container, false);

        onlSearch = view.findViewById(R.id.onl_search);
        rvOnlineMusic = view.findViewById(R.id.rv_onl_music);
        rvOnlineMusic.setHasFixedSize(true);
        rvOnlineMusic.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        loadMusic();
        onlSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchOnlSong(editable.toString().toLowerCase());
            }
        });
        onlSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if (!isFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(onlSearch.getWindowToken(), 0);
                }
            }
        });
        return view;
    }

    private void searchOnlSong(String songName) {
        if (songName.isEmpty()) {
            showMusic();
        } else {
            listOnlSearch = new ArrayList<>();
            listOnlSearch.clear();
            for (Music music : onlineMusic) {
                if (music.getTitle().toLowerCase().startsWith(songName)) {
                    listOnlSearch.add(music);
                }
            }
            adapter = new OnlineMusicAdapter(getActivity(), listOnlSearch);
            rvOnlineMusic.setAdapter(adapter);
        }
    }

    private void loadMusic() {
        onlineMusic = new ArrayList<>();
        Retrofit retrofit = RetrofitSingleton.getOurInstance().getmRetrofit();
        MusicService musicService = retrofit.create(MusicService.class);
        musicService.getMusic()
                .enqueue(new Callback<List<Music>>() {
                    @Override
                    public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {
                        onlineMusic.addAll(response.body());
                        showMusic();
                    }

                    @Override
                    public void onFailure(Call<List<Music>> call, Throwable t) {
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showMusic() {
        adapter = new OnlineMusicAdapter(getContext(), onlineMusic);
        rvOnlineMusic.setAdapter(adapter);
    }

}
