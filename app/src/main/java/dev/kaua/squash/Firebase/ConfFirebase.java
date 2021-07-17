package dev.kaua.squash.Firebase;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ConfFirebase {

    private static FirebaseAnalytics firebaseAnalytics;
    private static FirebaseAuth firebaseAuth;
    private static FirebaseUser firebaseUser;

    public static FirebaseAnalytics getFirebaseAnalytics(Context context){
        if (firebaseAnalytics == null){
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
        return firebaseAnalytics;
    }

    public static FirebaseAuth getFirebaseAuth(){
        if (firebaseAuth == null){
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }

    public static FirebaseUser getFirebaseUser(){
        if (firebaseUser == null)
            firebaseUser = getFirebaseAuth().getCurrentUser();
        return firebaseUser;
    }
}
