package dev.kaua.squash.Data.Post;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import dev.kaua.squash.LocalDataBase.DaoPosts;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.JsonHandler;
import dev.kaua.squash.Tools.Methods;

@SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
@SuppressLint("StaticFieldLeak")
public class AsyncLikes_Posts_Comment extends AsyncTask {
    ArrayList<DtoPost> arrayListDto = new ArrayList<>();
    Activity context;
    long account_id;

    public AsyncLikes_Posts_Comment(Activity context, long account_id) {
        this.account_id = account_id;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String json =  JsonHandler.getJson( Methods.BASE_URL_HTTPS + "post/list/likes/comment?account_id=" + EncryptHelper.encrypt(account_id + "")
                .replace("+", "XXXX7").replace("/", "XXXX1").replace("==", "XXXX9") + "&key="
                + Methods.RandomCharactersWithoutSpecials(9));
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("Search");
            if(jsonArray.length() > 0)
            for (int i = 0; i < jsonArray.length() ; i++) {
                DtoPost post = new DtoPost();
                post.setComment_id(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("comment_id")));
                post.setAccount_id(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("account_id")));
                arrayListDto.add(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ErrorNetWork", e.toString());
        }
        return arrayListDto;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onPostExecute(Object arrayListDto) {
        super.onPostExecute(arrayListDto);
        DaoPosts daoPosts = new DaoPosts(context);
        daoPosts.Register_Likes_Comments((ArrayList<DtoPost>) arrayListDto);
    }
}

