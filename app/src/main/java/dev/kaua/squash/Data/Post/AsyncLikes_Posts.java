package dev.kaua.squash.Data.Post;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import dev.kaua.squash.Adapters.Posts_Adapters;
import dev.kaua.squash.LocalDataBase.DaoPosts;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.JsonHandler;
import dev.kaua.squash.Tools.Methods;

@SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
@SuppressLint("StaticFieldLeak")
public class AsyncLikes_Posts extends AsyncTask {
    ArrayList<DtoPost> arrayListDto = new ArrayList<>();
    Activity context;
    long account_id, position;
    public static final long NOT_NOTIFY = 0x3f;

    public AsyncLikes_Posts(Activity context, long account_id, long position) {
        this.account_id = account_id;
        this.context = context;
        this.position = position;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String json =  JsonHandler.getJson( Methods.BASE_URL_HTTPS + "post/list/likes?account_id=" + EncryptHelper.encrypt(account_id + "")
                .replace("+", "XXXX7").replace("/", "XXXX1").replace("==", "XXXX9") + "&key="
                + Methods.RandomCharactersWithoutSpecials(9));
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("Search");
            if(jsonArray.length() > 0)
            for (int i = 0; i < jsonArray.length() ; i++) {
                DtoPost post = new DtoPost();
                post.setPost_id(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("post_id")));
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
        daoPosts.Register_Likes((ArrayList<DtoPost>) arrayListDto);
        if(position != NOT_NOTIFY)
            try {
                Posts_Adapters.getInstance().NotifyChanged(position);
                Log.d("AsyncLikes", position + " <- Notify");
            }catch (Exception ex){
                Log.d("AsyncLikes", ex.toString());
            }
    }
}

