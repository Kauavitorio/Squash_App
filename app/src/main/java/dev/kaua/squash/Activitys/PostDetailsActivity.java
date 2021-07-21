package dev.kaua.squash.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ToastHelper;

public class PostDetailsActivity extends AppCompatActivity {

    DtoAccount account = new DtoAccount();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        Bundle bundle = getIntent().getExtras();

        SharedPreferences sp_First = getSharedPreferences("myPrefs", MODE_PRIVATE);
        account.setAccount_id_cry(EncryptHelper.decrypt(sp_First.getString("pref_account_id", null)));
        ToastHelper.toast(this, account.getAccount_id_cry(), 0);

    }
}