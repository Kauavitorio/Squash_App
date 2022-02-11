package dev.kaua.squash.Notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Map;

import dev.kaua.squash.Activities.MainActivity;
import dev.kaua.squash.Activities.Chat.MessageActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.LocalDataBase.Notification.DaoNotification;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ShortCutsHelper;

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
        final FirebaseUser firebaseUser = myFirebaseHelper.getFirebaseUser();
        if(firebaseUser != null)
            myFirebaseHelper.getFirebaseDatabase()
                    .getReference(myFirebaseHelper.TOKENS_REFERENCE)
                    .child(firebaseUser.getUid()).setValue(new Token(newToken));
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(MyPrefs.isNotificationActive(this)){
            final Map<String, String> data_notify = remoteMessage.getData();

            final String user = remoteMessage.getData().get(Data.TAG_USER);

            final String currentUser = MyPrefs.getCurrentUser(this);

            if (myFirebaseHelper.getFirebaseUser() != null && data_notify.size() > 0) {
                if (!currentUser.equals(user))
                    sendOreoNotification(remoteMessage);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOreoNotification(@NonNull RemoteMessage remoteMessage) {
        int type = 0;
        final Data data = new Data();
        final DaoNotification daoNotification = new DaoNotification(this);
        String time_notification = String.valueOf(System.currentTimeMillis());

        final String user = remoteMessage.getData().get(Data.TAG_USER);
        final String type_str = remoteMessage.getData().get(Data.TAG_TYPE);
        final String chat_id = remoteMessage.getData().get(Data.TAG_CHAT_ID);
        String title = remoteMessage.getData().get(Data.TAG_TITLE);
        String body = remoteMessage.getData().get(Data.TAG_BODY);

        if(type_str != null)
            type = Integer.parseInt(type_str);

        if(user != null && type != Data.NO_TYPE){
            boolean can_show;
            data.setUser(user);
            data.setType(String.valueOf(type));
            data.setChat_id(chat_id);
            data.setBody(body);
            data.setDate_time(time_notification);

            if(type == Data.TYPE_FOLLOW)
                can_show = daoNotification.Test_Notification(data);
            else can_show = checkBlockOrMute(user, type);

            if(can_show){

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
                    bundle.putInt(ShortCutsHelper.SHORTCUT_TAG, ShortCutsHelper.NONE_SHORT);
                    bundle.putInt(MessageActivity.SHARE_ID, 0);
                    intent.putExtras(bundle);
                }else if(type == Data.TYPE_MESSAGE) {
                    title = getString(R.string.new_message);
                    intent = new Intent(this, MessageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(MessageActivity.USER_ID, user);
                    bundle.putString(MessageActivity.CHAT_ID, chat_id);
                    intent.putExtras(bundle);

                    if(body != null)
                        Update_Last_chat(user, body);
                }

                if(intent != null){
                    data.setTitle(title);
                    Register_Notification(data, daoNotification, type);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

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

    boolean checkBlockOrMute(String user, int type){
        final ArrayList<String> muteList = MyPrefs.getMutedUsers(this);
        if(type == Data.TYPE_MESSAGE)
            return !muteList.contains(user);

        return true;
    }

    void Register_Notification(Data data, DaoNotification daoNotification, int type){
        daoNotification.Register_Notification(data, type);
    }

    private void Update_Last_chat(String user, String body) {
        try {
            final DaoChat daoChat = new DaoChat(this);
            final DtoAccount account = new DtoAccount();
            account.setId(user);
            account.setLast_chat(String.valueOf(System.currentTimeMillis()));
            account.setStatus_chat(getString(R.string.waiting_for_reply));
            final String[] split = body.split(":");
            if(split.length > 0) account.setName_user(split[0]);
            daoChat.UPDATE_A_CHAT(account, 0);
        }catch (Exception ignore){}
    }
}