package dev.kaua.squash.LocalDataBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import dev.kaua.squash.Data.Account.DtoAccount;

public class DaoFollowing extends SQLiteOpenHelper {
    private final String TABLE = "TBL_USER_FOLLOWING";


    public DaoFollowing(@Nullable Context context) {
        super(context, "DB_FOLLOWING", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        //  Create Table
        String command = "CREATE TABLE " + TABLE + "(" +
                "id_following bigint primary key," +
                "account_id bigint not null," +
                "account_id_following bigint not null)";

        db.execSQL(command);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < newVersion){
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);

            // Create tables again
            onCreate(db);
        }
    }

    public void Register_Followers_Following(ArrayList<DtoAccount> account){
        DropTable();
        for (int i = 0; i < account.size(); i++){
            ContentValues values = new ContentValues();
            values.put("id_following", account.get(i).getId_following());
            values.put("account_id", account.get(i).getAccount_id());
            values.put("account_id_following", account.get(i).getAccount_id_following());

            getWritableDatabase().insert(TABLE, null, values);
        }
    }

    public ArrayList<DtoAccount> get_followers_following(long account_id, long account_id_following){
        String command = "SELECT * FROM " + TABLE + " WHERE account_id = ? and account_id_following = ?";
        String[] params = {account_id + "", account_id_following + ""};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);
        ArrayList<DtoAccount> list = new ArrayList<>();

        while (cursor.moveToNext()) {
            DtoAccount account = new DtoAccount();
            account.setId_following(cursor.getString(0));
            account.setAccount_id(cursor.getInt(1));
            account.setAccount_id_following(cursor.getString(2));
            list.add(account);
        }
        return list;
    }

    public boolean check_if_follow(long account_id, long account_id_following){
        String command = "SELECT * FROM " + TABLE + " WHERE account_id = ? and account_id_following = ?";
        String[] params = {account_id + "", account_id_following + ""};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);
        return cursor.moveToNext();
    }

    public void DropTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE,null,null);
        //createTable(db);
    }

}
