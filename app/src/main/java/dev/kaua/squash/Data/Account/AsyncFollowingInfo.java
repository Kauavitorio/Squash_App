package dev.kaua.squash.Data.Account;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.kaua.squash.Fragments.FollowingFragment;
import dev.kaua.squash.Tools.JsonHandler;
import dev.kaua.squash.Tools.Methods;

@SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
@SuppressLint("StaticFieldLeak")
public class AsyncFollowingInfo extends AsyncTask {
    List<DtoAccount> arrayListDto = new ArrayList<>();
    Activity context;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView txt_no_array_list;
    long account_id;

    public AsyncFollowingInfo(Activity context, long account_id, RecyclerView recyclerView, ProgressBar progressBar, TextView txt_no_array_list) {
        this.account_id = account_id;
        this.context = context;
        this.recyclerView = recyclerView;
        this.progressBar = progressBar;
        this.txt_no_array_list = txt_no_array_list;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        recyclerView.setVisibility(View.GONE);
        txt_no_array_list.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String json =  JsonHandler.getJson( Methods.BASE_URL_HTTPS + "user/action/get-following?id=" + account_id + "&request="
                + Methods.RandomCharactersWithoutSpecials(9));
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("Results");
            if(jsonArray.length() > 0)
            for (int i = 0; i < jsonArray.length() ; i++) {
                if(jsonArray.getJSONObject(i) != null && jsonArray.getJSONObject(i).getInt("verify") == 1){
                    DtoAccount account = new DtoAccount();
                    account.setAccount_id(jsonArray.getJSONObject(i).getLong("account_id"));
                    account.setName_user(jsonArray.getJSONObject(i).getString("name_user"));
                    account.setUsername(jsonArray.getJSONObject(i).getString("username"));
                    account.setVerify(jsonArray.getJSONObject(i).getInt("verify"));
                    account.setVerification_level(jsonArray.getJSONObject(i).getString("verification_level"));
                    account.setProfile_image(jsonArray.getJSONObject(i).getString("profile_image"));
                    arrayListDto.add(account);
                }
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

        Collections.reverse((List<?>) arrayListDto);
        FollowingFragment.getInstance().ShowFollowingList((List<DtoAccount>) arrayListDto);
    }
}

