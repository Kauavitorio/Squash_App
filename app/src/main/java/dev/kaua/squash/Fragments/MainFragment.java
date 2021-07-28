package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;


import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.BuildConfig;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Data.Post.Actions.RecommendedPosts;
import dev.kaua.squash.Data.System.DtoSystem;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.Methods;
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
    private ConstraintLayout btn_create_new_story_main;
    private static RecyclerView recyclerView_Posts;
    public static SwipeRefreshLayout swipe_posts;
    private ImageView btn_chat_main, btn_compose_main;
    private CircleImageView icon_ProfileUser_main;
    private CardView card_msg_notRead_main;
    private LinearLayout header_main;
    private static ProgressBar loading_posts;
    private static Context instance;
    private static ConstraintLayout loadingPanel;
    private final Handler timer = new Handler();
    DatabaseReference reference;
    FirebaseUser firebaseUser;

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
        btn_chat_main.setOnClickListener(v -> MainActivity.getInstance().CallChat());
        btn_compose_main.setOnClickListener(v -> MainActivity.getInstance().CallComposePost());

        swipe_posts.setOnRefreshListener(MainFragment::RefreshRecycler);

        loadMsgNotRead();
        loadCheckSystemInfo();

        return view;
    }

    long following = 0;
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            if(getContext() != null){
                DaoAccount db = new DaoAccount(getContext());
                DtoAccount account_follow = db.get_followers_following(account.getAccount_id());
                if(following != Long.parseLong(account_follow.getFollowing())) {
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
        if(Methods.isOnline(getContext())){
            int currentVersionCode = BuildConfig.VERSION_CODE;

            reference = FirebaseDatabase.getInstance().getReference("System");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(getContext() != null)
                    if(Methods.isOnline(getContext())){
                        DtoSystem system = snapshot.getValue(DtoSystem.class);
                        if(system != null){
                            if(currentVersionCode < system.getVersionCode()){
                                if(getContext() != null)
                                    if(MyPrefs.getUpdateRequest_Show(getContext()) == 0 || system.getNeedUpdate() == 1)
                                        Warnings.showNeedUpdate(requireContext(), system.getVersionName(), system.getVersionCode(), (int) system.getNeedUpdate());
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


    public static void RefreshRecycler(){ RecommendedPosts.getRecommendedPosts(instance, recyclerView_Posts, loadingPanel, loading_posts); }

    private void Ids(View view) {
        instance = requireActivity();
        requireActivity().getWindow().setStatusBarColor(requireActivity().getColor(R.color.background_menu_sheet));
        firebaseUser = ConfFirebase.getFirebaseUser();
        account = MyPrefs.getUserInformation(requireContext());
        loadingPanel = view.findViewById(R.id.loadingPanel);
        loading_posts = view.findViewById(R.id.loading_posts);
        swipe_posts = view.findViewById(R.id.swipe_posts);
        icon_ProfileUser_main = view.findViewById(R.id.icon_ProfileUser_main);
        btn_create_new_story_main = view.findViewById(R.id.btn_create_new_story_main);
        card_msg_notRead_main = view.findViewById(R.id.card_msg_notRead_main);
        btn_compose_main = view.findViewById(R.id.btn_compose_main);
        btn_chat_main = view.findViewById(R.id.btn_chat_main);
        recyclerView_Posts = view.findViewById(R.id.recyclerView_Posts);
        header_main = view.findViewById(R.id.header_main);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        recyclerView_Posts.setLayoutManager(linearLayout);
        RefreshRecycler();
    }
}
