package dev.kaua.squash.Activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import dev.kaua.squash.Activitys.Setting.AccountSetting.ChangeSingleInfoActivity;
import dev.kaua.squash.Activitys.Setting.SettingActivity;
import dev.kaua.squash.LocalDataBase.DaoSystem;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Security.Login;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ShortCutsHelper;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class SplashActivity extends AppCompatActivity {
    //  Create timer
    private final Handler timer = new Handler();
    public static final String TAG = "SplashActivity_LOG";
    public static final String ACCOUNT_DISABLE = "account_active";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setStatusBarColor(getColor(R.color.base_color));
        getWindow().setNavigationBarColor(getColor(R.color.base_color));

        SharedPreferences sp_network = getSharedPreferences(MyPrefs.PREFS_NETWORK_USAGE, MODE_PRIVATE);
        if (!sp_network.contains("pref_start_time")) MyPrefs.InsertNetworkCount(this);

        final Intent intent = getIntent();
        final Uri data = intent.getData();
        final String action = intent.getAction();
        final String type = intent.getType();
        if(data != null){
            if(data.getPath() != null){
                final String[] KnowContent = data.getPath().split("/");
                if (data.getPath().contains("verify-account")){
                    if(KnowContent.length > 1 && KnowContent[2] != null && KnowContent[2].length() > 2){
                        try {
                            String result = ValidateEmailActivity.TestIntent();
                            if(result.length() > 0){
                                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("validate_email_intent"));
                                DoValidation(KnowContent[2]);
                            }
                        }catch (Exception ex){
                            DoValidation(KnowContent[2]);
                        }
                    }else verifyIfUsersLogged();
                } else if(data.getPath().contains("share") && data.getPath().contains("post")){
                    if(KnowContent.length > 4){
                        try {
                            final String post_id = KnowContent[4];
                            Log.d(TAG, post_id);
                            finish();
                            Intent i = new Intent(this, PostDetailsActivity.class);
                            i.putExtra("post_id", Long.parseLong(post_id));
                            i.putExtra("comment", 0);
                            startActivity(i);
                        }catch (Exception ex){
                            Log.d(TAG, ex.toString());
                            finishAffinity();
                            verifyIfUsersLogged();
                        }
                    }else verifyIfUsersLogged();
                }else verifyIfUsersLogged();
            }else verifyIfUsersLogged();
        }else if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type))
                handleSendText(intent); // Handle text being sent
            else if (type.startsWith("image/"))
                handleSendImage(intent); // Handle single image being sent
        } else verifyIfUsersLogged();
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Intent goto_main = new Intent(this, MainActivity.class);
            goto_main.putExtra(ShortCutsHelper.SHORTCUT_TAG, ShortCutsHelper.NONE_SHORT);
            goto_main.putExtra(MainActivity.SHARED_TAG, MainActivity.SHARED_ID);
            goto_main.putExtra(MainActivity.SHARED_TYPE_TAG, MainActivity.SHARED_PLAIN_TEXT);
            goto_main.putExtra(MainActivity.SHARED_CONTENT_TAG, sharedText);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.move_to_left_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(this, goto_main, activityOptionsCompat.toBundle());
            finishAffinity();
        }else verifyIfUsersLogged();
    }

    void handleSendImage(@NonNull Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Intent goto_main = new Intent(this, MainActivity.class);
            goto_main.putExtra(ShortCutsHelper.SHORTCUT_TAG, ShortCutsHelper.NONE_SHORT);
            goto_main.putExtra(MainActivity.SHARED_TAG, MainActivity.SHARED_ID);
            goto_main.putExtra(MainActivity.SHARED_TYPE_TAG, MainActivity.SHARED_IMAGE);
            goto_main.putExtra(MainActivity.SHARED_CONTENT_TAG, imageUri);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.move_to_left_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(this, goto_main, activityOptionsCompat.toBundle());
            finishAffinity();
            Log.d(TAG, "image -> " + imageUri);
        }else verifyIfUsersLogged();
    }

    private void DoValidation(String value) {
        SharedPreferences sp_First = getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        Intent i = new Intent(this, ValidateEmailActivity.class);
        i.putExtra(ValidateEmailActivity.ACCOUNT_ID_ID, EncryptHelper.decrypt(sp_First.getString("pref_account_id", null)));
        i.putExtra(ValidateEmailActivity.PASSWORD_ID, EncryptHelper.decrypt(sp_First.getString("pref_password", null)));
        i.putExtra(ValidateEmailActivity.LOGIN_METHOD_ID, EncryptHelper.decrypt(sp_First.getString("pref_email", null)));
        i.putExtra("verify_id", value);
        i.putExtra(ValidateEmailActivity.TYPE_VALIDATE_ID, 2);
        startActivity(i);
        finish();
    }

    public void verifyIfUsersLogged() {
        final DaoSystem daoSystem = new DaoSystem(this);
        if(daoSystem.getNeedResetAccount()){
            final Bundle bundle = getIntent().getExtras();
            if(bundle != null) {
                if(bundle.getInt(ACCOUNT_DISABLE) == Login.DISABLE_ACCOUNT){
                    final Intent goto_intro = new Intent(this, SignInActivity.class);
                    goto_intro.putExtra(ACCOUNT_DISABLE, Login.DISABLE_ACCOUNT);
                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),R.anim.move_to_left_go, R.anim.move_to_right_go);
                    ActivityCompat.startActivity(this, goto_intro, activityOptionsCompat.toBundle());
                    finishAffinity();
                }else LoadBase();
            }else LoadBase();
        }else Login.LogOut(this, Login.LOGOUT_STATUS_WITHOUT_FLAG, Login.NOT_DISABLE_ACCOUNT);
    }

    void LoadBase(){
        MyPrefs.setUpdateRequest_Show(this, 0);
        //  Verification of user preference information
        SharedPreferences sp_First = getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        if (sp_First.contains("pref_token")) LoadBaseInfoAndMain();
        else timer.postDelayed(this::GoToIntro, 1500);
    }

    private void LoadBaseInfoAndMain() {
        Methods.LoadFollowersAndFollowing(this, 1);
        timer.postDelayed(this::GoToMain, 150);
    }

    private void GoToMain(){
        Intent goto_main = new Intent(this, MainActivity.class);
        goto_main.putExtra(ShortCutsHelper.SHORTCUT_TAG, ShortCutsHelper.NONE_SHORT);
        goto_main.putExtra(MainActivity.SHARED_TAG, ShortCutsHelper.NONE_SHORT);
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.move_to_left_go, R.anim.move_to_right_go);
        ActivityCompat.startActivity(this, goto_main, activityOptionsCompat.toBundle());
        finishAffinity();
    }

    private void GoToIntro(){
        Intent goto_intro = new Intent(this, IntroActivity.class);
        goto_intro.putExtra(ShortCutsHelper.SHORTCUT_TAG, ShortCutsHelper.NONE_SHORT);
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),R.anim.move_to_left_go, R.anim.move_to_right_go);
        ActivityCompat.startActivity(this, goto_intro, activityOptionsCompat.toBundle());
        finishAffinity();
    }
}