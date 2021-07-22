package dev.kaua.squash.Activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

import dev.kaua.squash.BuildConfig;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.Methods;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class SplashActivity extends AppCompatActivity {
    //  Create timer
    private final Handler timer = new Handler();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setStatusBarColor(getColor(R.color.base_color));
        getWindow().setNavigationBarColor(getColor(R.color.base_color));
        TextView app_version_splash = findViewById(R.id.app_version_splash);
        app_version_splash.setText( getString(R.string.app_name) + " - " + BuildConfig.VERSION_NAME);

        Intent intent = getIntent();
        Uri data = intent.getData();
        String action = intent.getAction();
        String type = intent.getType();
        if(data != null){
            String UrlGetFrom = data.toString();
            UrlGetFrom = UrlGetFrom.replace("https://squash-social.herokuapp.com/", "").replace("http://squash-social.herokuapp.com/", "");
            String[] KnowContent = UrlGetFrom.split("/");
            if (KnowContent[0].equals("verify-account")){
                if(KnowContent[1] != null && KnowContent[1].length() > 3 ){
                    try {
                        String result = ValidateEmailActivity.TestIntent();
                        if(result.length() > 0){
                            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("validate_email_intent"));
                            DoValidation(KnowContent[1]);
                        }
                    }catch (Exception ex){
                        DoValidation(KnowContent[1]);
                    }
                }
            } else if(KnowContent[0].equals("share")){
                try {
                    Log.d("ShareLink", KnowContent[3]);
                    int indexBase = KnowContent[3].indexOf("?");
                    String post_id = KnowContent[3].substring(0, indexBase);
                    Log.d("ShareLink", post_id);
                    finish();
                    Intent i = new Intent(this, PostDetailsActivity.class);
                    i.putExtra("post_id", Long.parseLong(post_id));
                    startActivity(i);
                }catch (Exception ex){
                    Log.d("ShareLink", ex.toString());
                    finishAffinity();
                    verifyIfUsersLogged();
                }
            }else verifyIfUsersLogged();
        }else if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type))
                handleSendText(intent); // Handle text being sent
            else if (type.startsWith("image/"))
                handleSendImage(intent); // Handle single image being sent
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/"))
                handleSendMultipleImages(intent); // Handle multiple images being sent
        } else verifyIfUsersLogged();

    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Log.d("SPLASH_HANDLER", "Text -> " + sharedText);
            Intent goto_main = new Intent(this, MainActivity.class);
            goto_main.putExtra("shortcut", 0);
            goto_main.putExtra("shared", 1);
            goto_main.putExtra("shared_type", 1);
            goto_main.putExtra("shared_content", sharedText);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.move_to_left_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(this, goto_main, activityOptionsCompat.toBundle());
            finishAffinity();
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Log.d("SPLASH_HANDLER", "image -> " + imageUri);
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            Log.d("SPLASH_HANDLER", imageUris + "");
        }
    }

    private void DoValidation(String value) {
        SharedPreferences sp_First = getSharedPreferences("myPrefs", MODE_PRIVATE);
        Intent i = new Intent(this, ValidateEmailActivity.class);
        i.putExtra("account_id", EncryptHelper.decrypt(sp_First.getString("pref_account_id", null)));
        i.putExtra("password", EncryptHelper.decrypt(sp_First.getString("pref_password", null)));
        i.putExtra("email_user", EncryptHelper.decrypt(sp_First.getString("pref_email", null)));
        i.putExtra("verify_id", value);
        i.putExtra("type_validate", 2);
        startActivity(i);
        finish();
    }

    public void verifyIfUsersLogged() {
        //  Verification of user preference information
        SharedPreferences sp_First = getSharedPreferences("myPrefs", MODE_PRIVATE);
        if (sp_First.contains("pref_token")) LoadBaseInfoAndMain();
        else timer.postDelayed(this::GoToIntro, 1500);
    }

    private void LoadBaseInfoAndMain() {
        Methods.LoadFollowersAndFollowing(this);
        timer.postDelayed(this::GoToMain, 300);
    }

    private void GoToMain(){
        Intent goto_main = new Intent(this, MainActivity.class);
        goto_main.putExtra("shortcut", 0);
        goto_main.putExtra("shared", 0);
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.move_to_left_go, R.anim.move_to_right_go);
        ActivityCompat.startActivity(this, goto_main, activityOptionsCompat.toBundle());
        finishAffinity();
    }

    private void GoToIntro(){
        Intent goto_intro = new Intent(this, IntroActivity.class);
        goto_intro.putExtra("shortcut", 0);
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),R.anim.move_to_left_go, R.anim.move_to_right_go);
        ActivityCompat.startActivity(this, goto_intro, activityOptionsCompat.toBundle());
        finishAffinity();
    }
}