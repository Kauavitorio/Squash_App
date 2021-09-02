package dev.kaua.squash.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import dev.kaua.squash.BuildConfig;
import dev.kaua.squash.R;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class OreoNotification extends ContextWrapper {

    private static String CHANNEL_ID = BuildConfig.APPLICATION_ID;
    private static final String CHANNEL_NAME = "squash_social_app";
    public static NotificationManager notificationManager;

    public OreoNotification(Context base, String receiver, int type) {
        super(base);
        //  Set Default Channel ID
        CHANNEL_ID = BuildConfig.APPLICATION_ID;

        //  Generate channel id with different types
        if(type == Data.TYPE_MESSAGE) CHANNEL_ID = receiver + "_" + type;
        else CHANNEL_ID = CHANNEL_ID + "_" + type;

        createChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME
                , NotificationManager.IMPORTANCE_HIGH);

        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (notificationManager == null)
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getOreoNotification(String title, String body, PendingIntent pendingIntent, Uri uri, int type) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.pumpkin_default_image)
                .setSound(uri)
                .setCategory(String.valueOf(type))
                .setGroupSummary(true)
                .setAutoCancel(true);

    }
}