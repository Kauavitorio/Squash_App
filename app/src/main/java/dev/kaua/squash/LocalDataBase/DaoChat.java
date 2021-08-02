package dev.kaua.squash.LocalDataBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.DtoPost;

public class DaoChat extends SQLiteOpenHelper {
    private final String TABLE_BG = "TBL_BACKGROUND";
    private final String TABLE_CHAT_LIST = "TBL_CHAT_LIST";

    public DaoChat(@Nullable Context context) {
        super(context, "DB_CHAT", null, 16);
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

        String command_chat_list = "CREATE TABLE " + TABLE_CHAT_LIST + "(" +
                "account_id_cry varchar(500) ," +
                "id varchar(1000) not null," +
                "imageURL varchar(800)," +
                "last_seen varchar(800)," +
                "name_user varchar(800)," +
                "search varchar(800) ," +
                "status_chat varchar(800) ," +
                "typingTo varchar(800)," +
                "last_chat varchar(800)," +
                "verification_level varchar(800)," +
                "username varchar(800)," +
                "chat_id varchar(1000))";

        db.execSQL(command_chat_list);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < newVersion){
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BG);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_LIST);

            // Create tables again
            onCreate(db);
        }
    }

    public void REGISTER_BG(String chat_name, String background_url){
        String command = "SELECT * FROM " + TABLE_BG + " WHERE  chat_name = ?";
        String[] params = {String.valueOf(chat_name)};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);

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

    public void REGISTER_CHAT_LIST(List<DtoAccount> accounts){
        if(accounts != null && accounts.size() > 0){
            for(int i = 0; i < accounts.size(); i++){
                String command = "SELECT * FROM " + TABLE_CHAT_LIST + " WHERE  id = ?";
                String[] params = {accounts.get(i).getId()};
                @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);

                if(cursor.moveToFirst()){
                    ContentValues values = new ContentValues();
                    values.put("account_id_cry", accounts.get(i).getAccount_id_cry());
                    values.put("id", accounts.get(i).getId());
                    values.put("imageURL", accounts.get(i).getImageURL());
                    values.put("last_seen", accounts.get(i).getLast_seen());
                    values.put("name_user", accounts.get(i).getName_user());
                    values.put("search", accounts.get(i).getSearch());
                    values.put("status_chat", accounts.get(i).getStatus_chat());
                    values.put("typingTo", accounts.get(i).getTypingTo());
                    values.put("verification_level", accounts.get(i).getVerification_level());
                    values.put("username", accounts.get(i).getUsername());
                    values.put("chat_id", accounts.get(i).getChat_id());

                    String where = "id=?";

                    getWritableDatabase().update(TABLE_CHAT_LIST, values, where, params);
                }else{
                    ContentValues values = new ContentValues();
                    values.put("account_id_cry", accounts.get(i).getAccount_id_cry());
                    values.put("id", accounts.get(i).getId());
                    values.put("imageURL", accounts.get(i).getImageURL());
                    values.put("last_seen", accounts.get(i).getLast_seen());
                    values.put("name_user", accounts.get(i).getName_user());
                    values.put("search", accounts.get(i).getSearch());
                    values.put("status_chat", accounts.get(i).getStatus_chat());
                    values.put("typingTo", accounts.get(i).getTypingTo());
                    values.put("verification_level", accounts.get(i).getVerification_level());
                    values.put("username", accounts.get(i).getUsername());
                    values.put("chat_id", accounts.get(i).getChat_id());

                    getWritableDatabase().insert(TABLE_CHAT_LIST, null, values);
                }
            }
        }

    }

    public void UPDATE_A_CHAT(DtoAccount account, int where_is_updating){
        String command = "SELECT * FROM " + TABLE_CHAT_LIST + " WHERE  id = ?";
        String[] params = {account.getId()};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);

        if(cursor.moveToFirst()){
            if(where_is_updating == 0){
                ContentValues values = new ContentValues();
                values.put("last_chat", account.getLast_chat());

                String where = "id=?";

                getWritableDatabase().update(TABLE_CHAT_LIST, values, where, params);
            }else{
                ContentValues values = new ContentValues();
                values.put("last_chat", account.getLast_chat());
                values.put("account_id_cry",account.getAccount_id_cry());
                values.put("id", account.getId());
                values.put("imageURL", account.getImageURL());
                values.put("last_seen", account.getLast_seen());
                values.put("name_user", account.getName_user());
                values.put("search", account.getSearch());
                values.put("status_chat", account.getStatus_chat());
                values.put("typingTo", account.getTypingTo());
                values.put("verification_level", account.getVerification_level());
                values.put("username", account.getUsername());

                String where = "id=?";

                getWritableDatabase().update(TABLE_CHAT_LIST, values, where, params);
            }
        }else{
            if(where_is_updating != 0){
                ContentValues values = new ContentValues();
                values.put("last_chat", account.getLast_chat());
                values.put("account_id_cry",account.getAccount_id_cry());
                values.put("id", account.getId());
                values.put("imageURL", account.getImageURL());
                values.put("last_seen", account.getLast_seen());
                values.put("name_user", account.getName_user());
                values.put("search", account.getSearch());
                values.put("status_chat", account.getStatus_chat());
                values.put("typingTo", account.getTypingTo());
                values.put("verification_level", account.getVerification_level());
                values.put("username", account.getUsername());
                values.put("chat_id", account.getChat_id());

                getWritableDatabase().insert(TABLE_CHAT_LIST, null, values);
            }else{
                ContentValues values = new ContentValues();
                values.put("last_chat", account.getLast_chat());
                values.put("id", account.getId());
                values.put("chat_id", account.getChat_id());

                getWritableDatabase().insert(TABLE_CHAT_LIST, null, values);
            }
        }
    }

    public List<DtoAccount> get_CHAT_LIST(){
        String command = "SELECT * FROM " + TABLE_CHAT_LIST + " ORDER BY last_chat DESC";
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, null);
        List<DtoAccount> accounts = new ArrayList<>();

        while (cursor.moveToNext()) {
            DtoAccount account = new DtoAccount();
            account.setAccount_id_cry(cursor.getString(0));
            account.setId(cursor.getString(1));
            account.setImageURL(cursor.getString(2));
            account.setLast_seen(cursor.getString(3));
            account.setName_user(cursor.getString(4));
            account.setSearch(cursor.getString(5));
            account.setStatus_chat(cursor.getString(6));
            account.setTypingTo(cursor.getString(7));
            account.setLast_chat(cursor.getString(8));
            account.setVerification_level(cursor.getString(9));
            account.setUsername(cursor.getString(10));
            account.setChat_id(cursor.getString(11));
            accounts.add(account);
        }
        return accounts;
    }

    public String get_BG(String chat_name){
        String command = "SELECT * FROM " + TABLE_BG + " WHERE  chat_name = ?";
        String[] params = {chat_name};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);
        ArrayList<DtoPost> dtoPosts = new ArrayList<>();

        if (cursor.moveToNext()) {
            return cursor.getString(1);
        }else return null;
    }

    public void DropTable(int type){
        SQLiteDatabase db = this.getWritableDatabase();
        if(type == 0){
            db.delete(TABLE_BG,null,null);
            Log.d("DB_CHAT", "Dropped BG");
        }else{
            db.delete(TABLE_CHAT_LIST,null,null);
            Log.d("DB_CHAT", "Dropped CHAT LIST");
        }
        //createTable(db);
    }

}
