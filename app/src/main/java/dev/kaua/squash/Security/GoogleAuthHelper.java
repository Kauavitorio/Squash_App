package dev.kaua.squash.Security;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class GoogleAuthHelper {
    private static GoogleSignInClient googleSignInClient;
    private static GoogleSignInOptions signInOptionsEmail;
    public static final int NO_ACCOUNT_SELECTED = 1201;
    public static final int NO_ACCOUNT = 204;
    public static final int ACCOUNT_NEW = 206;
    public static final int HAS_ACCOUNT = 200;
    public static String GOOGLE_ID;
    public static String GOOGLE_EMAIL;
    public static String GOOGLE_NAME;
    public static String GOOGLE_PHOTO;

    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    public static GoogleSignInOptions getGoogleSignInOptionsEmail(){
        if(signInOptionsEmail == null) signInOptionsEmail = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        return signInOptionsEmail;
    }

    // Build a GoogleSignInClient with the options specified by gso.
    public static GoogleSignInClient getGoogleSignInClient(Context context){
        googleSignInClient = GoogleSignIn.getClient(context, getGoogleSignInOptionsEmail());
        return googleSignInClient;
    }

    public static String getGoogleId(){
        return GOOGLE_ID;
    }

    public static boolean isGoogleLogin(Context context){
        return GoogleSignIn.getLastSignedInAccount(context) != null;
    }

    public static void ResetVariable(){
        googleSignInClient = null;
        signInOptionsEmail = null;
        GOOGLE_ID = null;
        GOOGLE_EMAIL = null;
        GOOGLE_NAME = null;
        GOOGLE_PHOTO = null;
    }
}
