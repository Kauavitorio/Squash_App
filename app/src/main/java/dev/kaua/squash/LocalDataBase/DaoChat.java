package dev.kaua.squash.LocalDataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import dev.kaua.squash.Data.Post.DtoPost;

public class DaoChat extends SQLiteOpenHelper {
    private final String TABLE_BG = "TBL_BACKGROUND";


    public DaoChat(@Nullable Context context) {
        super(context, "DB_CHAT", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {

        String command_likes = "CREATE TABLE " + TABLE_BG + "(" +
                "chat_name varchar(1000) not null," +
                "background_url varchar(1000) not null)";

        db.execSQL(command_likes);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < newVersion){
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BG);

            // Create tables again
            onCreate(db);
        }
    }

    public void REGISTER_BG(String chat_name, String background_url){
        String command = "SELECT * FROM " + TABLE_BG + " WHERE  chat_name = ?";
        String[] params = {String.valueOf(chat_name)};
        Cursor cursor = getWritableDatabase().rawQuery(command, params);


        if(cursor.moveToFirst()){
            ContentValues values = new ContentValues();
            values.put("chat_name", chat_name);
            values.put("background_url", background_url);

            String where = "chat_name=?";

            getWritableDatabase().update(TABLE_BG, values, where, params);
        }else{
            ContentValues values = new ContentValues();
            values.put("chat_name", chat_name);
            values.put("background_url", background_url);

            getWritableDatabase().insert(TABLE_BG, null, values);
        }
    }

    public String get_BG(String chat_name){
        String command = "SELECT * FROM " + TABLE_BG + " WHERE  chat_name = ?";
        String[] params = {chat_name};
        Cursor cursor = getWritableDatabase().rawQuery(command, params);
        ArrayList<DtoPost> dtoPosts = new ArrayList<>();

        if (cursor.moveToNext()) {
            return cursor.getString(1);
        }else return null;
    }

    public void DropTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BG,null,null);
        Log.d("InsertPost", "Dropped");
        //createTable(db);
    }

}
