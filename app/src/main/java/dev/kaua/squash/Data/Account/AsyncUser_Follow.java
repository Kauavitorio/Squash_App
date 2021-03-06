package dev.kaua.squash.Data.Account;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.JsonHandler;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;

@SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
@SuppressLint("StaticFieldLeak")
public class AsyncUser_Follow extends AsyncTask {
    ArrayList<DtoAccount> arrayListDto = new ArrayList<>();
    Activity context;

    public AsyncUser_Follow(Activity context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String json =  JsonHandler.getJson( Methods.BASE_URL_HTTPS + "user/action/get-users-followers/"
                + MyPrefs.getUserInformation(context).getAccount_id() + "/?token=" + Methods.RandomCharactersWithoutSpecials(5));
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("Search");
            for (int i = 0; i < jsonArray.length() ; i++) {
                DtoAccount account = new DtoAccount();
                account.setId_following(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("id_following")));
                account.setAccount_id(Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("account_id")))));
                account.setAccount_id_following(EncryptHelper.decrypt(jsonArray.getJSONObject(i).getString("account_id_following")));
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
        DaoFollowing daoFollowing = new DaoFollowing(context);
        daoFollowing.Register_Followers_Following((ArrayList<DtoAccount>) arrayListDto);
    }
}

