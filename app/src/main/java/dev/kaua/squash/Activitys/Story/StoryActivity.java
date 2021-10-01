package dev.kaua.squash.Activitys.Story;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Stories.DtoStory;
import dev.kaua.squash.Data.Stories.StoryHelper;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {
    private static final String TAG = "StoryDeBug";
    public static final String USER_ID_TAG = "userId_tag";
    public static final String USERNAME_TAG = "username_tag";
    public static final String USER_PHOTO_TAG = "user_photo_tag";
    public static final String UPLOAD_TIME_TAG = "uploadTime_tag";
    public static final String USER_LEVEL_TAG = "user_level_tag";

    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;
    final long STORY_DURATION = 5000L;

    StoriesProgressView storiesProgressView;
    ImageView image;
    CircleImageView story_photo;
    TextView story_username;
    LinearLayout r_seen;
    TextView seen_number, story_uploadTime;
    ImageView story_delete;
    LoadingDialog loadingDialog;
    ProgressBar loading_story;
    ImageView ic_account_badge_story;

    List<String> images;
    List<String> storiesId;
    List<String> storiesUploadTime;
    String userId;

    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    final long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        getWindow().setStatusBarColor(getColor(R.color.black));
        getWindow().setNavigationBarColor(getColor(R.color.black));

        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.story_image);
        story_photo = findViewById(R.id.story_photo_view);
        story_username = findViewById(R.id.story_username);
        r_seen = findViewById(R.id.r_seen);
        seen_number = findViewById(R.id.seen_number_story);
        story_delete = findViewById(R.id.story_delete);
        loading_story = findViewById(R.id.loading_story);
        story_uploadTime = findViewById(R.id.story_uploadTime);
        ic_account_badge_story = findViewById(R.id.ic_account_badge_story);

        userId = getIntent().getStringExtra(USER_ID_TAG);

        story_uploadTime.setText(Methods.loadLastSeen(this, getIntent().getStringExtra(UPLOAD_TIME_TAG)));

        if(userId.equals(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id()))){
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }

        r_seen.setOnClickListener(v -> startActivity(new Intent(this, StoryViewsActivity.class)));

        story_delete.setOnClickListener( v -> {
            storiesProgressView.pause();
            final AlertDialog.Builder alert = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete_story))
                    .setMessage(getString(R.string.do_you_really_want_to_delete_this_story_desc))
                    .setNeutralButton(getString(R.string.delete_story), (dialog, which) -> DeleteStory())
                    .setPositiveButton(getString(R.string.no), (dialog, which) -> {
                        dialog.dismiss();
                        storiesProgressView.resume();
                    })
                    .setCancelable(false);

            final Dialog mDialog = alert.create();
            mDialog.getWindow().getAttributes().windowAnimations = R.style.MyAlertDialogStyle;
            mDialog.show();
        });

        getStories(userId);

        final View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(v -> {
            storiesProgressView.reverse();
        });
        reverse.setOnTouchListener(onTouchListener);

        final View skip = findViewById(R.id.skip);
        skip.setOnClickListener(v -> {
            storiesProgressView.skip();
        });
        skip.setOnTouchListener(onTouchListener);

        userInfo();
    }

    void DeleteStory(){
        loadingDialog = new LoadingDialog(this);
            loadingDialog.startLoading();
            myFirebaseHelper.getFirebaseDatabase()
                    .getReference(myFirebaseHelper.STORY_REFERENCE).child(userId).child(storiesId.get(counter))
                    .removeValue().addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();
                    if(task.isSuccessful()){
                        StorageReference photoRef = myFirebaseHelper.getFirebaseStorageInstance().getReferenceFromUrl(images.get(counter));
                        photoRef.delete().addOnSuccessListener(aVoid -> {
                            // File deleted successfully
                            Log.d(TAG, "onSuccess: deleted file");
                        }).addOnFailureListener(exception -> {
                            // Uh-oh, an error occurred!
                            Log.d(TAG, "onFailure: did not delete file");
                        });
                        ToastHelper.toast(StoryActivity.this, getString(R.string.deleted), ToastHelper.SHORT_DURATION);
                        finish();
                    }
            });
    }

    @Override
    public void onNext() {
        LoadStory(images.get(++counter));
        addView(storiesId.get(counter));
        seenNumber(storiesId.get(counter));
    }

    @Override
    public void onPrev() {
        if((counter - 1) >= 0) {
            LoadStory(images.get(--counter));
            seenNumber(storiesId.get(counter));
        }
    }

    @Override
    public void onComplete() {
        finish();
        this.overridePendingTransition(R.anim.nothing, R.anim.bottom_down);
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    void LoadStory(final String imageUrl){
        ShowLoad(true);
        Glide.with(getApplicationContext()).load(imageUrl).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable final GlideException e,
                                        final Object model, final Target<Drawable> target,
                                        final boolean isFirstResource) {
                ShowLoad(false);

                return false;
            }
            @Override
            public boolean onResourceReady(final Drawable resource,
                                           final Object model,
                                           final Target<Drawable> target,
                                           final DataSource dataSource,
                                           final boolean isFirstResource) {
                ShowLoad(false);
                return false;
            }
        }).into(image);

        story_uploadTime.setText(Methods.loadLastSeen(StoryActivity.this, storiesUploadTime.get(counter)));
    }

    void ShowLoad(boolean show){
        if(show){
            loading_story.setVisibility(View.VISIBLE);
            image.setVisibility(View.GONE);
        }else{
            loading_story.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
        }
    }

    void getStories(String userId){
        images = new ArrayList<>();
        storiesId = new ArrayList<>();
        storiesUploadTime = new ArrayList<>();
        if(ConnectionHelper.isOnline(this)){
            loading_story.setVisibility(View.VISIBLE);
            image.setVisibility(View.GONE);
            myFirebaseHelper.getFirebaseDatabase()
                    .getReference(myFirebaseHelper.STORY_REFERENCE).child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!isFinishing() && !isDestroyed()){
                                images.clear();
                                storiesId.clear();
                                storiesUploadTime.clear();
                                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                                    final DtoStory story = snapshot.getValue(DtoStory.class);
                                    if(story != null){
                                        final long timeCurrent = System.currentTimeMillis();
                                        if(timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd()){
                                            images.add(EncryptHelper.decrypt(story.getImageUrl()));
                                            storiesId.add(story.getStoryId());
                                            storiesUploadTime.add(story.getUploadTime());
                                        }
                                    }
                                }

                                storiesProgressView.setStoriesCount(images.size());
                                storiesProgressView.setStoryDuration(STORY_DURATION);
                                storiesProgressView.setStoriesListener(StoryActivity.this);
                                storiesProgressView.startStories(counter);
                                LoadStory(images.get(counter));
                                loading_story.setVisibility(View.GONE);
                                image.setVisibility(View.VISIBLE);

                                addView(storiesId.get(counter));
                                seenNumber(storiesId.get(counter));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }else{
            ToastHelper.toast(this, getString(R.string.no_internet_connection), ToastHelper.SHORT_DURATION);
            finish();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void userInfo(){
        String username = getIntent().getStringExtra(USERNAME_TAG);
        if(username != null){
            if(username.equals(MyPrefs.getUserInformation(this).getUsername())) username = getString(R.string.your_story);

            if(username.length() > 25) username = username.substring(0, 25) + "...";
            story_username.setText(username);
            if(getIntent().getStringExtra(USER_PHOTO_TAG) != null)
                Glide.with(getApplicationContext()).load(getIntent().getStringExtra(USER_PHOTO_TAG)).into(story_photo);
        }

        final long level = Methods.parseUserLevel(getIntent().getStringExtra(USER_LEVEL_TAG));
        if(level > DtoAccount.NORMAL_ACCOUNT && !userId.equals(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id()))){
            ic_account_badge_story.setImageDrawable(getDrawable(Methods.loadUserImageLevel(level)));
            ic_account_badge_story.setVisibility(View.VISIBLE);
        }else ic_account_badge_story.setVisibility(View.GONE);
    }

    private void addView(String storyId){
        if(!userId.equals(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id()))
        && ConnectionHelper.isOnline(this)){
            myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.STORY_REFERENCE)
                    .child(userId).child(storyId).child(myFirebaseHelper.STORY_VIEWS)
                    .child(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id())).setValue(true);
        }
    }

    void seenNumber(final String storyId){
        if(userId.equals(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id()))){
            if(ConnectionHelper.isOnline(this)){
                myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.STORY_REFERENCE)
                        .child(userId).child(storyId).child(myFirebaseHelper.STORY_VIEWS)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(!isDestroyed() && !isFinishing()){
                                    StoryHelper.getStoryViewsList().clear();
                                    for(DataSnapshot view: snapshot.getChildren()){
                                        if(view != null) StoryHelper.getStoryViewsList().add(view.getKey());
                                    }
                                    seen_number.setText(Methods.NumberTrick(snapshot.getChildrenCount()));
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            }
        }
    }
}