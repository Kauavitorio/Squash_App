package dev.kaua.squash.Data.Post;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import dev.kaua.squash.Adapters.Posts_Adapters;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.JsonHandler;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;

@SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
@SuppressLint("StaticFieldLeak")
public class AsyncRecommended_Posts_feed extends AsyncTask {
    ArrayList<DtoPost> arrayListDto = new ArrayList<>();
    Activity context;
    RecyclerView recyclerView;
    TextView txt_empty_feed;
    private DaoFollowing daoFollowing;
    SwipeRefreshLayout swipeRefreshLayout;
    int size;

    public AsyncRecommended_Posts_feed(Activity context, RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout, TextView txt_empty_feed) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.txt_empty_feed = txt_empty_feed;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.daoFollowing = new DaoFollowing(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String json =  JsonHandler.getJson( Methods.BASE_URL_HTTPS + "post/list/recommended/feed?token="
                + Methods.shuffle(Methods.RandomCharactersWithoutSpecials(46) + "SQUASH"));
        Posts_Adapters posts_adapters = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("Search");
            if(jsonArray.length() > 0)
            for (int i = 0; i < jsonArray.length() ; i++) {
                if(jsonArray.getJSONObject(i) != null){
                    long account_id_post = Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("account_id"))));
                    if(account_id_post != MyPrefs.getUserInformation(context).getAccount_id() && !daoFollowing.check_if_follow(MyPrefs.getUserInformation(context).getAccount_id(), account_id_post)){
                        DtoPost post = new DtoPost();
                        post.setPost_id(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("post_id")));
                        post.setAccount_id(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("account_id")));
                        post.setVerification_level( EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("verification_level")));
                        post.setName_user(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("name_user")));
                        post.setUsername(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("username")));
                        post.setProfile_image(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("profile_image")));
                        post.setPost_date(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("post_date")));
                        post.setPost_time(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("post_time")));
                        post.setPost_content(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("post_content")));
                        post.setPost_comments_amount(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("post_comments_amount")));
                        post.setPost_likes(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("post_likes")));
                        post.setSuggestion(true);
                        arrayListDto.add(post);
                    }
                }
            }
            size = arrayListDto.size();
            posts_adapters = new Posts_Adapters(arrayListDto, context);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ErrorNetWork", e.toString());
        }
        return posts_adapters;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onPostExecute(Object comments_adapters) {
        super.onPostExecute(comments_adapters);
        swipeRefreshLayout.setRefreshing(false);
        if (size <= 0){
            swipeRefreshLayout.setVisibility(View.GONE);
            txt_empty_feed.setVisibility(View.VISIBLE);
        }else{
            //noinspection rawtypes
            recyclerView.setAdapter((RecyclerView.Adapter) comments_adapters);
            recyclerView.getRecycledViewPool().clear();
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            txt_empty_feed.setVisibility(View.GONE);
        }

    }
}

