package dev.kaua.squash.LocalDataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.Objects;

import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Security.EncryptHelper;

public class DaoAccount extends SQLiteOpenHelper {
    private final String TABLE = "TBL_ACCOUNT";


    public DaoAccount(@Nullable Context context) {
        super(context, "DB_ACCOUNT", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        //  Create Table
        String command = "CREATE TABLE " + TABLE + "(" +
                "id bigint primary key," +
                "account_id bigint not null," +
                "following bigint not null," +
                "followers bigint not null)";

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

    public long Register_Followers_Following(DtoAccount account){
        DropTable();
        ContentValues values = new ContentValues();
        values.put("account_id", account.getAccount_id());
        values.put("following", Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(account.getFollowing()))));
        values.put("followers", Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(account.getFollowers()))));

        return getWritableDatabase().insert(TABLE, null, values);
    }

    public DtoAccount get_followers_following(long account_id){
        String command = "SELECT * FROM " + TABLE + " WHERE account_id = ?";
        String[] params = {account_id + ""};
        Cursor cursor = getWritableDatabase().rawQuery(command, params);
        DtoAccount account = new DtoAccount();

        while (cursor.moveToNext()) {
            account.setAccount_id_cry(cursor.getString(1));
            account.setFollowing(cursor.getString(2) + "");
            account.setFollowers(cursor.getString(3) + "");
        }
        return account;
    }

    public void DropTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE,null,null);
        //createTable(db);
    }

}
