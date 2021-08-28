package dev.kaua.squash.Data.Post.Actions;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import dev.kaua.squash.Adapters.Posts_Adapters;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.MainFragment;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.LocalDataBase.DaoPosts;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RecommendedPosts extends MainFragment {
    private static final String TAG = "RECOMMENDED_POSTS";

    private static Parcelable recyclerViewState;
    private static DaoFollowing daoFollowing;
    private static boolean loaded = false;
    private static DatabaseReference reference_posts;
    static ArrayList<DtoPost> arraylist_base = new ArrayList<>();
    public static final String BASE_POST_ID = "99999";

    //  Method to get RecommendedPosts
    public static void getFeedPosts(@NonNull Activity context, @NonNull RecyclerView recyclerView, ConstraintLayout loadingPanel){

        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(25);
        if(recyclerView.getLayoutManager() != null) recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        DaoPosts daoPosts = new DaoPosts(context);

        Posts_Adapters posts_adapters;
        ArrayList<DtoPost> listPostDB = daoPosts.get_post();
        posts_adapters = new Posts_Adapters(listPostDB, context, Posts_Adapters.CAN_ANIME);
        SetInRecycler(posts_adapters, recyclerView);

        //  Checking if user is connected to a network
       if(ConnectionHelper.isOnline(context)){
            daoFollowing = new DaoFollowing(context);
            reference_posts = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD);
            reference_posts.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                    if(!context.isDestroyed() && !context.isFinishing()){
                        arraylist_base.clear();
                        for(DataSnapshot snapshot: datasnapshot.getChildren()){
                            DtoPost post = snapshot.getValue(DtoPost.class);
                            if(post != null  && post.getActive() > DtoAccount.ACCOUNT_DISABLE){
                                if(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post.getAccount_id()))) == MyPrefs.getUserInformation(context).getAccount_id()||
                                        daoFollowing.check_if_follow(MyPrefs.getUserInformation(context).getAccount_id(),
                                                Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post.getAccount_id()))))){
                                    post.setPost_id(EncryptHelper.decrypt(post.getPost_id()));
                                    post.setAccount_id(EncryptHelper.decrypt(post.getAccount_id()));
                                    post.setVerification_level(EncryptHelper.decrypt(post.getVerification_level()));
                                    post.setName_user(EncryptHelper.decrypt(post.getName_user()));
                                    post.setUsername(EncryptHelper.decrypt(post.getUsername()));
                                    post.setProfile_image(EncryptHelper.decrypt(post.getProfile_image()));
                                    post.setPost_date(EncryptHelper.decrypt(post.getPost_date()));
                                    post.setPost_time(EncryptHelper.decrypt(post.getPost_time()));
                                    post.setPost_content(EncryptHelper.decrypt(post.getPost_content()));
                                    if(post.getPost_images() != null && post.getPost_images().size() != 0) post.setPost_images(post.getPost_images());
                                    else post.setPost_images(null);
                                    post.setPost_likes(EncryptHelper.decrypt(post.getPost_likes()));
                                    post.setPost_comments_amount(EncryptHelper.decrypt(post.getPost_comments_amount()));
                                    post.setPost_topic(EncryptHelper.decrypt(post.getPost_topic()));
                                    post.setSuggestion(false);
                                    arraylist_base.add(post);
                                }
                            }
                        }
                        loaded = true;
                        if(recyclerView.getLayoutManager() != null) recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
                        daoPosts.Register_Home_Posts(arraylist_base);
                        LoadPostsFromLocal(context, recyclerView, loadingPanel, daoPosts);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
            LoadPostsFromLocal(context, recyclerView, loadingPanel, daoPosts);

        }else ToastHelper.toast(context, context.getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
    }

    static final ArrayList<DtoPost> SaveList = new ArrayList<>();
    static int normal = 0;
    static int load = 0;
    private static void LoadPostsFromLocal(Activity mContext, RecyclerView recyclerView, ConstraintLayout loadingPanel, @NonNull DaoPosts daoPosts) {
        try{
            normal++;
            Log.d(TAG, "Normal -> " + normal);
            Posts_Adapters posts_adapters;
            ArrayList<DtoPost> listPostDB = daoPosts.get_post();
            if (listPostDB.size() > 0) {
                if(ConnectionHelper.isOnline(mContext) && loaded){
                    if(arraylist_base.size() != SaveList.size()){
                        SaveList.clear();
                        SaveList.addAll(arraylist_base);
                        load++;
                        Log.d(TAG, "Load -> " + load);
                        if(listPostDB.size() == 100 && arraylist_base.size() > 100) posts_adapters = new Posts_Adapters(arraylist_base, mContext, Posts_Adapters.CAN_ANIME);
                        else if(!arraylist_base.equals(listPostDB)) {
                            daoPosts.Register_Home_Posts(arraylist_base);
                            ArrayList<DtoPost> listLocal = daoPosts.get_post();
                            posts_adapters = new Posts_Adapters(listLocal, mContext, Posts_Adapters.CAN_ANIME);
                        }
                        else posts_adapters = new Posts_Adapters(listPostDB, mContext, Posts_Adapters.CAN_ANIME);

                        SetInRecycler(posts_adapters, recyclerView);
                    }
                }else {
                    posts_adapters = new Posts_Adapters(listPostDB, mContext, Posts_Adapters.CAN_ANIME);
                    SetInRecycler(posts_adapters, recyclerView);
                }
                loadingPanel.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.GONE);
                loadingPanel.setVisibility(View.VISIBLE);
            }
        }catch (Exception ex){
            Log.d(TAG, "Load Feed -> " + ex.toString());
            Warnings.showWeHaveAProblem(mContext, ErrorHelper.LOAD_FEED_POSTS);
        }
    }

    private static void SetInRecycler(Posts_Adapters posts_adapters, RecyclerView recyclerView) {
        recyclerView.setAdapter(posts_adapters);
        recyclerView.getRecycledViewPool().clear();
        if(recyclerViewState != null && recyclerView.getLayoutManager() != null) recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    //  Method to get User Posts
    public static void getUsersPosts(Activity context, @NonNull RecyclerView recyclerView,
                                     RelativeLayout noPost_profile, TextView posts_size, DtoAccount account){
        if(recyclerView.getLayoutManager() != null) recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        ArrayList<DtoPost> arraylist = new ArrayList<>();
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);

        //  Checking if user is connected to a network
        if(ConnectionHelper.isOnline(context)){
            reference_posts = null;
            reference_posts = myFirebaseHelper.getFirebaseDatabase().getReference();
            Query applesQuery = reference_posts.child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD).orderByChild("account_id")
                    .equalTo(EncryptHelper.encrypt(String.valueOf(account.getAccount_id())));
            applesQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                    if(!context.isDestroyed() && !context.isFinishing()){
                        arraylist.clear();
                        for(DataSnapshot snapshot: datasnapshot.getChildren()){
                            DtoPost dtoPost = snapshot.getValue(DtoPost.class);
                            DtoPost post = new DtoPost();
                            if(dtoPost != null && dtoPost.getAccount_id() != null && dtoPost.getActive() > DtoAccount.ACCOUNT_DISABLE){
                                post.setPost_id(EncryptHelper.decrypt(dtoPost.getPost_id()));
                                post.setAccount_id(EncryptHelper.decrypt(dtoPost.getAccount_id()));
                                post.setVerification_level(EncryptHelper.decrypt(dtoPost.getVerification_level()));
                                post.setName_user(EncryptHelper.decrypt(dtoPost.getName_user()));
                                post.setUsername(EncryptHelper.decrypt(dtoPost.getUsername()));
                                post.setProfile_image(EncryptHelper.decrypt(dtoPost.getProfile_image()));
                                post.setPost_date(EncryptHelper.decrypt(dtoPost.getPost_date()));
                                post.setPost_time(EncryptHelper.decrypt(dtoPost.getPost_time()));
                                post.setPost_content(EncryptHelper.decrypt(dtoPost.getPost_content()));
                                if(dtoPost.getPost_images() != null && dtoPost.getPost_images().size() != 0) post.setPost_images(dtoPost.getPost_images());
                                else post.setPost_images(null);
                                post.setPost_likes(EncryptHelper.decrypt(dtoPost.getPost_likes()));
                                post.setPost_comments_amount(EncryptHelper.decrypt(dtoPost.getPost_comments_amount()));
                                post.setPost_topic(EncryptHelper.decrypt(dtoPost.getPost_topic()));
                                post.setSuggestion(false);
                                arraylist.add(post);
                            }
                        }
                        arraylist.sort(Collections.reverseOrder());

                        if(arraylist.size() == 0){
                            noPost_profile.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                        else{
                            Posts_Adapters posts_adapters = new Posts_Adapters(arraylist, context, Posts_Adapters.CAN_ANIME);
                            recyclerView.setAdapter(posts_adapters);
                            recyclerView.getRecycledViewPool().clear();
                            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                            noPost_profile.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        posts_size.setText(Methods.NumberTrick(arraylist.size()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }else ToastHelper.toast(context, context.getString(R.string.you_are_without_internet), 0);
    }
}