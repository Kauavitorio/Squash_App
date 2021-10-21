package dev.kaua.squash.Notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import dev.kaua.squash.Activities.MainActivity;
import dev.kaua.squash.Activities.Chat.MessageActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.LocalDataBase.Notification.DaoNotification;
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
        if (myFirebaseHelper.getFirebaseUser() != null) updateToken(s);
    }

    private void updateToken(String newToken) {
        FirebaseUser firebaseUser = myFirebaseHelper.getFirebaseUser();
        if(firebaseUser != null){
            DatabaseReference reference = myFirebaseHelper.getFirebaseDatabase().getReference("Tokens");
            Token token = new Token(newToken);
            reference.child(firebaseUser.getUid()).setValue(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data_notify = remoteMessage.getData();

        String user = remoteMessage.getData().get(Data.TAG_USER);

        String currentUser = MyPrefs.getCurrentUser(this);
        Log.d("Current", currentUser);

        FirebaseUser firebaseUser = myFirebaseHelper.getFirebaseUser();

        if (firebaseUser != null && data_notify.size() > 0) {
            if (!currentUser.equals(user)) {
                Log.d("Current", user);
                sendOreoNotification(remoteMessage);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOreoNotification(@NonNull RemoteMessage remoteMessage) {
        int type = 0;
        final Data data = new Data();
        final DaoNotification daoNotification = new DaoNotification(this);
        final Calendar c = Calendar.getInstance();
        final @SuppressLint("SimpleDateFormat") SimpleDateFormat format_notification = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
        String time_notification = format_notification.format(c.getTime());

        String user = remoteMessage.getData().get(Data.TAG_USER);
        String type_str = remoteMessage.getData().get(Data.TAG_TYPE);
        String title = remoteMessage.getData().get(Data.TAG_TITLE);
        String chat_id = remoteMessage.getData().get(Data.TAG_CHAT_ID);
        String body = remoteMessage.getData().get(Data.TAG_BODY);

        if(type_str != null)
            type = Integer.parseInt(type_str);

        if(user != null && type != Data.NO_TYPE){
            boolean can_show = false;
            data.setUser(user);
            data.setType(String.valueOf(type));
            data.setChat_id(chat_id);
            data.setBody(body);
            data.setDate_time(time_notification);

            if(type == Data.TYPE_FOLLOW)
                can_show = daoNotification.Test_Notification(data);

            if(!can_show){

                RemoteMessage.Notification notification  = remoteMessage.getNotification();

                int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

                Intent intent = null;
                if(type == Data.TYPE_COMMENT || type == Data.TYPE_FOLLOW){
                    if(type == Data.TYPE_COMMENT) title = getString(R.string.new_comment);
                    else {
                        body = body + " " + getString(R.string.started_following_you);
                        title = getString(R.string.new_follow);
                    }

                    intent = new Intent(this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("shortcut", 0);
                    bundle.putInt("shared", 0);
                    intent.putExtras(bundle);
                }else if(type == Data.TYPE_MESSAGE) {
                    title = getString(R.string.new_message);
                    intent = new Intent(this, MessageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", user);
                    bundle.putString("chat_id", chat_id);
                    intent.putExtras(bundle);

                    if(body != null)
                        Update_Last_chat(user, body);
                }

                if(intent != null){
                    data.setTitle(title);
                    Register_Notification(data, daoNotification, type);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

                    Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    OreoNotification oreoNotification = new OreoNotification(this, user, type);
                    Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, type);

                    int i = 0;
                    if (j > 0) i=j;

                    oreoNotification.getManager().notify(i, builder.build());
                }
            }
        }
    }

    void Register_Notification(Data data, DaoNotification daoNotification, int type){
        daoNotification.Register_Notification(data, type);
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