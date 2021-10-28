package dev.kaua.squash.LocalDataBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DaoSystem extends SQLiteOpenHelper {
    private final String TABLE = "TBL_SYSTEM_INFO";

    public DaoSystem(@Nullable Context context) {
        super(context, "DB_SYSTEM_INFO", null, 18);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        //  Create Table
        String command = "CREATE TABLE " + TABLE + "(" +
                "id_system bigint," +
                "PrivacyPolicy bigint," +
                "need_reset String not null)";

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

    public boolean getNeedResetAccount(){
        String command = "SELECT * FROM " + TABLE + " WHERE id_system = 1";
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, null);
        return cursor.moveToNext();
    }

    public void setNeedResetAccount(String info){
        String command = "SELECT * FROM " + TABLE + " WHERE id_system = 1";
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, null);
        if(cursor.moveToNext()){
            ContentValues values = new ContentValues();
            values.put("PrivacyPolicy", cursor.getLong(1));
            getWritableDatabase().update(TABLE, values, "id_system=?", new String[]{"1"});
        }else{
            ContentValues values = new ContentValues();
            values.put("id_system", 1);
            values.put("PrivacyPolicy", 0);
            values.put("need_reset", info);
            getWritableDatabase().insert(TABLE, null, values);
        }
    }

    public void DropTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE,null,null);
        //createTable(db);
    }

}
