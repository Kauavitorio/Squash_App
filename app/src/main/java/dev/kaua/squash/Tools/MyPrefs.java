package dev.kaua.squash.Tools;

import android.content.Context;
import android.content.SharedPreferences;

import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Security.EncryptHelper;

import static android.content.Context.MODE_PRIVATE;

public abstract class MyPrefs {
    public static final String PREFS_USER = "myPrefs";
    public static final String PREFS_CONFIG = "myPrefsConfiguration";
    public static final String PREFS_NOTIFICATION = "myPrefsNOTFY";
    public static final String PREFS_TERMS = "myPrefsTerms_Experience";
    private static final DtoAccount account = new DtoAccount();
    public static SharedPreferences sp;

    @SuppressWarnings("ConstantConditions")
    public static DtoAccount getUserInformation(Context context){
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
}
