package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.EditProfileActivity;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.MessageActivity;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.AsyncUser_Follow;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.Actions.RecommendedPosts;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/River_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

@SuppressWarnings({"unchecked", "FieldCanBeLocal"})
@SuppressLint({"UseCompatLoadingForDrawables", "StaticFieldLeak"})
public class ProfileFragment extends Fragment {
    //  Fragments Items
    private static ImageView img_banner_profile, ic_account_badge_profile, btn_go_chat_profile;
    private static CircleImageView ic_ProfileUser_profile;
    private static TextView txt_user_name, txt_username_name, txt_user_bio_profile, txt_amount_following_profile, txt_amount_followers_profile;
    private static Button btn_follow_following_profile;
    private RelativeLayout loadingPanel_profile, noPost_profile;
    private CardView btn_plus_story_profile;
    private String username;
    private RecyclerView recyclerView_Posts_profile;

    private static View view;
    private static ProfileFragment instance;
    private final Handler timer = new Handler();
    private static long account_another_user = 0, account_id;
    private static int control;
    private boolean visible_control;

    // User Info
    private static DtoAccount account = new DtoAccount();
    static final Retrofit retrofitUser = Methods.GetRetrofitBuilder();

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_activity_profile, container, false);
        Ids(view);
        GetUserInfo(requireActivity());

        btn_follow_following_profile.setOnClickListener(v -> {
            String follow = getString(R.string.follow);
            String following = getString(R.string.following);
            String edit_profile = getString(R.string.edit_profile);
            int actual = Integer.parseInt(txt_amount_followers_profile.getText().toString());
            if(btn_follow_following_profile.getText().toString().equals(follow)){
                btn_follow_following_profile.setBackground(requireActivity().getDrawable(R.drawable.background_button_following));
                btn_follow_following_profile.setText(requireContext().getString(R.string.following));
                btn_follow_following_profile.setTextColor(requireActivity().getColor(R.color.black));
                txt_amount_followers_profile.setText((actual + 1) + "");
                account = new DtoAccount();
                account.setAccount_id_cry(EncryptHelper.encrypt(account_id + ""));
                account.setAccount_id_following(EncryptHelper.encrypt(account_another_user + ""));
                AccountServices services = retrofitUser.create(AccountServices.class);
                Call<DtoAccount> call = services.follow_a_user(account);
                call.enqueue(new Callback<DtoAccount>() {
                    @Override
                    public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                        if(response.code() == 201){
                            SearchFragment.getInstance().LoadSearch();
                            Methods.LoadFollowersAndFollowing(requireActivity());
                            AsyncUser_Follow asyncUser_follow = new AsyncUser_Follow(requireActivity(), account_id);
                            asyncUser_follow.execute();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                        Warnings.showWeHaveAProblem(requireActivity());
                    }
                });
            }
            else if(btn_follow_following_profile.getText().toString().equals(following)){
                btn_follow_following_profile.setBackground(requireActivity().getDrawable(R.drawable.background_button_follow));
                btn_follow_following_profile.setText(requireContext().getString(R.string.follow));
                btn_follow_following_profile.setTextColor(requireActivity().getColor(R.color.white));
                txt_amount_followers_profile.setText((actual - 1) + "");
                account = new DtoAccount();
                account.setAccount_id_cry(EncryptHelper.encrypt(account_id + ""));
                account.setAccount_id_following(EncryptHelper.encrypt(account_another_user + ""));
                AccountServices services = retrofitUser.create(AccountServices.class);
                Call<DtoAccount> call = services.un_follow_a_user(account);
                call.enqueue(new Callback<DtoAccount>() {
                    @Override
                    public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                        if(response.code() == 201){
                            SearchFragment.getInstance().LoadSearch();
                            Methods.LoadFollowersAndFollowing(requireActivity());
                            AsyncUser_Follow asyncUser_follow = new AsyncUser_Follow(requireActivity(), account_id);
                            asyncUser_follow.execute();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                        Warnings.showWeHaveAProblem(requireActivity());
                    }
                });
            }
            else if(btn_follow_following_profile.getText().toString().equals(edit_profile)){
                Intent i = new Intent(requireContext(), EditProfileActivity.class);
                startActivity(i);
            }
        });

        //  Start a chat button click
        btn_go_chat_profile.setOnClickListener(v -> {
            if(username != null && !username.isEmpty() && username.length() > 2){
                LoadingDialog loadingDialog = new LoadingDialog(requireActivity());
                loadingDialog.startLoading();
                DtoAccount account_chat = new DtoAccount();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot fullSnapshot) {
                        for(DataSnapshot snapshot: fullSnapshot.getChildren()){
                            DtoAccount account = snapshot.getValue(DtoAccount.class);
                            if(account != null){
                                if(account.getUsername().equals(username)){
                                    if(account_chat.getAccount_id_cry() == null){
                                        account_chat.setId(account.getId());
                                        account_chat.setUsername(account.getUsername());
                                        account_chat.setAccount_id_cry(account.getId());
                                    }
                                }
                            }
                        }

                        if(account_chat.getAccount_id_cry() != null){
                            if(account_chat.getName_user() == null || !account_chat.getName_user().equals("go")){
                                if(account_chat.getUsername().equals(username)){
                                    Intent intent = new Intent(requireContext(), MessageActivity.class);
                                    intent.putExtra("userId", account_chat.getId());
                                    requireActivity().startActivity(intent);
                                    loadingDialog.dismissDialog();
                                    account_chat.setName_user("go");
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                });

            }else ToastHelper.toast(requireActivity(), getString(R.string.not_possible_start_a_chat), 0);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(visible_control)
            if(control > 0) LoadAnotherUser();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        visible_control = visible;
    }

    public static ProfileFragment getInstance(){ return instance;}

    public void LoadAnotherUser(){
        btn_go_chat_profile.setVisibility(View.GONE);
        ic_account_badge_profile.setVisibility(View.GONE);
        Methods.LoadFollowersAndFollowing(requireContext());
        AsyncUser_Follow asyncUser_follow = new AsyncUser_Follow(requireActivity(), account.getAccount_id());
        asyncUser_follow.execute();
        control++;
        Bundle bundle = MainActivity.getInstance().SetBundleProfile();
        if(bundle != null){
            loadingPanel_profile.setVisibility(View.VISIBLE);
            recyclerView_Posts_profile.setVisibility(View.GONE);
            control = bundle.getInt("control");
            if(bundle.getString("account_id") != null && Long.parseLong(bundle.getString("account_id")) != account.getAccount_id()){
                LoadingDialog loadingDialog = new LoadingDialog(requireActivity());
                loadingDialog.startLoading();
                btn_plus_story_profile.setVisibility(View.GONE);
                DtoAccount account = new DtoAccount();
                account_another_user = Long.parseLong(bundle.getString("account_id"));
                account.setAccount_id_cry(EncryptHelper.encrypt(bundle.getString("account_id")));
                RecommendedPosts.getUsersPosts(requireContext(), recyclerView_Posts_profile, loadingPanel_profile, noPost_profile, account);
                AccountServices services = retrofitUser.create(AccountServices.class);
                Call<DtoAccount> call = services.getUserInfo(account);
                call.enqueue(new Callback<DtoAccount>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                        loadingDialog.dismissDialog();
                        if(response.code() == 200){
                            if(response.body() != null){
                                Glide.with(requireActivity()).load(EncryptHelper.decrypt(response.body().getProfile_image())).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                        .into(ic_ProfileUser_profile);
                                txt_user_name.setText(EncryptHelper.decrypt(response.body().getName_user()));
                                txt_username_name.setText( " | @" + EncryptHelper.decrypt(response.body().getUsername()));
                                txt_user_bio_profile.setText(EncryptHelper.decrypt(response.body().getBio_user()));
                                Linkify.addLinks(txt_user_bio_profile, Linkify.ALL);
                                username = EncryptHelper.decrypt(response.body().getUsername());
                                txt_amount_following_profile.setText(Methods.NumberTrick(Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getFollowing())))));
                                txt_amount_followers_profile.setText(Methods.NumberTrick(Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getFollowers())))));
                                btn_follow_following_profile.setBackground(requireActivity().getDrawable(R.drawable.background_button_follow));
                                btn_follow_following_profile.setText(requireContext().getString(R.string.follow));
                                btn_follow_following_profile.setTextColor(requireActivity().getColor(R.color.white));

                                int verified = Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getVerification_level())));
                                if(verified != 0){
                                    if (verified == 2)
                                        ic_account_badge_profile.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_verified_employee_account));
                                     else
                                        ic_account_badge_profile.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_verified_account));
                                    ic_account_badge_profile.setVisibility(View.VISIBLE);
                                }
                                RecommendedPosts.getUsersPosts(requireContext(), recyclerView_Posts_profile, loadingPanel_profile, noPost_profile, account);

                                DaoFollowing daoFollowing = new DaoFollowing(getContext());
                                ArrayList<DtoAccount> accounts = daoFollowing.get_followers_following(account_id, Long.parseLong(bundle.getString("account_id")));
                                if(accounts.size() > 0){
                                    btn_go_chat_profile.setVisibility(View.VISIBLE);
                                    btn_follow_following_profile.setBackground(requireActivity().getDrawable(R.drawable.background_button_following));
                                    btn_follow_following_profile.setText(requireContext().getString(R.string.following));
                                    btn_follow_following_profile.setTextColor(requireActivity().getColor(R.color.black));
                                }
                                MainActivity.getInstance().ResetBundleProfile();
                                timer.postDelayed(() -> control++,500);
                            }
                        }else{
                            ToastHelper.toast(requireActivity(), getString(R.string.user_not_found), 0);
                            GetUserInfo(requireActivity());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                        loadingDialog.dismissDialog();
                        Warnings.showWeHaveAProblem(requireContext());
                    }
                });
            }else GetUserInfo(requireActivity());
        }else GetUserInfo(requireActivity());
    }

    @SuppressLint("SetTextI18n")
    public void GetUserInfo(Activity activity) {
        ic_account_badge_profile.setVisibility(View.GONE);
        btn_go_chat_profile.setVisibility(View.GONE);
        btn_plus_story_profile.setVisibility(View.VISIBLE);
        DtoAccount user = MyPrefs.getUserInformation(requireContext());
        account_id = user.getAccount_id();
        account.setAccount_id(account_id);
        account.setAccount_id_cry(EncryptHelper.encrypt(account_id + ""));
        RecommendedPosts.getUsersPosts(requireContext(), recyclerView_Posts_profile, loadingPanel_profile, noPost_profile, account);

        DaoAccount db = new DaoAccount(activity);
        DtoAccount account_follow = db.get_followers_following(account_id);
        txt_amount_following_profile.setText(Methods.NumberTrick(Integer.parseInt(account_follow.getFollowing())));
        txt_amount_followers_profile.setText(Methods.NumberTrick(Integer.parseInt(account_follow.getFollowers())));

        int verified = Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(user.getVerification_level())));
        if(verified != 0){
            if (verified == 2)
                ic_account_badge_profile.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_verified_employee_account));
            else
                ic_account_badge_profile.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_verified_account));
            ic_account_badge_profile.setVisibility(View.VISIBLE);
        }

        Glide.with(requireActivity()).load(user.getProfile_image()).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(ic_ProfileUser_profile);
        txt_user_name.setText(user.getName_user());
        txt_username_name.setText(" | @" + user.getUsername());
        txt_user_bio_profile.setText(user.getBio_user());
        btn_follow_following_profile.setBackground(activity.getDrawable(R.drawable.background_button_following));
        btn_follow_following_profile.setEnabled(true);
        btn_follow_following_profile.setText(activity.getString(R.string.edit_profile));
        btn_follow_following_profile.setTextColor(activity.getColor(R.color.black));
        Linkify.addLinks(txt_user_bio_profile, Linkify.ALL);
    }

    private void Ids(View view) {
        instance = this;
        account = MyPrefs.getUserInformation(requireContext());
        img_banner_profile = view.findViewById(R.id.img_banner_profile);
        loadingPanel_profile = view.findViewById(R.id.loadingPanel_profile);
        btn_go_chat_profile = view.findViewById(R.id.btn_go_chat_profile);
        btn_plus_story_profile = view.findViewById(R.id.btn_plus_story_profile);
        recyclerView_Posts_profile = view.findViewById(R.id.recyclerView_Posts_profile);
        noPost_profile = view.findViewById(R.id.noPost_profile);
        ic_account_badge_profile = view.findViewById(R.id.ic_account_badge_profile);
        ic_ProfileUser_profile = view.findViewById(R.id.ic_ProfileUser_profile);
        txt_user_name = view.findViewById(R.id.txt_user_name);
        txt_username_name = view.findViewById(R.id.txt_username_name);
        txt_user_bio_profile = view.findViewById(R.id.txt_user_bio_profile);
        txt_user_bio_profile.setMovementMethod(BetterLinkMovementMethod.newInstance().setOnLinkClickListener((textView, url) -> {
            if (Patterns.WEB_URL.matcher(url).matches()) {
                //An web url is detected
                Methods.browseTo(requireContext(), url);
                return true;
            }
            return false;
        }));
        btn_follow_following_profile = view.findViewById(R.id.btn_follow_following_profile);
        txt_amount_following_profile = view.findViewById(R.id.txt_amount_following_profile);
        txt_amount_followers_profile = view.findViewById(R.id.txt_amount_followers_profile);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        recyclerView_Posts_profile.setLayoutManager(linearLayout);
    }
}
