package dev.kaua.squash.Activities.Setting;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.Locale;

import dev.kaua.squash.Activities.Setting.AccountSetting.Fragments.LanguageFragment;
import dev.kaua.squash.Activities.SplashActivity;
import dev.kaua.squash.R;

public class LocaleHelper extends LanguageFragment{
    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    private static final String TAG = "LocaleHelper";

    // the method is used to set the language at runtime
    public static void setLocale(Activity context, String language) {
        persist(context, language);

        // updating the language for devices above android nougat
        updateResources(context, language);
        // for devices having lower version of android os
    }

    private static void persist(Context context, String language) {
        Log.d(TAG, "Lang -> " + language);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    public static String getCurrentLanguage(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, context.getString(R.string.language_tag));
    }

    // the method is used update the language of application by creating
    // object of inbuilt Locale class and passing language argument to it
    @TargetApi(Build.VERSION_CODES.N)
    private static void updateResources(Activity context, String languageCode) {
        Log.d(TAG, "Lang -> " + languageCode);
        Locale locale;
        if(languageCode.equals(PORTUGUESE)){
            String[] PT_BR = PORTUGUESE.split("-");
            locale = new Locale(PT_BR[0], PT_BR[1]);
        }else locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        context.finishAffinity();
        Intent i = new Intent(context, SplashActivity.class);
        context.startActivity(i);
    }
}