package dev.kaua.squash.Activitys.Setting.AccountSetting.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.Localization;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;

@SuppressLint("StaticFieldLeak")
public class AboutYourAccountFragment extends Fragment {
    private static final String TAG = "ABOUT_ACCOUNT_LOG";
    TextView username_user, joined_user, based_in_user, account_type_user, account_level_user;
    CircleImageView ic_profile;
    static View view;
    ProgressBar about_account_loading;
    ImageView ic_account_badge_user;
    LinearLayout container_about_account;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_about_your_account, container, false);
        Ids();
        return view;
    }

    void Ids() {
        about_account_loading = view.findViewById(R.id.about_account_loading);
        ic_account_badge_user = view.findViewById(R.id.ic_account_badge_about_account);
        container_about_account = view.findViewById(R.id.container_about_account);
        username_user = view.findViewById(R.id.username_about_account_setting);
        ic_profile = view.findViewById(R.id.ic_about_account_profile);
        joined_user = view.findViewById(R.id.joined_about_account);
        based_in_user = view.findViewById(R.id.based_in_about_account);
        account_type_user = view.findViewById(R.id.account_type_about_account);
        account_level_user = view.findViewById(R.id.account_level_about_account);
        container_about_account.setVisibility(View.GONE);
        about_account_loading.setVisibility(View.VISIBLE);

        LoadUserInfo();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void LoadUserInfo() {
        DtoAccount account = MyPrefs.getUserInformation(requireContext());
        username_user.setText(account.getUsername());
        joined_user.setText(LoadJoined(account.getJoined_date()));
        based_in_user.setText(Localization.getUserCountry(requireContext()));
        Glide.with(this).load(account.getProfile_image()).into(ic_profile);

        final int account_type = Integer.parseInt(account.getType_acc());
        if(account_type > DtoAccount.NORMAL_ACCOUNT) account_type_user.setText(getString(R.string.business_account));
        else account_type_user.setText(getString(R.string.personal_account));

        final long verification_type = Methods.parseUserLevel(account.getVerification_level());
        if(verification_type > DtoAccount.NORMAL_ACCOUNT){
            ic_account_badge_user.setImageDrawable(requireContext().getDrawable(Methods.loadUserImageLevel(verification_type)));
            ic_account_badge_user.setVisibility(View.VISIBLE);
            if(verification_type == DtoAccount.VERIFY_ACCOUNT)
                account_level_user.setText(getString(R.string.your_account_is_verified_accounts_group));
            else
                account_level_user.setText(getString(R.string.your_account_is_employee_accounts_group));
        }else {
            ic_account_badge_user.setVisibility(View.GONE);
            account_level_user.setText(getString(R.string.your_account_is_common_accounts_group));
        }

        new Handler().postDelayed(() -> {
            container_about_account.setVisibility(View.VISIBLE);
            about_account_loading.setVisibility(View.GONE);
        }, 1000);
    }

    private String LoadJoined(final String date) {
        String format_date = date;
        if(date != null){
            try {
                String[] split_date = format_date.split("/");
                format_date = split_date[0] + " " + Methods.getMonth(Integer.parseInt(split_date[1])) + " " + split_date[2];
            }catch (Exception ex){
                Log.d(TAG, "Joined -> " + ex.toString());
            }
        }
        return format_date;
    }
}
