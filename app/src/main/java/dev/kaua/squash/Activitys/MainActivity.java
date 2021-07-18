package dev.kaua.squash.Activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.AsyncUser_Follow;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.Fragments.FragmentPageAdapter;
import dev.kaua.squash.Fragments.MainFragment;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Security.Login;
import dev.kaua.squash.Tools.Methods;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/River_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint({"StaticFieldLeak", "UseCompatLoadingForDrawables"})
public class MainActivity extends AppCompatActivity {
    private static ImageView btn_search_main, btn_home_main;
    private CircleImageView btn_profile_main;
    private LinearLayout container_btn_profile_main;
    private static ViewPager viewPager;
    private FragmentPageAdapter adapter;
    private static FirebaseAnalytics mFirebaseAnalytics;


    private Bundle bundle;
    private static Bundle args;
    private static FragmentTransaction transaction;
    private static MainActivity instance;
    //  Set preferences
    private SharedPreferences mPrefs;
    public static final String PREFS_NAME = "myPrefs";

    private static final DtoAccount account = new DtoAccount();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Ids();

        // Create an adapter that
        // knows which fragment should
        // be shown on each page
        adapter = new FragmentPageAdapter(getSupportFragmentManager());

        // Set the adapter onto
        // the view pager
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1, true);

        //  Get all SharedPreferences
        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        bundle = getIntent().getExtras();
        if (sp.contains("pref_account_id") && sp.contains("pref_username")) StartNavigation();
        else Login.LogOut(this, 1);

        btn_search_main.setOnClickListener(v -> LoadSearchFragment());
        btn_home_main.setOnClickListener(v -> LoadMainFragment());

        container_btn_profile_main.setOnClickListener(v -> CallProfile());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                adapter.notifyDataSetChanged();
                viewPager.setCurrentItem(position, true);
                Check_Fragments(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setPageTransformer(false, (page, position) -> {
            page.setTranslationX(-position * page.getWidth());

            if (Math.abs(position) <= 0.5) {
                page.setVisibility(View.VISIBLE);
                page.setScaleX(1 - Math.abs(position));
                page.setScaleY(1 - Math.abs(position));
            } else if (Math.abs(position) > 0.5)
                page.setVisibility(View.GONE);


            if (position < -1) // [-Infinity,-1)
                // This page is way off-screen to the left.
                page.setAlpha(0);
            else if (position <= 0) {   // [-1,0]
                page.setAlpha(1);
                page.setRotation(360 * Math.abs(position));
            }
            else if (position <= 1) {   // (0,1]
                page.setAlpha(1);
                page.setRotation(-360 * Math.abs(position));
            }
            else // (1,+Infinity]
                // This page is way off-screen to the right.
                page.setAlpha(0);
        });
    }

    private void StartNavigation() {
        getUserInformation();
        LoadMainFragment();
        AsyncUser_Follow asyncUser_follow = new AsyncUser_Follow(this, account.getAccount_id());
        //noinspection unchecked
        asyncUser_follow.execute();
    }

    public static MainActivity getInstance(){ return instance; }

    Bundle bundle_profile;
    public void GetBundleProfile(Bundle bundle){
        bundle_profile = bundle;
    }
    public Bundle SetBundleProfile(){ return bundle_profile; }
    public void ResetBundleProfile() { bundle_profile = null; }

    public void CallProfile(){
        viewPager.setCurrentItem(3, true);
        adapter.notifyDataSetChanged();
        try {
            ProfileFragment.getInstance().onResume();
        }catch (Exception ex){
            Log.d("ResumeProfile", ex.getMessage());
        }
    }

    public void CallChat(){
        viewPager.setCurrentItem(0, true);
        adapter.notifyDataSetChanged();
    }

    public void CallComposePost(){
        Intent compose = new Intent(this, ComposeActivity.class);
        startActivity(compose);
    }

    @SuppressWarnings("ConstantConditions")
    public DtoAccount getUserInformation(){
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        account.setAccount_id(Integer.parseInt(EncryptHelper.decrypt(sp.getString("pref_account_id", null))));
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
        account.setToken(EncryptHelper.decrypt(sp.getString("pref_token", null)));
        account.setVerification_level(EncryptHelper.decrypt(sp.getString("pref_verification_level", null)));

        Picasso.get().load(account.getProfile_image()).into(btn_profile_main);
        return account;
    }

    private void LoadMainFragment() {
        viewPager.setCurrentItem(1, true);
        adapter.notifyDataSetChanged();
    }

    private void LoadSearchFragment() {
        viewPager.setCurrentItem(2, true);
        adapter.notifyDataSetChanged();
    }

    public void Check_Fragments(int position){
        btn_search_main.setImageDrawable(getDrawable(R.drawable.ic_search));
        btn_home_main.setImageDrawable(getDrawable(R.drawable.ic_home));
        btn_profile_main.setBorderWidth(0);
        if(position == 1) {
            btn_home_main.setImageDrawable(getDrawable(R.drawable.ic_home_select));
            MainFragment.RefreshRecycler();
        }
        else if(position == 2) btn_search_main.setImageDrawable(getDrawable(R.drawable.ic_search_select));
        else if(position == 3) btn_profile_main.setBorderWidth(3);
    }


    private void Ids() {
        getUserInformation();
        mFirebaseAnalytics = ConfFirebase.getFirebaseAnalytics(this);
        instance = this;
        getWindow().setStatusBarColor(getColor(R.color.BarColor));
        getWindow().setNavigationBarColor(getColor(R.color.BarColor));
        viewPager = findViewById(R.id.viewpager_main);
        btn_profile_main = findViewById(R.id.btn_profile_main);
        btn_search_main = findViewById(R.id.btn_search_main);
        btn_home_main = findViewById(R.id.btn_home_main);
        container_btn_profile_main = findViewById(R.id.container_btn_profile_main);

        //  Creating analytic for open app event
        Bundle bundle_Analytics = new Bundle();
        bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_ID, ConfFirebase.getFirebaseUser().getUid());
        bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_NAME, EncryptHelper.decrypt(account.getUsername()));
        bundle_Analytics.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle_Analytics);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onResume() {
        super.onResume();
        getUserInformation();
        Methods.status_chat("online");
        Methods.LoadFollowersAndFollowing(this);
        AsyncUser_Follow asyncUser_follow = new AsyncUser_Follow(this, account.getAccount_id());
        asyncUser_follow.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Methods.status_chat("offline");
    }
}