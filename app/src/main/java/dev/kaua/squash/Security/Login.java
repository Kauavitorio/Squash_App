package dev.kaua.squash.Security;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.SignInActivity;
import dev.kaua.squash.Activitys.SplashActivity;
import dev.kaua.squash.Activitys.ValidateEmailActivity;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.AsyncUser_Follow;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.LocalDataBase.DaoPosts;
import dev.kaua.squash.LocalDataBase.DaoSystem;
import dev.kaua.squash.LocalDataBase.Notification.DaoNotification;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ShortCutsHelper;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public abstract class Login extends SignInActivity{
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

        String encrypt_password = EncryptHelper.encrypt(EncryptHelper.encrypt(password));
        final String placed = Methods.shuffle(Methods.RandomCharacters(Methods.getRandomAmount()));
        String ip = ConnectionHelper.getIp(context);
        ip =  Objects.requireNonNull(EncryptHelper.encrypt(EncryptHelper.encrypt(EncryptHelper.encrypt(EncryptHelper.encrypt(ip)))))
                .replace("+", "XXXX7").replace("/", "XXXX1").replace("==", "XXXX9") + placed;
        encrypt_password = placed + encrypt_password;
        DtoAccount account = new DtoAccount(EncryptHelper.encrypt(login_method), encrypt_password,
                EncryptHelper.encrypt(device_login.substring(0,1).toUpperCase().concat(device_login.substring(1))),
                EncryptHelper.encrypt("0-river-reliable"), EncryptHelper.encrypt(formattedDate), 0, EncryptHelper.encrypt(placed),
                ip);
        final AccountServices login_service = retrofitUser.create(AccountServices.class);
        Call<DtoAccount> call;
        if(!GoogleAuthHelper.isGoogleLogin(context)) call = login_service.login(account, Methods.RandomCharactersWithoutSpecials(Methods.getRandomAmount()));
        else
            call = login_service.login_with_Google(account, Methods.RandomCharactersWithoutSpecials(Methods.getRandomAmount()));
        call.enqueue(new Callback<DtoAccount>() {
            @Override
            public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                //  Checking api return code
                if(response.code() == 200){
                    loadingDialog.dismissDialog();
                    new Handler().postDelayed(() -> loadingDialog.startLoading(), 100);
                    //  Clear all prefs before login user
                    mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
                    mPrefs.edit().clear().apply();

                    if(response.body() != null && response.body().getActive() > DtoAccount.ACCOUNT_DISABLE){
                        try {
                            txt_login_title.setText(context.getString(R.string.welcome));
                        }catch (Exception ex){
                            Log.d(TAG, ex.toString());
                        }
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
                        editor.putLong("pref_active", response.body().getActive());
                        editor.putString("pref_type_acc", response.body().getType_acc());
                        editor.apply();

                        //  Getting Followers and Followings
                        Methods.LoadFollowersAndFollowing(context, 0);
                        AsyncUser_Follow asyncUser_follow = new AsyncUser_Follow((Activity) context);
                        //noinspection unchecked
                        asyncUser_follow.execute();

                        //  Log in User On Firebase
                        mAuth = myFirebaseHelper.getFirebaseAuth();
                        mAuth.signOut();

                        //  Init Analytics
                        mFirebaseAnalytics = myFirebaseHelper.getFirebaseAnalytics(context);

                        //  Login user in firebase to get user instance
                        mAuth.signInWithEmailAndPassword(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getEmail())), Objects.requireNonNull(EncryptHelper.decrypt(response.body().getToken())))
                                .addOnCompleteListener(task -> {
                                    Log.d(TAG, "Login Ok");
                                    Log.d(TAG, "User " + mAuth.getUid());

                                    DaoNotification daoNotification = new DaoNotification(context);
                                    daoNotification.Register_User(mAuth.getUid());

                                    //  Creating analytic for login event
                                    Bundle bundle_Analytics = new Bundle();
                                    bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_ID, mAuth.getUid());
                                    bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_NAME, EncryptHelper.decrypt(response.body().getUsername()));
                                    bundle_Analytics.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle_Analytics);

                                    //  Go To main
                                    new Handler().postDelayed(() -> {
                                        final Intent i = new Intent(context, MainActivity.class);
                                        i.putExtra("shared", 0);
                                        ActivityCompat.startActivity(context, i, ActivityOptionsCompat.makeCustomAnimation(context, R.anim.move_to_left_go, R.anim.move_to_right_go).toBundle());
                                        ((Activity) context).finish();
                                        try {
                                            new Handler().postDelayed(() -> loadingDialog.dismissDialog(), 300);
                                        }catch (Exception ex){
                                            Log.d(TAG, ex.toString());
                                        }
                                    }, 1000);
                                });
                    }else LogOut(context, LOGOUT_STATUS_WITHOUT_FLAG, DISABLE_ACCOUNT);

                }else if(response.code() == 206){
                    Log.d(TAG, "Email not validated");
                    loadingDialog.dismissDialog();
                    mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
                    mPrefs.edit().clear().apply();
                    Intent i = new Intent(context, ValidateEmailActivity.class);
                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(context,R.anim.move_to_left_go, R.anim.move_to_right_go);
                    //noinspection ConstantConditions
                    i.putExtra(ValidateEmailActivity.ACCOUNT_ID_ID, EncryptHelper.decrypt(response.body().getAccount_id_cry()));
                    i.putExtra(ValidateEmailActivity.LOGIN_METHOD_ID, login_method);
                    i.putExtra(ValidateEmailActivity.PASSWORD_ID, password);
                    i.putExtra(ValidateEmailActivity.TYPE_VALIDATE_ID, 1);
                    ActivityCompat.startActivity(context, i, activityOptionsCompat.toBundle());
                    ((Activity) context).finish();
                }else if(response.code() == 401) {
                    loadingDialog.dismissDialog();
                    mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
                    mPrefs.edit().clear().apply();
                    try {
                        SignInActivity.getInstance().Invalid_email_or_password();
                    }catch (Exception ex){
                        Warnings.showWeHaveAProblem(context, ErrorHelper.LOGIN_ACTION_PASSWORD_EMAIL_WARNING);
                    }
                }
                else {
                    loadingDialog.dismissDialog();
                    Warnings.showWeHaveAProblem(context, ErrorHelper.LOGIN_ACTION);
                }
            }
            @Override
            public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                loadingDialog.dismissDialog();
                Warnings.showWeHaveAProblem(context, ErrorHelper.LOGIN_ACTION);
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
        Log.d(TAG, "Root => "+ isRooted());
        Log.d(TAG, "Debug => "+ Debug.isDebuggerConnected());

        String encrypt_password = EncryptHelper.encrypt(EncryptHelper.encrypt(password));
        String placed = Methods.shuffle(Methods.RandomCharacters(Methods.getRandomAmount()));
        String ip = ConnectionHelper.getIp(context);
        ip =  placed + Objects.requireNonNull(EncryptHelper.encrypt(EncryptHelper.encrypt(EncryptHelper.encrypt(EncryptHelper.encrypt(ip)))))
                .replace("+", "XXXX7").replace("/", "XXXX1").replace("==", "XXXX9");
        encrypt_password = placed + encrypt_password;
        DtoAccount account = new DtoAccount(EncryptHelper.encrypt(login_method), encrypt_password,
                EncryptHelper.encrypt(device_login.substring(0,1).toUpperCase().concat(device_login.substring(1))),
                EncryptHelper.encrypt(placed), EncryptHelper.encrypt(formattedDate), 0, EncryptHelper.encrypt(placed), ip);
        final AccountServices login_service = retrofitUser.create(AccountServices.class);
        Call<DtoAccount> call;
        if(!GoogleAuthHelper.isGoogleLogin(context)) call = login_service.login(account, Methods.RandomCharactersWithoutSpecials(Methods.getRandomAmount()));
        else call = login_service.login_with_Google(account, Methods.RandomCharactersWithoutSpecials(Methods.getRandomAmount()));
        call.enqueue(new Callback<DtoAccount>() {
            @Override
            public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                Log.d(TAG, "Login Status => " + response.code());
                //  Checking api return code
                if(response.code() == 200){
                    if(response.body() != null){
                        //  Clear all prefs before login user
                        mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);

                        new DaoNotification(context).Register_User(myFirebaseHelper.getFirebaseUser().getUid());

                        if(response.body().getActive() > DtoAccount.ACCOUNT_DISABLE){

                            //  Update active info
                            reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE).child(Objects.requireNonNull(myFirebaseHelper.getFirebaseAuth().getUid()));
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("active", response.body().getActive());
                            hashMap.put("verification_level", response.body().getVerification_level());

                            reference.updateChildren(hashMap).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()) Log.d(TAG, "Update in Realtime database Successful");
                            });

                            //  Update all user posts
                            DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference();
                            Query applesQuery = ref.child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD).orderByChild("account_id")
                                    .equalTo(EncryptHelper.encrypt(String.valueOf(MyPrefs.getUserInformation(context).getAccount_id())));
                            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("active", response.body().getActive());
                                        hashMap.put("verification_level", response.body().getVerification_level());
                                        appleSnapshot.getRef().updateChildren(hashMap);
                                    }
                                }
                                @Override
                                public void onCancelled(@NotNull DatabaseError databaseError) {
                                    Log.e(TAG, "onCancelled", databaseError.toException());
                                }
                            });

                            if(response.body() != null){
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
                                editor.putLong("pref_active", response.body().getActive());
                                editor.putString("pref_type_acc", response.body().getType_acc());
                                editor.apply();

                                //  Getting Followers and Followings
                                Methods.LoadFollowersAndFollowing(context, 1);
                            }
                        }else LogOut(context, LOGOUT_STATUS_WITHOUT_FLAG, DISABLE_ACCOUNT);
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
                    LogOut(context, LOGOUT_STATUS_WITHOUT_FLAG, NOT_DISABLE_ACCOUNT);
                }
            }
            @Override
            public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) { Log.d(TAG, t.getMessage()); }
        });
    }

    private static boolean isRooted() {
        return findBinary("su");
    }

    public static boolean findBinary(String binaryName) {
        boolean found = false;
        String[] places = {"/sbin/", "/system/bin/",
                "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/"};
        for (String where : places) {
            if (new File(where + binaryName).exists()) {
                found = true;
                break;
            }
        }
        return found;
    }

    static Handler timer = new Handler();
    public static final int LOGOUT_STATUS_FLAG = 0;
    public static final int LOGOUT_STATUS_WITHOUT_FLAG = 1;
    public static final int NOT_DISABLE_ACCOUNT = 1;
    public static final int DISABLE_ACCOUNT = 333;
    public static void LogOut(Context context, int status, int active){
        final ShortCutsHelper shortCutsHelper = new ShortCutsHelper(context);
        shortCutsHelper.removeShorCuts();
        loadingDialog = null;
        loadingDialog = new LoadingDialog((Activity)context);
        loadingDialog.startLoading();
        Methods.status_chat(Methods.OFFLINE, context); // Set User status with offline

        myFirebaseHelper.LogOut(); // LogOut on firebase

        clearApplicationData(context); // Clear app cache and data

        MyPrefs.logOut(context); // Remove all preferences on app

        if(GoogleAuthHelper.isGoogleLogin(context))
            GoogleAuthHelper.getGoogleSignInClient(context).signOut()
                .addOnCompleteListener((Activity) context, task -> GoogleAuthHelper.ResetVariable());

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
            daoPosts.ClearTable(DaoPosts.DROP_ALL);

            timer.postDelayed(() -> {
                ((Activity)context).finishAffinity();
                DaoSystem daoSystem = new DaoSystem(context);
                daoSystem.setNeedResetAccount(MyPrefs.OKAY_RESET);
                final Intent i;
                if(status == LOGOUT_STATUS_FLAG ) i = new Intent(context, SplashActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                else i = new Intent(context, SplashActivity.class);
                i.putExtra(SplashActivity.ACCOUNT_DISABLE, active);
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
