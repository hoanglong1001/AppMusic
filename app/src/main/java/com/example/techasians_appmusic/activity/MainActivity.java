package com.example.techasians_appmusic.activity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.techasians_appmusic.R;
import com.example.techasians_appmusic.adapter.ViewPagerAdapter;
import com.example.techasians_appmusic.fragment.LocalMusicFragment;
import com.example.techasians_appmusic.fragment.OnlineMusicFragment;
import com.example.techasians_appmusic.model.Music;
import com.example.techasians_appmusic.service.CreateNotification;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.techasians_appmusic.activity.PlayerActivity.mediaPlayer;
import static com.example.techasians_appmusic.activity.PlayerActivity.notificationManager;
import static com.example.techasians_appmusic.activity.PlayerActivity.uri;
import static com.example.techasians_appmusic.adapter.LocalMusicAdapter.REQUEST_SONG;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    public static final int REQUEST_CODE = 1;
    private CardView cardRl;
    private RelativeLayout rlViewPlayer;
    private ImageView imgCoverArt;
    private TextView txtSongName;
    private ImageView imgPrev;
    private ImageView imgPlayPause;
    private ImageView imgNext;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    public static ArrayList<Music> localMusic;
    public static ArrayList<Music> onlineMusic;
    public static boolean shuffleBoolean = false;
    public static boolean repeatBoolean = false;
    public static boolean isLocal;
    private int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        } else {
            initView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.cancelAll();
//        }
    }

    private void initView() {
        cardRl = findViewById(R.id.card_rl);
        rlViewPlayer = findViewById(R.id.rl_view_player);
        imgCoverArt = findViewById(R.id.img_cover_art);
        txtSongName = findViewById(R.id.txt_song_name);
        imgPrev = findViewById(R.id.img_prev);
        imgPlayPause = findViewById(R.id.img_play_pause);
        imgNext = findViewById(R.id.img_next);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new LocalMusicFragment(), "Local music");
        viewPagerAdapter.addFragments(new OnlineMusicFragment(), "Online music");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        imgPrev.setOnClickListener(this);
        imgPlayPause.setOnClickListener(this);
        imgNext.setOnClickListener(this);
        rlViewPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("currentSong", position);
                bundle.putBoolean("checkSong", isLocal);
                intent.putExtra("song", bundle);
                startActivityForResult(intent, REQUEST_SONG);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == 0) {
            initView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SONG && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getBundleExtra("data");
            position = bundle.getInt("position");
            isLocal = bundle.getBoolean("isLocal");
            cardRl.setVisibility(View.VISIBLE);
            if (isLocal) {
                Glide.with(this).load(localMusic.get(position).getCover())
                        .placeholder(R.drawable.cover_art)
                        .into(imgCoverArt);
                txtSongName.setText(localMusic.get(position).getTitle());
                if (mediaPlayer.isPlaying()) {
                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                    CreateNotification.createNotification(MainActivity.this, localMusic.get(position),
                            R.drawable.ic_pause, position, localMusic.size() - 1);
                } else {
                    imgPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    CreateNotification.createNotification(MainActivity.this, localMusic.get(position),
                            R.drawable.ic_play_arrow, position, localMusic.size() - 1);
                }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel();
                registerReceiver(broadcastReceiver, new IntentFilter("Track"));
//                startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
//            }
            } else {
                Glide.with(this).load(onlineMusic.get(position).getCover())
                        .placeholder(R.drawable.cover_art)
                        .into(imgCoverArt);
                txtSongName.setText(onlineMusic.get(position).getTitle());
                if (mediaPlayer.isPlaying()) {
                    imgPlayPause.setImageResource(R.drawable.ic_pause);
                    CreateNotification.createNotification(MainActivity.this, onlineMusic.get(position),
                            R.drawable.ic_pause, position, onlineMusic.size() - 1);
                } else {
                    imgPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    CreateNotification.createNotification(MainActivity.this, onlineMusic.get(position),
                            R.drawable.ic_play_arrow, position, onlineMusic.size() - 1);
                }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel();
                registerReceiver(broadcastReceiver, new IntentFilter("Track"));
//                startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
//            }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_prev:
                prevBtnClicked();
                break;
            case R.id.img_play_pause:
                playPauseBtnClicked();
                break;
            case R.id.img_next:
                nextBtnClicked();
                break;
        }
    }

    private void nextBtnClicked() {
        List<Music> localMusic = new ArrayList<>();
        if (isLocal) {
            localMusic.addAll(MainActivity.localMusic);
        } else {
            localMusic.addAll(MainActivity.onlineMusic);
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position + 1) % localMusic.size());
            initPlayer(isLocal);
            Glide.with(this).load(localMusic.get(position).getCover())
                    .placeholder(R.drawable.cover_art)
                    .into(imgCoverArt);
            txtSongName.setText(localMusic.get(position).getTitle());
            mediaPlayer.setOnCompletionListener(this);
            imgPlayPause.setImageResource(R.drawable.ic_pause);
            CreateNotification.createNotification(MainActivity.this, localMusic.get(position),
                    R.drawable.ic_pause, position, localMusic.size() - 1);
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position + 1) % localMusic.size());
            initPlayer(isLocal);
            Glide.with(this).load(localMusic.get(position).getCover())
                    .placeholder(R.drawable.cover_art)
                    .into(imgCoverArt);
            txtSongName.setText(localMusic.get(position).getTitle());
            mediaPlayer.setOnCompletionListener(this);
            imgPlayPause.setImageResource(R.drawable.ic_play_arrow);
            CreateNotification.createNotification(MainActivity.this, localMusic.get(position),
                    R.drawable.ic_play_arrow, position, localMusic.size() - 1);
        }
    }

    private void playPauseBtnClicked() {
        List<Music> localMusic = new ArrayList<>();
        if (isLocal) {
            localMusic.addAll(MainActivity.localMusic);
        } else {
            localMusic.addAll(MainActivity.onlineMusic);
        }
        if (mediaPlayer.isPlaying()) {
            imgPlayPause.setImageResource(R.drawable.ic_play_arrow);
            mediaPlayer.pause();
            CreateNotification.createNotification(MainActivity.this, localMusic.get(position),
                    R.drawable.ic_play_arrow, position, localMusic.size() - 1);
        } else {
            imgPlayPause.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
            CreateNotification.createNotification(MainActivity.this, localMusic.get(position),
                    R.drawable.ic_pause, position, localMusic.size() - 1);
        }
    }

    private void prevBtnClicked() {
        List<Music> localMusic = new ArrayList<>();
        if (isLocal) {
            localMusic.addAll(MainActivity.localMusic);
        } else {
            localMusic.addAll(MainActivity.onlineMusic);
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position - 1) < 0 ? (localMusic.size() - 1) : (position - 1));
            initPlayer(isLocal);
            Glide.with(this).load(localMusic.get(position).getCover())
                    .placeholder(R.drawable.cover_art)
                    .into(imgCoverArt);
            txtSongName.setText(localMusic.get(position).getTitle());
            mediaPlayer.setOnCompletionListener(this);
            imgPlayPause.setImageResource(R.drawable.ic_pause);
            CreateNotification.createNotification(MainActivity.this, localMusic.get(position),
                    R.drawable.ic_pause, position, localMusic.size() - 1);
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position - 1) < 0 ? (localMusic.size() - 1) : (position - 1));
            initPlayer(isLocal);
            Glide.with(this).load(localMusic.get(position).getCover())
                    .placeholder(R.drawable.cover_art)
                    .into(imgCoverArt);
            txtSongName.setText(localMusic.get(position).getTitle());
            mediaPlayer.setOnCompletionListener(this);
            imgPlayPause.setImageResource(R.drawable.ic_play_arrow);
            CreateNotification.createNotification(MainActivity.this, localMusic.get(position),
                    R.drawable.ic_play_arrow, position, localMusic.size() - 1);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        nextBtnClicked();
    }

    private void initPlayer(boolean check) {
        if (check) {
            uri = Uri.fromFile(new File(localMusic.get(position).getPath()));
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        } else {
            uri = Uri.parse(onlineMusic.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        }
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                "Long", NotificationManager.IMPORTANCE_HIGH);
        notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action) {
                case CreateNotification.ACTION_PREVIOUS:
                    prevBtnClicked();
                    break;
                case CreateNotification.ACTION_PLAY:
                    playPauseBtnClicked();
                    break;
                case CreateNotification.ACTION_NEXT:
                    nextBtnClicked();
                    break;
            }
        }
    };
}
