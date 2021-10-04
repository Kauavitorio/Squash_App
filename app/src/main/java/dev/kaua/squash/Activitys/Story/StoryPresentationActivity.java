package dev.kaua.squash.Activitys.Story;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.MyPrefs;
import jp.wasabeef.blurry.Blurry;

public class StoryPresentationActivity extends AppCompatActivity {
    private static String userId;
    private static String uploadTime;
    private static String userName;
    private static String userPhoto;
    private static String userLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_presentation);
        getWindow().setStatusBarColor(getColor(R.color.black));
        getWindow().setNavigationBarColor(getColor(R.color.black));

        userId = getIntent().getStringExtra(StoryActivity.USER_ID_TAG);
        uploadTime = getIntent().getStringExtra(StoryActivity.UPLOAD_TIME_TAG);
        userName = getIntent().getStringExtra(StoryActivity.USERNAME_TAG);
        userPhoto = getIntent().getStringExtra(StoryActivity.USER_PHOTO_TAG);
        userLevel = getIntent().getStringExtra(StoryActivity.USER_LEVEL_TAG);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        findViewById(R.id.tap_here_to_keep_watching).setOnClickListener(v -> ApplyStoryTutorial());

        LoadBackground();
    }

    void LoadBackground(){
        LoadDefaultBackground();

        Glide.with(this)
                .asBitmap()
                .placeholder(R.drawable.background_intro)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .load(getIntent().getStringExtra(StoryActivity.FIRST_STORY))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Blurry.with(StoryPresentationActivity.this).radius(10).sampling(8)
                                .color(Color.argb(66, 5, 5, 5))
                                .from(resource).into((ImageView) findViewById(R.id.story_presentation));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    void LoadDefaultBackground(){
        Glide.with(this).asBitmap().load(R.drawable.background_intro)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Blurry.with(StoryPresentationActivity.this).radius(10).sampling(8)
                                .color(Color.argb(66, 5, 5, 5))
                                .from(resource).into((ImageView) findViewById(R.id.story_presentation));
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    void ApplyStoryTutorial(){
        if(ConnectionHelper.isOnline(this)){
            myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.SYSTEM_REFERENCE)
                    .child(myFirebaseHelper.STORY_TUTORIAL_REFERENCE)
                    .child(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id())).setValue(true);

            final Intent i = new Intent(this, StoryActivity.class);
            i.putExtra(StoryActivity.USER_ID_TAG, userId);
            i.putExtra(StoryActivity.UPLOAD_TIME_TAG, uploadTime);
            i.putExtra(StoryActivity.USERNAME_TAG, userName);
            i.putExtra(StoryActivity.USER_PHOTO_TAG, userPhoto);
            i.putExtra(StoryActivity.USER_LEVEL_TAG, userLevel);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ApplyStoryTutorial();
    }
}