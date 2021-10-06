package dev.kaua.squash.Activities.Setting.AccountSetting.Fragments;

import android.annotation.SuppressLint;
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

import dev.kaua.squash.Activities.Setting.AccountSetting.Activity.Adapter.LinksAdapter;
import dev.kaua.squash.LocalDataBase.DaoBrowser;
import dev.kaua.squash.R;

@SuppressLint("StaticFieldLeak")
public class Links_YourActivityFragment extends Fragment {
    private static View view;
    TextView history_is_empty;
    RecyclerView recycler_links;
    private DaoBrowser daoBrowser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_links_account_setting, container, false);
        Ids();

        return view;
    }

    void Ids() {
        daoBrowser = new DaoBrowser(requireContext());
        history_is_empty = view.findViewById(R.id.history_is_empty_links_account_setting);
        recycler_links = view.findViewById(R.id.recycler_links_account_setting);
        recycler_links.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        Ids();
        LoadLinks();
    }

    private void LoadLinks() {
        final long list_size = daoBrowser.getLinks().size();
        if(list_size > 0){
            history_is_empty.setVisibility(View.GONE);
            recycler_links.setVisibility(View.VISIBLE);
            LinksAdapter linksAdapter = new LinksAdapter(requireActivity(), daoBrowser.getLinks());
            recycler_links.setAdapter(linksAdapter);
        }else {
            history_is_empty.setVisibility(View.VISIBLE);
            recycler_links.setVisibility(View.GONE);
        }
    }
}
