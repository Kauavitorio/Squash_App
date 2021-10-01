package dev.kaua.squash.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import dev.kaua.squash.Activitys.SquashShop.SquashShopActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.TypeWriter;

@SuppressLint("SetTextI18n")
public class AppSupportActivity extends AppCompatActivity {
    private static final String TAG = "APP_SUPPORT_LOG";
    private RewardedAd mRewardedAd;
    private AdView adView_bottom;
    private CardView ad_support, paypal_donate, btnShareApp_support;
    private ProgressBar progress_load_ad;
    private LinearLayout btn_squash_shop;
    private TextView txt_watch_ad, my_points_ad, desc_paypal_donate, desc_share_app, desc_ad_app, my_goal;
    private DatabaseReference reference;

    private static boolean DESC_PAYPAL_DONATE = false;
    private static boolean DESC_SHARE = false;
    private static boolean DESC_AD = false;
    private static boolean COOLDOWN_AD = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_support);
        Ids();

        final TypeWriter writer_paypal = new TypeWriter(this);
        final TypeWriter writer_share = new TypeWriter(this);
        final TypeWriter writer_ad = new TypeWriter(this);

        //  Ad Click
        ad_support.setOnClickListener(v -> {
            ad_support.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_anim));
            if(ConnectionHelper.isOnline(this)){
                if(!COOLDOWN_AD){
                    ad_support.setEnabled(false);
                    progress_load_ad.setVisibility(View.VISIBLE);
                    txt_watch_ad.setVisibility(View.GONE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                RewardedAd.load(this, Methods.REWARDED_AD_ID,
                        adRequest, new RewardedAdLoadCallback() {
                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                // Handle the error.
                                Log.d(TAG, loadAdError.getMessage());
                                progress_load_ad.setVisibility(View.GONE);
                                txt_watch_ad.setVisibility(View.VISIBLE);
                                ToastHelper.toast(AppSupportActivity.this, loadAdError.getMessage(), ToastHelper.SHORT_DURATION);
                                ad_support.setEnabled(true);
                                mRewardedAd = null;
                            }

                            @Override
                            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                                mRewardedAd = rewardedAd;
                                progress_load_ad.setVisibility(View.GONE);
                                txt_watch_ad.setVisibility(View.VISIBLE);
                                ad_support.setEnabled(true);

                                COOLDOWN_AD = true;
                                Log.d(TAG, "Ad was loaded.");

                                if (mRewardedAd != null) {
                                    mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                        @Override
                                        public void onAdShowedFullScreenContent() {
                                            // Called when ad is shown.
                                            Log.d(TAG, "Ad was shown.");
                                        }

                                        @Override
                                        public void onAdFailedToShowFullScreenContent(@androidx.annotation.NonNull AdError adError) {
                                            // Called when ad fails to show.
                                            Log.d(TAG, "Ad failed to show.");
                                            Log.d(TAG, "Error -> " + adError.getMessage());
                                            StartCoolDown();
                                        }

                                        @Override
                                        public void onAdDismissedFullScreenContent() {
                                            // Called when ad is dismissed.
                                            // Set the ad reference to null so you don't show the ad a second time.
                                            Log.d(TAG, "Ad was dismissed.");
                                            StartCoolDown();
                                            mRewardedAd = null;
                                        }
                                    });
                                    Activity activityContext = AppSupportActivity.this;
                                    mRewardedAd.show(activityContext, rewardItem -> {
                                        final int rewardAmount = rewardItem.getAmount();
                                        final String rewardType = rewardItem.getType();
                                        // Handle the reward.
                                        Log.d(TAG, "The user earned the reward.");
                                        Log.d(TAG, "Amount -> " + rewardAmount);
                                        Log.d(TAG, "Type -> " + rewardType);
                                        UpdateAd_Points(rewardAmount);
                                        Toast.makeText(AppSupportActivity.this,
                                                getString(R.string.thanks_for_watching_the_ad, String.valueOf(rewardAmount)), Toast.LENGTH_SHORT).show();

                                        StartCoolDown();
                                    });
                                } else {
                                    Log.d(TAG, "The rewarded ad wasn't ready yet.");
                                }
                            }
                        });
                }
            }else ToastHelper.toast(this, getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
        });

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

        //  Ad Description CLick
        desc_ad_app.setOnClickListener(v -> {
            desc_ad_app.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_anim));
            if(!DESC_AD) {
                DESC_AD = true;
                writer_ad.setCharacterDelay(40);
                writer_ad.setTextView(desc_ad_app);
                writer_ad.animateText(getString(R.string.ad_desc) + "\n\n" + getString(R.string.click_to_collapse));
            }else{
                writer_ad.Cancel();
                desc_ad_app.setText(getString(R.string.more_information));
                DESC_AD = false;
            }
        });

        //  Squash Shop Click
        btn_squash_shop.setOnClickListener(v -> {
            btn_squash_shop.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_anim));
            Intent i = new Intent(this, SquashShopActivity.class);
            startActivity(i);
            finish();
        });
    }

    private static boolean ON_COOLDOWN = false;
    private void StartCoolDown() {
        if(!ON_COOLDOWN){
            ON_COOLDOWN = true;
            Log.d(TAG, "----- Start CoolDown -----");
            new CountDownTimer(25000, 1000) {

                public void onTick(long duration) {
                    //tTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
                    //here you can have your logic to set text to edittext resource id
                    // Duration
                    long Mmin = (duration / 1000) / 60;
                    long Ssec = (duration / 1000) % 60;
                    if (Ssec < 10) {
                        txt_watch_ad.setText("" + Mmin + ":0" + Ssec);
                    } else txt_watch_ad.setText("" + Mmin + ":" + Ssec);
                }

                public void onFinish() {
                    COOLDOWN_AD = false;
                    ON_COOLDOWN = false;
                    txt_watch_ad.setText(getString(R.string.watch_ad));
                    Log.d(TAG, "----- End CoolDown -----");
                }

            }.start();
        }
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
        adView_bottom = findViewById(R.id.adView_bottom);
        ad_support = findViewById(R.id.btnWatchAd_support);
        paypal_donate = findViewById(R.id.btnMakeDonation_support);
        btnShareApp_support = findViewById(R.id.btnShareApp_support);
        txt_watch_ad = findViewById(R.id.txt_watch_ad);
        progress_load_ad = findViewById(R.id.progress_load_ad);
        my_points_ad = findViewById(R.id.my_points_ad);
        desc_paypal_donate = findViewById(R.id.txt_desc_paypal_donate);
        desc_share_app = findViewById(R.id.txt_desc_share_app);
        desc_ad_app = findViewById(R.id.txt_desc_ad_app);
        my_goal = findViewById(R.id.my_goal);
        btn_squash_shop = findViewById(R.id.btn_squash_shop);

        InitializeBannerAd();
        InitializeRewardedAd();
        InitializeAd_Points();

        if(!MyPrefs.getUserInformation(this).isSupport_visit()){
            final TypeWriter writer_msg = new TypeWriter(this);
            writer_msg.setCharacterDelay(40);
            writer_msg.setTextView(msg_support);
            writer_msg.animateText(getString(R.string.support_description_header));
        }

        MyPrefs.SetSupportVisit(this);
    }

    String Format_Point(final long points){
        String point_ft;
        try {
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
            DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

            symbols.setGroupingSeparator(' ');
            formatter.setDecimalFormatSymbols(symbols);
            point_ft = formatter.format(points);
        }catch (Exception ex){
            Log.d(TAG, ex.getMessage());
            point_ft = String.valueOf(points);
        }
        return point_ft;
    }

    private void InitializeAd_Points() {
        my_goal.setText(getString(R.string.goal) + ": " + Format_Point(Methods.VERIFY_AD_GOAL));
        my_points_ad.setText(getString(R.string.squash_points) + ": " + Format_Point(MyPrefs.getUserInformation(this).getAd_points()));
        ComparePoints(MyPrefs.getUserInformation(this).getAd_points());

        reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.POINTS_REFERENCE).child(myFirebaseHelper.getFirebaseUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                DtoAccount account = snapshot.getValue(DtoAccount.class);
                if(account != null){
                    my_points_ad.setText(getString(R.string.squash_points) + ": " + Format_Point(account.getAd_points()));
                    ComparePoints(account.getAd_points());
                    if(account.getAd_points() != MyPrefs.getUserInformation(AppSupportActivity.this).getAd_points()){
                        MyPrefs.SetAd_Point(AppSupportActivity.this, account.getAd_points());
                    }
                }
            }
            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {}
        });
    }

    void ComparePoints(final long current){
        DecimalFormat df =  new DecimalFormat("0.00");
        String percentage_str;
        my_goal.setVisibility(View.VISIBLE);
        my_points_ad.setVisibility(View.VISIBLE);
        if(current >= Methods.VERIFY_AD_GOAL){
            my_goal.setTextColor(getColor(R.color.status_on));
        }else my_goal.setTextColor(getColor(R.color.black));

        float percentage = (float) (current * 100) / Methods.VERIFY_AD_GOAL;
        if(percentage > 100) percentage_str = "+100%";
        else if(percentage == 0) percentage_str = "0%";
        else percentage_str = df.format(percentage) + "%";
        my_points_ad.setText(my_points_ad.getText().toString() + " | " + percentage_str);
    }

    private void UpdateAd_Points(final long points) {
        final long current_points = MyPrefs.getUserInformation(this).getAd_points();
        final long final_points = current_points + points;
        reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.POINTS_REFERENCE).child(myFirebaseHelper.getFirebaseUser().getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("ad_points", final_points);
        reference.updateChildren(hashMap).addOnCompleteListener(task1 -> {
            my_points_ad.setText(getString(R.string.squash_points) + ": " + Format_Point(MyPrefs.SetAd_Point(AppSupportActivity.this, final_points)));
            if(task1.isSuccessful()) Log.d(TAG, "Register in database Successful");
            ComparePoints(MyPrefs.getUserInformation(AppSupportActivity.this).getAd_points());
        });
    }

    private void InitializeRewardedAd() {
        ad_support.setEnabled(false);
        txt_watch_ad.setVisibility(View.GONE);
        progress_load_ad.setVisibility(View.VISIBLE);
        if(ConnectionHelper.isOnline(this)){
            MobileAds.initialize(this, initializationStatus -> {
                Log.d(TAG, "Successfully initialized");
                ad_support.setEnabled(true);
                progress_load_ad.setVisibility(View.GONE);
                txt_watch_ad.setVisibility(View.VISIBLE);
            });

        }else{
            txt_watch_ad.setText(getString(R.string.no_internet_connection));
            progress_load_ad.setVisibility(View.GONE);
            txt_watch_ad.setVisibility(View.VISIBLE);
        }
    }

    private void InitializeBannerAd() {
        if(ConnectionHelper.isOnline(this)){
            AdView adView = new AdView(this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(getString(R.string.main_banner));
            AdRequest adRequest = new AdRequest.Builder().build();
            adView_bottom.loadAd(adRequest);
        }
    }
}