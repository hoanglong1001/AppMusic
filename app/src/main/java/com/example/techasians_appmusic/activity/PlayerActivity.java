package com.example.techasians_appmusic.activity;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.techasians_appmusic.R;
import com.example.techasians_appmusic.async.DownloadSong;
import com.example.techasians_appmusic.model.Music;
import com.example.techasians_appmusic.service.CreateNotification;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import static com.example.techasians_appmusic.activity.MainActivity.localMusic;
import static com.example.techasians_appmusic.activity.MainActivity.onlineMusic;
import static com.example.techasians_appmusic.activity.MainActivity.repeatBoolean;
import static com.example.techasians_appmusic.activity.MainActivity.shuffleBoolean;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private TextView songName;
    private TextView artistName;
    private TextView durationPlayed;
    private TextView durationTotal;
    private ImageView coverArt;
    private ImageView btnNext;
    private ImageView btnPrevious;
    private ImageView btnBack;
    private ImageView btnMenu;
    private ImageView btnShuffle;
    private ImageView btnRepeat;
    private FloatingActionButton btnPlayPause;
    private SeekBar seekBar;
    private int position = -1;
    private boolean isLocal;
    static ArrayList<Music> listSongs;
    static Uri uri;
    public static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread;
    private Thread nextThread;
    private Thread previousThread;
    public static NotificationManager notificationManager;
    public static boolean isDownload = false;
    public ObjectAnimator anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();
        getIntentMethod();
        getIntentFromViewPlayer();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createChannel();
        registerReceiver(broadcastReceiver, new IntentFilter("TrackPlay"));
//            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
//        }
        songName.setText(listSongs.get(position).getTitle());
        artistName.setText(listSongs.get(position).getArtist());
        mediaPlayer.setOnCompletionListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    int duration = mediaPlayer.getDuration() / 1000;
                    seekBar.setProgress(currentPosition);
                    durationPlayed.setText(formattedTime(currentPosition));
                    durationTotal.setText(formattedTime(duration));
                }
                handler.postDelayed(this, 1000);
            }
        });
        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_off);
                } else {
                    shuffleBoolean = true;
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });
        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    btnRepeat.setImageResource(R.drawable.ic_repeat_off);
                } else {
                    repeatBoolean = true;
                    btnRepeat.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putBoolean("isLocal", isLocal);
                data.putExtra("data", bundle);
                setResult(RESULT_OK, data);
                finish();
            }
        });
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(PlayerActivity.this,
                        R.style.Theme_AppCompat_Light_Dialog_Alert);
                dialog.setTitle("Tải ảnh");
                dialog.setMessage("Bạn có muốn tải bài hát này không?");
                dialog.setPositiveButton("Có", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    ProgressDialog progressDialog = new ProgressDialog(PlayerActivity.this,
                            R.style.Theme_AppCompat_Light_Dialog_Alert);
                    progressDialog.setTitle("Đang tải");
                    progressDialog.setProgressStyle(progressDialog.STYLE_HORIZONTAL);
                    progressDialog.setMax(100);
                    new DownloadSong(PlayerActivity.this, progressDialog).execute(onlineMusic.get(position).getPath(),
                            onlineMusic.get(position).getTitle() + ".mp3");
                });
                dialog.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putBoolean("isLocal", isLocal);
        data.putExtra("data", bundle);
        setResult(RESULT_OK, data);
        finish();
    }

    public void getIntentFromViewPlayer() {
        if (getIntent().hasExtra("song")) {
            Bundle bundle = getIntent().getBundleExtra("song");
            position = bundle.getInt("currentSong", 0);
            isLocal = bundle.getBoolean("checkSong");
            if (isLocal) {
//                listSongs.clear();
//                listSongs = localMusic;
                if (listSongs != null) {
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                    uri = Uri.fromFile(new File(listSongs.get(position).getPath()));
                }
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        anim.resume();
                        btnPlayPause.setImageResource(R.drawable.ic_pause);
                        CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                                R.drawable.ic_pause, position, listSongs.size());
                    } else {
                        anim.pause();
                        btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                        CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                                R.drawable.ic_play_arrow, position, listSongs.size());
                    }
                    seekBar.setMax(mediaPlayer.getDuration() / 1000);
                    PlayerActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                            int duration = mediaPlayer.getDuration() / 1000;
                            durationPlayed.setText(formattedTime(currentPosition));
                            durationTotal.setText(formattedTime(duration));
                            seekBar.setProgress(currentPosition);
                            handler.postDelayed(this, 1000);
                        }
                    });
                }
                metaData();
            } else {
//                listSongs.clear();
//                listSongs = onlineMusic;
                if (listSongs != null) {
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                    uri = Uri.parse(listSongs.get(position).getPath());
                }
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        anim.resume();
                        btnPlayPause.setImageResource(R.drawable.ic_pause);
                        CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                                R.drawable.ic_pause, position, listSongs.size());
                    } else {
                        anim.pause();
                        btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                        CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                                R.drawable.ic_play_arrow, position, listSongs.size());
                    }
                    seekBar.setMax(mediaPlayer.getDuration() / 1000);
                    PlayerActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                            int duration = mediaPlayer.getDuration() / 1000;
                            durationPlayed.setText(formattedTime(currentPosition));
                            durationTotal.setText(formattedTime(duration));
                            seekBar.setProgress(currentPosition);
                            handler.postDelayed(this, 1000);
                        }
                    });
                }
                metaData();
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        playThreadBtn();
        nextThreadBtn();
        previousThreadBtn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        anim.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void previousThreadBtn() {
        previousThread = new Thread() {
            @Override
            public void run() {
                super.run();
                btnPrevious.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        previousBtnClicked();
                    }
                });
            }
        };
        previousThread.start();
    }

    private void previousBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
            }
            initPlayer(isLocal);
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData();
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                    R.drawable.ic_pause, position, listSongs.size() - 1);
            mediaPlayer.start();
            anim.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            anim.pause();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
            }
            initPlayer(isLocal);
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData();
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                    R.drawable.ic_play_arrow, position, listSongs.size() - 1);
        }
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    private void nextBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listSongs.size());
            }
            initPlayer(isLocal);
            metaData();
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                    R.drawable.ic_pause, position, listSongs.size() - 1);
            mediaPlayer.start();
            anim.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            anim.pause();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listSongs.size());
            }
            initPlayer(isLocal);
            metaData();
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                    R.drawable.ic_play_arrow, position, listSongs.size() - 1);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                btnPlayPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    private void playPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            anim.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                    R.drawable.ic_play_arrow, position, listSongs.size() - 1);
        } else {
            anim.resume();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                    R.drawable.ic_pause, position, listSongs.size() - 1);
        }
    }

    private String formattedTime(int currentPosition) {
        String totalOut = "";
        String totalNew = "";
        String second = String.valueOf(currentPosition % 60);
        String minute = String.valueOf(currentPosition / 60);
        totalOut = minute + ":" + second;
        totalNew = minute + ":" + "0" + second;
        if (second.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }
    }

    private void getIntentMethod() {
        if (getIntent().hasExtra("local")) {
            Bundle bundle = getIntent().getBundleExtra("local");
            position = bundle.getInt("posLocalSong", 0);
            isLocal = bundle.getBoolean("isLocalSong");
            listSongs = new ArrayList<>();
            listSongs.clear();
            listSongs = localMusic;
            if (listSongs != null) {
                btnPlayPause.setImageResource(R.drawable.ic_pause);
                uri = Uri.fromFile(new File(listSongs.get(position).getPath()));
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
            } else {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
            }
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            metaData();
            CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                    R.drawable.ic_pause, position, listSongs.size());
        }
        if (getIntent().hasExtra("online")) {
            btnMenu.setVisibility(View.VISIBLE);
            Bundle bundle = getIntent().getBundleExtra("online");
            position = bundle.getInt("posOnlSong", 0);
            isLocal = bundle.getBoolean("isLocalSong");
            listSongs = new ArrayList<>();
            listSongs.clear();
            listSongs = onlineMusic;
            if (listSongs != null) {
                btnPlayPause.setImageResource(R.drawable.ic_pause);
                uri = Uri.parse(listSongs.get(position).getPath());
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
            } else {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
            }
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            metaData();
            CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
                    R.drawable.ic_pause, position, listSongs.size());
        }
    }

    private void initView() {
        songName = findViewById(R.id.song_name);
        artistName = findViewById(R.id.artist_name);
        durationPlayed = findViewById(R.id.duration_played);
        durationTotal = findViewById(R.id.duration_total);
        coverArt = findViewById(R.id.cover_art);
        btnNext = findViewById(R.id.btn_next);
        btnPrevious = findViewById(R.id.btn_prev);
        btnBack = findViewById(R.id.btn_back);
        btnMenu = findViewById(R.id.btn_download);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnRepeat = findViewById(R.id.btn_repeat);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        seekBar = findViewById(R.id.seekbar);
        anim = ObjectAnimator.ofFloat(coverArt, View.ROTATION, 0f, 360f)
                .setDuration(15000);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    private void metaData() {
        if (listSongs.get(position).getCover() != null) {
            Picasso.get()
                    .load(listSongs.get(position).getCover())
                    .error(R.drawable.cover_art)
                    .into(coverArt);
//            new GetBitMap().execute(listSongs.get(position).getCover());

        } else {
            coverArt.setImageResource(R.drawable.cover_art);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        PlayerActivity.mediaPlayer.start();
        nextBtnClicked();
//        btnPlayPause.setImageResource(R.drawable.ic_pause);
//        CreateNotification.createNotification(PlayerActivity.this, listSongs.get(position),
//                R.drawable.ic_pause, position, listSongs.size() - 1);
//        anim.start();
    }

    private void initPlayer(boolean check) {
        if (check) {
            uri = Uri.fromFile(new File(listSongs.get(position).getPath()));
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        } else {
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action) {
                case CreateNotification.ACTION_PREVIOUS:
                    previousBtnClicked();
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


//    class GetBitMap extends AsyncTask<String, Void, Bitmap> {
//        @Override
//        protected Bitmap doInBackground(String... strings) {
//            try {
//                if(  Uri.parse(strings[0])!=null   ){
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(PlayerActivity.this.getContentResolver() , Uri.parse(strings[0]));
//                return bitmap;
//                }
//            }
//            catch (Exception e) {
//                //handle exception
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            super.onPostExecute(bitmap);
//            if (bitmap != null) {
//                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//                    @Override
//                    public void onGenerated(@Nullable Palette palette) {
//                        Palette.Swatch swatch = palette.getDominantSwatch();
//                        if (swatch != null) {
//                            ImageView gradient = findViewById(R.id.img_gradient);
//                            RelativeLayout container = findViewById(R.id.container);
//                            gradient.setBackgroundResource(R.drawable.background_gradient);
//                            container.setBackgroundResource(R.drawable.background_gradient);
//                            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
//                                    new int[] {swatch.getRgb(), 0x00000000});
//                            gradient.setBackground(gradientDrawable);
//                            GradientDrawable gdBackground = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
//                                    new int[] {swatch.getRgb(), swatch.getRgb()});
//                            container.setBackground(gdBackground);
//                            songName.setTextColor(swatch.getTitleTextColor());
//                            artistName.setTextColor(swatch.getBodyTextColor());
//                        } else {
//                            ImageView gradient = findViewById(R.id.img_gradient);
//                            RelativeLayout container = findViewById(R.id.container);
//                            gradient.setBackgroundResource(R.drawable.background_gradient);
//                            container.setBackgroundResource(R.drawable.background_gradient);
//                            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
//                                    new int[] {0xff000000, 0x00000000});
//                            gradient.setBackground(gradientDrawable);
//                            GradientDrawable gdBackground = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
//                                    new int[] {0xff000000, 0xff000000});
//                            container.setBackground(gdBackground);
//                            songName.setTextColor(Color.WHITE);
//                            artistName.setTextColor(Color.DKGRAY);
//                        }
//                    }
//                });
//            }
//        }
//    }

}
