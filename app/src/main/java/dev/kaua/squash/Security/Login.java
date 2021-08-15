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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import dev.kaua.squash.Activitys.EditProfileActivity;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.SignInActivity;
import dev.kaua.squash.Activitys.SplashActivity;
import dev.kaua.squash.Activitys.ValidateEmailActivity;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.AsyncLikes_Posts;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.LocalDataBase.DaoPosts;
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
    private final static String TAG = "LOGIN_ACTIONS";

    //  Set preferences
    private static SharedPreferences mPrefs;
    private static FirebaseAnalytics mFirebaseAnalytics;
    private static DatabaseReference reference;

    static final Retrofit retrofitUser = Methods.GetRetrofitBuilder();

    public static void DoLogin(Context context, String login_method, String password){
        loadingDialog = new LoadingDialog((Activity) context);
        loadingDialog.startLoading();

        //  Getting user mobile information and date time
        String device_login = Build.MANUFACTURER + ", " + Build.MODEL;
        Calendar c = Calendar.getInstance();
        Log.d(TAG, "Current time => "+c.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("MMMM dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("HH:mm a z");
        String formattedDate = df.format(c.getTime()) + " at " + df_time.format(c.getTime());
        Log.d(TAG, "Current date => "+ formattedDate);

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

                    AsyncLikes_Posts async = new AsyncLikes_Posts((Activity) context , Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getAccount_id_cry()))), AsyncLikes_Posts.NOT_NOTIFY);
                    //noinspection unchecked
                    async.execute();

                    //  Getting Followers and Followings
                    Methods.LoadFollowersAndFollowing(context, 1);

                    //  Log in User On Firebase
                    mAuth = myFirebaseHelper.getFirebaseAuth();
                    mAuth.signOut();

                    //  Init Analytics
                    mFirebaseAnalytics = myFirebaseHelper.getFirebaseAnalytics(context);

                    //  Login user in firebase to get user instance
                    mAuth.signInWithEmailAndPassword(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getEmail())), Objects.requireNonNull(EncryptHelper.decrypt(response.body().getToken())))
                            .addOnCompleteListener(task -> {
                                loadingDialog.dismissDialog();
                                Log.d(TAG, "Login Ok");
                                Log.d(TAG, "User " + mAuth.getUid());

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
                    Log.d(TAG, "Email not validated");
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
        Log.d(TAG, "Current time => "+ c.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("MMMM dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("HH:mm a z");
        String formattedDate = df.format(c.getTime()) + " at " + df_time.format(c.getTime());
        Log.d(TAG, "Current date => "+ formattedDate);
        Log.d(TAG, "Device => "+ device_login);

        DtoAccount account = new DtoAccount(EncryptHelper.encrypt(login_method), EncryptHelper.encrypt(password),
                EncryptHelper.encrypt(device_login.substring(0,1).toUpperCase().concat(device_login.substring(1))), EncryptHelper.encrypt("0-river"), EncryptHelper.encrypt(formattedDate), 0);
        AccountServices login_service = retrofitUser.create(AccountServices.class);
        Call<DtoAccount> call = login_service.login(account);
        call.enqueue(new Callback<DtoAccount>() {
            @Override
            public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                Log.d(TAG, "Login Status => " + response.code());
                //  Checking api return code
                if(response.code() == 200){
                    if(response.body() != null){
                        //  Clear all prefs before login user
                        mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);

                        if(MyPrefs.getUserInformation(context).getVerification_level() != null && !MyPrefs.getUserInformation(context).getVerification_level().equals(EncryptHelper.decrypt(response.body().getVerification_level()))){
                            //  Register new user on Firebase Database
                            reference = myFirebaseHelper.getFirebaseDatabase().getReference("Users").child(Objects.requireNonNull(myFirebaseHelper.getFirebaseAuth().getUid()));
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("verification_level", response.body().getVerification_level());

                            reference.updateChildren(hashMap).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()) Log.d(TAG, "Register in Realtime database Successful");
                            });

                            //  Update all user posts
                            DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference();
                            Query applesQuery = ref.child("Posts").child("Published").orderByChild("account_id")
                                    .equalTo(EncryptHelper.encrypt(MyPrefs.getUserInformation(context).getAccount_id() + ""));
                            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("verification_level", response.body().getVerification_level());
                                        appleSnapshot.getRef().updateChildren(hashMap);
                                    }
                                }
                                @Override
                                public void onCancelled(@NotNull DatabaseError databaseError) {
                                    Log.e("EditProfile", "onCancelled", databaseError.toException());
                                }
                            });
                        }

                        mPrefs.edit().clear().apply();
                        //  Add User prefs
                        SharedPreferences.Editor editor = mPrefs.edit();
                        //noinspection ConstantConditions
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
                        Methods.LoadFollowersAndFollowing(context, 1);
                    }
                }else if(response.code() == 206){
                    Log.d(TAG, "Email not validated");
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
                    Log.d(TAG, "Login Method or Password is not valid");
                    loadingDialog.dismissDialog();
                    LogOut(context, 1);
                }
            }
            @Override
            public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) { Log.d(TAG, t.getMessage()); }
        });
    }

    static Handler timer = new Handler();
    public static void LogOut(Context context, int status){
        loadingDialog = null;
        loadingDialog = new LoadingDialog((Activity)context);
        loadingDialog.startLoading();
        Methods.status_chat("offline", context); // Set User status with offline

        myFirebaseHelper.LogOut(); // LogOut on firebase

        clearApplicationData(context); // Clear app cache and data

        MyPrefs.logOut(context); // Remove all preferences on app

        timer.postDelayed(() -> {
            loadingDialog.dismissDialog();
            loadingDialog = null;
            loadingDialog = new LoadingDialog((Activity)context);
            loadingDialog.startLoading();

            DaoAccount daoAccount = new DaoAccount(context);
            daoAccount.DropTable(); //  Drop follower and following information

            DaoChat daoChat = new DaoChat(context);
            daoChat.DropTable(DaoChat.DROP_ALL); // Drop all information on chat system

            DaoFollowing daoFollowing = new DaoFollowing(context);
            daoFollowing.DropTable();

            DaoPosts daoPosts = new DaoPosts(context);
            daoPosts.DropTable(DaoPosts.DROP_ALL);

            timer.postDelayed(() -> {
                ((Activity)context).finishAffinity();
                final Intent i;
                if(status == 0 ) i = new Intent(context, SplashActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                else i = new Intent(context, SplashActivity.class);
                context.startActivity(i);
                loadingDialog.dismissDialog();
            },1000);
        }, 500);
    }

    public static void clearApplicationData(@NonNull Context context) {
        File cacheDirectory = context.getCacheDir();
        File applicationDirectory = new File(Objects.requireNonNull(cacheDirectory.getParent()));
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            if(fileNames != null)
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) deleteFile(new File(applicationDirectory, fileName));
            }
        }
    }
    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                if(children != null)
                    for (String child : children) {
                        deletedAll = deleteFile(new File(file, child)) && deletedAll;
                    }
            } else
                deletedAll = file.delete();
        }
        return deletedAll;
    }

}
