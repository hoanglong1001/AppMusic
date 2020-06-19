package com.example.techasians_appmusic.fragment;


import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techasians_appmusic.R;
import com.example.techasians_appmusic.adapter.MusicAdapter;
import com.example.techasians_appmusic.model.MusicFile;

import java.util.ArrayList;

import static com.example.techasians_appmusic.activity.MainActivity.musicFiles;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllSongFragment extends Fragment {

    RecyclerView recyclerView;
    MusicAdapter musicAdapter;

    public AllSongFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_song, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        musicFiles = getAllAudio(getActivity());
            musicAdapter = new MusicAdapter(getContext(), musicFiles);
            recyclerView.setAdapter(musicAdapter);
        return view;
    }


    public ArrayList<MusicFile> getAllAudio(Context context) {
        ArrayList<MusicFile> audioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID,
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            Log.d("LONGH", "SIZE: " +cursor.getCount());
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);
                String cover = getAlbumArt(Integer.parseInt(id), getActivity()).toString();


                MusicFile musicFile = new MusicFile(path, title, artist, album, duration, id, cover);
                Log.d("LONGH", "Album: " + album + "\nPath: " + path);
                audioList.add(musicFile);
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
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
            }
            cursor.close();
        } catch (Exception e) {
        }
        return albumArtUri;
    }

}
