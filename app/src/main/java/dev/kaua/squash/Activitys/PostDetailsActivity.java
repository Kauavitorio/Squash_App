package dev.kaua.squash.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.AsyncComments_Posts;
import dev.kaua.squash.Data.Post.AsyncLikes_Posts;
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
    private ImageView ic_account_badge_post, img_firstImage_post, img_secondImage_post, img_heart_like_post, btn_actions, ic_account_badge_profile_compose_comment;
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
                swipeRefreshLayout_comments.setVisibility(View.GONE);
                DtoAccount account = MyPrefs.getUserInformation(PostDetailsActivity.this);
                Picasso.get().load(account.getProfile_image()).into(ic_ProfileUser_profile_compose_comment);
                txt_user_name_compose_comment.setText(account.getName_user());
                txt_username_name_compose_comment.setText("| @" + account.getUsername());
                if(Integer.parseInt(account.getVerification_level()) != 0){
                    ic_account_badge_profile_compose_comment.setVisibility(View.VISIBLE);
                    if (Integer.parseInt(Objects.requireNonNull(account.getVerification_level())) == 1)
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
            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String body = Methods.BASE_URL_HTTPS + "share/" + EncryptHelper.decrypt(post_info.getUsername()) + "/post/" + EncryptHelper.decrypt(post_info.getPost_id())
                    + "?s=" + Methods.RandomCharactersWithoutSpecials(3);
            myIntent.putExtra(Intent.EXTRA_TEXT,body);
            startActivity(Intent.createChooser(myIntent, "Share Using"));
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
                                    Warnings.showWeHaveAProblem(PostDetailsActivity.this);
                            }

                            @Override
                            public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                                loadingDialog.dismissDialog();
                                Warnings.showWeHaveAProblem(PostDetailsActivity.this);
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
                dtoPost.setPost_id(EncryptHelper.encrypt(post_id + ""));
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
                            txt_comments_post.setText(Methods.NumberTrick(current_comments));
                            btn_post_comment.setEnabled(true);
                            container_compose_comment.setVisibility(View.GONE);
                            edit_comment_msg.setText("");
                            LoadComments(post_id);
                            try {
                                MainFragment.RefreshRecycler();
                            }catch (Exception ex){
                                Log.d("Post_Details", ex.getMessage());
                            }
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
                        Warnings.showWeHaveAProblem(PostDetailsActivity.this);
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

                boolean result_like = daoPosts.get_A_Like(post_id, Long.parseLong(user.getAccount_id() + ""));
                if(result_like) {
                    img_heart_like_post.setImageDrawable(getDrawable(R.drawable.ic_heart));
                    long like_now = current_likes;
                    like_now = like_now - 1;
                    txt_likes_post.setText(Methods.NumberTrick(like_now));
                }else{
                    img_heart_like_post.setImageDrawable(getDrawable(R.drawable.red_heart));
                    long like_now = current_likes;
                    like_now = like_now + 1;
                    txt_likes_post.setText(Methods.NumberTrick(like_now));
                }

                //  Do Like or Un Like
                DtoPost dtoPost = new DtoPost();
                dtoPost.setPost_id(EncryptHelper.encrypt(post_id + ""));
                dtoPost.setAccount_id_cry(EncryptHelper.encrypt(user.getAccount_id() + ""));
                PostServices services = retrofit.create(PostServices.class);
                Call<DtoPost> call = services.like_Un_Like_A_Post(dtoPost);
                call.enqueue(new Callback<DtoPost>() {
                    @Override
                    public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                        AsyncLikes_Posts async = new AsyncLikes_Posts(PostDetailsActivity.this , Long.parseLong(user.getAccount_id() + ""));
                        //noinspection unchecked
                        async.execute();
                        try {
                            MainFragment.RefreshRecycler();
                        }catch (Exception ex){
                            Log.d("Post_Details", ex.getMessage());
                        }
                    }
                    @Override
                    public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) { Warnings.showWeHaveAProblem(PostDetailsActivity.this); }
                });
            }
            else Warnings.NeedLoginWithShortCutAlert(this, 0);
        });
    }

    private void StartSendNotify(String comment) {
        if(post_info.getUsername() != null && Objects.requireNonNull(EncryptHelper.decrypt(post_info.getUsername())).length() > 2){
            DtoAccount account_chat = new DtoAccount();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.addValueEventListener(new ValueEventListener() {
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

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fUser.getUid(), R.drawable.pumpkin_default_image, username+": "+ comment, getString(R.string.new_comment), receiver, "comment_id");

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
                            Warnings.showWeHaveAProblem(PostDetailsActivity.this);
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
        SharedPreferences sp_First = getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        account.setAccount_id_cry(EncryptHelper.decrypt(sp_First.getString("pref_account_id", null)));
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
        img_firstImage_post = findViewById(R.id.img_firstImage_post_details);
        txt_user_name_compose_comment = findViewById(R.id.txt_user_name_compose_comment);
        btn_post_comment_Cancel = findViewById(R.id.btn_post_comment_Cancel);
        txt_username_name_compose_comment = findViewById(R.id.txt_username_name_compose_comment);
        img_secondImage_post = findViewById(R.id.img_secondImage_post_details);
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
        recycler_comments.setLayoutManager(new LinearLayoutManager(this));

        edit_comment_msg.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void LoadAPost(long post_id) {
        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoading();
        PostServices services = retrofit.create(PostServices.class);
        Call<DtoPost> call = services.get_post_info(post_id);
        call.enqueue(new Callback<DtoPost>() {
            @Override
            public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                loadingDialog.dismissDialog();
                if(response.code() != 200){
                    Toast.makeText(PostDetailsActivity.this, getString(R.string.post_not_found), Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    container_post.setVisibility(View.VISIBLE);
                    if(response.body() != null){
                        post_info.setPost_id(response.body().getPost_id());
                        post_info.setAccount_id(response.body().getAccount_id());
                        post_info.setVerification_level(response.body().getVerification_level());
                        post_info.setName_user(response.body().getName_user());
                        post_info.setUsername(response.body().getUsername());
                        post_info.setProfile_image(response.body().getProfile_image());
                        post_info.setPost_date(response.body().getPost_date());
                        post_info.setPost_time(response.body().getPost_time());
                        post_info.setPost_content(response.body().getPost_content());
                        post_info.setPost_images(response.body().getPost_images());
                        post_info.setPost_likes(response.body().getPost_likes());
                        post_info.setPost_comments_amount(response.body().getPost_comments_amount());
                        post_info.setPost_topic(response.body().getPost_topic());
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
                        txt_username_post.setText("| @" + EncryptHelper.decrypt(post_info.getUsername()));
                        txt_date_time_post.setText(" • " + EncryptHelper.decrypt(post_info.getPost_time()));
                        txt_post_content.setText(EncryptHelper.decrypt(post_info.getPost_content()));
                        Linkify.addLinks(txt_post_content, Linkify.ALL);
                        txt_likes_post.setText(Methods.NumberTrick(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post_info.getPost_likes())))));
                        txt_comments_post.setText(Methods.NumberTrick(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post_info.getPost_comments_amount())))));

                        if(post_info.getPost_images() == null || post_info.getPost_images().size() <= 0) container_post_images.setVisibility(View.GONE);
                        else{
                            container_post_images.setVisibility(View.VISIBLE);
                            String url_img_one = EncryptHelper.decrypt(post_info.getPost_images().get(0));
                            Picasso.get().load(url_img_one)
                                    .into(img_firstImage_post);
                            img_firstImage_post.setVisibility(View.VISIBLE);
                            container_post_images.setVisibility(View.VISIBLE);
                            if(post_info.getPost_images().size() > 1) {
                                Picasso.get().load(EncryptHelper.decrypt(post_info.getPost_images().get(1)))
                                        .into(img_secondImage_post);
                                img_secondImage_post.setVisibility(View.VISIBLE);
                            }
                        }

                        swipeRefreshLayout_comments.setVisibility(View.VISIBLE);
                        if(comment_type != 0){
                            swipeRefreshLayout_comments.setVisibility(View.GONE);
                            DtoAccount account = MyPrefs.getUserInformation(PostDetailsActivity.this);
                            Picasso.get().load(account.getProfile_image()).into(ic_ProfileUser_profile_compose_comment);
                            txt_user_name_compose_comment.setText(account.getName_user());
                            txt_username_name_compose_comment.setText("| @" + account.getUsername());
                            if(Integer.parseInt(account.getVerification_level()) != 0){
                                ic_account_badge_profile_compose_comment.setVisibility(View.VISIBLE);
                                if (Integer.parseInt(Objects.requireNonNull(account.getVerification_level())) == 1)
                                    ic_account_badge_profile_compose_comment.setImageDrawable(getDrawable(R.drawable.ic_verified_account));
                                else
                                    ic_account_badge_profile_compose_comment.setImageDrawable(getDrawable(R.drawable.ic_verified_employee_account));
                            }else ic_account_badge_profile_compose_comment.setVisibility(View.GONE);
                            container_compose_comment.setVisibility(View.VISIBLE);
                            edit_comment_msg.requestFocus();
                            KeyboardUtils.showKeyboard(PostDetailsActivity.this);
                            edit_comment_msg.requestFocus();
                        }

                        LoadComments(post_id);

                    }else{
                        Toast.makeText(PostDetailsActivity.this, getString(R.string.post_not_found), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
            @Override
            public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                loadingDialog.dismissDialog();
                Warnings.showWeHaveAProblem(PostDetailsActivity.this);
            }
        });

    }

    private void LoadComments(long post_id) {
        AsyncComments_Posts async = new AsyncComments_Posts(PostDetailsActivity.this, recycler_comments,
                swipeRefreshLayout_comments, post_id);
        //noinspection unchecked
        async.execute();
    }
}