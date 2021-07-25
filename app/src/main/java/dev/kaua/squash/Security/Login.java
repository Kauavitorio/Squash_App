package dev.kaua.squash.Security;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.SignInActivity;
import dev.kaua.squash.Activitys.SplashActivity;
import dev.kaua.squash.Activitys.ValidateEmailActivity;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public abstract class Login {
    @SuppressLint("StaticFieldLeak")
    private static LoadingDialog loadingDialog;
    private static FirebaseAuth mAuth;

    //  Set preferences
    private static SharedPreferences mPrefs;
    private static FirebaseAnalytics mFirebaseAnalytics;

    static final Retrofit retrofitUser = Methods.GetRetrofitBuilder();

    public static void DoLogin(Context context, String login_method, String password){
        loadingDialog = new LoadingDialog((Activity) context);
        loadingDialog.startLoading();

        //  Getting user mobile information and date time
        String device_login = Build.MANUFACTURER + ", " + Build.MODEL;
        Calendar c = Calendar.getInstance();
        Log.d("DateTime", "Current time => "+c.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("MMMM dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("HH:mm a z");
        String formattedDate = df.format(c.getTime()) + " at " + df_time.format(c.getTime());
        Log.d("DateTime", "Current date => "+ formattedDate);

        DtoAccount account = new DtoAccount(EncryptHelper.encrypt(login_method), EncryptHelper.encrypt(password),
                EncryptHelper.encrypt(device_login.substring(0,1).toUpperCase().concat(device_login.substring(1))), EncryptHelper.encrypt("0-river-reliable"), EncryptHelper.encrypt(formattedDate), 0);
        AccountServices login_service = retrofitUser.create(AccountServices.class);
        Call<DtoAccount> call = login_service.login(account);
        call.enqueue(new Callback<DtoAccount>() {
            @Override
            public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                //  Checking api return code
                if(response.code() == 200){
                    //  Clear all prefs before login user
                    mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
                    mPrefs.edit().clear().apply();

                    //  Add User prefs
                    SharedPreferences.Editor editor = mPrefs.edit();
                    assert response.body() != null;
                    editor.putString("pref_account_id", response.body().getAccount_id_cry());
                    editor.putString("pref_uid", response.body().getUID());
                    editor.putString("pref_name_user", response.body().getName_user());
                    editor.putString("pref_username", response.body().getUsername());
                    editor.putString("pref_email", response.body().getEmail());
                    editor.putString("pref_phone_user", response.body().getPhone_user());
                    editor.putString("pref_banner_user", response.body().getBanner_user());
                    editor.putString("pref_profile_image", response.body().getProfile_image());
                    editor.putString("pref_bio_user", response.body().getBio_user());
                    editor.putString("pref_url_user", response.body().getUrl_user());
                    editor.putString("pref_following", response.body().getFollowing());
                    editor.putString("pref_followers", response.body().getFollowers());
                    editor.putString("pref_born_date", response.body().getBorn_date());
                    editor.putString("pref_joined_date", response.body().getJoined_date());
                    editor.putString("pref_token", response.body().getToken());
                    editor.putString("pref_password", EncryptHelper.encrypt(password));
                    editor.putString("pref_verification_level", response.body().getVerification_level());
                    editor.apply();

                    //  Getting Followers and Followings
                    Methods.LoadFollowersAndFollowing(context);

                    //  Log in User On Firebase
                    mAuth = ConfFirebase.getFirebaseAuth();
                    mAuth.signOut();

                    //  Init Analytics
                    mFirebaseAnalytics = ConfFirebase.getFirebaseAnalytics(context);

                    //  Login user in firebase to get user instance
                    mAuth.signInWithEmailAndPassword(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getEmail())), Objects.requireNonNull(EncryptHelper.decrypt(response.body().getToken())))
                            .addOnCompleteListener(task -> {
                                loadingDialog.dismissDialog();
                                Log.d("Auth", "Login Ok");
                                Log.d("Auth", "User " + mAuth.getUid());

                                //  Creating analytic for login event
                                Bundle bundle_Analytics = new Bundle();
                                bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_ID, mAuth.getUid());
                                bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_NAME, EncryptHelper.decrypt(response.body().getUsername()));
                                bundle_Analytics.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle_Analytics);

                                //  Go To main
                                Intent i = new Intent(context, MainActivity.class);
                                i.putExtra("shared", 0);
                                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.move_to_left_go, R.anim.move_to_right_go);
                                ActivityCompat.startActivity(context, i, activityOptionsCompat.toBundle());
                                ((Activity) context).finish();
                            });
                }else if(response.code() == 206){
                    Log.d("LoginActions", "Email not validated");
                    loadingDialog.dismissDialog();
                    mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
                    mPrefs.edit().clear().apply();
                    Intent i = new Intent(context, ValidateEmailActivity.class);
                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(context,R.anim.move_to_left_go, R.anim.move_to_right_go);
                    //noinspection ConstantConditions
                    i.putExtra("account_id", EncryptHelper.decrypt(response.body().getMessage()));
                    i.putExtra("email_user", login_method);
                    i.putExtra("password", password);
                    i.putExtra("type_validate", 1);
                    ActivityCompat.startActivity(context, i, activityOptionsCompat.toBundle());
                    ((Activity) context).finish();
                }else if(response.code() == 401) {
                    loadingDialog.dismissDialog();
                    mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
                    mPrefs.edit().clear().apply();
                    try {
                        SignInActivity.getInstance().Invalid_email_or_password();
                    }catch (Exception ex){
                        Warnings.showWeHaveAProblem(context);
                    }
                }
                else {
                    loadingDialog.dismissDialog();
                    Warnings.showWeHaveAProblem(context);
                }
            }
            @Override
            public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                loadingDialog.dismissDialog();
                Warnings.showWeHaveAProblem(context);
            }
        });
    }

    public static void ReloadUserinfo(Context context, String login_method, String password){

        //  Getting user mobile information and date time
        String device_login = Build.MANUFACTURER + ", " + Build.MODEL;
        Calendar c = Calendar.getInstance();
        Log.d("DateTime", "Current time => "+ c.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("MMMM dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("HH:mm a z");
        String formattedDate = df.format(c.getTime()) + " at " + df_time.format(c.getTime());
        Log.d("DateTime", "Current date => "+ formattedDate);
        Log.d("LoginActions", "Device => "+ device_login);

        DtoAccount account = new DtoAccount(EncryptHelper.encrypt(login_method), EncryptHelper.encrypt(password),
                EncryptHelper.encrypt(device_login.substring(0,1).toUpperCase().concat(device_login.substring(1))), EncryptHelper.encrypt("0-river"), EncryptHelper.encrypt(formattedDate), 0);
        AccountServices login_service = retrofitUser.create(AccountServices.class);
        Call<DtoAccount> call = login_service.login(account);
        call.enqueue(new Callback<DtoAccount>() {
            @Override
            public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                Log.d("LoginActions", "Login Status => " + response.code());
                //  Checking api return code
                if(response.code() == 200){
                    if(response.body() != null){
                        //  Clear all prefs before login user
                        mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
                        mPrefs.edit().clear().apply();

                        //  Add User prefs
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putString("pref_account_id", response.body().getAccount_id_cry());
                        editor.putString("pref_uid", response.body().getUID());
                        editor.putString("pref_name_user", response.body().getName_user());
                        editor.putString("pref_username", response.body().getUsername());
                        editor.putString("pref_email", response.body().getEmail());
                        editor.putString("pref_phone_user", response.body().getPhone_user());
                        editor.putString("pref_banner_user", response.body().getBanner_user());
                        editor.putString("pref_profile_image", response.body().getProfile_image());
                        editor.putString("pref_bio_user", response.body().getBio_user());
                        editor.putString("pref_url_user", response.body().getUrl_user());
                        editor.putString("pref_following", response.body().getFollowing());
                        editor.putString("pref_followers", response.body().getFollowers());
                        editor.putString("pref_born_date", response.body().getBorn_date());
                        editor.putString("pref_joined_date", response.body().getJoined_date());
                        editor.putString("pref_token", response.body().getToken());
                        editor.putString("pref_password", EncryptHelper.encrypt(password));
                        editor.putString("pref_verification_level", response.body().getVerification_level());
                        editor.apply();

                        //  Getting Followers and Followings
                        Methods.LoadFollowersAndFollowing(context);
                    }
                }else if(response.code() == 206){
                    Log.d("LoginActions", "Email not validated");
                    mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
                    mPrefs.edit().clear().apply();
                    Intent i = new Intent(context, ValidateEmailActivity.class);
                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(context,R.anim.move_to_left_go, R.anim.move_to_right_go);
                    //noinspection ConstantConditions
                    i.putExtra("account_id", EncryptHelper.decrypt(response.body().getMessage()));
                    i.putExtra("email_user", login_method);
                    i.putExtra("password", password);
                    i.putExtra("type_validate", 1);
                    ActivityCompat.startActivity(context, i, activityOptionsCompat.toBundle());
                    ((Activity) context).finish();
                }else if(response.code() == 401) {
                    Log.d("LoginActions", "Login Method or Password is not valid");
                    loadingDialog.dismissDialog();
                    LogOut(context, 1);
                }
            }
            @Override
            public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) { Log.d("LoginActions", t.getMessage()); }
        });
    }

    static Handler timer = new Handler();
    public static void LogOut(Context context, int status){
        FirebaseAuth.getInstance().signOut();
        Methods.status_chat("offline", context);
        clearApplicationData(context);
        LoadingDialog loadingDialog = new LoadingDialog((Activity)context);
        loadingDialog.startLoading();
        timer.postDelayed(() -> {
            FirebaseAuth.getInstance().signOut();
            mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
            mPrefs.edit().clear().apply();
            ((Activity)context).finishAffinity();
            Intent i;
            if(status == 0 ) i = new Intent(context, SplashActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            else i = new Intent(context, SplashActivity.class);
            context.startActivity(i);
            loadingDialog.dismissDialog();
        },1000);
    }

    public static void clearApplicationData(Context context) {
        File cacheDirectory = context.getCacheDir();
        File applicationDirectory = new File(Objects.requireNonNull(cacheDirectory.getParent()));
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
    }
    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }
        return deletedAll;
    }

}
