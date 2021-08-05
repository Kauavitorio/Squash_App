package dev.kaua.squash.Tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Security.EncryptHelper;

import static android.content.Context.MODE_PRIVATE;

import androidx.annotation.NonNull;

public abstract class MyPrefs {
    public static final String PREFS_USER = "myPrefs";
    public static final String PREFS_CONFIG = "myPrefsConfiguration";
    public static final String PREFS_UPDATES = "myPrefsUpdates";
    public static final String PREFS_PRIVACY_POLICY = "myPrefsPrivacyPolicy";
    public static final String PREFS_NOTIFICATION = "myPrefsNotify";
    public static final String PREFS_TERMS = "myPrefsTerms_Experience";
    public static final String TAG = "MyPrefs";
    private static final DtoAccount account = new DtoAccount();
    public static SharedPreferences sp;

    @SuppressWarnings("ConstantConditions")
    public static DtoAccount getUserInformation(@NonNull Context context){
        sp = context.getSharedPreferences(PREFS_USER, MODE_PRIVATE);

        //  Passing all preferences to DTO
        account.setAccount_id(Long.parseLong(EncryptHelper.decrypt(sp.getString("pref_account_id", null))));
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

        return account;
    }

    public static void setUpdateRequest_Show(@NonNull Context context, int request){
        sp = context.getSharedPreferences(PREFS_UPDATES, MODE_PRIVATE);
        sp.edit().clear().apply();

        //  Add Request prefs
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("pref_request", request);
        editor.apply();
    }

    public static int getUpdateRequest_Show(@NonNull Context context){
        sp = context.getSharedPreferences(PREFS_UPDATES, MODE_PRIVATE);
        return sp.getInt("pref_request", 0);
    }

    public static long Privacy_Policy_Version(@NonNull Context context){
        sp = context.getSharedPreferences(PREFS_PRIVACY_POLICY, MODE_PRIVATE);
        Log.d(TAG, "Current version -> " + sp.getLong("pref_version", 0));
        return sp.getLong("pref_version", 0);
    }

    public static void setPrivacy_Policy(@NonNull Context context, long version){
        sp = context.getSharedPreferences(PREFS_PRIVACY_POLICY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("pref_version", version);
        editor.apply();
        Log.d(TAG, version + " <- New Version");
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
        //  Clear Privacy Policy Prefs
        sp = context.getSharedPreferences(PREFS_PRIVACY_POLICY, MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}
