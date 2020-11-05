package com.example.techasians_appmusic.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;

import com.example.techasians_appmusic.R;
import com.example.techasians_appmusic.activity.MainActivity;
import com.example.techasians_appmusic.activity.PlayerActivity;
import com.example.techasians_appmusic.model.Music;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.techasians_appmusic.activity.MainActivity.isLocal;
import static com.example.techasians_appmusic.adapter.LocalMusicAdapter.REQUEST_SONG;

public class CreateNotification {
    public static final String CHANNEL_ID = "channel";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_PLAY = "actionplay";
    public static final String ACTION_NEXT = "actionnext";
    public static final int NOTIFICATION_ID = 1;
    public static Notification notification;

    public static void createNotification(Context context, Music musicFile, int playbutton, int pos, int size) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

        PendingIntent pendingIntentPrev;
        int drw_prev;
        Intent intentPrev = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_PREVIOUS);
        if (context instanceof PlayerActivity) {
            intentPrev.putExtra("type", 1);
        } else {
            intentPrev.putExtra("type", 0);
        }
        pendingIntentPrev = PendingIntent.getBroadcast(context, 0,
                intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
        drw_prev = R.drawable.ic_skip_previous_black;


        Intent intentPlay = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_PLAY);
        if (context instanceof PlayerActivity) {
            intentPlay.putExtra("type", 1);
        } else {
            intentPlay.putExtra("type", 0);
        }
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,
                intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pendingIntentNext;
        int drw_next;
        Intent intentNext = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_NEXT);
        if (context instanceof PlayerActivity) {
            intentNext.putExtra("type", 1);
        } else {
            intentNext.putExtra("type", 0);
        }
        pendingIntentNext = PendingIntent.getBroadcast(context, 0,
                intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        drw_next = R.drawable.ic_skip_next_black;

        Intent notificationIntent = new Intent(context, context instanceof PlayerActivity ?
                PlayerActivity.class : MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("posLocalSong", pos);
        bundle.putBoolean("isLocalSong", isLocal);
        notificationIntent.putExtra("local", bundle);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                REQUEST_SONG, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(musicFile.getTitle())
                .setShowWhen(true)
                .setOnlyAlertOnce(true)
                .setContentText(musicFile.getArtist())
                .addAction(drw_prev, "Previous", pendingIntentPrev)
                .addAction(playbutton, "Play", pendingIntentPlay)
                .addAction(drw_next, "Next", pendingIntentNext)
                .setContentIntent(contentIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
