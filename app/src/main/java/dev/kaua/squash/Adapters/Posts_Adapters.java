package dev.kaua.squash.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.AppSupportActivity;
import dev.kaua.squash.Activitys.DeletePostReportActivity;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.PostDetailsActivity;
import dev.kaua.squash.Activitys.SquashShop.SquashShopActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Data.Post.PostServices;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.MainFragment;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.LocalDataBase.DaoPosts;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ErrorHelper;
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

@SuppressLint({"UseCompatLoadingForDrawables", "StaticFieldLeak", "SetTextI18n"})
public class Posts_Adapters extends RecyclerView.Adapter<Posts_Adapters.MyHolderPosts> {
    ArrayList<DtoPost> mPostList;
    static Activity mContext;
    static DaoPosts daoPosts;
    private static BottomSheetDialog bottomSheetDialog;
    private final Animation myAnim;
    FirebaseStorage firebaseStorage;
    private static FirebaseAnalytics mFirebaseAnalytics;
    private static Posts_Adapters instance;
    private static DtoAccount user;
    public static final boolean CAN_ANIME = true;
    public static final boolean CAN_NOT_ANIME = false;
    private static final String TAG = "POSTS_ADAPTER";
    private static int LayoutType;

    final Retrofit retrofit = Methods.GetRetrofitBuilder();

    public Posts_Adapters(ArrayList<DtoPost> ArrayList, Activity mContext) {
        /*DtoPost post_ad = new DtoPost();
        post_ad.setPost_type(DtoPost.AD_POST);
        ArrayList<DtoPost> mPostListAD = new ArrayList<>();
        mPostListAD.add(post_ad);
        mPostListAD.addAll(ArrayList);
        this.mPostList = mPostListAD;*/
        this.mPostList = ArrayList;
        instance = this;
        Posts_Adapters.mContext = mContext;
        daoPosts = new DaoPosts(mContext);
        myAnim = AnimationUtils.loadAnimation(mContext,R.anim.click_anim);
        mFirebaseAnalytics = myFirebaseHelper.getFirebaseAnalytics(mContext);
        user = MyPrefs.getUserInformation(mContext);
    }

    @NonNull
    @Override
    public MyHolderPosts onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutType = viewType;
        Context context = parent.getContext();
        View view;
        if(LayoutType == DtoPost.NORMAL_POST){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_posts, parent, false);
            return new MyHolderPosts(view);
        }else if(LayoutType == DtoPost.AD_POST){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_ad_post, parent, false);
            return new MyHolderPosts(view);
        } else{
            view = LayoutInflater.from(context).inflate(R.layout.adapter_posts, parent, false);
            return new MyHolderPosts(view);
        }
    }

    @SuppressWarnings("RegExpRedundantEscape")
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull MyHolderPosts holder, int position) {
        final DtoPost postInfo = mPostList.get(position);

        if(LayoutType == DtoPost.NORMAL_POST){
            LoadBaseInformation(holder, postInfo, position, CAN_NOT_ANIME);
            LoadMentions(holder);

            SetInfoClickable(holder);

            Check_Like(holder, position);

            LoadImages(holder, postInfo);

            holder.itemView.setOnClickListener(v -> {
                holder.itemView.startAnimation(myAnim);
                if(ConnectionHelper.isOnline(mContext)){
                    Intent i = new Intent(mContext, PostDetailsActivity.class);
                    i.putExtra("post_id", Long.parseLong(postInfo.getPost_id()));
                    i.putExtra("comment", 0);
                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(mContext, R.anim.move_to_left_go, R.anim.move_to_right_go);
                    ActivityCompat.startActivity(mContext, i, activityOptionsCompat.toBundle());
                }else ToastHelper.toast(mContext, mContext.getString(R.string.you_are_without_internet), 0);
            });

            holder.btn_comment_post.setOnClickListener(v -> {
                holder.btn_comment_post.startAnimation(myAnim);
                if(ConnectionHelper.isOnline(mContext)){
                    Intent i = new Intent(mContext, PostDetailsActivity.class);
                    i.putExtra("post_id", Long.parseLong(postInfo.getPost_id()));
                    i.putExtra("comment", 1);
                    mContext.startActivity(i);
                }else ToastHelper.toast(mContext, mContext.getString(R.string.you_are_without_internet), 0);
            });

            holder.btn_share_post.setOnClickListener(v -> {
                holder.btn_share_post.startAnimation(myAnim);
                Methods.SharePost(mContext, postInfo, mFirebaseAnalytics);
            });

            EnableActions(holder, position);

            holder.btn_like_post.setOnClickListener(v -> Like_Un_Like_A_Post(holder, position, postInfo.getPost_id()));

            if(postInfo.isSuggestion()) {
                holder.suggestion_container.setVisibility(View.VISIBLE);
                if(postInfo.getAccount_id().equals("5")) holder.txt_suggestion.setText(mContext.getString(R.string.developer_post));
            }
            else holder.suggestion_container.setVisibility(View.GONE);

            final int PostPosition = position;
            if(ConnectionHelper.isOnline(mContext)){
                final DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference();
                Query applesQuery = ref.child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD).orderByChild("post_id")
                        .equalTo(EncryptHelper.encrypt(postInfo.getPost_id()));
                applesQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!mContext.isDestroyed() && !mContext.isFinishing()){
                            for (DataSnapshot appleSnapshot: snapshot.getChildren()) {
                                final DtoPost post = appleSnapshot.getValue(DtoPost.class);
                                final DtoPost finalPost = new DtoPost();
                                if(post != null){
                                    finalPost.setPost_id(EncryptHelper.decrypt(post.getPost_id()));
                                    finalPost.setAccount_id(EncryptHelper.decrypt(post.getAccount_id()));
                                    finalPost.setActive(post.getActive());
                                    finalPost.setName_user(EncryptHelper.decrypt(post.getName_user()));
                                    finalPost.setPost_comments_amount(EncryptHelper.decrypt(post.getPost_comments_amount()));
                                    finalPost.setPost_content(EncryptHelper.decrypt(post.getPost_content()));
                                    finalPost.setPost_date(EncryptHelper.decrypt(post.getPost_date()));
                                    finalPost.setPost_likes(EncryptHelper.decrypt(post.getPost_likes()));
                                    finalPost.setPost_time(EncryptHelper.decrypt(post.getPost_time()));
                                    finalPost.setPost_topic(EncryptHelper.decrypt(post.getPost_topic()));
                                    finalPost.setProfile_image(EncryptHelper.decrypt(post.getProfile_image()));
                                    finalPost.setUsername(EncryptHelper.decrypt(post.getUsername()));
                                    finalPost.setVerification_level(EncryptHelper.decrypt(post.getVerification_level()));

                                    if(Long.parseLong(finalPost.getPost_likes()) != Long.parseLong(mPostList.get(PostPosition).getPost_likes()))
                                        LoadBaseInformation(holder, finalPost, PostPosition, CAN_ANIME);
                                    else LoadBaseInformation(holder, finalPost, PostPosition, CAN_NOT_ANIME);

                                    mPostList.get(PostPosition).setPost_likes(finalPost.getPost_likes());

                                    LoadMentions(holder);

                                    SetInfoClickable(holder);

                                    Check_Like(holder, PostPosition);

                                    LoadImages(holder, finalPost);

                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        }else if(LayoutType == DtoPost.AD_POST){
            if(ConnectionHelper.isOnline(mContext)){
                AdView adView = new AdView(mContext);
                AdSize adSize = new AdSize(300, 200);
                adView.setAdSize(adSize);
                adView.setAdUnitId(mContext.getString(R.string.main_banner));
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
                holder.adView_post.addView(adView);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mPostList.get(position).getPost_type() == DtoPost.NORMAL_POST)
            return DtoPost.NORMAL_POST;
        else if(mPostList.get(position).getPost_type() == DtoPost.AD_POST)
            return DtoPost.AD_POST;
        else return DtoPost.NORMAL_POST;
    }

    private void LoadImages(@NonNull MyHolderPosts holder, DtoPost postInfo) {
        final DtoPost img_list = daoPosts.get_post_img(Long.parseLong(postInfo.getPost_id()));
        if(img_list.getPost_images() != null && img_list.getPost_images().size() > 0 && !img_list.getPost_images().get(0).equals("NaN")){
            try{
                if(holder.getAdapterPosition() >= 0){
                    if(!mPostList.get(holder.getAdapterPosition()).isImage_loaded()){
                        // Creating Object of ViewPagerAdapterImages
                        ViewPagerAdapterImages mViewPagerAdapterImages;
                        // Initializing the ViewPager Object

                        // Initializing the ViewPagerAdapterImages
                        mViewPagerAdapterImages = new ViewPagerAdapterImages(mContext, postInfo, img_list.getPost_images());

                        // Adding the Adapter to the ViewPager
                        holder.img_view_paper.setAdapter(mViewPagerAdapterImages);

                        holder.tab_indicator_images.setupWithViewPager(holder.img_view_paper);

                        if(img_list.getPost_images().size() > 1){
                            holder.tab_indicator_images.setVisibility(View.VISIBLE);
                        }else holder.tab_indicator_images.setVisibility(View.GONE);

                        mPostList.get(holder.getAdapterPosition()).setImage_loaded(true);
                    }
                }
            }catch (Exception ex){
                Log.d(TAG, "Load Image -> " +  ex.toString());
            }
        }else {
            holder.img_view_paper.setVisibility(View.GONE);
            holder.tab_indicator_images.setVisibility(View.GONE);
        }

        holder.icon_user_profile_post.setOnClickListener(v -> {
            holder.icon_user_profile_post.startAnimation(myAnim);
            Bundle bundle = new Bundle();
            bundle.putString("account_id", postInfo.getAccount_id());
            bundle.putInt("control", 0);
            MainActivity.getInstance().GetBundleProfile(bundle);
            MainActivity.getInstance().CallProfile();
            ProfileFragment.getInstance().LoadAnotherUser();
        });
    }

    private void SetInfoClickable(@NonNull MyHolderPosts holder) {
        //  Apply all url on Texts Views
        Linkify.addLinks(holder.txt_post_content, Linkify.ALL);

        //  URL CLICK'S listener
        holder.txt_post_content.setMovementMethod(BetterLinkMovementMethod.newInstance().setOnLinkClickListener((textView, url) -> {
            if (Patterns.WEB_URL.matcher(url).matches()) {
                //An web url is detected
                Methods.browseTo(mContext, url);
                return true;
            }

            return false;
        }));
    }

    private void LoadBaseInformation(@NonNull MyHolderPosts holder, DtoPost postInfo, int position, boolean CAN_ANIMATE) {
        if(postInfo.getVerification_level() != null){
            if(Integer.parseInt(postInfo.getVerification_level()) != DtoAccount.NORMAL_ACCOUNT){
                holder.ic_account_badge.setVisibility(View.VISIBLE);
                if (Integer.parseInt(postInfo.getVerification_level()) == DtoAccount.VERIFY_ACCOUNT)
                    holder.ic_account_badge.setImageDrawable(mContext.getDrawable(R.drawable.ic_verified_account));
                else
                    holder.ic_account_badge.setImageDrawable(mContext.getDrawable(R.drawable.ic_verified_employee_account));

            }else holder.ic_account_badge.setVisibility(View.GONE);
            Glide.with(mContext).load(postInfo.getProfile_image()).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(holder.icon_user_profile_post);
            holder.txt_name_user_post.setText(postInfo.getName_user());
            holder.txt_username_post.setText( "@" + postInfo.getUsername());
            holder.txt_post_content.setText(postInfo.getPost_content());

            holder.txt_likes_post.setText(Methods.NumberTrick(Long.parseLong(postInfo.getPost_likes())));
            if(CAN_ANIMATE) holder.txt_likes_post.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up));
            holder.txt_date_time_post.setText(LastSeenRefactor(position));
            holder.txt_comments_post.setText(Methods.NumberTrick(Long.parseLong(postInfo.getPost_comments_amount())));
        }
    }

    private void LoadMentions(@NonNull MyHolderPosts holder) {
        new PatternEditableBuilder().
                addPattern(Pattern.compile("@(\\w+)"), mContext.getColor(R.color.base_color),
                        text -> Methods.Profile_From_USERNAME(mContext, text)).into(holder.txt_post_content);

        HelpSquashPattern(holder, "Ajudar o Squash");
        HelpSquashPattern(holder, "Ajudar o squash");
        HelpSquashPattern(holder, "ajudar o Squash");
        HelpSquashPattern(holder, "ajudar o squash");
        HelpSquashPattern(holder, "Help Squash");
        HelpSquashPattern(holder, "Help squash");
        HelpSquashPattern(holder, "help Squash");
        HelpSquashPattern(holder, "help squash");
        new PatternEditableBuilder().addPattern(Pattern.compile(mContext.getString(R.string.squash_shop)),
                mContext.getColor(R.color.base_color),
                        text -> {
                            Intent intent = new Intent(mContext, SquashShopActivity.class);
                            mContext.startActivity(intent);
                        }).into(holder.txt_post_content);
    }

    private void HelpSquashPattern(@NonNull MyHolderPosts holder, String s) {
        new PatternEditableBuilder().addPattern(Pattern.compile(s), mContext.getColor(R.color.base_color),
                text -> {
                    Intent intent = new Intent(mContext, AppSupportActivity.class);
                    mContext.startActivity(intent);
                }).into(holder.txt_post_content);
    }

    private String LastSeenRefactor(final int position) {
        String date = mPostList.get(position).getPost_date();
        try{
            final String[] split_date = date.split("/");
            if(split_date.length >= 5){
                @SuppressLint("SimpleDateFormat") Date date_change = new SimpleDateFormat("MMMM").parse(split_date[1]);
                if(date_change != null){
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date_change);
                    String mouthNumber = cal.get(Calendar.MONTH) + "";
                    if(cal.get(Calendar.MONTH) < 10) mouthNumber = "0" + mouthNumber;
                    date = date.replace(split_date[1], mouthNumber);
                    char[] chars = date.toCharArray();
                    chars[10] = ' ';
                    chars[16] = ' ';
                    date = new String(chars);
                }
            }
            return Methods.loadLastSeenUser(mContext, date).replace(mContext.getString(R.string.today) + " ", "");
        }catch (Exception ex){
            return Methods.loadLastSeenUser(mContext, date).replace(mContext.getString(R.string.today) + " ", "");
        }
    }

    private void EnableActions(final MyHolderPosts holder, int position) {
        try{
            int verified = Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(user.getVerification_level())));
            if(mPostList.get(position).getAccount_id() != null &&
                    Long.parseLong(mPostList.get(position).getAccount_id()) == Long.parseLong(String.valueOf(user.getAccount_id()))
            || verified == 2){
                holder.btn_actions.setVisibility(View.VISIBLE);
                holder.btn_actions.setOnClickListener(v -> {
                    DtoPost dtoPost = new DtoPost();
                    final String user_id;
                    if(verified == 2 && Long.parseLong(mPostList.get(position).getAccount_id()) != user.getAccount_id())
                        user_id = mPostList.get(position).getAccount_id();
                    else user_id = String.valueOf(user.getAccount_id());
                    dtoPost.setAccount_id(EncryptHelper.encrypt(user_id));
                    dtoPost.setPost_id(EncryptHelper.encrypt(mPostList.get(position).getPost_id()));
                    dtoPost.setDelete_by(EncryptHelper.encrypt("byUser"));
                    bottomSheetDialog = new BottomSheetDialog(mContext, R.style.BottomSheetTheme);
                    bottomSheetDialog.setCancelable(true);
                    //  Creating View for SheetMenu
                    View sheetView = LayoutInflater.from(mContext).inflate(R.layout.adapter_sheet_menu_post_action,
                            mContext.findViewById(R.id.sheet_menu_post_action));

                    sheetView.findViewById(R.id.btn_delete_post).setOnClickListener(v1 -> {
                        AlertDialog.Builder alert = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle)
                                .setTitle(mContext.getString(R.string.delete_post))
                                .setMessage(mContext.getString(R.string.delete_post_message))
                                .setPositiveButton(mContext.getString(R.string.yes), (dialog, which) -> {
                                    if(ConnectionHelper.isOnline(mContext)){
                                        dialog.dismiss();
                                        if(verified == 2 &&  Long.parseLong(mPostList.get(position).getAccount_id()) != user.getAccount_id()){
                                            Intent i = new Intent(mContext, DeletePostReportActivity.class);
                                            i.putExtra("post_id", mPostList.get(position).getPost_id());
                                            i.putExtra("user_id", user_id);
                                            mContext.startActivity(i);
                                        }else{
                                            try {
                                                PostServices services = retrofit.create(PostServices.class);
                                                Call<DtoPost> call = services.delete_post(dtoPost);

                                                LoadingDialog loadingDialog = new LoadingDialog(mContext);
                                                loadingDialog.startLoading();

                                                //  Delete post in api
                                                call.enqueue(new Callback<DtoPost>() {
                                                    @Override
                                                    public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                                                        loadingDialog.dismissDialog();
                                                        if(response.code() == 200){
                                                            try{
                                                                if(mPostList.size() > 0 && mPostList.size() >= position){
                                                                    DtoPost img_list = daoPosts.get_post_img(Long.parseLong(mPostList.get(position).getPost_id()));
                                                                    if(img_list.getPost_images() != null && img_list.getPost_images().size() > 0){
                                                                        for (int i = 0; i < img_list.getPost_images().size(); i++){
                                                                            try {
                                                                                if(img_list.getPost_images().get(i) != null){
                                                                                    firebaseStorage = myFirebaseHelper.getFirebaseStorageInstance();
                                                                                    StorageReference photoRef = firebaseStorage.getReferenceFromUrl(Objects.requireNonNull(EncryptHelper.decrypt(img_list.getPost_images().get(i))));
                                                                                    photoRef.delete().addOnSuccessListener(aVoid -> {
                                                                                        // File deleted successfully
                                                                                        Log.d(TAG, "onSuccess: deleted file");
                                                                                    }).addOnFailureListener(exception -> {
                                                                                        // Uh-oh, an error occurred!
                                                                                        Log.d(TAG, "onFailure: did not delete file");
                                                                                    });
                                                                                }
                                                                            }catch (Exception ex){
                                                                                Log.d(TAG, "onFailure: " + ex.toString());
                                                                            }
                                                                        }
                                                                    }
                                                                    MainFragment.RefreshRecycler();
                                                                }
                                                            }catch (Exception ex){
                                                                Log.d(TAG, ex.toString());
                                                            }
                                                        }else
                                                            Warnings.showWeHaveAProblem(mContext, ErrorHelper.POST_DELETE_ACTION);


                                                        LoadingDialog loadingDialog1 = new LoadingDialog(mContext);
                                                        new Handler().postDelayed(() -> {
                                                            loadingDialog1.startLoading();
                                                            DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference();
                                                            Query applesQuery = ref.child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD).orderByChild("post_id")
                                                                    .equalTo(EncryptHelper.encrypt(mPostList.get(position).getPost_id()));

                                                            //  Delete post in firebase
                                                            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                                                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                                                        Log.d(TAG, "Remove -> " + appleSnapshot.getRef());
                                                                        appleSnapshot.getRef().removeValue();
                                                                        new Handler().postDelayed(loadingDialog1::dismissDialog, 500);
                                                                    }
                                                                }
                                                                @Override
                                                                public void onCancelled(@NotNull DatabaseError databaseError) {
                                                                    Log.e(TAG, "onCancelled", databaseError.toException());
                                                                    new Handler().postDelayed(loadingDialog1::dismissDialog, 500);
                                                                }
                                                            });
                                                        }, 500);
                                                    }

                                                    @Override
                                                    public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                                                        loadingDialog.dismissDialog();
                                                        Warnings.showWeHaveAProblem(mContext, ErrorHelper.POST_DELETE_ACTION);
                                                    }
                                                });
                                            }catch (Exception ex){
                                                Log.d(TAG, ex.toString());
                                            }
                                        }
                                    }else ToastHelper.toast(mContext, mContext.getString(R.string.you_are_without_internet), 0);
                                })
                                .setNeutralButton(mContext.getString(R.string.no), (dialogInterface, i) -> dialogInterface.dismiss());
                        Dialog mDialog = alert.create();
                        mDialog.getWindow().getAttributes().windowAnimations = R.style.MyAlertDialogStyle;
                        mDialog.show();
                        bottomSheetDialog.dismiss();
                    });

                    sheetView.findViewById(R.id.btn_cancel_actions).setOnClickListener(v1 -> bottomSheetDialog.dismiss());

                    bottomSheetDialog.setContentView(sheetView);
                    bottomSheetDialog.show();
                });
            }else holder.btn_actions.setVisibility(View.GONE);
        }catch (Exception ex){
            Log.w("POST_ADAPTER", ex.toString());
            holder.btn_actions.setVisibility(View.GONE);
        }
    }

    private void Check_Like(@NotNull MyHolderPosts holder, int position) {
        boolean result_like = daoPosts.get_A_Like(Long.parseLong(mPostList.get(position).getPost_id()), user.getAccount_id());
        if(result_like) holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.red_heart));
        else holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.ic_heart));
    }

    private void Like_Un_Like_A_Post(@NotNull MyHolderPosts holder, long position, String post_id) {
        if(ConnectionHelper.isOnline(mContext)){

            boolean result_like = daoPosts.get_A_Like(Long.parseLong(post_id), user.getAccount_id());
            long like_now = Long.parseLong(mPostList.get((int) position).getPost_likes());
            Log.d(TAG, like_now + " <- Base Like");
            if(result_like) {
                holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.ic_heart));
                like_now--;
                daoPosts.delete_like(Long.parseLong(post_id), user.getAccount_id());
            }else{
                holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.red_heart));
                like_now++;
                daoPosts.Register_A_Like(Long.parseLong(post_id), user.getAccount_id());
            }
            if(like_now >= 0){
                mPostList.get((int) position).setPost_likes(String.valueOf(like_now));
                Log.d(TAG, like_now + " <- Final Like");

                //  Set posts like in firebase
                Query applesQuery = myFirebaseHelper.getFirebaseDatabase().getReference()
                        .child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD).orderByChild("post_id")
                        .equalTo(EncryptHelper.encrypt(post_id));

                final long finalLike_now = like_now;
                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("post_likes", EncryptHelper.encrypt(String.valueOf(finalLike_now)));
                            appleSnapshot.getRef().updateChildren(hashMap);
                        }
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                        Log.e("PostsAdapter", "onCancelled", databaseError.toException());
                    }
                });

                //  Do Like or Un Like
                DtoPost dtoPost = new DtoPost();
                dtoPost.setPost_id(EncryptHelper.encrypt(post_id));
                dtoPost.setAccount_id_cry(EncryptHelper.encrypt( String.valueOf(user.getAccount_id())));
                PostServices services = retrofit.create(PostServices.class);
                Call<DtoPost> call = services.like_Un_Like_A_Post(dtoPost);
                call.enqueue(new Callback<DtoPost>() {
                    @Override
                    public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {}
                    @Override
                    public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                        ToastHelper.toast(mContext, mContext.getString(R.string.problem_performing_this_action), ToastHelper.SHORT_DURATION);
                        notifyItemChanged((int) position);
                    }
                });
            }
        }else ToastHelper.toast(mContext, mContext.getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
    }

    public static Posts_Adapters getInstance(){ return instance; }

    public void NotifyChanged(long position){
        notifyItemChanged((int) position);
    }

    @Override
    public int getItemCount() { return mPostList.size(); }

    static class MyHolderPosts extends RecyclerView.ViewHolder{
        CircleImageView icon_user_profile_post;
        TextView txt_name_user_post, txt_username_post, txt_post_content, txt_date_time_post;
        TextView txt_likes_post, txt_comments_post, txt_suggestion;
        ImageView ic_account_badge, img_heart_like, btn_actions;
        RelativeLayout container_post_adapter;
        ViewPager img_view_paper;
        TabLayout tab_indicator_images;
        AdView adView_post;
        LinearLayout btn_like_post, btn_share_post, btn_comment_post, suggestion_container;

        public MyHolderPosts(@NonNull View itemView) {
            super(itemView);
            if(LayoutType == DtoPost.NORMAL_POST){
                txt_date_time_post = itemView.findViewById(R.id.txt_date_time_post);
                suggestion_container = itemView.findViewById(R.id.suggestion_container);
                txt_suggestion = itemView.findViewById(R.id.txt_suggestion);
                container_post_adapter = itemView.findViewById(R.id.container_post_adapter);
                btn_comment_post = itemView.findViewById(R.id.btn_comment_post);
                icon_user_profile_post = itemView.findViewById(R.id.icon_user_profile_post);
                txt_name_user_post = itemView.findViewById(R.id.txt_name_user_post);
                btn_actions = itemView.findViewById(R.id.btn_actions);
                img_heart_like = itemView.findViewById(R.id.img_heart_like_post);
                txt_username_post = itemView.findViewById(R.id.txt_username_post);
                txt_post_content = itemView.findViewById(R.id.txt_post_content);
                btn_share_post = itemView.findViewById(R.id.btn_share_post);
                btn_like_post = itemView.findViewById(R.id.btn_like_post);
                ic_account_badge = itemView.findViewById(R.id.ic_account_badge);
                txt_likes_post = itemView.findViewById(R.id.txt_likes_post);
                txt_comments_post = itemView.findViewById(R.id.txt_comments_post);
                img_view_paper = itemView.findViewById(R.id.img_view_paper);
                tab_indicator_images = itemView.findViewById(R.id.tab_indicator_images);
            }else{
                adView_post = itemView.findViewById(R.id.adView_post);
            }
        }
    }
}
