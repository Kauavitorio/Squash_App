package dev.kaua.squash.Activities.Setting.FollowAndInvite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.analytics.FirebaseAnalytics;

import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import jp.wasabeef.blurry.Blurry;

public class FollowAndInviteActivity extends AppCompatActivity {

    LinearLayout invite_by_whatsapp, invite_by_email, invite_by_choose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_and_invite);
        loadDefaultBackground();
        invite_by_whatsapp = findViewById(R.id.invite_by_whatsapp);
        invite_by_email = findViewById(R.id.invite_by_email);
        invite_by_choose = findViewById(R.id.invite_by_choose);

        final String SHARE_MSG = getString(R.string.text_share_app, MyPrefs.getUserInformation(this).getUsername())
                + "\n\nGoogle Play link: " + Methods.GOOGLE_PLAY_APP_LINK_SHORT;

        invite_by_whatsapp.setOnClickListener(v -> {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, SHARE_MSG);
            try {
                startActivity(whatsappIntent);
                LogShare();
            } catch (android.content.ActivityNotFoundException ex) {
                ToastHelper.toast(this, getString(R.string.whatsapp_have_not_been_installed), ToastHelper.LONG_DURATION);
            }
        });

        invite_by_email.setOnClickListener(v -> composeEmail(MyPrefs.getUserInformation(this).getEmail(), SHARE_MSG));

        invite_by_choose.setOnClickListener(v -> {
            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            myIntent.putExtra(Intent.EXTRA_TEXT, SHARE_MSG);
            startActivity(Intent.createChooser(myIntent, getString(R.string.share_using)));
            LogShare();
        });
    }

    public void composeEmail(String addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.invited_you_to_join_squash,
                MyPrefs.getUserInformation(this).getName_user()));
        intent.putExtra(Intent.EXTRA_TEXT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            LogShare();
        }
    }

    void LogShare(){
        if(ConnectionHelper.isOnline(this)){
            //  Creating analytic for share action
            Bundle bundle_Analytics = new Bundle();
            bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_ID, myFirebaseHelper.getFirebaseUser().getUid() + "_SHARE_APP");
            bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_NAME, MyPrefs.getUserInformation(this).getUsername());
            bundle_Analytics.putString(FirebaseAnalytics.Param.CONTENT_TYPE, MyPrefs.getUserInformation(this).getName_user());
            myFirebaseHelper.getFirebaseAnalytics(this).logEvent(FirebaseAnalytics.Event.SHARE, bundle_Analytics);
        }
    }

    void loadDefaultBackground(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        Glide.with(this).asBitmap().load(R.drawable.bg_invite_friends)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Blurry.with(FollowAndInviteActivity.this).radius(10).sampling(8)
                                .color(Color.argb(66, 5, 5, 5))
                                .from(resource).into(findViewById(R.id.invite_follow_image_background));
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }
}