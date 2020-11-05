package com.example.techasians_appmusic.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techasians_appmusic.R;
import com.example.techasians_appmusic.activity.PlayerActivity;
import com.example.techasians_appmusic.model.Music;

import java.util.ArrayList;

import static com.example.techasians_appmusic.adapter.LocalMusicAdapter.REQUEST_SONG;

public class OnlineMusicAdapter extends RecyclerView.Adapter<OnlineMusicAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Music> listMusic;

    public OnlineMusicAdapter(Context context, ArrayList<Music> listMusic) {
        this.context = context;
        this.listMusic = listMusic;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Music music = listMusic.get(position);
        holder.songName.setText(music.getTitle());
        holder.songImage.setImageResource(R.drawable.cover_art);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isLocalSong = false;
                Intent intent = new Intent(context, PlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("posOnlSong", position);
                bundle.putBoolean("isLocalSong", isLocalSong);
                intent.putExtra("online", bundle);
                ((Activity) context).startActivityForResult(intent, REQUEST_SONG);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listMusic.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView songName;
        ImageView songImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.music_name);
            songImage = itemView.findViewById(R.id.music_image);
        }
    }
}
