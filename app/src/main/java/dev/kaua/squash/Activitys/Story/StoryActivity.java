package dev.kaua.squash.Activitys.Story;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.Setting.FollowAndInvite.FollowAndInviteActivity;
import dev.kaua.squash.Data.Stories.DtoStory;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
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

    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    StoriesProgressView storiesProgressView;
    ImageView image;
    CircleImageView story_photo;
    TextView story_username;
    LinearLayout r_seen;
    TextView seen_number, story_uploadTime;
    ImageView story_delete;
    LoadingDialog loadingDialog;
    ProgressBar loading_story;

    List<String> images;
    List<String> storiesId;
    String userId;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
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

        userId = getIntent().getStringExtra(USER_ID_TAG);

        story_uploadTime.setText(Methods.loadLastSeen(this, getIntent().getStringExtra(UPLOAD_TIME_TAG)));

        if(userId.equals(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id()))){
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }

        r_seen.setOnClickListener(v -> {
            final Intent i = new Intent(this, FollowAndInviteActivity.class);
            i.putExtra("id", userId);
            i.putExtra("storyId", storiesId.get(counter));
            i.putExtra("title", userId);
            startActivity(i);
        });

        story_delete.setOnClickListener( v -> {
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

    @Override
    public void onNext() {
        Glide.with(getApplicationContext()).load(images.get(++counter)).into(image);
        addView(storiesId.get(counter));
        seenNumber(storiesId.get(counter));
    }

    @Override
    public void onPrev() {
        if((counter - 1) >= 0) {
            Glide.with(getApplicationContext()).load(images.get(--counter)).into(image);
            seenNumber(storiesId.get(counter));
        }
    }

    @Override
    public void onComplete() {
        finish();
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

    private void getStories(String userId){
        images = new ArrayList<>();
        storiesId = new ArrayList<>();
        loading_story.setVisibility(View.VISIBLE);
        image.setVisibility(View.GONE);
        final DatabaseReference reference = myFirebaseHelper.getFirebaseDatabase()
                .getReference(myFirebaseHelper.STORY_REFERENCE).child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                images.clear();
                storiesId.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    final DtoStory story = snapshot.getValue(DtoStory.class);
                    if(story != null){
                        final long timeCurrent = System.currentTimeMillis();
                        if(timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd()){
                            images.add(EncryptHelper.decrypt(story.getImageUrl()));
                            storiesId.add(story.getStoryId());
                        }
                    }
                }

                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);
                Glide.with(getApplicationContext()).load(images.get(counter)).into(image);
                loading_story.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);

                addView(storiesId.get(counter));
                seenNumber(storiesId.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userInfo(){
        story_username.setText(getIntent().getStringExtra(USERNAME_TAG));
        if(getIntent().getStringExtra(USER_PHOTO_TAG) != null)
        Glide.with(getApplicationContext()).load(getIntent().getStringExtra(USER_PHOTO_TAG)).into(story_photo);
    }

    private void addView(String storyId){
        myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.STORY_REFERENCE)
                .child(userId).child(storyId).child(myFirebaseHelper.STORY_VIEWS)
                .child(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id())).setValue(true);
    }

    void seenNumber(String storyId){
        myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.STORY_REFERENCE)
                .child(userId).child(storyId).child(myFirebaseHelper.STORY_VIEWS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        seen_number.setText(Methods.NumberTrick(snapshot.getChildrenCount()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}