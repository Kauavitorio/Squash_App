package dev.kaua.squash.LocalDataBase.Notification;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.kaua.squash.Notifications.Data;

public class DaoNotification extends SQLiteOpenHelper {
    public static final int NOT_SEEN = 0;
    public static final int SEEN = 1;
    private final String TABLE = "TBL_NOTIFICATION";
    private final String TABLE_USER = "TBL_CONFIRM_USER";

    public DaoNotification(@Nullable Context context) {
        super(context, "DB_NOTIFICATION", null, 17);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        //  Create Table
        String command = "CREATE TABLE " + TABLE + "(" +
                "type int," +
                "sender varchar(1000)," +
                "body varchar(1000)," +
                "title varchar(1000)," +
                "seen int," +
                "date_time varchar(100))";

        db.execSQL(command);
        //  Create Table
        String commandUser = "CREATE TABLE " + TABLE_USER + "(" +
                "user_id varchar(100))";

        db.execSQL(commandUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < newVersion){
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

            // Create tables again
            onCreate(db);
        }
    }

    public void Register_Notification(Data data, int type){
        String commandTest = "SELECT * FROM " + TABLE_USER;
        @SuppressLint("Recycle") Cursor cursorTest = getWritableDatabase().rawQuery(commandTest, null);
        if(cursorTest.moveToNext()){

            boolean result = false;
            if(type == Data.TYPE_FOLLOW){
                String command = "SELECT * FROM " + TABLE + " WHERE sender = ? and type = ?";
                String[] params = {data.getUser(), data.getType()};
                @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);
                result = cursor.moveToFirst();
            }

            if(!result)
                if(data.getUser() != null){
                    ContentValues values = new ContentValues();
                    values.put("type", type);
                    values.put("sender", data.getUser());
                    values.put("body", data.getBody());
                    values.put("title", data.getTitle());
                    values.put("seen", NOT_SEEN);
                    values.put("date_time", data.getDate_time());

                    getWritableDatabase().insert(TABLE, null, values);
                }
        }
    }

    public void Register_User(String user){
        String command = "SELECT * FROM " + TABLE_USER + " WHERE user_id = ?";
        String[] params = {user};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);
        final boolean result = cursor.moveToFirst();

        if(!result){
            DropTables();
            new Handler().postDelayed(() -> {
                ContentValues values = new ContentValues();
                values.put("user_id", user);

                getWritableDatabase().insert(TABLE_USER, null, values);
            }, 500);
        }
    }

    public void Read_All_Notification(){
        ContentValues values = new ContentValues();
        values.put("seen", SEEN);
        String[] params = {"0"};

        String where = "type>?";

        getWritableDatabase().update(TABLE, values, where, params);
    }

    public boolean Test_Notification(Data data){
        String command = "SELECT * FROM " + TABLE + " WHERE sender = ? and type = ?";
        String[] params = {data.getUser(), data.getType()};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);
        return !cursor.moveToFirst();
    }

    public List<Data> getNotifications(){
        String command = "SELECT * FROM " + TABLE;
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, null);
        final List<Data> list = new ArrayList<>();

        if (cursor.moveToFirst()){
            do{
                Data data = new Data();
                data.setType(String.valueOf(cursor.getInt(0)));
                data.setUser(cursor.getString(1));
                data.setBody(cursor.getString(2));
                data.setTitle(cursor.getString(3));
                data.setSeen(cursor.getInt(4));
                data.setDate_time(cursor.getString(5));
                list.add(data);
            }while(cursor.moveToNext());
        }
        Collections.reverse(list);
        return list;
    }

    public void DropTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER,null,null);
        db.delete(TABLE,null,null);
    }

}
