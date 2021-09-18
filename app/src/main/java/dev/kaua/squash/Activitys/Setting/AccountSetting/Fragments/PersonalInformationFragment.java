package dev.kaua.squash.Activitys.Setting.AccountSetting.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;

public class PersonalInformationFragment extends Fragment {
    private static final String TAG = "PERSONAL_INFO_LOG";
    private TextView user_email, user_phone, user_birthday;
    static View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account_personal_information, container, false);
        Ids();
        return view;
    }

    void Ids() {
        user_email = view.findViewById(R.id.email_personal_information);
        user_phone = view.findViewById(R.id.phone_personal_information);
        user_birthday = view.findViewById(R.id.birthday_personal_information);

        LoadUserInfo();
    }

    private void LoadUserInfo() {
        DtoAccount account = MyPrefs.getUserInformation(requireContext());
        user_email.setText(account.getEmail());
        user_phone.setText(account.getPhone_user());
        user_birthday.setText(LoadBirthday(account.getBorn_date()));
    }

    private String LoadBirthday(final String date) {
        String format_date = date;
        if(date != null){
            try {
                String[] split_date = format_date.split("/");
                format_date = split_date[0] + " " + Methods.getMonth(Integer.parseInt(split_date[1])) + " " + split_date[2];
            }catch (Exception ex){
                Log.d(TAG, "Birthday -> " + ex.getMessage());
            }
        }
        return format_date;
    }
}
