package dev.kaua.squash.Notifications;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class Data {
    public static final int NO_TYPE = 0;
    public static final int TYPE_FOLLOW = 11;
    public static final int TYPE_MESSAGE = 22;
    public static final int TYPE_COMMENT = 33;
    public static final String TAG_USER = "user";
    public static final String TAG_TYPE = "type";
    public static final String TAG_TITLE = "title";
    public static final String TAG_CHAT_ID = "chat_id";
    public static final String TAG_BODY = "body";
    private String user;
    private int icon;
    private int seen;
    private String type;
    private String body;
    private String title;
    private String receiver;
    private String chat_id;
    private String date_time;

    public Data(String user, String TYPE, String body, String title, String receiver, String chat_id) {
        this.user = user;
        this.type = TYPE;
        this.body = body;
        this.title = title;
        this.receiver = receiver;
        this.chat_id = chat_id;
    }

    public Data(String user, String TYPE, String body, String title, String receiver) {
        this.user = user;
        this.type = TYPE;
        this.body = body;
        this.title = title;
        this.receiver = receiver;
    }

    public Data(String user, String type, String body, String receiver) {
        this.user = user;
        this.type = type;
        this.body = body;
        this.receiver = receiver;
    }

    public Data() {}

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getType() {
        return type;
    }

    public int getSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
