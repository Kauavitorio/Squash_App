package dev.kaua.squash.Data.Account;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import dev.kaua.squash.Adapters.SearchItemArrayAdapter;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.JsonHandler;

@SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
@SuppressLint("StaticFieldLeak")
public class AsyncUser_Search extends AsyncTask {
    ArrayList<DtoAccount> arrayListDto = new ArrayList<>();
    Activity context;
    AutoCompleteTextView edit_search;

    public AsyncUser_Search(AutoCompleteTextView edit_search, Activity context) {
        this.context = context;
        this.edit_search = edit_search;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String json =  JsonHandler.getJson("https://dev-river-api.herokuapp.com/user/action/search");
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("Search");
            for (int i = 0; i < jsonArray.length() ; i++) {
                DtoAccount account = new DtoAccount();
                account.setAccount_id_cry(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("account_id_cry")));
                account.setName_user(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("name_user")));
                account.setUsername(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("username")));
                account.setEmail(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("email")));
                account.setPhone_user(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("phone_user")));
                account.setBanner_user(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("banner_user")));
                account.setProfile_image(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("profile_image")));
                account.setBio_user(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("bio_user")));
                account.setUrl_user(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("url_user")));
                account.setFollowing(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("following")));
                account.setFollowers(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("followers")));
                account.setBorn_date(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("born_date")));
                account.setJoined_date(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("joined_date")));
                account.setVerification_level(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("verification_level")));
                arrayListDto.add(account);
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
        SearchItemArrayAdapter adapter = new SearchItemArrayAdapter(context, R.layout.adapter_search_layout, R.id.txt_user_name_search, (ArrayList<DtoAccount>) arrayListDto);

        edit_search.setDropDownBackgroundDrawable(context.getDrawable(R.drawable.background_adapter_search));
        edit_search.setAdapter(adapter);
    }
}

