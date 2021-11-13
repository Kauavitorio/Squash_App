package dev.kaua.squash;

import android.app.Application;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class BaseApplication extends Application { // extend from MultidexApplication if multidex is required by your application

    @Override
    public void onCreate() {
        super.onCreate();

        /*
         *  Crashlytics is enabled by default,
         *  Disable it for debug builds & USB Debugging
         * */

        if(BuildConfig.DEBUG){
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
        }
    }
}
