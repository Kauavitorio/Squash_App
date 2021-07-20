package dev.kaua.squash.Firebase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfFirebase {

    private static FirebaseAnalytics firebaseAnalytics;
    private static FirebaseAuth firebaseAuth;
    private static FirebaseUser firebaseUser;
    private static StorageReference firebaseStorage;

    public static FirebaseAnalytics getFirebaseAnalytics(Context context){
        if (firebaseAnalytics == null)
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        return firebaseAnalytics;
    }

    public static FirebaseAuth getFirebaseAuth(){
        if (firebaseAuth == null)
            firebaseAuth = FirebaseAuth.getInstance();
        return firebaseAuth;
    }

    public static FirebaseUser getFirebaseUser(){
        if (firebaseUser == null)
            firebaseUser = getFirebaseAuth().getCurrentUser();
        return firebaseUser;
    }

    public static StorageReference getFirebaseStorage(){
        if (firebaseStorage == null)
            firebaseStorage = FirebaseStorage.getInstance().getReference();
        return firebaseStorage;
    }
}
