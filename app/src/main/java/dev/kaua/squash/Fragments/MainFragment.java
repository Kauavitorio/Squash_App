package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Adapters.Story.StoryAdapter;
import dev.kaua.squash.BuildConfig;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Data.Post.Actions.RecommendedPosts;
import dev.kaua.squash.Data.Stories.DtoStory;
import dev.kaua.squash.Data.System.DtoSystem;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.LocalDataBase.DaoSystem;
import dev.kaua.squash.LocalDataBase.Notification.DaoNotification;
import dev.kaua.squash.Notifications.Data;
import dev.kaua.squash.Notifications.NotificationActivity;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ShortCutsHelper;
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
    private ConstraintLayout btn_notifications_click;
    private static RecyclerView recyclerView_Posts;
    private static ShortCutsHelper shortCutsHelper;
    private ImageView btn_compose_main;
    private static CardView card_msg_notRead_main, have_notification;
    private static Activity instance;
    private static ConstraintLayout loadingPanel;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    private static DaoSystem daoSystem;
    private static final String TAG = "MAIN_FRAGMENT_LOG";
    private static DaoAccount db_account;
    private RecyclerView recyclerView_Story;
    private StoryAdapter storyAdapter;
    private final List<DtoStory> storyList = new ArrayList<>();

    private View view;
    private static DtoAccount account = new DtoAccount();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_activity_main, container, false);
        Ids(view);


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
                if(db_account == null) db_account = new DaoAccount(getContext());
                final DtoAccount account_follow = db_account.get_followers_following(account.getAccount_id());
                if(account_follow.getFollowing() != null && following != Long.parseLong(account_follow.getFollowing())) {
                    following = Long.parseLong(account_follow.getFollowing());
                    RefreshRecycler();
                    readStory();
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

            reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.SYSTEM_REFERENCE);
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

                                     MyPrefs.setStoryTutorial(getContext(),
                                             snapshot.child(myFirebaseHelper.STORY_TUTORIAL_REFERENCE)
                                             .child(String.valueOf(MyPrefs.getUserInformation(getContext()).getAccount_id()))
                                             .exists());
                                }
                            }
                        }
                }
                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {}
            });
        }
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
        RecommendedPosts.posts_adapters = null;
        Log.d(TAG, "Set Null Post Adapter");
        super.onDestroy();
    }

    public static void RefreshRecycler(){ RecommendedPosts.getFeedPosts(instance, recyclerView_Posts, loadingPanel); }

    private void Ids(View view) {
        instance = requireActivity();
        daoNotification = new DaoNotification(instance);
        daoSystem = new DaoSystem(instance);
        shortCutsHelper = new ShortCutsHelper(instance);
        requireActivity().getWindow().setStatusBarColor(requireActivity().getColor(R.color.background_menu_sheet));
        requireActivity().getWindow().setNavigationBarColor(requireActivity().getColor(R.color.base_color));
        firebaseUser = myFirebaseHelper.getFirebaseUser();
        account = MyPrefs.getUserInformation(requireContext());
        loadingPanel = view.findViewById(R.id.loadingPanel);
        btn_notifications_click = view.findViewById(R.id.btn_notifications_click);
        have_notification = view.findViewById(R.id.have_notification);
        card_msg_notRead_main = view.findViewById(R.id.card_msg_notRead_main);
        btn_compose_main = view.findViewById(R.id.btn_compose_main);
        recyclerView_Posts = view.findViewById(R.id.recyclerView_Posts);

        recyclerView_Posts.setLayoutManager(new LinearLayoutManager(getActivity()));

        storyAdapter = new StoryAdapter(getContext(), storyList);
        recyclerView_Story = view.findViewById(R.id.recyclerView_Story);
        recyclerView_Story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView_Story.setLayoutManager(linearLayoutManager);
        recyclerView_Story.setAdapter(storyAdapter);

        RefreshRecycler();
        readStory();
        Check_Notification();
        shortCutsHelper.launchShortcuts();
    }

    private void readStory(){
        final DaoFollowing daoFollowing = new DaoFollowing(instance);
        final List<String> followingList = daoFollowing.get_followingLIST(instance);
        if(ConnectionHelper.isOnline(instance)){
            myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.STORY_REFERENCE)
                    .addValueEventListener(new ValueEventListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                            if(!instance.isDestroyed() && !instance.isFinishing()){
                                long timeCurrent = System.currentTimeMillis();
                                storyList.clear();
                                storyList.add(new DtoStory("", 0, 0, "", String.valueOf(MyPrefs.getUserInformation(instance).getAccount_id())));

                                for(String id: followingList){
                                    int countStory = 0;
                                    DtoStory story = null;
                                    for (DataSnapshot snapshot: datasnapshot.child(id).getChildren()){
                                        story = snapshot.getValue(DtoStory.class);
                                        if(story != null){
                                            if(timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd())
                                                countStory++;
                                            story.setSeen(snapshot.child(myFirebaseHelper.STORY_VIEWS)
                                                    .child(String.valueOf(MyPrefs.getUserInformation(instance).getAccount_id())).exists());
                                        }
                                    }

                                    if(countStory > 0)
                                        storyList.add(story);
                                }
                                storyAdapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }
}
