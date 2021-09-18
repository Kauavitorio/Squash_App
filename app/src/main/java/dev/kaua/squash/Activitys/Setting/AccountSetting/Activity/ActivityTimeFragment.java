package dev.kaua.squash.Activitys.Setting.AccountSetting.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.kaua.squash.R;

@SuppressLint("StaticFieldLeak")
public class ActivityTimeFragment extends Fragment {
    private static final String TAG = "ActivityTimeFragment";
    private static View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_time_account_setting, container, false);
        Ids();

        return view;
    }

    private void Ids() {

    }

}
