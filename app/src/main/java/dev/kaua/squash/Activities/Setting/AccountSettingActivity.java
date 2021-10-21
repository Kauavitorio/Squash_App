package dev.kaua.squash.Activities.Setting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Objects;

import dev.kaua.squash.Activities.Setting.AccountSetting.Fragments.AboutYourAccountFragment;
import dev.kaua.squash.Activities.Setting.AccountSetting.Fragments.LanguageFragment;
import dev.kaua.squash.Activities.Setting.AccountSetting.Fragments.PersonalInformationFragment;
import dev.kaua.squash.Activities.Setting.AccountSetting.Fragments.RequestVerificationFragment;
import dev.kaua.squash.Activities.Setting.AccountSetting.Fragments.YourActivityFragment;
import dev.kaua.squash.Activities.Setting.AccountSetting.OptionsFragment;
import dev.kaua.squash.R;

@SuppressLint("StaticFieldLeak")
public class AccountSettingActivity extends AppCompatActivity {
    private static FragmentTransaction transaction;
    private static Bundle args;
    private TextView txt_title;
    private static AccountSettingActivity instance;
    private static int FRAGMENT_PRESENT = -1;
    public static final String REQUEST_TAG = "REQUEST";
    public static final int ACTIVITY = 1;
    private static int request = FRAGMENT_PRESENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        Ids();
    }

    private void Ids() {
        instance = this;
        txt_title = findViewById(R.id.txt_title_account_setting);
        getWindow().setStatusBarColor(getColor(R.color.black_intro));
        Toolbar toolbar = findViewById(R.id.toolbar_my_account);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> GoBackOrFinish());

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) if(bundle.getInt(REQUEST_TAG) != 0) request = bundle.getInt(REQUEST_TAG);

        LoadOptions();
    }

    private void GoBackOrFinish() {
        if(FRAGMENT_PRESENT == OptionsFragment.OPTIONS) finish();
        else LoadOptions();
    }

    @Override
    public void onBackPressed() {
        GoBackOrFinish();
    }

    public static AccountSettingActivity getInstance(){ return instance; }

    public void LoadOptions() {
        if(request != FRAGMENT_PRESENT){
            if(request == ACTIVITY) LoadYourActivity();
            else {
                FRAGMENT_PRESENT = OptionsFragment.OPTIONS;
                txt_title.setText(getString(R.string.my_account));
                LoadFragment(new OptionsFragment());
            }
            request = -1;
        }else{
            FRAGMENT_PRESENT = OptionsFragment.OPTIONS;
            txt_title.setText(getString(R.string.my_account));
            LoadFragment(new OptionsFragment());
        }
    }

    public void LoadPersonalInformation(){
        FRAGMENT_PRESENT = OptionsFragment.PERSONAL_INFORMATION;
        txt_title.setText(getString(R.string.personal_information));
        LoadFragment(new PersonalInformationFragment());
    }

    public void LoadAboutYourAccount(){
        FRAGMENT_PRESENT = OptionsFragment.ABOUT_YOUR_ACCOUNT;
        txt_title.setText(getString(R.string.about_your_account));
        LoadFragment(new AboutYourAccountFragment());
    }

    public void LoadLanguage(){
        FRAGMENT_PRESENT = OptionsFragment.LANGUAGE;
        txt_title.setText(getString(R.string.language));
        LoadFragment(new LanguageFragment());
    }

    public void LoadVerification(){
        FRAGMENT_PRESENT = OptionsFragment.REQUEST_VERIFICATION;
        txt_title.setText(getString(R.string.request_verification));
        LoadFragment(new RequestVerificationFragment());
    }

    public void LoadYourActivity(){
        FRAGMENT_PRESENT = OptionsFragment.YOUR_ACTIVITY;
        txt_title.setText(getString(R.string.your_activity));
        LoadFragment(new YourActivityFragment());
    }

    void LoadFragment(Fragment fragment){
        args = new Bundle();
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.frameLayoutAccount_Setting, fragment);
        transaction.commit();
    }
}