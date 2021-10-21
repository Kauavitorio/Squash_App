package dev.kaua.squash.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activities.EditProfileActivity;
import dev.kaua.squash.Activities.MainActivity;
import dev.kaua.squash.Activities.MessageActivity;
import dev.kaua.squash.Activities.ProfileInfoActivity;
import dev.kaua.squash.Activities.QrCodeActivity;
import dev.kaua.squash.Activities.Story.StoryActivity;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.AsyncUser_Follow;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.Actions.RecommendedPosts;
import dev.kaua.squash.Data.Stories.DtoStory;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.FollowAccountHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.PatternEditableBuilder;
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
    private static ImageView ic_account_badge_profile;
    private static CircleImageView ic_ProfileUser_profile;
    private static final String TAG = "PROFILE_FRAGMENT";
    public static TextView txt_user_name, txt_username_name, txt_user_bio_profile,
            txt_amount_following_profile, txt_amount_followers_profile, txt_joined, posts_size;
    private static Button btn_follow_following_profile, btn_go_chat_profile, btn_contact_info_profile;
    private RelativeLayout noPost_profile;
    private LinearLayout container_following_profile, container_followers_profile;
    private ImageView btn_qr_code, btn_menu_profile;
    private String username;
    private RecyclerView recyclerView_Posts_profile;

    private static View view;
    private static ProfileFragment instance;
    private final Handler timer = new Handler();
    public static long account_another_user = 0, account_id, warn_id, active_level;
    private static int control;
    public static String user_image;
    private static List<DtoStory> storyList;
    private static DaoFollowing daoFollowing;

    static final Retrofit retrofit = Methods.GetRetrofitBuilder();

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
            final FollowAccountHelper followAccountHelper = new FollowAccountHelper(requireActivity());
            String follow = getString(R.string.follow);
            String following = getString(R.string.following);
            String edit_profile = getString(R.string.edit_profile);
            int actual = Methods.parseUserLevel(txt_amount_followers_profile.getText().toString());
            if(btn_follow_following_profile.getText().toString().equals(follow)){
                btn_follow_following_profile.setBackground(requireActivity().getDrawable(R.drawable.background_button_following));
                btn_follow_following_profile.setText(requireContext().getString(R.string.following));
                btn_follow_following_profile.setTextColor(requireActivity().getColor(R.color.black));
                txt_amount_followers_profile.setText(String.valueOf((actual + 1)));
                new Handler().postDelayed(() -> btn_go_chat_profile.setVisibility(View.VISIBLE), 1000);
                followAccountHelper.DoFollow(account_another_user, username, requireActivity());
            }
            else if(btn_follow_following_profile.getText().toString().equals(following)){
                btn_follow_following_profile.setBackground(requireActivity().getDrawable(R.drawable.background_button_follow));
                btn_follow_following_profile.setText(requireContext().getString(R.string.follow));
                btn_follow_following_profile.setTextColor(requireActivity().getColor(R.color.white));
                txt_amount_followers_profile.setText(String.valueOf((actual - 1)));
                btn_go_chat_profile.setVisibility(View.GONE);
                followAccountHelper.DoUnFollow(account_another_user, requireActivity());
            }
            else if(btn_follow_following_profile.getText().toString().equals(edit_profile)){
                Intent i = new Intent(requireContext(), EditProfileActivity.class);
                startActivity(i);
            }
        });

        //  Start a chat button click
        btn_go_chat_profile.setOnClickListener(v -> {
            if(username != null && username.length() > 2){
                LoadingDialog loadingDialog = new LoadingDialog(requireActivity());
                loadingDialog.startLoading();
                DtoAccount account_chat = new DtoAccount();
                DatabaseReference reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
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

            }else ToastHelper.toast(requireActivity(), getString(R.string.not_possible_start_a_chat), ToastHelper.SHORT_DURATION);
        });

        return view;
    }

    public static ProfileFragment getInstance(){ return instance;}

    public void ReloadRecycler(){ RecommendedPosts.getUsersPosts(requireActivity(), recyclerView_Posts_profile, noPost_profile
            , posts_size, account);}

    public void LoadAnotherUser(){
        ic_ProfileUser_profile.setEnabled(false);
        btn_go_chat_profile.setVisibility(View.GONE);
        ic_account_badge_profile.setVisibility(View.GONE);
        Methods.LoadFollowersAndFollowing(requireContext(), 1);
        AsyncUser_Follow asyncUser_follow = new AsyncUser_Follow(requireActivity());
        asyncUser_follow.execute();
        control++;
        Bundle bundle = MainActivity.getInstance().SetBundleProfile();
        if(bundle != null){
            recyclerView_Posts_profile.setVisibility(View.GONE);
            control = bundle.getInt("control");
            try {
                if(bundle.getString("account_id") != null && Long.parseLong(bundle.getString("account_id")) != account.getAccount_id()){
                    if(getContext() != null){
                        if(ConnectionHelper.isOnline(getContext())){
                            LoadingDialog loadingDialog = new LoadingDialog(requireActivity());
                            loadingDialog.startLoading();
                            DtoAccount account = new DtoAccount();
                            account_another_user = Long.parseLong(bundle.getString("account_id"));
                            account.setAccount_id_cry(EncryptHelper.encrypt(bundle.getString("account_id")));
                            DtoAccount search_account = new DtoAccount();
                            search_account.setAccount_id(account_another_user);
                            RecommendedPosts.getUsersPosts(requireActivity(), recyclerView_Posts_profile, noPost_profile,
                                    posts_size, search_account);
                            AccountServices services = retrofitUser.create(AccountServices.class);
                            Call<DtoAccount> call = services.getUserInfo(account);
                            call.enqueue(new Callback<DtoAccount>() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                                    loadingDialog.dismissDialog();
                                    if(response.code() == 200){
                                        if(response.body() != null){
                                            active_level = response.body().getActive();
                                            if(active_level > DtoAccount.ACCOUNT_DISABLE
                                                    || Methods.getUserLevel(requireActivity()) == DtoAccount.ACCOUNT_IS_STAFF){
                                                Glide.with(requireActivity()).load(EncryptHelper.decrypt(response.body().getProfile_image())).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                                        .into(ic_ProfileUser_profile);
                                                user_image = EncryptHelper.decrypt(response.body().getProfile_image());
                                                txt_user_name.setText(EncryptHelper.decrypt(response.body().getName_user()));
                                                txt_username_name.setText("@" + EncryptHelper.decrypt(response.body().getUsername()));
                                                if(EncryptHelper.decrypt(response.body().getBio_user()) == null ||
                                                        EncryptHelper.decrypt(response.body().getBio_user()) != null &&
                                                                Objects.requireNonNull(EncryptHelper.decrypt(response.body().getBio_user())).replace(" ", "").length() == 0)
                                                    txt_user_bio_profile.setVisibility(View.GONE);
                                                else{
                                                    txt_user_bio_profile.setVisibility(View.VISIBLE);
                                                    txt_user_bio_profile.setText(EncryptHelper.decrypt(response.body().getBio_user()));
                                                }
                                                txt_joined.setText(LoadJoined(EncryptHelper.decrypt(response.body().getJoined_date())));
                                                Linkify.addLinks(txt_user_bio_profile, Linkify.ALL);
                                                LoadUserMentions();
                                                username = EncryptHelper.decrypt(response.body().getUsername());
                                                if(response.body().getFollowing() != null){
                                                    txt_amount_following_profile.setText(Methods.NumberTrick(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getFollowing())))));
                                                    txt_amount_followers_profile.setText(Methods.NumberTrick(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getFollowers())))));
                                                }
                                                btn_follow_following_profile.setBackground(requireActivity().getDrawable(R.drawable.background_button_follow));
                                                btn_follow_following_profile.setText(requireContext().getString(R.string.follow));
                                                btn_follow_following_profile.setTextColor(requireActivity().getColor(R.color.white));

                                                final int verified = Methods.parseUserLevel(EncryptHelper.decrypt(response.body().getVerification_level()));
                                                if(verified > DtoAccount.NORMAL_ACCOUNT){
                                                    ic_account_badge_profile.setImageDrawable(requireActivity().getDrawable(Methods.loadUserImageLevel(verified)));
                                                    ic_account_badge_profile.setVisibility(View.VISIBLE);
                                                    BangedAnimation(verified);
                                                }else ic_account_badge_profile.setVisibility(View.GONE);

                                                final DtoAccount accountStory = new DtoAccount();
                                                accountStory.setAccount_id(account_another_user);
                                                accountStory.setUsername(EncryptHelper.decrypt(response.body().getUsername()));
                                                accountStory.setProfile_image(EncryptHelper.decrypt(response.body().getProfile_image()));
                                                accountStory.setVerification_level(EncryptHelper.decrypt(response.body().getVerification_level()));

                                                SearchStory(accountStory.getAccount_id(), accountStory);

                                                btn_menu_profile.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_menu_profile_dot));
                                                btn_menu_profile.setOnClickListener(v -> Warnings.Sheet_Menu_Profile(requireActivity(), username, account_another_user));

                                                Check_Contact_Account(response.body());

                                                RecommendedPosts.getUsersPosts(requireActivity(), recyclerView_Posts_profile, noPost_profile
                                                        , posts_size, search_account);

                                                final ArrayList<DtoAccount> accounts = daoFollowing.get_followers_following(account_id, Long.parseLong(bundle.getString("account_id")));
                                                if(accounts.size() > 0){
                                                    btn_go_chat_profile.setVisibility(View.VISIBLE);
                                                    btn_follow_following_profile.setBackground(requireActivity().getDrawable(R.drawable.background_button_following));
                                                    btn_follow_following_profile.setText(requireContext().getString(R.string.following));
                                                    btn_follow_following_profile.setTextColor(requireActivity().getColor(R.color.black));
                                                }
                                                MainActivity.getInstance().ResetBundleProfile();
                                                timer.postDelayed(() -> control++,500);

                                                btn_qr_code.setVisibility(View.GONE);

                                                EnableOptions(account_another_user);

                                                CreateFollowersAndFollowingClick(account_another_user,
                                                        EncryptHelper.decrypt(response.body().getUsername()),
                                                        EncryptHelper.decrypt(response.body().getProfile_image()),
                                                        Methods.NumberTrick(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getFollowing())))),
                                                        Methods.NumberTrick(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(response.body().getFollowers())))));
                                            }else{
                                                ToastHelper.toast(requireActivity(), getString(R.string.user_not_found), ToastHelper.SHORT_DURATION);
                                                GetUserInfo(requireActivity());
                                            }

                                        }
                                    }else{
                                        ToastHelper.toast(requireActivity(), getString(R.string.user_not_found), ToastHelper.SHORT_DURATION);
                                        GetUserInfo(requireActivity());
                                    }
                                }

                                @Override
                                public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                                    loadingDialog.dismissDialog();
                                    Warnings.showWeHaveAProblem(requireContext(), ErrorHelper.PROFILE_GET_ANOTHER_USER_INFO);
                                }
                            });
                        }else ToastHelper.toast((Activity) getContext(), getContext().getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
                    }else MainActivity.getInstance().LoadMainFragment();
                }else GetUserInfo(requireActivity());
            }catch (Exception ex){
                Log.d(TAG, ex.toString());
                Warnings.showWeHaveAProblem(getContext(), ErrorHelper.PROFILE_GET_ANOTHER_USER_INFO);
            }
        }else GetUserInfo(requireActivity());
    }

    private void Check_Contact_Account(DtoAccount account) {
        btn_contact_info_profile.setVisibility(View.GONE);
        if(account != null && account.getType_acc() != null){
            try {
                final int type_acc;
                final String acc_value = EncryptHelper.decrypt(account.getType_acc());
                if(acc_value == null) type_acc = DtoAccount.ACCOUNT_DISABLE;
                else type_acc = Integer.parseInt(acc_value);
                if(type_acc == DtoAccount.BUSINESS_ACCOUNT
                        || Methods.getUserLevel(requireContext()) == DtoAccount.ACCOUNT_IS_STAFF){
                    btn_contact_info_profile.setVisibility(View.VISIBLE);
                    btn_contact_info_profile.setOnClickListener(v -> {
                        DtoAccount contact_account = new DtoAccount();
                        contact_account.setEmail(EncryptHelper.decrypt(account.getEmail()));
                        Warnings.Sheet_Contact_Profile(getActivity(), contact_account);
                    });
                }
            }catch (Exception ex){
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    private String LoadJoined(final String date) {
        String format_date = date;
        if(date != null){
            try {
                String[] split_date = format_date.split("/");
                format_date = Methods.getMonth(Integer.parseInt(split_date[1])) + " " + split_date[2];
            }catch (Exception ex){
                Log.d(TAG, "Joined -> " + ex.toString());
            }
        }
        return requireActivity().getString(R.string.joined) + " " + format_date;
    }

    @SuppressLint("SetTextI18n")
    public void GetUserInfo(Activity activity) {
        ic_ProfileUser_profile.setEnabled(false);
        ic_account_badge_profile.setVisibility(View.GONE);
        btn_go_chat_profile.setVisibility(View.GONE);
        final DtoAccount user = MyPrefs.getUserInformation(requireContext());
        account_id = user.getAccount_id();
        account.setAccount_id(account_id);
        account.setAccount_id_cry(EncryptHelper.encrypt(String.valueOf(account_id)));
        RecommendedPosts.getUsersPosts(requireActivity(), recyclerView_Posts_profile, noPost_profile, posts_size, account);

        btn_menu_profile.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_arrow_down_simple));
        btn_menu_profile.setOnClickListener(v -> Warnings.Sheet_Menu_Profile(requireActivity(), username, account_id));

        SearchStory(user.getAccount_id(), user);

        DaoAccount db = new DaoAccount(activity);
        DtoAccount account_follow = db.get_followers_following(account_id);
        if(account_follow != null && account_follow.getFollowing() != null && account_follow.getFollowers() != null){
            txt_amount_following_profile.setText(Methods.NumberTrick(Long.parseLong(account_follow.getFollowing())));
            txt_amount_followers_profile.setText(Methods.NumberTrick(Long.parseLong(account_follow.getFollowers())));
        }else{
            txt_amount_following_profile.setText(Methods.NumberTrick(0));
            txt_amount_followers_profile.setText(Methods.NumberTrick(0));
        }

        final long verified = Methods.getUserLevel(activity);
        if(verified > DtoAccount.NORMAL_ACCOUNT){
            ic_account_badge_profile.setImageDrawable(requireActivity().getDrawable(Methods.loadUserImageLevel(verified)));
            ic_account_badge_profile.setVisibility(View.VISIBLE);
            BangedAnimation(verified);
        }else ic_account_badge_profile.setVisibility(View.GONE);

        Glide.with(requireActivity()).load(user.getProfile_image()).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(ic_ProfileUser_profile);
        txt_user_name.setText(user.getName_user());
        txt_username_name.setText("@" + user.getUsername());
        if(user.getBio_user() == null ||
                user.getBio_user() != null &&
                        user.getBio_user().replace(" ", "").length() == 0)
            txt_user_bio_profile.setVisibility(View.GONE);
        else{
            txt_user_bio_profile.setVisibility(View.VISIBLE);
            txt_user_bio_profile.setText(user.getBio_user());
        }
        txt_joined.setText(LoadJoined(user.getJoined_date()));
        btn_follow_following_profile.setBackground(activity.getDrawable(R.drawable.background_button_following));
        btn_follow_following_profile.setEnabled(true);
        btn_follow_following_profile.setText(activity.getString(R.string.edit_profile));
        btn_follow_following_profile.setTextColor(activity.getColor(R.color.black));
        Linkify.addLinks(txt_user_bio_profile, Linkify.ALL);
        LoadUserMentions();

        DtoAccount account_contact = new DtoAccount();
        account_contact.setType_acc(EncryptHelper.encrypt(user.getType_acc()));
        account_contact.setEmail(EncryptHelper.encrypt(user.getEmail()));
        Check_Contact_Account(account_contact);

        btn_qr_code.setVisibility(View.VISIBLE);
        btn_qr_code.setOnClickListener(v -> {
            if(ConnectionHelper.isOnline(requireContext())){
                Intent i = new Intent(requireContext(), QrCodeActivity.class);
                i.putExtra(QrCodeActivity.PROFILE_IMAGE_TAG, user.getProfile_image());
                i.putExtra(QrCodeActivity.PROFILE_NAME_TAG, user.getName_user());
                i.putExtra(QrCodeActivity.PROFILE_USERNAME_TAG, user.getUsername());
                i.putExtra(QrCodeActivity.PROFILE_ID_TAG, account_id);
                startActivity(i);
            }else ToastHelper.toast(requireActivity(), getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
        });

        String followings = "0", followers = "0";
        if(account_follow != null && account_follow.getFollowing() != null){
            followings = Methods.NumberTrick(Long.parseLong(account_follow.getFollowing()));
            followers = Methods.NumberTrick(Long.parseLong(account_follow.getFollowers()));
        }

        EnableOptions(account_id);

        CreateFollowersAndFollowingClick(account_id, user.getUsername(), user.getProfile_image(),
                followings, followers);
    }

    void EnableOptions(long id){
        warn_id = id;
    }

    public static String UID_USER_WARN = null;

    public static void ReportUser(Activity activity, String reason){
        if(activity != null){
            LoadingDialog loadingDialog = new LoadingDialog(activity);
            loadingDialog.startLoading();

            DtoAccount dtoAccount = new DtoAccount();
            dtoAccount.setReport_from(EncryptHelper.encrypt(String.valueOf(MyPrefs.getUserInformation(activity).getAccount_id())));
            dtoAccount.setReport_to(EncryptHelper.encrypt(String.valueOf(warn_id)));
            dtoAccount.setReport_reason(reason);
            AccountServices services = retrofit.create(AccountServices.class);
            Call<DtoAccount> call = services.report_an_user(dtoAccount);
            call.enqueue(new Callback<DtoAccount>() {
                @Override
                public void onResponse(@NonNull Call<DtoAccount> call, @NonNull Response<DtoAccount> response) {
                    loadingDialog.dismissDialog();
                    if(response.code() == 200){
                        ToastHelper.toast(activity, activity.getString(R.string.report_sent_successfully), ToastHelper.SHORT_DURATION);
                    }else
                        Warnings.showWeHaveAProblem(activity, ErrorHelper.REPORT_AN_USER);
                }

                @Override
                public void onFailure(@NonNull Call<DtoAccount> call, @NonNull Throwable t) {
                    loadingDialog.dismissDialog();
                    Warnings.showWeHaveAProblem(activity, ErrorHelper.REPORT_AN_USER);
                }
            });
        }
    }

    private Animation myAnim;
    private void CreateFollowersAndFollowingClick(long id, String username, String profile_image,
                                                  String following, String followers) {
        Intent i = new Intent(requireContext(), ProfileInfoActivity.class);
        i.putExtra(ProfileInfoActivity.REQUEST_ACCOUNT_ID, String.valueOf(id));
        i.putExtra(ProfileInfoActivity.REQUEST_USERNAME_ID, username);
        i.putExtra(ProfileInfoActivity.REQUEST_FOLLOWING_AMOUNT, following);
        i.putExtra(ProfileInfoActivity.REQUEST_FOLLOWERS_AMOUNT, followers);
        i.putExtra(ProfileInfoActivity.REQUEST_PROFILE_IMAGE_ID, profile_image);

        //  Go to profile info see following
        container_following_profile.setOnClickListener(v -> {
            container_following_profile.startAnimation(myAnim);
            if(ConnectionHelper.isOnline(requireActivity())){
                i.putExtra(ProfileInfoActivity.REQUEST_ID, ProfileInfoActivity.REQUEST_FOLLOWING);
                startActivity(i);
            }else ToastHelper.toast(requireActivity(), getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
        });

        //  Go to profile info see followers
        container_followers_profile.setOnClickListener(v -> {
            container_followers_profile.startAnimation(myAnim);
            if(ConnectionHelper.isOnline(requireActivity())){
                i.putExtra(ProfileInfoActivity.REQUEST_ID, ProfileInfoActivity.REQUEST_FOLLOWERS);
                startActivity(i);
            }else ToastHelper.toast(requireActivity(), getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
        });
    }

    void Ids(View view) {
        instance = this;
        myAnim = AnimationUtils.loadAnimation(requireContext() ,R.anim.click_anim);
        account = MyPrefs.getUserInformation(requireContext());
        daoFollowing = new DaoFollowing(requireContext());
        btn_menu_profile = view.findViewById(R.id.btn_menu_profile);
        posts_size = view.findViewById(R.id.txt_posts_size_amount_profile);
        btn_go_chat_profile = view.findViewById(R.id.btn_go_chat_profile);
        btn_contact_info_profile = view.findViewById(R.id.btn_contact_info_profile);
        btn_qr_code = view.findViewById(R.id.btn_qr_code_profile);
        txt_joined = view.findViewById(R.id.txt_joined);
        recyclerView_Posts_profile = view.findViewById(R.id.recyclerView_Posts_profile);
        noPost_profile = view.findViewById(R.id.noPost_profile);
        ic_account_badge_profile = view.findViewById(R.id.ic_account_badge_profile);
        ic_ProfileUser_profile = view.findViewById(R.id.ic_ProfileUser_profile);
        txt_user_name = view.findViewById(R.id.txt_user_name);
        txt_username_name = view.findViewById(R.id.txt_username_name);
        txt_user_bio_profile = view.findViewById(R.id.txt_user_bio_profile);
        container_following_profile = view.findViewById(R.id.container_following_profile);
        container_followers_profile = view.findViewById(R.id.container_followers_profile);
        txt_user_bio_profile.setMovementMethod(BetterLinkMovementMethod.newInstance().setOnLinkClickListener((textView, url) -> {
            if (Patterns.WEB_URL.matcher(url).matches()) {
                //An web url is detected
                Methods.browseTo(requireContext(), url);
                return true;
            }
            return false;
        }));


        btn_contact_info_profile.setVisibility(View.GONE);
        btn_follow_following_profile = view.findViewById(R.id.btn_follow_following_profile);
        txt_amount_following_profile = view.findViewById(R.id.txt_amount_following_profile);
        txt_amount_followers_profile = view.findViewById(R.id.txt_amount_followers_profile);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        recyclerView_Posts_profile.setLayoutManager(linearLayout);
    }

    void LoadUserMentions(){
        if(getContext() != null){
            new PatternEditableBuilder().
                    addPattern(Pattern.compile("@(\\w+)"), getContext().getColor(R.color.base_color),
                            text -> Methods.Profile_From_USERNAME(getActivity(), text)).into(txt_user_bio_profile);

            txt_user_bio_profile.setMovementMethod(BetterLinkMovementMethod.newInstance().setOnLinkClickListener((textView, url) -> {
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    //An web url is detected
                    Methods.browseTo(requireContext(), url);
                    return true;
                }
                return false;
            }));
        }
    }

    void BangedAnimation(long level){
        if(level == DtoAccount.ACCOUNT_IS_STAFF){
            final ObjectAnimator oa1 = ObjectAnimator.ofFloat(ic_account_badge_profile, "scaleX", 1f, 0f);
            final ObjectAnimator oa2 = ObjectAnimator.ofFloat(ic_account_badge_profile, "scaleX", 0f, 1f);
            oa1.setInterpolator(new DecelerateInterpolator());
            oa2.setInterpolator(new AccelerateDecelerateInterpolator());
            oa1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    oa2.start();
                }
            });
            oa1.setDuration(1500);
            oa2.setDuration(1500);
            oa1.start();
        }else if(level == DtoAccount.VERIFY_ACCOUNT)
            ic_account_badge_profile.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.banged_anim));
    }

    void SearchStory(long userId, final DtoAccount account){
        if(ic_ProfileUser_profile != null) {
            ic_ProfileUser_profile.setEnabled(false);
            ic_ProfileUser_profile.setBorderWidth(0);
        }
        if(storyList == null) storyList = new ArrayList<>();
        if(getContext() != null && getActivity() != null &&
                ConnectionHelper.isOnline(requireContext())){
            myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.STORY_REFERENCE).child(String.valueOf(userId))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                            storyList.clear();
                            long timeCurrent = System.currentTimeMillis();

                            DtoStory story;
                            for (DataSnapshot snapshot: datasnapshot.getChildren()){
                                int countStory = 0;
                                story = snapshot.getValue(DtoStory.class);
                                if(story != null){
                                    if(timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd())
                                        countStory++;
                                    story.setSeen(snapshot.child(myFirebaseHelper.STORY_VIEWS)
                                            .child(String.valueOf(MyPrefs.getUserInformation(requireContext())
                                                    .getAccount_id())).exists());
                                }
                                if(countStory > 0)
                                    storyList.add(story);
                            }

                            if(storyList.size() > 0){
                                final DtoStory lastStory = storyList.get(storyList.size() - 1);

                                if(lastStory != null){
                                    ic_ProfileUser_profile.setEnabled(true);
                                    ic_ProfileUser_profile.setBorderWidth(9);
                                    if(!lastStory.isSeen() && !lastStory.getUserId()
                                            .equals(String.valueOf(MyPrefs.getUserInformation(requireContext()).getAccount_id()))){
                                        ic_ProfileUser_profile.setBorderColor(requireContext()
                                                .getColor(R.color.cards_background));
                                    }else{
                                        ic_ProfileUser_profile.setBorderColor(requireContext()
                                                .getColor(R.color.setting_icons));
                                    }

                                    ic_ProfileUser_profile.setOnClickListener(v -> {
                                        ic_ProfileUser_profile.startAnimation(AnimationUtils
                                                .loadAnimation(requireContext(),R.anim.click_anim));
                                        final Intent i = new Intent(requireActivity(), StoryActivity.class);
                                        i.putExtra(StoryActivity.USER_ID_TAG, lastStory.getUserId());
                                        i.putExtra(StoryActivity.USERNAME_TAG, account.getUsername());
                                        i.putExtra(StoryActivity.USER_PHOTO_TAG, account.getProfile_image());
                                        i.putExtra(StoryActivity.UPLOAD_TIME_TAG, lastStory.getUploadTime());
                                        i.putExtra(StoryActivity.USER_LEVEL_TAG, account.getVerification_level());
                                        startActivity(i);
                                    });
                                }

                            }else{
                                ic_ProfileUser_profile.setEnabled(false);
                                ic_ProfileUser_profile.setBorderWidth(0);
                            }

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    @Override
    public void onDestroy() {
        storyList = null;
        super.onDestroy();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible){
            if(getContext() != null){
                Toolbar toolbar = view.findViewById(R.id.toolbar_profile);
                ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("");
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(false); // Hide default toolbar title
                toolbar.setNavigationOnClickListener(v -> MainActivity.getInstance().LoadMainFragment());
            }
        }
    }
}
