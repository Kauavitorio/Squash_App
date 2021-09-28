package dev.kaua.squash.Activitys.Setting.AccountSetting.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.kaua.squash.Activitys.Setting.AccountSetting.Lang.Adapter.LanguageAdapter;
import dev.kaua.squash.Activitys.Setting.AccountSetting.Lang.DtoLang;
import dev.kaua.squash.R;

@SuppressLint("StaticFieldLeak")
public class LanguageFragment extends Fragment {
    private RecyclerView languages_RecyclerView;
    private static View view;
    static Activity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_language_account_setting, container, false);
        Ids();
        return view;
    }

    private void Ids() {
        activity = requireActivity();
        languages_RecyclerView = view.findViewById(R.id.language_list_account_setting);
        languages_RecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        GenerateLangList();
    }


    public static final String ENGLISH = "en";
    public static final String PORTUGUESE = "pt-BR";
    List<DtoLang> languages_list = new ArrayList<>();
    private void GenerateLangList() {
        languages_list.add(new DtoLang(getString(R.string.english), ENGLISH, R.drawable.ic_usa_flag));
        languages_list.add(new DtoLang(getString(R.string.portuguese_brazil), PORTUGUESE, R.drawable.ic_brazil_flag));

        LanguageAdapter languageAdapter = new LanguageAdapter(requireActivity(),languages_list);
        languages_RecyclerView.setAdapter(languageAdapter);
    }
}
