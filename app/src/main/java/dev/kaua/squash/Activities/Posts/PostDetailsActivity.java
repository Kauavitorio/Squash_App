package dev.kaua.squash.Activities.Posts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Adapters.Posts.ViewPagerAdapterImages;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.AsyncComments_Posts;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Data.Post.PostServices;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.MainFragment;
import dev.kaua.squash.LocalDataBase.DaoPosts;
import dev.kaua.squash.Notifications.APIService;
import dev.kaua.squash.Notifications.Client;
import dev.kaua.squash.Notifications.Data;
import dev.kaua.squash.Notifications.MyResponse;
import dev.kaua.squash.Notifications.Sender;
import dev.kaua.squash.Notifications.Token;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.KeyboardUtils;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
public class PostDetailsActivity extends AppCompatActivity {
    private CircleImageView icon_user_profile, ic_ProfileUser_profile_compose_comment;
    private RelativeLayout container_post;
    private ImageView ic_account_badge_post, img_heart_like_post, btn_actions, ic_account_badge_profile_compose_comment;
    private TextView txt_name_user_post, txt_username_post, txt_date_time_post, txt_post_content, txt_likes_post, txt_comments_post;
    private TextView txt_user_name_compose_comment, txt_username_name_compose_comment;
    private LinearLayout btn_like_post, btn_comment_post, btn_share_post;
    private LinearLayout container_post_images;
    private CardView container_compose_comment, btn_post_comment, btn_post_comment_Cancel;
    private EditText edit_comment_msg;
    private SwipeRefreshLayout swipeRefreshLayout_comments;
    private BottomSheetDialog bottomSheetDialog;
    private RecyclerView recycler_comments;
    static DaoPosts daoPosts;
    ViewPager img_view_paper;
    TabLayout tab_indicator_images;
    InputMethodManager imm;
    FirebaseStorage firebaseStorage;
    FirebaseUser fUser;
    private final String TAG = "PostDetails";
    String base_comment = "";

    APIService apiService;

    DtoAccount account = new DtoAccount();
    DtoPost post_info = new DtoPost();
    final Retrofit retrofit = Methods.GetRetrofitBuilder();
    long post_id, current_comments, current_likes;
    int comment_type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        Ids();

        Bundle bundle = getIntent().getExtras();
        post_id = bundle.getLong("post_id");
        comment_type = bundle.getInt("comment");
        LoadAPost(post_id);

        swipeRefreshLayout_comments.setOnRefreshListener(() -> LoadComments(post_id));

        btn_comment_post.setOnClickListener(v -> {
            if(account.getAccount_id_cry() != null){
                final long user_level = Methods.getUserLevel(PostDetailsActivity.this);
                swipeRefreshLayout_comments.setVisibility(View.GONE);
                DtoAccount account = MyPrefs.getUserInformation(PostDetailsActivity.this);
                Picasso.get().load(account.getProfile_image()).into(ic_ProfileUser_profile_compose_comment);
                txt_user_name_compose_comment.setText(account.getName_user());
                txt_username_name_compose_comment.setText("| @" + account.getUsername());
                if(user_level != DtoAccount.ACCOUNT_DISABLE){
                    ic_account_badge_profile_compose_comment.setVisibility(View.VISIBLE);
                    if (user_level == DtoAccount.VERIFY_ACCOUNT)
                        ic_account_badge_profile_compose_comment.setImageDrawable(getDrawable(R.drawable.ic_verified_account));
                    else
                        ic_account_badge_profile_compose_comment.setImageDrawable(getDrawable(R.drawable.ic_verified_employee_account));
                }else ic_account_badge_profile_compose_comment.setVisibility(View.GONE);
                container_compose_comment.setVisibility(View.VISIBLE);
                edit_comment_msg.requestFocus();
                KeyboardUtils.showKeyboard(PostDetailsActivity.this);
                edit_comment_msg.requestFocus();
            }else Warnings.NeedLoginWithShortCutAlert(this, 0);
        });

        btn_share_post.setOnClickListener(v -> {
            DtoPost post = new DtoPost();
            post.setUsername(EncryptHelper.decrypt(post_info.getUsername()));
            post.setPost_id(EncryptHelper.decrypt(post_info.getPost_id()));
            Methods.SharePost(this, post, myFirebaseHelper.getFirebaseAnalytics(this));
        });

        btn_actions.setOnClickListener(v -> {
            if(account.getAccount_id_cry() != null){
                DtoPost dtoPost = new DtoPost();
                dtoPost.setAccount_id(EncryptHelper.encrypt(account.getAccount_id_cry() + ""));
                dtoPost.setPost_id(EncryptHelper.encrypt(post_id + ""));
                bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetTheme);
                bottomSheetDialog.setCancelable(true);
                //  Creating View for SheetMenu
                View sheetView = LayoutInflater.from(this).inflate(R.layout.adapter_sheet_menu_post_action,
                        findViewById(R.id.sheet_menu_post_action));

                sheetView.findViewById(R.id.btn_delete_post).setOnClickListener(v1 -> {
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle(getString(R.string.delete_post));
                    alert.setMessage(getString(R.string.delete_post_message));
                    alert.setNeutralButton(getString(R.string.no), null);
                    alert.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        PostServices services = retrofit.create(PostServices.class);
                        Call<DtoPost> call = services.delete_post(dtoPost);

                        LoadingDialog loadingDialog = new LoadingDialog(this);
                        loadingDialog.startLoading();
                        call.enqueue(new Callback<DtoPost>() {
                            @Override
                            public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                                loadingDialog.dismissDialog();
                                if(response.code() == 200){
                                    DtoPost img_list = daoPosts.get_post_img(post_id);
                                    if(img_list.getPost_images() != null && img_list.getPost_images().size() > 0){
                                        for (int i = 0; i < img_list.getPost_images().size(); i++){
                                            if(img_list.getPost_images().get(i) != null){
                                                firebaseStorage = FirebaseStorage.getInstance();
                                                StorageReference photoRef = firebaseStorage.getReferenceFromUrl(Objects.requireNonNull(EncryptHelper.decrypt(img_list.getPost_images().get(i))));
                                                photoRef.delete().addOnSuccessListener(aVoid -> {
                                                    // File deleted successfully
                                                    Log.d("POSTS_ADAPTER", "onSuccess: deleted file");
                                                }).addOnFailureListener(exception -> {
                                                    // Uh-oh, an error occurred!
                                                    Log.d("POSTS_ADAPTER", "onFailure: did not delete file");
                                                });
                                            }
                                        }
                                    }
                                    MainFragment.RefreshRecycler();
                                    finish();
                                }else
                                    Warnings.showWeHaveAProblem(PostDetailsActivity.this, ErrorHelper.POST_DETAIL_DELETE_ACTION);
                            }

                            @Override
                            public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                                loadingDialog.dismissDialog();
                                Warnings.showWeHaveAProblem(PostDetailsActivity.this, ErrorHelper.POST_DETAIL_DELETE_ACTION);
                            }
                        });
                    });
                    bottomSheetDialog.dismiss();
                    alert.show();
                });

                sheetView.findViewById(R.id.btn_cancel_actions).setOnClickListener(v1 -> bottomSheetDialog.dismiss());

                bottomSheetDialog.setContentView(sheetView);
                bottomSheetDialog.show();
            } else Warnings.NeedLoginWithShortCutAlert(this, 0);
        });

        btn_post_comment.setOnClickListener(v -> {
            btn_post_comment.setEnabled(false);
            if(edit_comment_msg.getText() != null && edit_comment_msg.getText().toString().length() > 0){
                LoadingDialog loadingDialog = new LoadingDialog(this);
                loadingDialog.startLoading();

                String comment = edit_comment_msg.getText().toString();

                DtoPost dtoPost = new DtoPost();
                dtoPost.setPost_id(EncryptHelper.encrypt(String.valueOf(post_id)));
                dtoPost.setAccount_id(EncryptHelper.encrypt(account.getAccount_id_cry()));
                dtoPost.setComment(EncryptHelper.encrypt(comment));
                PostServices services = retrofit.create(PostServices.class);
                Call<DtoPost> call = services.create_a_comment(dtoPost);
                call.enqueue(new Callback<DtoPost>() {
                    @Override
                    public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                        loadingDialog.dismissDialog();
                        if(response.code() == 201){
                            StartSendNotify(comment);

                            swipeRefreshLayout_comments.setVisibility(View.VISIBLE);
                            current_comments++;

                            //  Update post comment number
                            DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference();
                            Query applesQuery = ref.child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD).orderByChild("post_id")
                                    .equalTo(EncryptHelper.encrypt(String.valueOf(post_id)));

                            final long FinalCurrent_comments = current_comments;
                            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("post_comments_amount", EncryptHelper.encrypt(String.valueOf(FinalCurrent_comments)));
                                        appleSnapshot.getRef().updateChildren(hashMap);
                                    }
                                }

                                @Override
                                public void onCancelled(@NotNull DatabaseError databaseError) {
                                    Log.e("PostsAdapter", "onCancelled", databaseError.toException());
                                }
                            });

                            txt_comments_post.setText(Methods.NumberTrick(current_comments));
                            btn_post_comment.setEnabled(true);
                            container_compose_comment.setVisibility(View.GONE);
                            edit_comment_msg.setText("");
                            LoadComments(post_id);
                        }else {
                            btn_post_comment.setEnabled(true);
                            hideSoftKeyboard(PostDetailsActivity.this);
                            finish();
                            ToastHelper.toast(PostDetailsActivity.this, getString(R.string.post_not_found), 0);
                            try {
                                MainFragment.RefreshRecycler();
                            }catch (Exception ex){
                                Log.d("Post_Details", ex.getMessage());
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                        loadingDialog.dismissDialog();
                        hideSoftKeyboard(PostDetailsActivity.this);
                        Warnings.showWeHaveAProblem(PostDetailsActivity.this, ErrorHelper.POST_DETAIL_COMMENT_ACTION);
                    }
                });
            }else showError(edit_comment_msg, getString(R.string.required_field));
        });

        btn_post_comment_Cancel.setOnClickListener(v -> {
            swipeRefreshLayout_comments.setVisibility(View.VISIBLE);
            current_comments++;
            txt_comments_post.setText(Methods.NumberTrick(current_comments));
            btn_post_comment.setEnabled(true);
            container_compose_comment.setVisibility(View.GONE);
            edit_comment_msg.setText("");
        });

        btn_like_post.setOnClickListener(v -> {
            if(account.getAccount_id_cry() != null){
                DtoAccount user = MyPrefs.getUserInformation(this);

                boolean result_like = daoPosts.get_A_Like(post_id, user.getAccount_id());
                long like_now = current_likes;
                if(result_like) {
                    img_heart_like_post.setImageDrawable(getDrawable(R.drawable.ic_heart));
                    like_now--;
                    daoPosts.delete_like(post_id, user.getAccount_id());
                }else{
                    img_heart_like_post.setImageDrawable(getDrawable(R.drawable.red_heart));
                    like_now++;
                    daoPosts.Register_A_Like(post_id, user.getAccount_id());
                }
                if(like_now >= 0){
                   // txt_likes_post.setText(Methods.NumberTrick(like_now));
                    current_likes = like_now;

                    //  Set posts like in firebase
                    DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference();
                    Query applesQuery = ref.child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD).orderByChild("post_id")
                            .equalTo(EncryptHelper.encrypt(String.valueOf(post_id)));

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
                            Log.e("PostInfo", "onCancelled", databaseError.toException());
                        }
                    });

                    //  Do Like or Un Like
                    final DtoPost dtoPost = new DtoPost();
                    dtoPost.setPost_id(EncryptHelper.encrypt(String.valueOf(post_id)));
                    dtoPost.setAccount_id_cry(EncryptHelper.encrypt(String.valueOf(user.getAccount_id())));
                    PostServices services = retrofit.create(PostServices.class);
                    Call<DtoPost> call = services.like_Un_Like_A_Post(dtoPost);
                    call.enqueue(new Callback<DtoPost>() {
                        @Override
                        public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                            try {
                                MainFragment.RefreshRecycler();
                            }catch (Exception ex){
                                Log.d("Post_Details", ex.getMessage());
                            }
                        }
                        @Override
                        public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) { Warnings.showWeHaveAProblem(PostDetailsActivity.this, ErrorHelper.POST_DETAIL_LIKE_ACTION); }
                    });
                }
            }
            else Warnings.NeedLoginWithShortCutAlert(this, 0);
        });
    }

    private void StartSendNotify(String comment) {
        if(post_info.getUsername() != null && Objects.requireNonNull(EncryptHelper.decrypt(post_info.getUsername())).length() > 2){
            DtoAccount account_chat = new DtoAccount();
            DatabaseReference reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot fullSnapshot) {
                    for(DataSnapshot snapshot: fullSnapshot.getChildren()){
                        DtoAccount account = snapshot.getValue(DtoAccount.class);
                        if(account != null){
                            if(account.getUsername().equals(EncryptHelper.decrypt(post_info.getUsername()))){
                                if(account_chat.getAccount_id_cry() == null){
                                    account_chat.setId(account.getId());
                                    account_chat.setUsername(account.getUsername());
                                    account_chat.setAccount_id_cry(account.getId());
                                    account_chat.setChat_id("go");
                                }
                            }
                        }
                    }

                    if(account_chat.getAccount_id_cry() != null){
                        if(account_chat.getUsername().equals(EncryptHelper.decrypt(post_info.getUsername())) && account_chat.getChat_id().equals("go")
                                && !base_comment.equals(comment)){
                            if(!account_chat.getId().equals(myFirebaseHelper.getFirebaseUser().getUid()))
                                sendNotification(account_chat.getId(),MyPrefs.getUserInformation(PostDetailsActivity.this).getUsername(),
                                        comment);
                            account_chat.setChat_id(account_chat.getId());
                            base_comment = comment;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {}
            });

        }
    }

    private void sendNotification(String receiver, String username, String comment){

        DatabaseReference tokens = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.TOKENS_REFERENCE);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fUser.getUid(), String.valueOf(Data.TYPE_COMMENT), username+": "+ comment, getString(R.string.new_comment), receiver);

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {
                            if(response.code() == 200){
                                assert response.body() != null;
                                if(response.body().success != 1)
                                    Log.w(TAG, "Send Message Notification -> Failed");
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<MyResponse> call, @NotNull Throwable t) {
                            Warnings.showWeHaveAProblem(PostDetailsActivity.this, ErrorHelper.NOTIFICATION_SENDER_POST_COMMENT);
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    private void showError(@NonNull EditText editText, String error){
        btn_post_comment.setEnabled(true);
        editText.setError(error);
        editText.requestFocus();
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void Ids() {
        fUser = myFirebaseHelper.getFirebaseUser();
        apiService = Client.getClient(Methods.FCM_URL).create(APIService.class);
        daoPosts = new DaoPosts(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        account.setAccount_id_cry(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id()));
        icon_user_profile = findViewById(R.id.icon_user_profile_post_details);
        txt_name_user_post = findViewById(R.id.txt_name_user_post_details);
        swipeRefreshLayout_comments = findViewById(R.id.swipeRefreshLayout_comments);
        recycler_comments = findViewById(R.id.recycler_comments);
        ic_ProfileUser_profile_compose_comment = findViewById(R.id.ic_ProfileUser_profile_compose_comment);
        ic_account_badge_post = findViewById(R.id.ic_account_badge_post);
        txt_username_post = findViewById(R.id.txt_username_post_details);
        txt_post_content = findViewById(R.id.txt_post_content_details);
        ic_account_badge_profile_compose_comment = findViewById(R.id.ic_account_badge_profile_compose_comment);
        edit_comment_msg = findViewById(R.id.edit_comment_msg);
        txt_date_time_post = findViewById(R.id.txt_date_time_post_details);
        txt_user_name_compose_comment = findViewById(R.id.txt_user_name_compose_comment);
        btn_post_comment_Cancel = findViewById(R.id.btn_post_comment_Cancel);
        txt_username_name_compose_comment = findViewById(R.id.txt_username_name_compose_comment);
        btn_like_post = findViewById(R.id.btn_like_post_details);
        container_post_images = findViewById(R.id.container_post_images_details);
        img_heart_like_post = findViewById(R.id.img_heart_like_post_details);
        txt_likes_post = findViewById(R.id.txt_likes_post_details);
        btn_post_comment = findViewById(R.id.btn_post_comment);
        btn_comment_post = findViewById(R.id.btn_comment_post_details);
        txt_comments_post = findViewById(R.id.txt_comments_post_details);
        btn_share_post = findViewById(R.id.btn_share_post_details);
        btn_actions = findViewById(R.id.btn_actions_details);
        container_post = findViewById(R.id.container_post);
        container_compose_comment = findViewById(R.id.container_compose_comment);
        container_post.setVisibility(View.GONE);
        container_compose_comment.setVisibility(View.GONE);
        ic_account_badge_post.setVisibility(View.GONE);
        img_view_paper = findViewById(R.id.img_view_paper_details);
        tab_indicator_images = findViewById(R.id.tab_indicator_images_details);
        recycler_comments.setLayoutManager(new LinearLayoutManager(this));

        edit_comment_msg.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void LoadAPost(long post_id) {
        if(ConnectionHelper.isOnline(this)){
            LoadingDialog loadingDialog = new LoadingDialog(this);
            loadingDialog.startLoading();
            PostServices services = retrofit.create(PostServices.class);
            Call<DtoPost> call = services.get_post_info(post_id);
            call.enqueue(new Callback<DtoPost>() {
                @Override
                public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                    if(response.code() != 200){
                        loadingDialog.dismissDialog();
                        Toast.makeText(PostDetailsActivity.this, getString(R.string.post_not_found), Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        if(myFirebaseHelper.getFirebaseAuth().getUid() != null){
                            container_post.setVisibility(View.VISIBLE);
                            final DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference();
                            Query applesQuery = ref.child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD).orderByChild("post_id")
                                    .equalTo(EncryptHelper.encrypt(String.valueOf(post_id)));
                            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!PostDetailsActivity.this.isDestroyed() && !PostDetailsActivity.this.isFinishing()){
                                        for (DataSnapshot appleSnapshot: snapshot.getChildren()) {
                                            final DtoPost post = appleSnapshot.getValue(DtoPost.class);
                                            if(post != null){
                                                container_post.setVisibility(View.VISIBLE);
                                                loadingDialog.dismissDialog();

                                                post_info.setPost_id(post.getPost_id());
                                                post_info.setAccount_id(post.getAccount_id());
                                                post_info.setVerification_level(post.getVerification_level());
                                                post_info.setName_user(post.getName_user());
                                                post_info.setUsername(post.getUsername());
                                                post_info.setProfile_image(post.getProfile_image());
                                                post_info.setPost_date(post.getPost_date());
                                                post_info.setPost_time(post.getPost_time());
                                                post_info.setPost_content(post.getPost_content());
                                                post_info.setPost_images(post.getPost_images());
                                                post_info.setPost_likes(post.getPost_likes());
                                                post_info.setPost_comments_amount(post.getPost_comments_amount());
                                                post_info.setPost_topic(post.getPost_topic());
                                                current_comments = Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post_info.getPost_comments_amount())));
                                                current_likes = Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post_info.getPost_likes())));
                                                if(Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(post_info.getVerification_level()))) != 0){
                                                    ic_account_badge_post.setVisibility(View.VISIBLE);
                                                    if (Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(post_info.getVerification_level()))) == 1)
                                                        ic_account_badge_post.setImageDrawable(getDrawable(R.drawable.ic_verified_account));
                                                    else
                                                        ic_account_badge_post.setImageDrawable(getDrawable(R.drawable.ic_verified_employee_account));

                                                }else ic_account_badge_post.setVisibility(View.GONE);

                                                if(account.getAccount_id_cry() != null){
                                                    boolean result_like = daoPosts.get_A_Like(post_id, Long.parseLong(account.getAccount_id_cry()));
                                                    if(result_like) img_heart_like_post.setImageDrawable(getDrawable(R.drawable.red_heart));
                                                    else img_heart_like_post.setImageDrawable(getDrawable(R.drawable.ic_heart));

                                                    if(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post_info.getAccount_id())))
                                                            == Long.parseLong(account.getAccount_id_cry())) btn_actions.setVisibility(View.VISIBLE);
                                                    else btn_actions.setVisibility(View.GONE);
                                                }else btn_actions.setVisibility(View.GONE);

                                                Glide.with(PostDetailsActivity.this).load(EncryptHelper.decrypt(post_info.getProfile_image())).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                                        .into(icon_user_profile);

                                                txt_name_user_post.setText(EncryptHelper.decrypt(post_info.getName_user()));
                                                txt_username_post.setText("@" + EncryptHelper.decrypt(post_info.getUsername()));
                                                txt_date_time_post.setText(LastSeenRefactor(EncryptHelper.decrypt(post_info.getPost_date())));
                                                txt_post_content.setText(EncryptHelper.decrypt(post_info.getPost_content()));
                                                Linkify.addLinks(txt_post_content, Linkify.ALL);
                                                txt_likes_post.setText(Methods.NumberTrick(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post_info.getPost_likes())))));
                                                txt_comments_post.setText(Methods.NumberTrick(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post_info.getPost_comments_amount())))));

                                                if(post_info.getPost_images() == null || post_info.getPost_images().size() <= 0) container_post_images.setVisibility(View.GONE);
                                                else{
                                                    container_post_images.setVisibility(View.VISIBLE);
                                                    img_view_paper.setVisibility(View.VISIBLE);
                                                    // Creating Object of ViewPagerAdapterImages
                                                    ViewPagerAdapterImages mViewPagerAdapterImages;
                                                    // Initializing the ViewPager Object

                                                    // Initializing the ViewPagerAdapterImages
                                                    mViewPagerAdapterImages = new ViewPagerAdapterImages(PostDetailsActivity.this, post_info, post_info.getPost_images());

                                                    // Adding the Adapter to the ViewPager
                                                    img_view_paper.setAdapter(mViewPagerAdapterImages);

                                                    tab_indicator_images.setupWithViewPager(img_view_paper);

                                                    if(post_info.getPost_images().size() > 1){
                                                        tab_indicator_images.setVisibility(View.VISIBLE);
                                                    }else tab_indicator_images.setVisibility(View.GONE);
                                                    container_post_images.setVisibility(View.VISIBLE);
                                                }

                                                swipeRefreshLayout_comments.setVisibility(View.VISIBLE);
                                                if(comment_type != 0){
                                                    swipeRefreshLayout_comments.setVisibility(View.GONE);
                                                    DtoAccount account = MyPrefs.getUserInformation(PostDetailsActivity.this);
                                                    Picasso.get().load(account.getProfile_image()).into(ic_ProfileUser_profile_compose_comment);
                                                    txt_user_name_compose_comment.setText(account.getName_user());
                                                    txt_username_name_compose_comment.setText(" @" + account.getUsername());
                                                    if(Integer.parseInt(account.getVerification_level()) != 0){
                                                        ic_account_badge_profile_compose_comment.setVisibility(View.VISIBLE);
                                                        if (Integer.parseInt(Objects.requireNonNull(account.getVerification_level())) == 1)
                                                            ic_account_badge_profile_compose_comment.setImageDrawable(getDrawable(R.drawable.ic_verified_account));
                                                        else
                                                            ic_account_badge_profile_compose_comment.setImageDrawable(getDrawable(R.drawable.ic_verified_employee_account));
                                                        BangedAnimation();
                                                    }else ic_account_badge_profile_compose_comment.setVisibility(View.GONE);
                                                    container_compose_comment.setVisibility(View.VISIBLE);
                                                    edit_comment_msg.requestFocus();
                                                    KeyboardUtils.showKeyboard(PostDetailsActivity.this);
                                                    edit_comment_msg.requestFocus();
                                                }

                                                LoadComments(post_id);

                                                //  Get current likes
                                                Query applesQuery = ref.child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD).orderByChild("post_id")
                                                        .equalTo(EncryptHelper.encrypt(String.valueOf(post_id)));

                                                applesQuery.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(!PostDetailsActivity.this.isDestroyed() && !PostDetailsActivity.this.isFinishing()){
                                                            for (DataSnapshot appleSnapshot: snapshot.getChildren()) {
                                                                try {
                                                                    DtoPost dtoPost = appleSnapshot.getValue(DtoPost.class);
                                                                    if(dtoPost != null){
                                                                        current_likes = Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(dtoPost.getPost_likes())));
                                                                        txt_likes_post.setText(Methods.NumberTrick(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(dtoPost.getPost_likes())))));
                                                                        txt_comments_post.setText(Methods.NumberTrick(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(dtoPost.getPost_comments_amount())))));
                                                                    }
                                                                }catch (Exception ex){
                                                                    Log.d("PostDetails", ex.toString());
                                                                }
                                                            }
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {}
                                                });

                                            }else{
                                                Toast.makeText(PostDetailsActivity.this, getString(R.string.post_not_found), Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }else{
                            ToastHelper.toast(PostDetailsActivity.this, getString(R.string.must_be_logged_in), ToastHelper.SHORT_DURATION);
                            finish();
                        }
                    }
                }
                @Override
                public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                    loadingDialog.dismissDialog();
                    Warnings.showWeHaveAProblem(PostDetailsActivity.this, ErrorHelper.POST_DETAIL_LOAD_INFO);
                }
            });
        }else{
            ToastHelper.toast(this, getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
            finish();
        }
    }

    private String LastSeenRefactor(final String date_get) {
        String date = date_get;
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
            return Methods.loadLastSeenUser(this, date);
        }catch (Exception ex){
            return Methods.loadLastSeenUser(this, date);
        }
    }

    void BangedAnimation(){

        final ObjectAnimator oa1 = ObjectAnimator.ofFloat(ic_account_badge_post, "scaleX", 1f, 0f);
        final ObjectAnimator oa2 = ObjectAnimator.ofFloat(ic_account_badge_post, "scaleX", 0f, 1f);
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
    }

    private void LoadComments(long post_id) {
        AsyncComments_Posts async = new AsyncComments_Posts(PostDetailsActivity.this, recycler_comments,
                swipeRefreshLayout_comments, post_id);
        //noinspection unchecked
        async.execute();
    }
}