package dev.kaua.squash.Activitys.Story;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.kaua.squash.Adapters.User.Followers_FollowingAdapter;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Stories.StoryHelper;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;

@SuppressLint("StaticFieldLeak")
public class StoryViewsActivity extends AppCompatActivity {
    RecyclerView recycler_views_stories;
    TextView seen_number, txt_no_views_yet;
    ProgressBar loading_bar;
    List<DtoAccount> mAccounts = new ArrayList<>();
    private static Followers_FollowingAdapter userChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_views);
        Ids();

        seen_number.setText(Methods.NumberTrick(StoryHelper.getStoryViewsList().size()));
        if(StoryHelper.getStoryViewsList().size() > 0){
            if(ConnectionHelper.isOnline(this)){
                loading_bar.setVisibility(View.VISIBLE);
                myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                                mAccounts.clear();
                                for (DataSnapshot snapshot: datasnapshot.getChildren()){
                                    if(snapshot != null){
                                        final DtoAccount account = snapshot.getValue(DtoAccount.class);
                                        if(account != null){
                                            final String account_id = EncryptHelper.decrypt(account.getAccount_id_cry());
                                            if(account_id != null){
                                                if(StoryHelper.getStoryViewsList().contains(account_id)){
                                                    final DtoAccount acc = new DtoAccount();
                                                    acc.setAccount_id(Long.parseLong(account_id));
                                                    acc.setName_user(EncryptHelper.encrypt(account.getName_user()));
                                                    acc.setUsername(EncryptHelper.encrypt(account.getUsername()));
                                                    acc.setVerify(1);
                                                    acc.setVerification_level(account.getVerification_level());
                                                    if(!account.getImageURL().equals(DtoAccount.DEFAULT))
                                                        acc.setProfile_image(EncryptHelper.encrypt(account.getImageURL()));
                                                    else
                                                        acc.setProfile_image(DtoAccount.DEFAULT);
                                                    mAccounts.add(acc);
                                                }
                                            }
                                        }
                                    }
                                }
                                loading_bar.setVisibility(View.GONE);
                                userChatAdapter = new Followers_FollowingAdapter(StoryViewsActivity.this, mAccounts);
                                recycler_views_stories.setAdapter(userChatAdapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }else{
                finish();
                ToastHelper.toast(this, getString(R.string.no_internet_connection), ToastHelper.SHORT_DURATION);
            }
        }else {
            txt_no_views_yet.setVisibility(View.VISIBLE);
            recycler_views_stories.setVisibility(View.GONE);
        }
    }

    void Ids(){
        getWindow().setStatusBarColor(getColor(R.color.background_menu_sheet));
        recycler_views_stories = findViewById(R.id.recycler_views_stories);
        seen_number = findViewById(R.id.seen_number_story_views);
        txt_no_views_yet = findViewById(R.id.txt_no_views_yet_story);
        loading_bar = findViewById(R.id.loading_bar_views_stories);
        recycler_views_stories.setLayoutManager(new LinearLayoutManager(this));
    }
}