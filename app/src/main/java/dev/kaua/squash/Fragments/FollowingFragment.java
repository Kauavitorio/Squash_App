package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.kaua.squash.Adapters.Followers_FollowingAdapter;
import dev.kaua.squash.Data.Account.AsyncFollowingInfo;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;

@SuppressLint("StaticFieldLeak")
public class FollowingFragment extends Fragment {
    private static RecyclerView recycler_view;
    private static ImageView btn_filter_following;
    private static Activity instance_ac;
    private static FollowingFragment instance;
    private static TextView txt_no_array_list, txt_filter_status_following;
    private static ProgressBar progress_following;
    private static BottomSheetDialog bottomSheetDialog;
    static View view;
    static Followers_FollowingAdapter userChatAdapter;

    private static int filter_type = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_following, container, false);
        Ids();
        readAccounts();

        btn_filter_following.setOnClickListener(v -> {
            bottomSheetDialog = null;
            bottomSheetDialog = new BottomSheetDialog(instance_ac, R.style.BottomSheetTheme);
            //  Creating View for SheetMenu
            View sheetView = LayoutInflater.from(instance_ac).inflate(R.layout.adapter_sheet_sort_by,
                    view.findViewById(R.id.sheet_sort_by_following));

            CheckBox default_check, date_followed_latest, date_followed_earliest;
            default_check = sheetView.findViewById(R.id.filter_default_following);
            date_followed_latest = sheetView.findViewById(R.id.filter_date_followed_latest_following);
            date_followed_earliest = sheetView.findViewById(R.id.filter_date_followed_earliest_following);

            if(filter_type == 0){
                default_check.setChecked(true);
                date_followed_latest.setChecked(false);
                date_followed_earliest.setChecked(false);
            }else if(filter_type == 1){
                date_followed_latest.setChecked(true);
                default_check.setChecked(false);
                date_followed_earliest.setChecked(false);
            }else{
                date_followed_earliest.setChecked(true);
                default_check.setChecked(false);
                date_followed_latest.setChecked(false);
            }

            default_check.setOnClickListener(v1 -> {
                filter_type = 0;
                ApplyFilter();
                bottomSheetDialog.dismiss();
            });

            date_followed_latest.setOnClickListener(v1 -> {
                filter_type = 1;
                ApplyFilter();
                bottomSheetDialog.dismiss();
            });

            date_followed_earliest.setOnClickListener(v1 -> {
                filter_type = 2;
                ApplyFilter();
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();
        });
        return view;
    }

    public static FollowingFragment getInstance() { return instance; }

    List<DtoAccount> base_list = new ArrayList<>();
    boolean base = false;
    public void ShowFollowingList(List<DtoAccount> list){
        try {
            if(!base){
                base_list = new ArrayList<>();
                base_list.addAll(list);
                base = true;
            }
            userChatAdapter = new Followers_FollowingAdapter(instance_ac, list);
            recycler_view.setAdapter(userChatAdapter);

            if(list.size() > 0){
                recycler_view.setVisibility(View.VISIBLE);
            }else{
                txt_no_array_list.setText(getString(R.string.this_user_is_not_following));
                recycler_view.setVisibility(View.GONE);
                txt_no_array_list.setVisibility(View.VISIBLE);
            }
            progress_following.setVisibility(View.GONE);
        }catch (Exception ex){
            Log.d("FollowingLog", ex.toString());
            Warnings.showWeHaveAProblem(instance_ac, ErrorHelper.FOLLOWING_SHOW_LIST);
        }
    }

    boolean already_reverse = false;
    void ApplyFilter(){
        if(filter_type == 0){
            ShowFollowingList(base_list);
            already_reverse = false;
            txt_filter_status_following.setText(getString(R.string.sorted_by_default));
        }else if(filter_type == 1){
            ShowFollowingList(base_list);
            already_reverse = false;
            txt_filter_status_following.setText(getString(R.string.sorted_by_latest));
        }else{
            if(!already_reverse){
                List<DtoAccount> list = new ArrayList<>(base_list);
                Collections.reverse(list);
                ShowFollowingList(list);
                already_reverse = true;
            }
            txt_filter_status_following.setText(getString(R.string.sorted_by_earliest));
        }
    }

    void Ids() {
        instance = this;
        filter_type = 0;
        if(getActivity() != null) instance_ac = getActivity();
        recycler_view = view.findViewById(R.id.recycler_view_following);
        btn_filter_following = view.findViewById(R.id.btn_filter_following);
        txt_filter_status_following = view.findViewById(R.id.txt_filter_status_following);
        txt_filter_status_following.setText(getString(R.string.sorted_by_default));
        progress_following = view.findViewById(R.id.progress_following);
        txt_no_array_list = view.findViewById(R.id.txt_no_array_list_following);
        recycler_view.setHasFixedSize(true);
        recycler_view.setItemViewCacheSize(20);
        recycler_view.setDrawingCacheEnabled(true);
        recycler_view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recycler_view.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void readAccounts() {
        if(instance_ac != null){
            if(ConnectionHelper.isOnline(instance_ac)){
                AsyncFollowingInfo async = new AsyncFollowingInfo(instance_ac, Methods.getIdHold(), recycler_view, progress_following, txt_no_array_list);
                //noinspection unchecked
                async.execute();
            }else ToastHelper.toast(instance_ac, getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
        }
    }
}