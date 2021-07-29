package dev.kaua.squash.Data.Post.Actions;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import dev.kaua.squash.Adapters.Posts_Adapters;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Data.Post.PostServices;
import dev.kaua.squash.Fragments.MainFragment;
import dev.kaua.squash.LocalDataBase.DaoPosts;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;

public class RecommendedPosts extends MainFragment {

    final static Retrofit retrofit = Methods.GetRetrofitBuilder();
    private static Parcelable recyclerViewState;
    public static final String PREFS_NAME = "myPrefs";
    public static final String BASE_POST_ID = "99999";

    //  Method to get RecommendedPosts
    public static void getRecommendedPosts(@NonNull Context context, @NonNull RecyclerView recyclerView, ConstraintLayout loadingPanel, ProgressBar loading_posts){
        PostServices services = retrofit.create(PostServices.class);
        DtoAccount sameAccount = new DtoAccount();
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sameAccount.setAccount_id_cry(sp.getString("pref_account_id", null));
        Call<ArrayList<DtoPost>> call = services.getRecommendedPosts(sameAccount);
        recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
        ArrayList<DtoPost> arraylist = new ArrayList<>();
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(25);
        DaoPosts daoPosts = new DaoPosts(context);

        swipe_posts.setRefreshing(true);

        LoadPostsFromLocal(context, recyclerView, loadingPanel, loading_posts, daoPosts);

        //  Checking if user is connected to a network
        if(Methods.isOnline(context)){
            call.enqueue(new Callback<ArrayList<DtoPost>>() {
                @Override
                public void onResponse(@NotNull Call<ArrayList<DtoPost>> call, @NotNull Response<ArrayList<DtoPost>> response) {
                    //swipe_main.setRefreshing(false);
                    if(response.code() == 200 || response.code() == 201){
                        ArrayList<DtoPost> list = response.body();
                        if(list != null){
                            if(list.get(0).getPosts() != null){
                                daoPosts.DropTable(0);
                                for (int i = 0; i < Objects.requireNonNull(list).get(0).getPosts().size(); i++){
                                    DtoPost.Posts_Search dtoPost = list.get(0).getPosts().get(i);
                                    DtoPost post = new DtoPost();
                                    if(dtoPost.getPost_id() != null){
                                        if(!Objects.equals(EncryptHelper.decrypt(dtoPost.getPost_id()), BASE_POST_ID)){
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
                                }
                                daoPosts.Register_Home_Posts(arraylist);

                            }
                        }
                    }
                    LoadPostsFromLocal(context, recyclerView, loadingPanel, loading_posts, daoPosts);
                }
                @Override
                public void onFailure(@NotNull Call<ArrayList<DtoPost>> call, @NotNull Throwable t) {
                    Log.d("Posts", t.getMessage());
                    LoadPostsFromLocal(context, recyclerView, loadingPanel, loading_posts, daoPosts);
                }
            });
        }else ToastHelper.toast((Activity)context , context.getString(R.string.you_are_without_internet), 0);
    }

    private static void LoadPostsFromLocal(Context context, RecyclerView recyclerView, ConstraintLayout loadingPanel, ProgressBar loading_posts, @NonNull DaoPosts daoPosts) {
        ArrayList<DtoPost> listPostDB = daoPosts.get_post(0);
        if (listPostDB.size() > 0) {
            swipe_posts.setRefreshing(false);
            Posts_Adapters posts_adapters = new Posts_Adapters(listPostDB, context);
            recyclerView.setAdapter(posts_adapters);
            recyclerView.getRecycledViewPool().clear();
            Objects.requireNonNull(recyclerView.getLayoutManager()).onRestoreInstanceState(recyclerViewState);
            loadingPanel.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else
            loading_posts.setVisibility(View.GONE);
    }

    //  Method to get User Posts
    public static void getUsersPosts(Context context, @NonNull RecyclerView recyclerView, RelativeLayout noPost_profile, DtoAccount account){
        PostServices services = retrofit.create(PostServices.class);
        Call<ArrayList<DtoPost>> call = services.getUserPosts(account);
        recyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
        ArrayList<DtoPost> arraylist = new ArrayList<>();
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);


        //  Checking if user is connected to a network
        if(Methods.isOnline(context)){
            call.enqueue(new Callback<ArrayList<DtoPost>>() {
                @Override
                public void onResponse(@NotNull Call<ArrayList<DtoPost>> call, @NotNull Response<ArrayList<DtoPost>> response) {
                    if(response.code() == 200 || response.code() == 201){
                        ArrayList<DtoPost> list = response.body();
                        if(list != null){
                            if(list.get(0).getPosts() != null){
                                for (int i = 0; i < Objects.requireNonNull(list).get(0).getPosts().size(); i++){
                                    DtoPost.Posts_Search dtoPost = list.get(0).getPosts().get(i);
                                    DtoPost post = new DtoPost();
                                    if(dtoPost.getPost_id() != null){
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
                            }
                        }
                    }
                    else {
                        noPost_profile.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onFailure(@NotNull Call<ArrayList<DtoPost>> call, @NotNull Throwable t) {
                    Log.d("Posts", t.getMessage());
                }
            });
        }else ToastHelper.toast((Activity)context , context.getString(R.string.you_are_without_internet), 0);


    }
}