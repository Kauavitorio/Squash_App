package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.kaua.squash.Adapters.Chat.UserChatAdapter;
import dev.kaua.squash.Data.Account.AsyncFollowersInfo;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;

@SuppressLint("StaticFieldLeak")
public class FollowersFragment extends Fragment {
    private static RecyclerView recycler_view;
    private static DaoFollowing daoFollowing;
    private static DtoAccount myAccount;
    private static ProgressBar progress_followers;
    private static TextView txt_no_array_list;
    private static Activity instance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followers, container, false);
        Ids(view);
        readAccounts();

        return view;
    }

    private void Ids(View view) {
        if(getActivity() != null) instance = getActivity();
        daoFollowing = new DaoFollowing(requireContext());
        myAccount = MyPrefs.getUserInformation(requireContext());
        recycler_view = view.findViewById(R.id.recycler_view_followers);
        progress_followers = view.findViewById(R.id.progress_followers);
        txt_no_array_list = view.findViewById(R.id.txt_no_array_list);
        recycler_view.setHasFixedSize(true);
        recycler_view.setItemViewCacheSize(20);
        recycler_view.setDrawingCacheEnabled(true);
        recycler_view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recycler_view.setLayoutManager(new LinearLayoutManager(requireContext()));
    }


    private void readAccounts() {
        if(instance != null){
            if(ConnectionHelper.isOnline(instance)){
                AsyncFollowersInfo async = new AsyncFollowersInfo(instance, Methods.getIdHold(), recycler_view, progress_followers, txt_no_array_list);
                //noinspection unchecked
                async.execute();
            }else ToastHelper.toast(instance, getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
        }
    }
}