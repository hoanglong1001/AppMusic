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

import com.bumptech.glide.Glide;
import com.example.techasians_appmusic.R;
import com.example.techasians_appmusic.activity.PlayerActivity;
import com.example.techasians_appmusic.model.Music;

import java.util.ArrayList;

public class LocalMusicAdapter extends RecyclerView.Adapter<LocalMusicAdapter.ViewHolder> {

    public static final int REQUEST_SONG = 123;
    private Context mContext;
    private ArrayList<Music> mFiles;

    public LocalMusicAdapter(Context mContext, ArrayList<Music> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Music music = mFiles.get(position);
        holder.musicName.setText(music.getTitle());
        holder.artistName.setVisibility(View.VISIBLE);
        holder.artistName.setText(music.getArtist());
        if (music.getCover() != null) {
            Glide.with(mContext).load(music.getCover())
                    .error(R.drawable.cover_art)
                    .into(holder.musicImage);
        } else {
            holder.musicImage.setImageResource(R.drawable.cover_art);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isLocalSong = true;
                Intent intent = new Intent(mContext, PlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("posLocalSong", position);
                bundle.putBoolean("isLocalSong", isLocalSong);
                intent.putExtra("local", bundle);
                ((Activity) mContext).startActivityForResult(intent, REQUEST_SONG);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView musicName;
        TextView artistName;
        ImageView musicImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            musicImage = itemView.findViewById(R.id.music_image);
            musicName = itemView.findViewById(R.id.music_name);
            artistName = itemView.findViewById(R.id.artist_name);
        }
    }
}
