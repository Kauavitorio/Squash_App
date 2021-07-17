package dev.kaua.squash.Activitys;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Data.Post.PostServices;
import dev.kaua.squash.Fragments.MainFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ComposeActivity extends AppCompatActivity {
    ImageView btn_close_compose;
    EditText edit_compose_msg;
    CardView btn_post;
    List<String> post_image = new ArrayList<>();

    DtoAccount userAccount;

    final Retrofit retrofit = Methods.GetRetrofitBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        Ids();

        btn_post.setOnClickListener(v -> {
            LoadingDialog loadingDialog = new LoadingDialog(this);
            loadingDialog.startLoading();
            Calendar c = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df_date = new SimpleDateFormat("dd MMMM yyyy HH:mm a");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("HH:mm a");
            String date = df_date.format(c.getTime()).replace(" ", "/");
            String time = df_time.format(c.getTime());

            //  Set on DtoPost post information
            DtoPost post = new DtoPost();
            post.setAccount_id(EncryptHelper.encrypt(userAccount.getAccount_id() + ""));
            post.setPost_time(EncryptHelper.encrypt(time));
            post.setPost_date(EncryptHelper.encrypt(date));
            post.setPost_content(EncryptHelper.encrypt(edit_compose_msg.getText().toString()));
            post.setPost_topic(EncryptHelper.encrypt(""));
            post.setPost_images(post_image);

            PostServices services = retrofit.create(PostServices.class);
            Call<DtoPost> call = services.do_new_post(post);
            call.enqueue(new Callback<DtoPost>() {
                @Override
                public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                    loadingDialog.dismissDialog();
                    if(response.code() == 201){
                        MainFragment.RefreshRecycler();
                        finish();
                    }else Warnings.showWeHaveAProblem(ComposeActivity.this);
                }

                @Override
                public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                    loadingDialog.dismissDialog();
                    Warnings.showWeHaveAProblem(ComposeActivity.this);
                }
            });


        });
    }

    private void Ids() {
        userAccount = MainActivity.getInstance().getUserInformation();
        btn_post = findViewById(R.id.btn_post);
        btn_close_compose = findViewById(R.id.btn_close_compose);
        edit_compose_msg = findViewById(R.id.edit_compose_msg);
        btn_close_compose.setOnClickListener(v -> finish());
    }
}