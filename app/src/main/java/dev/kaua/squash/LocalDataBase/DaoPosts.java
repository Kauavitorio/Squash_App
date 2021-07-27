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
import java.util.Collections;
import java.util.Objects;

import dev.kaua.squash.Data.Post.DtoPost;

public class DaoPosts extends SQLiteOpenHelper {
    private final String TABLE_POSTS = "TBL_POSTS";
    private final String TABLE_LIKES = "TBL_POSTS_LIKES";
    private final String TABLE_LIKES_COMMENTS = "TBL_POSTS_LIKES_COMMENTS";
    private final String TABLE_POST_IMAGE = "tbl_posts_image";


    public DaoPosts(@Nullable Context context) {
        super(context, "DB_POSTS", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        //  Create Table
        String command = "CREATE TABLE " + TABLE_POSTS + "(" +
                "post_id bigint primary key," +
                "account_id bigint not null," +
                "verification_level varchar(500) not null," +
                "name_user varchar(800) not null," +
                "username varchar(800) not null," +
                "profile_image varchar(800) default null," +
                "post_date varchar(500) not null," +
                "post_time varchar(500) not null," +
                "post_content varchar(2000) not null," +
                "post_likes varchar(400) not null," +
                "post_images varchar(400)," +
                "post_comments_amount varchar(400) not null," +
                "post_topic varchar(500) not null)";

        db.execSQL(command);

        String command_likes = "CREATE TABLE " + TABLE_LIKES + "(" +
                "post_id bigint not null," +
                "account_id bigint not null)";

        db.execSQL(command_likes);

        String command_images = "CREATE TABLE " + TABLE_POST_IMAGE + "(" +
                "post_id bigint not null," +
                "account_id bigint not null," +
                "image_link varchar(600) not null)";

        db.execSQL(command_images);

        String command_likes_comments = "CREATE TABLE " + TABLE_LIKES_COMMENTS + "(" +
                "comment_id bigint not null," +
                "account_id bigint not null)";

        db.execSQL(command_likes_comments);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < newVersion){
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIKES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_POST_IMAGE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIKES_COMMENTS);

            // Create tables again
            onCreate(db);
        }
    }

    public void Register_Home_Posts(ArrayList<DtoPost> post){
        //DropTable();
        int size = Math.min(post.size(), 50);
        for (int i = 0; i< size; i++){
            ContentValues values = new ContentValues();
            values.put("post_id", Long.parseLong(Objects.requireNonNull(post.get(i).getPost_id())));
            values.put("account_id", Long.parseLong(Objects.requireNonNull(post.get(i).getAccount_id())));
            values.put("verification_level", post.get(i).getVerification_level());
            values.put("name_user", post.get(i).getName_user());
            values.put("username", post.get(i).getUsername());
            values.put("profile_image", post.get(i).getProfile_image());
            values.put("post_date", post.get(i).getPost_date());
            values.put("post_time", post.get(i).getPost_time());
            values.put("post_content", post.get(i).getPost_content());
            values.put("post_likes", post.get(i).getPost_likes());
            if(post.get(i).getPost_images() != null && post.get(i).getPost_images().size() > 0) {
                long post_id = Long.parseLong(Objects.requireNonNull(post.get(i).getPost_id()));
                long account_id = Long.parseLong(Objects.requireNonNull(post.get(i).getAccount_id()));
                for (int img = 0; img < post.get(i).getPost_images().size(); img++){
                    if(post.get(i).getPost_images().get(img) != null){
                        ContentValues values_images = new ContentValues();
                        values_images.put("post_id", post_id);
                        values_images.put("image_link", post.get(i).getPost_images().get(img));
                        values_images.put("account_id", account_id);
                        getWritableDatabase().insert(TABLE_POST_IMAGE, null, values_images);
                    }
                }
            }
            else values.put("post_images", "NaN");
            values.put("post_comments_amount", post.get(i).getPost_comments_amount());
            values.put("post_topic", post.get(i).getPost_topic());
            Log.d("InsertPost", post.get(i).getName_user());

            getWritableDatabase().insert(TABLE_POSTS, null, values);
        }
    }

    public void Register_Likes(ArrayList<DtoPost> post){
        DropTable(1);
        if(post != null && post.size() > 0)
        for (int i = 0; i< post.size(); i++){
            ContentValues values = new ContentValues();
            values.put("post_id", Long.parseLong(Objects.requireNonNull(post.get(i).getPost_id())));
            values.put("account_id", Long.parseLong(Objects.requireNonNull(post.get(i).getAccount_id())));

            getWritableDatabase().insert(TABLE_LIKES, null, values);
        }
    }

    public ArrayList<DtoPost> get_post(long account_id){
        String command = "SELECT * FROM " + TABLE_POSTS + " WHERE account_id > ? ORDER BY post_id DESC";
        String[] params = {account_id + ""};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);
        ArrayList<DtoPost> dtoPosts = new ArrayList<>();

        while (cursor.moveToNext()) {
            DtoPost post = new DtoPost();
            post.setPost_id(cursor.getString(0));
            post.setAccount_id(cursor.getString(1));
            post.setVerification_level(cursor.getString(2));
            post.setName_user(cursor.getString(3));
            post.setUsername(cursor.getString(4));
            post.setProfile_image(cursor.getString(5));
            post.setPost_date(cursor.getString(6));
            post.setPost_time(cursor.getString(7));
            post.setPost_content(cursor.getString(8));
            post.setPost_likes(cursor.getString(9));
            post.setPost_images(Collections.singletonList(cursor.getString(10)));
            post.setPost_comments_amount(cursor.getString(11));
            post.setPost_topic(cursor.getString(12));
            dtoPosts.add(post);
        }
        return dtoPosts;
    }

    public DtoPost get_post_img(long post_id){
        String command = "SELECT * FROM " + TABLE_POST_IMAGE + " WHERE post_id = ?";
        String[] params = {post_id + ""};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);
        DtoPost post = new DtoPost();

        ArrayList<String> img_list = new ArrayList<>();

        while (cursor.moveToNext()) {
            post.setPost_id(cursor.getString(0));
            post.setAccount_id(cursor.getString(1));
            img_list.add(cursor.getString(2));
        }
        post.setPost_images(img_list);
        return post;
    }

    public boolean get_A_Like(long post_id, long account_id){
        String command = "SELECT * FROM " + TABLE_LIKES + " WHERE  post_id = ? and account_id = ?";
        String[] params = {String.valueOf(post_id), String.valueOf(account_id)};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);
        return cursor.moveToFirst();
    }
    public void Register_Likes_Comments(ArrayList<DtoPost> post){
        DropTable(2);
        if(post != null && post.size() > 0)
            for (int i = 0; i< post.size(); i++){
                ContentValues values = new ContentValues();
                values.put("comment_id", Long.parseLong(Objects.requireNonNull(post.get(i).getComment_id())));
                values.put("account_id", Long.parseLong(Objects.requireNonNull(post.get(i).getAccount_id())));
                getWritableDatabase().insert(TABLE_LIKES_COMMENTS, null, values);
            }
    }

    public boolean get_A_Like_comment(long comment_id, long account_id){
        String command = "SELECT * FROM " + TABLE_LIKES_COMMENTS + " WHERE  comment_id = ? and account_id = ?";
        String[] params = {String.valueOf(comment_id), String.valueOf(account_id)};
        @SuppressLint("Recycle") Cursor cursor = getWritableDatabase().rawQuery(command, params);
        return cursor.moveToFirst();
    }

    public void DropTable(int type){
        SQLiteDatabase db = this.getWritableDatabase();
        if(type == 0){
            db.delete(TABLE_POSTS,null,null);
            db.delete(TABLE_POST_IMAGE,null,null);
        }
        else if(type == 1)
            db.delete(TABLE_LIKES,null,null);
        else
            db.delete(TABLE_LIKES_COMMENTS,null,null);
        Log.d("InsertPost", "Dropped");
        //createTable(db);
    }

}
