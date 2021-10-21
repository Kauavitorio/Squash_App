package dev.kaua.squash.Activities.Admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Data.Post.PostServices;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DeletePostReportActivity extends AppCompatActivity {
    CircleImageView ic_ProfileUser;
    EditText edit_reason;
    CardView btn_delete;
    DtoAccount account;
    final Retrofit retrofit = Methods.GetRetrofitBuilder();
    final DtoPost dtoPost = new DtoPost();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_post_report);
        Ids();

        Bundle bundle = getIntent().getExtras();
        dtoPost.setPost_id(EncryptHelper.encrypt(bundle.getString("post_id")));
        dtoPost.setAccount_id(EncryptHelper.encrypt(bundle.getString("user_id")));

        btn_delete.setOnClickListener(v -> {
            String reason = edit_reason.getText().toString();
            if(reason.length() > 20){
                dtoPost.setDelete_reason(EncryptHelper.encrypt(reason));
                dtoPost.setDelete_by(EncryptHelper.encrypt(account.getAccount_id() + ""));
                try {
                    PostServices services = retrofit.create(PostServices.class);
                    Call<DtoPost> call = services.delete_post(dtoPost);

                    LoadingDialog loadingDialog = new LoadingDialog(this);
                    loadingDialog.startLoading();

                    DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference();
                    Query applesQuery = ref.child("Posts").child("Published").orderByChild("post_id")
                            .equalTo(dtoPost.getPost_id());

                    //  Delete post in firebase
                    applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                appleSnapshot.getRef().removeValue();
                            }
                        }
                        @Override
                        public void onCancelled(@NotNull DatabaseError databaseError) {
                            Log.e("PostsAdapter", "onCancelled", databaseError.toException());
                        }
                    });

                    //  Delete post in api
                    call.enqueue(new Callback<DtoPost>() {
                        @Override
                        public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                            loadingDialog.dismissDialog();
                            if(response.code() == 200)
                                finish();
                            else
                                Warnings.showWeHaveAProblem(DeletePostReportActivity.this, ErrorHelper.DELETE_POST_REPORT_API);
                        }

                        @Override
                        public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                            loadingDialog.dismissDialog();
                            Warnings.showWeHaveAProblem(DeletePostReportActivity.this, ErrorHelper.DELETE_POST_REPORT_API);
                        }
                    });
                }catch (Exception ex){
                    ToastHelper.toast(this, getString(R.string.there_was_a_communication_problem), ToastHelper.SHORT_DURATION);
                    finish();
                }

            }else ToastHelper.toast(this, getString(R.string.need_to_insert_reason_delete_post), ToastHelper.SHORT_DURATION);
        });


    }

    private void Ids() {
        ic_ProfileUser = findViewById(R.id.ic_ProfileUser_profile_delete_post);
        edit_reason = findViewById(R.id.edit_reason_post_delete_report);
        btn_delete = findViewById(R.id.btn_delete_post_report);

        Toolbar toolbar = findViewById(R.id.toolbar_delete_post_report);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        account = MyPrefs.getUserInformation(this);
        Glide.with(this).load(account.getProfile_image()).into(ic_ProfileUser);
    }
}