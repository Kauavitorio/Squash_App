package dev.kaua.squash.Tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Security.EncryptHelper;

import static android.content.Context.MODE_PRIVATE;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class MyPrefs {
    public static final String PREFS_USER = "myPrefs";
    public static final String PREFS_NETWORK_USAGE = "myPrefsNetWorkUsage";
    public static final String PREFS_CONFIG = "myPrefsConfiguration";
    public static final String PREFS_BASE = "myPrefsBASE01";
    public static final String PREFS_UPDATES = "myPrefsUpdates";
    public static final String PREFS_NOTIFICATION = "myPrefsNotify";
    public static final String PREFS_TERMS = "myPrefsTerms_Experience";
    public static final String TAG = "MyPrefs";
    private static final DtoAccount account = new DtoAccount();
    public static SharedPreferences sp;

    public static DtoAccount getUserInformation(@NonNull Context context){
        sp = context.getSharedPreferences(PREFS_USER, MODE_PRIVATE);

        //  Passing all preferences to DTO
        String account_id = EncryptHelper.decrypt(sp.getString("pref_account_id", null));
        if(account_id != null) account.setAccount_id(Long.parseLong(account_id));
        account.setName_user(EncryptHelper.decrypt(sp.getString("pref_name_user", null)));
        account.setUsername(EncryptHelper.decrypt(sp.getString("pref_username", null)));
        account.setEmail(EncryptHelper.decrypt(sp.getString("pref_email", null)));
        account.setPhone_user(EncryptHelper.decrypt(sp.getString("pref_phone_user", null)));
        account.setBanner_user(EncryptHelper.decrypt(sp.getString("pref_banner_user", null)));
        account.setPhone_user(EncryptHelper.decrypt(sp.getString("pref_phone_user", null)));
        account.setProfile_image(EncryptHelper.decrypt(sp.getString("pref_profile_image", null)));
        account.setBio_user(EncryptHelper.decrypt(sp.getString("pref_bio_user", null)));
        account.setUrl_user(EncryptHelper.decrypt(sp.getString("pref_url_user", null)));
        account.setFollowing(EncryptHelper.decrypt(sp.getString("pref_following", null)));
        account.setFollowers(EncryptHelper.decrypt(sp.getString("pref_followers", null)));
        account.setBorn_date(EncryptHelper.decrypt(sp.getString("pref_born_date", null)));
        account.setJoined_date(EncryptHelper.decrypt(sp.getString("pref_joined_date", null)));
        account.setPassword(EncryptHelper.decrypt(sp.getString("pref_password", null)));
        account.setToken(EncryptHelper.decrypt(sp.getString("pref_token", null)));
        account.setVerification_level(EncryptHelper.decrypt(sp.getString("pref_verification_level", null)));
        account.setActive(sp.getLong("pref_active", DtoAccount.ACCOUNT_ACTIVE));

        return account;
    }

    public static void setUpdateRequest_Show(@NonNull Context context, int request){
        sp = context.getSharedPreferences(PREFS_UPDATES, MODE_PRIVATE);

        //  Add Request prefs
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("pref_request", request);
        editor.apply();
    }

    public static int getUpdateRequest_Show(@NonNull Context context){
        sp = context.getSharedPreferences(PREFS_UPDATES, MODE_PRIVATE);
        return sp.getInt("pref_request", 0);
    }

    public static void InsertNetworkCount(@NonNull Context context){
        sp = context.getSharedPreferences(PREFS_NETWORK_USAGE, MODE_PRIVATE);

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm a").format(new Date());
        //  Add Request prefs
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("pref_start_time", timeStamp);
        editor.apply();
    }

    public static final String OKAY_RESET = "OKAY";

    public static void InsertNetworkStatisticsReset(@NonNull Context context, long rx, long tx){
        sp = context.getSharedPreferences(PREFS_NETWORK_USAGE, MODE_PRIVATE);

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm a").format(new Date());
        //  Add Request prefs
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("pref_last_reset", timeStamp);
        editor.putLong("pref_rx", rx);
        editor.putLong("pref_tx", tx);
        editor.apply();
    }

    public static String get_NetWorkLastReset(@NonNull Context context){
        sp = context.getSharedPreferences(PREFS_NETWORK_USAGE, MODE_PRIVATE);
        return sp.getString("pref_last_reset", null);
    }

    @NonNull
    @Contract("_ -> new")
    public static long[] get_RX_TX_Subtraction(@NonNull Context context){
        sp = context.getSharedPreferences(PREFS_NETWORK_USAGE, MODE_PRIVATE);
        return new long[]{sp.getLong("pref_rx", 0), sp.getLong("pref_tx", 0)};
    }

    public static String get_NetWorkStartCount(@NonNull Context context){
        sp = context.getSharedPreferences(PREFS_NETWORK_USAGE, MODE_PRIVATE);
        return sp.getString("pref_start_time", null);
    }

    public static void logOut(@NonNull Context context){
        //  Clear User Prefs
        sp = context.getSharedPreferences(PREFS_USER, MODE_PRIVATE);
        sp.edit().clear().apply();
        //  Clear User Config Prefs
        sp = context.getSharedPreferences(PREFS_CONFIG, MODE_PRIVATE);
        sp.edit().clear().apply();
        //  Clear User Notification Config Prefs
        sp = context.getSharedPreferences(PREFS_NOTIFICATION, MODE_PRIVATE);
        sp.edit().clear().apply();
        //  Clear User Terms Prefs
        sp = context.getSharedPreferences(PREFS_TERMS, MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    public static final String NONE_USER = "none";
    public static final String CURRENT_USER = "currentUser";
    public static void currentUser(Context context, String userId){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NOTIFICATION, MODE_PRIVATE).edit();
        editor.putString(CURRENT_USER, userId);
        editor.apply();
    }

    public static String getCurrentUser(Context context){
        sp = context.getSharedPreferences(MyPrefs.PREFS_NOTIFICATION, MODE_PRIVATE);
        return sp.getString(CURRENT_USER, NONE_USER);
    }
}
