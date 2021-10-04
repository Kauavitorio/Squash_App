package dev.kaua.squash.Firebase;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class myFirebaseHelper {
    public static final String POSTS_REFERENCE = "Posts";
    public static final String MEDIAS_REFERENCE = "Medias";
    public static final String AUDIOS_REFERENCE = "Audios";
    public static final String USERS_REFERENCE = "Users";
    public static final String STORY_REFERENCE = "Story";
    public static final String STORY_VIEWS = "views";
    public static final String VERIFICATION_REFERENCE = "Verification";
    public static final String POINTS_REFERENCE = "Points";
    public static final String PROFILE_REFERENCE = "Profile";
    public static final String PUBLISHED_CHILD = "Published";
    public static final String CHATS_REFERENCE = "Chats";
    public static final String TOKENS_REFERENCE = "Tokens";
    public static final String CHAT_LIST_REFERENCE = "Chatslist";
    public static final String STORY_TUTORIAL_REFERENCE = "StoryTutorial";
    public static final String SYSTEM_REFERENCE = "System";
    private static FirebaseAnalytics firebaseAnalytics;
    private static FirebaseAuth firebaseAuth;
    private static FirebaseUser firebaseUser;
    private static StorageReference firebaseStorage;
    private static FirebaseStorage firebaseStorageInstance;
    private static FirebaseDatabase firebaseDatabase;
    private static FirebaseMessaging firebaseMessaging;
    private static FirebaseDynamicLinks firebaseDynamicLinks;

    public static FirebaseAnalytics getFirebaseAnalytics(Context context){
        if (firebaseAnalytics == null) firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        return firebaseAnalytics;
    }

    public static FirebaseDynamicLinks getFirebaseDynamicLinks(){
        if (firebaseDynamicLinks == null) firebaseDynamicLinks = FirebaseDynamicLinks.getInstance();
        return firebaseDynamicLinks;
    }

    public static FirebaseAuth getFirebaseAuth(){
        if (firebaseAuth == null) firebaseAuth = FirebaseAuth.getInstance();
        return firebaseAuth;
    }

    public static FirebaseDatabase getFirebaseDatabase(){
        if (firebaseDatabase == null) firebaseDatabase = FirebaseDatabase.getInstance();
        return firebaseDatabase;
    }

    public static FirebaseUser getFirebaseUser(){
        if (firebaseUser == null) firebaseUser = getFirebaseAuth().getCurrentUser();
        return firebaseUser;
    }

    public static StorageReference getFirebaseStorage(){
        if (firebaseStorage == null) firebaseStorage = FirebaseStorage.getInstance().getReference();
        return firebaseStorage;
    }

    public static FirebaseStorage getFirebaseStorageInstance(){
        if (firebaseStorageInstance == null) firebaseStorageInstance = FirebaseStorage.getInstance();
        return firebaseStorageInstance;
    }

    public static FirebaseMessaging getFirebaseMessaging(){
        if (firebaseMessaging == null) firebaseMessaging = FirebaseMessaging.getInstance();
        return firebaseMessaging;
    }

    public static void LogOut(){
        getFirebaseAuth().signOut();
        firebaseAnalytics = null;
        firebaseAuth = null;
        firebaseUser = null;
        firebaseStorage = null;
        firebaseDatabase = null;
        firebaseDynamicLinks = null;
        firebaseStorageInstance = null;
        firebaseMessaging = null;
    }
}
