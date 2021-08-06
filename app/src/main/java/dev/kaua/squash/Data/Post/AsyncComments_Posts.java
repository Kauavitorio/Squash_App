package dev.kaua.squash.Data.Post;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import dev.kaua.squash.Adapters.Comments_Adapters;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.JsonHandler;
import dev.kaua.squash.Tools.Methods;

@SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
@SuppressLint("StaticFieldLeak")
public class AsyncComments_Posts extends AsyncTask {
    ArrayList<DtoPost> arrayListDto = new ArrayList<>();
    Activity context;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    long post_id;

    public AsyncComments_Posts(Activity context, RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout, long post_id) {
        this.post_id = post_id;
        this.context = context;
        this.recyclerView = recyclerView;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String json =  JsonHandler.getJson( Methods.BASE_URL_HTTPS + "post/action/get-comments?post_id=" + post_id);
        Comments_Adapters comments_adapters = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("Search");
            if(jsonArray.length() > 0)
            for (int i = 0; i < jsonArray.length() ; i++) {
                DtoPost post = new DtoPost();
                post.setComment_id(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("comment_id")));
                post.setPost_id(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("post_id")));
                post.setAccount_id(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("account_id")));
                post.setComment(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("comment")));
                post.setLikes(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("likes")));
                post.setName_user(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("name_user")));
                post.setUsername(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("username")));
                post.setVerification_level(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("verification_level")));
                post.setProfile_image(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("profile_image")));
                post.setReply_to(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("reply_to")));
                arrayListDto.add(post);
            }
            comments_adapters = new Comments_Adapters(arrayListDto, context);
            comments_adapters.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ErrorNetWork", e.toString());
        }
        return comments_adapters;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onPostExecute(Object comments_adapters) {
        super.onPostExecute(comments_adapters);
        swipeRefreshLayout.setRefreshing(false);
        //noinspection rawtypes
        recyclerView.setAdapter((RecyclerView.Adapter) comments_adapters);
        recyclerView.getRecycledViewPool().clear();

    }
}

