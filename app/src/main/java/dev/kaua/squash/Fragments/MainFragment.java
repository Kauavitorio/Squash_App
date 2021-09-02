package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;


import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.BuildConfig;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Data.Post.Actions.RecommendedPosts;
import dev.kaua.squash.Data.System.DtoSystem;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.LocalDataBase.DaoSystem;
import dev.kaua.squash.LocalDataBase.Notification.DaoNotification;
import dev.kaua.squash.Notifications.Data;
import dev.kaua.squash.Notifications.NotificationActivity;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/River_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("StaticFieldLeak")
public class MainFragment extends Fragment {
    //private static SwipeRefreshLayout swipe_main;
    private ConstraintLayout btn_create_new_story_main, btn_notifications_click;
    private static RecyclerView recyclerView_Posts;
    private ImageView btn_compose_main;
    private CircleImageView icon_ProfileUser_main;
    private static CardView card_msg_notRead_main, have_notification;
    private LinearLayout header_main;
    private static Activity instance;
    private static ConstraintLayout loadingPanel;
    private final Handler timer = new Handler();
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    private static DaoSystem daoSystem;
    private static final String TAG = "MAIN_FRAGMENT_LOG";

    private View view;
    private static DtoAccount account;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_activity_main, container, false);
        Ids(view);

        Glide.with(this).load(account.getProfile_image()).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(icon_ProfileUser_main);
        btn_create_new_story_main.setOnClickListener(v -> StoryClick());
        btn_compose_main.setOnClickListener(v -> MainActivity.getInstance().CallComposePost());

        //loadMsgNotRead();
        loadCheckSystemInfo();

        btn_notifications_click.setOnClickListener(v -> {
            Intent i = new Intent(requireActivity(), NotificationActivity.class);
            startActivity(i);
        });

        return view;
    }

    long following = 0;
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            if(getContext() != null){
                Check_Notification();
                DaoAccount db = new DaoAccount(getContext());
                DtoAccount account_follow = db.get_followers_following(account.getAccount_id());
                if(account_follow.getFollowing() != null && following != Long.parseLong(account_follow.getFollowing())) {
                    following = Long.parseLong(account_follow.getFollowing());
                    RefreshRecycler();
                }
            }
        }
    }

    private void loadMsgNotRead() {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                int unread = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    DtoMessage message = snapshot1.getValue(DtoMessage.class);
                    if(message != null)
                        if(message.getReceiver() != null)
                            if(message.getReceiver().equals(firebaseUser.getUid()) && message.getIsSeen() == 0)
                                unread++;
                }

                if(unread == 0) card_msg_notRead_main.setVisibility(View.GONE);
                else card_msg_notRead_main.setVisibility(View.VISIBLE);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private void loadCheckSystemInfo() {
        if(getContext() != null)
        if(ConnectionHelper.isOnline(getContext())){
            int currentVersionCode = BuildConfig.VERSION_CODE;

            reference = myFirebaseHelper.getFirebaseDatabase().getReference("System");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(getContext() != null && getActivity() != null)
                        if(!getActivity().isFinishing() && !getActivity().isDestroyed()){
                            if(ConnectionHelper.isOnline(getContext())){
                                DtoSystem system = snapshot.getValue(DtoSystem.class);
                                if(system != null && system.getVersionName() != null){
                                    if(currentVersionCode < system.getVersionCode())
                                        if(getContext() != null)
                                            if(MyPrefs.getUpdateRequest_Show(getContext()) == 0 || system.getNeedUpdate() == 1)
                                                Warnings.showNeedUpdate(requireContext(), system.getVersionName(), system.getVersionCode(), (int) system.getNeedUpdate());

                                    if(daoSystem.getPrivacyPolicy() < system.getPrivacy_policy())
                                        Warnings.goToUpdateInPrivacyPolicy(getActivity(), system.getPrivacy_policy());
                                }
                            }
                        }
                }
                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {}
            });
        }
    }

    private void StoryClick() {
        ToastHelper.toast(requireActivity(), getString(R.string.under_development), 0);
    }

    static DaoNotification daoNotification;
    public static void Check_Notification(){
        final List<Data> list = daoNotification.getNotifications();
        Collections.reverse(list);
        if(list.size() > 0){
            try {
                final Data last = list.get(list.size() - 1);
                if(last.getSeen() == DaoNotification.NOT_SEEN) have_notification.setVisibility(View.VISIBLE);
                else have_notification.setVisibility(View.GONE);
            }catch (Exception ex){
                Log.d(TAG, ex.toString());
                have_notification.setVisibility(View.GONE);
            }
        }else have_notification.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() != null && !getActivity().isDestroyed() && !getActivity().isFinishing())
            Check_Notification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void RefreshRecycler(){ RecommendedPosts.getFeedPosts(instance, recyclerView_Posts, loadingPanel); }

    private void Ids(View view) {
        instance = requireActivity();
        daoNotification = new DaoNotification(instance);
        daoSystem = new DaoSystem(instance);
        requireActivity().getWindow().setStatusBarColor(requireActivity().getColor(R.color.background_menu_sheet));
        requireActivity().getWindow().setNavigationBarColor(requireActivity().getColor(R.color.base_color));
        firebaseUser = myFirebaseHelper.getFirebaseUser();
        account = MyPrefs.getUserInformation(requireContext());
        loadingPanel = view.findViewById(R.id.loadingPanel);
        btn_notifications_click = view.findViewById(R.id.btn_notifications_click);
        have_notification = view.findViewById(R.id.have_notification);
        icon_ProfileUser_main = view.findViewById(R.id.icon_ProfileUser_main);
        btn_create_new_story_main = view.findViewById(R.id.btn_create_new_story_main);
        card_msg_notRead_main = view.findViewById(R.id.card_msg_notRead_main);
        btn_compose_main = view.findViewById(R.id.btn_compose_main);
        recyclerView_Posts = view.findViewById(R.id.recyclerView_Posts);
        header_main = view.findViewById(R.id.header_main);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        recyclerView_Posts.setLayoutManager(linearLayout);
        RefreshRecycler();

        Check_Notification();
    }
}
