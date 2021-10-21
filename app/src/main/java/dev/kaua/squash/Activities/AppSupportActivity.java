package dev.kaua.squash.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Objects;

import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.TypeWriter;

@SuppressLint("SetTextI18n")
public class AppSupportActivity extends AppCompatActivity {
    private static final String TAG = "APP_SUPPORT_LOG";
    private CardView paypal_donate, btnShareApp_support;
    private TextView desc_paypal_donate, desc_share_app;

    private static boolean DESC_PAYPAL_DONATE = false;
    private static boolean DESC_SHARE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_support);
        Ids();

        final TypeWriter writer_paypal = new TypeWriter(this);
        final TypeWriter writer_share = new TypeWriter(this);

        //  PayPal Donate Click
        paypal_donate.setOnClickListener(v -> {
            paypal_donate.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_anim));
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(Methods.PAYPAL_DONATE));
            startActivity(i);
        });

        //  PayPal Description CLick
        desc_paypal_donate.setOnClickListener(v -> {
            desc_paypal_donate.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_anim));
            if(!DESC_PAYPAL_DONATE) {
                DESC_PAYPAL_DONATE = true;
                writer_paypal.setCharacterDelay(50);
                writer_paypal.setTextView(desc_paypal_donate);
                writer_paypal.animateText(getString(R.string.paypal_donate_desc) + "\n\n" + getString(R.string.click_to_collapse));
            }else{
                writer_paypal.Cancel();
                desc_paypal_donate.setText(getString(R.string.more_information));
                DESC_PAYPAL_DONATE = false;
            }
        });

        // Share App Click
        btnShareApp_support.setOnClickListener(v -> {
            btnShareApp_support.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_anim));
            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String body = getString(R.string.text_share_app, MyPrefs.getUserInformation(this).getUsername())
                    + "\n\nGoogle Play link: " + Methods.GOOGLE_PLAY_APP_LINK_SHORT;
            myIntent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(myIntent, getString(R.string.share_using)));

            if(ConnectionHelper.isOnline(this)){
                //  Creating analytic for share action
                Bundle bundle_Analytics = new Bundle();
                bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_ID, myFirebaseHelper.getFirebaseUser().getUid() + "_SHARE_APP");
                bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_NAME, MyPrefs.getUserInformation(this).getUsername());
                bundle_Analytics.putString(FirebaseAnalytics.Param.CONTENT_TYPE, MyPrefs.getUserInformation(this).getName_user());
                myFirebaseHelper.getFirebaseAnalytics(this).logEvent(FirebaseAnalytics.Event.SHARE, bundle_Analytics);
            }
        });

        //  Share App Description CLick
        desc_share_app.setOnClickListener(v -> {
            desc_share_app.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_anim));
            if(!DESC_SHARE) {
                DESC_SHARE = true;
                writer_share.setCharacterDelay(50);
                writer_share.setTextView(desc_share_app);
                writer_share.animateText(getString(R.string.share_app_desc) + "\n\n" + getString(R.string.click_to_collapse));
            }else{
                writer_share.Cancel();
                desc_share_app.setText(getString(R.string.more_information));
                DESC_SHARE = false;
            }
        });
    }

    private void Ids() {
        getWindow().setStatusBarColor(getColor(R.color.black_intro));
        Toolbar toolbar = findViewById(R.id.toolbar_app_support);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default toolbar title
        toolbar.setNavigationOnClickListener(v -> finish());

        final TextView msg_support = findViewById(R.id.msg_support_activity);
        paypal_donate = findViewById(R.id.btnMakeDonation_support);
        btnShareApp_support = findViewById(R.id.btnShareApp_support);
        desc_paypal_donate = findViewById(R.id.txt_desc_paypal_donate);
        desc_share_app = findViewById(R.id.txt_desc_share_app);

        if(!MyPrefs.getUserInformation(this).isSupport_visit()){
            final TypeWriter writer_msg = new TypeWriter(this);
            writer_msg.setCharacterDelay(40);
            writer_msg.setTextView(msg_support);
            writer_msg.animateText(getString(R.string.support_description_header));
        }

        MyPrefs.SetSupportVisit(this);
    }

}