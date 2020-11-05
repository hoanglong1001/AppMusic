package com.example.techasians_appmusic.fragment;


import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techasians_appmusic.R;
import com.example.techasians_appmusic.adapter.LocalMusicAdapter;
import com.example.techasians_appmusic.model.Music;

import java.util.ArrayList;

import static com.example.techasians_appmusic.activity.MainActivity.localMusic;
import static com.example.techasians_appmusic.activity.PlayerActivity.isDownload;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalMusicFragment extends Fragment {

    private RecyclerView recyclerView;
    private LocalMusicAdapter musicAdapter;
    private EditText localSearch;
    private ArrayList<Music> listLocalSearch;

    public LocalMusicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        localMusic = getAllAudio(getActivity());
        musicAdapter = new LocalMusicAdapter(getActivity(), localMusic);
        recyclerView.setAdapter(musicAdapter);
        localSearch = view.findViewById(R.id.local_search);
        localSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchLocalSong(editable.toString().toLowerCase());
            }
        });
        localSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if (!isFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(localSearch.getWindowToken(), 0);
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isDownload) {
            localMusic = getAllAudio(getActivity());
            musicAdapter = new LocalMusicAdapter(getActivity(), localMusic);
            recyclerView.setAdapter(musicAdapter);
        }
    }

    private void searchLocalSong(String songName) {
        if (songName.isEmpty()) {
            musicAdapter = new LocalMusicAdapter(getActivity(), localMusic);
            recyclerView.setAdapter(musicAdapter);
        } else {
            listLocalSearch = new ArrayList<>();
            listLocalSearch.clear();
           for (Music music : localMusic) {
               if (music.getTitle().toLowerCase().startsWith(songName)) {
                    listLocalSearch.add(music);
               }
           }
           musicAdapter = new LocalMusicAdapter(getActivity(), listLocalSearch);
           recyclerView.setAdapter(musicAdapter);
        }
    }


    public ArrayList<Music> getAllAudio(Context context) {
        ArrayList<Music> audioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID,
        };
        String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " ASC");
        if (cursor != null) {
            Log.d("LONGH", "SIZE: " + cursor.getCount());
            cursor.moveToFirst();
            int index = 0;
            while (!cursor.isAfterLast()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);
                String cover = getAlbumArt(Integer.parseInt(id), getActivity()).toString();

                Music musicFile = new Music(index, path, title, artist, album, duration, id, cover);
                Log.d("LONGH", "Album: " + album + "\nCover: " + cover + "\nPath: " + path);
                audioList.add(musicFile);
                index++;
                cursor.moveToNext();
            }
            cursor.close();
        }
        return audioList;
    }

    public Uri getAlbumArt(int songId, Context context) {
        Uri albumArtUri = null;
        try {
            String selection = MediaStore.Audio.Media._ID + " = " + songId + "";
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{
                            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID},
                    selection, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                Log.e("TEST", "albumId: " + albumId);
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
            }
            cursor.close();
        } catch (Exception e) {
        }
        return albumArtUri;
    }
}
