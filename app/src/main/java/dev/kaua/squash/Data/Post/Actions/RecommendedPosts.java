package dev.kaua.squash.Data.Post.Actions;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
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
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RecommendedPosts extends MainFragment {

    private static Parcelable recyclerViewState;
    private static DaoFollowing daoFollowing;
    private static DatabaseReference reference_posts;
    static ArrayList<DtoPost> arraylist_base = new ArrayList<>();
    public static final String BASE_POST_ID = "99999";

    //  Method to get RecommendedPosts
    public static void getFeedPosts(@NonNull Context context, @NonNull RecyclerView recyclerView, ConstraintLayout loadingPanel){

        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(25);
        if(recyclerView.getLayoutManager() != null) recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        DaoPosts daoPosts = new DaoPosts(context);

        LoadPostsFromLocal(context, recyclerView, loadingPanel, daoPosts);

        //  Checking if user is connected to a network
        if(ConnectionHelper.isOnline(context)){
            daoFollowing = new DaoFollowing(context);
            reference_posts = myFirebaseHelper.getFirebaseDatabase().getReference("Posts").child("Published");
            reference_posts.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                    daoPosts.DropTable(0);
                    arraylist_base.clear();
                    for(DataSnapshot snapshot: datasnapshot.getChildren()){
                        DtoPost post = snapshot.getValue(DtoPost.class);
                        if(post != null){
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
                    if(recyclerView.getLayoutManager() != null) recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
                    daoPosts.Register_Home_Posts(arraylist_base);
                    LoadPostsFromLocal(context, recyclerView, loadingPanel, daoPosts);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
            LoadPostsFromLocal(context, recyclerView, loadingPanel, daoPosts);

        }else ToastHelper.toast((Activity)context , context.getString(R.string.you_are_without_internet), 0);
    }

    private static void LoadPostsFromLocal(Context context, RecyclerView recyclerView, ConstraintLayout loadingPanel, @NonNull DaoPosts daoPosts) {
        ArrayList<DtoPost> listPostDB = daoPosts.get_post(0);
        if (listPostDB.size() > 0) {
            Posts_Adapters posts_adapters;
            if(ConnectionHelper.isOnline(context)){
                if(listPostDB.size() == 100 && arraylist_base.size() > 100) posts_adapters = new Posts_Adapters(arraylist_base, context);
                else
                if(!arraylist_base.equals(listPostDB)) {
                    daoPosts.DropTable(0);
                    daoPosts.Register_Home_Posts(arraylist_base);
                    posts_adapters = new Posts_Adapters(daoPosts.get_post(0), context);
                }
                else posts_adapters = new Posts_Adapters(listPostDB, context);
            }else posts_adapters = new Posts_Adapters(listPostDB, context);
            recyclerView.setAdapter(posts_adapters);
            recyclerView.getRecycledViewPool().clear();
            if(recyclerViewState != null && recyclerView.getLayoutManager() != null) recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            loadingPanel.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
            loadingPanel.setVisibility(View.VISIBLE);
        }
    }

    //  Method to get User Posts
    public static void getUsersPosts(Context context, @NonNull RecyclerView recyclerView,
                                     RelativeLayout noPost_profile, TextView posts_size, DtoAccount account){
        recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
        ArrayList<DtoPost> arraylist = new ArrayList<>();
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);

        //  Checking if user is connected to a network
        if(ConnectionHelper.isOnline(context)){
            reference_posts = myFirebaseHelper.getFirebaseDatabase().getReference();
            Query applesQuery = reference_posts.child("Posts").child("Published").orderByChild("account_id")
                    .equalTo(EncryptHelper.encrypt(account.getAccount_id() + ""));
            applesQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                    arraylist.clear();
                    for(DataSnapshot snapshot: datasnapshot.getChildren()){
                        DtoPost dtoPost = snapshot.getValue(DtoPost.class);
                        DtoPost post = new DtoPost();
                        if(dtoPost != null){
                            post.setPost_id(EncryptHelper.decrypt(dtoPost.getPost_id()));
                            post.setAccount_id(EncryptHelper.decrypt(EncryptHelper.decrypt(dtoPost.getAccount_id())));
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
                    }else{
                        Posts_Adapters posts_adapters = new Posts_Adapters(arraylist, context);
                        recyclerView.setAdapter(posts_adapters);
                        recyclerView.getRecycledViewPool().clear();
                        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                        noPost_profile.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    posts_size.setText(Methods.NumberTrick(arraylist.size()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }else ToastHelper.toast((Activity)context , context.getString(R.string.you_are_without_internet), 0);


    }
}