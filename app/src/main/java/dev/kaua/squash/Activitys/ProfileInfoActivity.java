package dev.kaua.squash.Activitys;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Fragments.FollowersFragment;
import dev.kaua.squash.Fragments.FollowingFragment;
import dev.kaua.squash.Fragments.FragmentPageAdapter;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;

public class ProfileInfoActivity extends AppCompatActivity {
    public static final String REQUEST_ID = "request_id";
    public static final String REQUEST_ACCOUNT_ID = "account_request_id";
    public static final String REQUEST_USERNAME_ID = "username_id";
    public static final String REQUEST_PROFILE_IMAGE_ID = "profile_image_id";
    public static final String REQUEST_FOLLOWERS_AMOUNT = "followers_amount_id";
    public static final String REQUEST_FOLLOWING_AMOUNT = "following_amount_id";
    public static final int REQUEST_FOLLOWERS = 0;
    public static final int REQUEST_FOLLOWING = 1;

    CircleImageView profile_image;
    TextView txt_username;
    static long account_id;
    int request;
    private ViewPager view_paper;
    private TabLayout tab_layout;
    static ViewPaperAdapter viewPaperAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);
        Ids();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.getString(REQUEST_USERNAME_ID) != null){
            //  Load base user info
            account_id = Long.parseLong(bundle.getString(REQUEST_ACCOUNT_ID));
            Methods.HoldId(account_id);
            request = bundle.getInt(REQUEST_ID);
            txt_username.setText( "@" + bundle.getString(REQUEST_USERNAME_ID));
            Glide.with(this).load(bundle.getString(REQUEST_PROFILE_IMAGE_ID)).into(profile_image);

            viewPaperAdapter = new ViewPaperAdapter(getSupportFragmentManager());
            viewPaperAdapter.addFragment(new FollowersFragment(), bundle.getString(REQUEST_FOLLOWERS_AMOUNT) + " " + getString(R.string.followers));
            viewPaperAdapter.addFragment(new FollowingFragment(), bundle.getString(REQUEST_FOLLOWING_AMOUNT) + " " + getString(R.string.following));
            view_paper.setAdapter(viewPaperAdapter);
            tab_layout.setupWithViewPager(view_paper);

            view_paper.setCurrentItem(request);
        }else{
            ToastHelper.toast(this, getString(R.string.user_not_found), ToastHelper.SHORT_DURATION);
            finish();
        }
    }

    void Ids(){
        getWindow().setStatusBarColor(getColor(R.color.black_intro));
        profile_image = findViewById(R.id.profile_image_profile_info);
        txt_username = findViewById(R.id.txt_username_profile_info);
        view_paper = findViewById(R.id.view_paper_profile_info);
        tab_layout = findViewById(R.id.tab_layout_profile_info);
        Toolbar toolbar = findViewById(R.id.toolbar_profile_info);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    static class ViewPaperAdapter extends FragmentPageAdapter {

        private final ArrayList<Fragment> fragments;
        private final ArrayList<String> titles;

        ViewPaperAdapter(FragmentManager fm){
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public @NotNull Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}