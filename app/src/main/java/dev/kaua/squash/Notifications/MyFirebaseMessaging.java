package dev.kaua.squash.Notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.MessageActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.MyPrefs;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser firebaseUser = myFirebaseHelper.getFirebaseUser();
        if (firebaseUser != null) updateToken(s);
    }

    private void updateToken(String newToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
            Token token = new Token(newToken);
            reference.child(firebaseUser.getUid()).setValue(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data_notify = remoteMessage.getData();

        String user = remoteMessage.getData().get("user");

        SharedPreferences preferences  = getSharedPreferences(MyPrefs.PREFS_NOTIFICATION, MODE_PRIVATE);
        String currentUser = preferences.getString("currentUser", "none");
        Log.d("Current", currentUser);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && data_notify.size() > 0) {
            if (!currentUser.equals(user)) {
                Log.d("Current", user);
                sendOreoNotification(remoteMessage);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOreoNotification(@NonNull RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        //String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String chat_id = remoteMessage.getData().get("chat_id");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification  = remoteMessage.getNotification();

        assert user != null;
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        final Intent intent;
        if(chat_id != null && chat_id.equals("comment_id")){
            intent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("shortcut", 0);
            bundle.putInt("shared", 0);
            intent.putExtras(bundle);
        }else{
            intent = new Intent(this, MessageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("userId", user);
            bundle.putString("chat_id", chat_id);
            intent.putExtras(bundle);

            assert body != null;
            Update_Last_chat(user, body);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon);

        int i = 0;
        if (j > 0) i=j;

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void Update_Last_chat(String user, String body) {
        DaoChat daoChat = new DaoChat(this);
        DtoAccount account = new DtoAccount();
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time_last_chat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        account.setId(user);
        account.setLast_chat(df_time_last_chat.format(c.getTime()));
        account.setStatus_chat(getString(R.string.waiting_for_reply));
        String[] split = body.split(":");
        if(split.length > 0) account.setName_user(split[0]);
        daoChat.UPDATE_A_CHAT(account, 0);
    }
}