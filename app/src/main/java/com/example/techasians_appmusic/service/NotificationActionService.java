package com.example.techasians_appmusic.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getIntExtra("type", -1) == 0) {
                context.sendBroadcast(new Intent("Track")
                        .putExtra("actionname", intent.getAction()));
            } else {
                context.sendBroadcast(new Intent("TrackPlay")
                        .putExtra("actionname", intent.getAction()));
            }
        }
    }
}
