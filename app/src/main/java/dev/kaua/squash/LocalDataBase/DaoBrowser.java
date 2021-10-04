package dev.kaua.squash.LocalDataBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.net.URL;
import java.util.ArrayList;

import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.System.DtoSystem;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Tools.MyPrefs;

public class DaoBrowser extends SQLiteOpenHelper {
    private final String TABLE = "TBL_BROWSER";

    public DaoBrowser(@Nullable Context context) {
        super(context, "DB_BROWSER", null, 8);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        //  Create Table
        String command = "CREATE TABLE " + TABLE + "(" +
                "id bigint," +
                "title String," +
                "link String," +
                "link_display String," +
                "date_time String," +
                "webSite_Image String)";

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

    public void InsertLink(DtoSystem info, Context context){
        try {
            if(MyPrefs.getUserInformation(context).getAccount_id() > DtoAccount.ACCOUNT_DISABLE
                    && myFirebaseHelper.getFirebaseAuth().getUid() != null)
            if(info.getTitle() != null && info.getDate_time() != null && info.getLink() != null
            && info.getLink().length() > 5){
                final ContentValues values = new ContentValues();
                values.put("id", (getLinks().size() +1));
                values.put("title", info.getTitle());
                values.put("link", info.getLink());
                URL url = new URL(info.getLink());
                values.put("link_display", url.getHost().replace("www.", ""));
                values.put("date_time", info.getDate_time());
                values.put("webSite_Image", info.getWebSite_Image());
                getWritableDatabase().insert(TABLE, null, values);
            }else{
                if(info.getTitle() != null && info.getTitle().equals(DtoSystem.Squash_Privacy_Policy)){
                    final ContentValues values = new ContentValues();
                    values.put("title", info.getTitle());
                    getWritableDatabase().insert(TABLE, null, values);
                }
            }
        }catch (Exception ignore){}
    }

    public ArrayList<DtoSystem> getLinks(){
        String command = "SELECT * FROM " + TABLE + " ORDER BY id desc";
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, null);
        ArrayList<DtoSystem> list = new ArrayList<>();

        while (cursor.moveToNext()) {
            DtoSystem link = new DtoSystem();
            link.setTitle(cursor.getString(1));
            link.setLink(cursor.getString(2));
            link.setLink_display(cursor.getString(3));
            link.setDate_time(cursor.getString(4));
            link.setWebSite_Image(cursor.getString(5));
            list.add(link);
        }
        return list;
    }

    public void DropTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE,null,null);
        //createTable(db);
    }

}
