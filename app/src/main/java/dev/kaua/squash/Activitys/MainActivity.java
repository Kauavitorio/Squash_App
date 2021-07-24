package dev.kaua.squash.Activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Security.Login;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;

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

    private static DtoAccount account = new DtoAccount();

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
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1, true);

        //  Get all SharedPreferences
        mPrefs = getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        SharedPreferences sp = getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        bundle = getIntent().getExtras();
        if (sp.contains("pref_account_id") && sp.contains("pref_username")) StartNavigation();
        else Login.LogOut(this, 1);

        if(bundle != null){
            if(bundle.getInt("shared") == 1){
                Intent i = new Intent(this, ShareContentActivity.class);
                if(bundle.getInt("shared_type") == 1){
                    i.putExtra("shared_type", bundle.getInt("shared_type"));
                    i.putExtra("shared_content", bundle.getString("shared_content"));
                }
                startActivity(i);
            }
        }

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
    }

    private void StartNavigation() {
        getUserInformationAndLoadProfile();
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

    public void getUserInformationAndLoadProfile(){
        account = MyPrefs.getUserInformation(this);
        Picasso.get().load(account.getProfile_image()).into(btn_profile_main);
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
        if(position == 1) btn_home_main.setImageDrawable(getDrawable(R.drawable.ic_home_select));
        else if(position == 2) btn_search_main.setImageDrawable(getDrawable(R.drawable.ic_search_select));
        else if(position == 3) btn_profile_main.setBorderWidth(3);
    }

    private void Ids() {
        instance = this;
        getWindow().setStatusBarColor(getColor(R.color.BarColor));
        getWindow().setNavigationBarColor(getColor(R.color.BarColor));
        viewPager = findViewById(R.id.viewpager_main);
        btn_profile_main = findViewById(R.id.btn_profile_main);
        btn_search_main = findViewById(R.id.btn_search_main);
        btn_home_main = findViewById(R.id.btn_home_main);
        container_btn_profile_main = findViewById(R.id.container_btn_profile_main);
        mFirebaseAnalytics = ConfFirebase.getFirebaseAnalytics(this);
        btn_home_main.setImageDrawable(getDrawable(R.drawable.ic_home_select));

        //  Creating analytic for open app event
        Bundle bundle_Analytics = new Bundle();
        bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_ID, ConfFirebase.getFirebaseUser().getUid());
        bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_NAME, EncryptHelper.decrypt(account.getUsername()));
        bundle_Analytics.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle_Analytics);
        getUserInformationAndLoadProfile();
        Login.ReloadUserinfo(this, MyPrefs.getUserInformation(this).getEmail(), MyPrefs.getUserInformation(this).getPassword());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onResume() {
        super.onResume();
        getUserInformationAndLoadProfile();
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