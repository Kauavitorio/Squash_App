package dev.kaua.squash.Activities.Setting.AccountSetting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.kaua.squash.Activities.Setting.AccountSettingActivity;
import dev.kaua.squash.R;

public class OptionsFragment extends Fragment {
    private ListView options_list;
    String[] options;
    private static View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_options_account_setting, container, false);
        Ids();
        return view;
    }

    private void Ids() {
        options_list = view.findViewById(R.id.options_list_account_setting);

        GenerateOptionsList();
    }


    public static final int OPTIONS = -1;
    public static final int PERSONAL_INFORMATION = 0;
    public static final int ABOUT_YOUR_ACCOUNT = 1;
    public static final int YOUR_ACTIVITY = 2;
    public static final int LANGUAGE = 3;
    public static final int REQUEST_VERIFICATION = 4;
    private void GenerateOptionsList() {
        options = new String[]{
                getString(R.string.personal_information),
                getString(R.string.about_your_account),
                getString(R.string.your_activity),
                getString(R.string.language),
                getString(R.string.request_verification)
        };


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, options);
        options_list.setAdapter(adapter);

        options_list.setOnItemClickListener((adapterView, view, position, l) -> {
            if(position == PERSONAL_INFORMATION) AccountSettingActivity.getInstance().LoadPersonalInformation();
            else if(position == ABOUT_YOUR_ACCOUNT) AccountSettingActivity.getInstance().LoadAboutYourAccount();
            else if(position == YOUR_ACTIVITY) AccountSettingActivity.getInstance().LoadYourActivity();
            else if(position == LANGUAGE) AccountSettingActivity.getInstance().LoadLanguage();
            else if(position == REQUEST_VERIFICATION) AccountSettingActivity.getInstance().LoadVerification();
        });
    }
}
