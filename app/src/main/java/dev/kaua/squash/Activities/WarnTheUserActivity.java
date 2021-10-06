package dev.kaua.squash.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

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
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WarnTheUserActivity extends AppCompatActivity {
    public static final String ACCOUNT_ID_REQUEST_ID = "id_request";
    public static final String ACCOUNT_ACTIVE_REQUEST_ID = "active_request";
    public static final String ACCOUNT_ACTIVE_REQUEST_UID = "uid_request";
    public static final String ACCOUNT_NAME_REQUEST_ID = "name_request";
    public static final String ACCOUNT_USERNAME_REQUEST_ID = "username_request";
    public static final String ACCOUNT_IMAGE_REQUEST_ID = "image_request";

    CardView btn_warn_user_action;
    CircleImageView ic_profile_warn;
    EditText edit_reason_warn_user;
    TextView txt_name_user, txt_warn_reason;
    long account_id;
    String account_UID;
    private static DatabaseReference reference;
    final Retrofit retrofit = Methods.GetRetrofitBuilder();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warn_the_user);
        Ids();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            account_id = bundle.getLong(ACCOUNT_ID_REQUEST_ID);
            account_UID = bundle.getString(ACCOUNT_ACTIVE_REQUEST_UID);
            txt_warn_reason.setText(CheckWarnLevel(bundle.getLong(ACCOUNT_ACTIVE_REQUEST_ID)));
            txt_name_user.setText(bundle.getString(ACCOUNT_NAME_REQUEST_ID) + " | " + bundle.getString(ACCOUNT_USERNAME_REQUEST_ID));
            Glide.with(this).load(bundle.getString(ACCOUNT_IMAGE_REQUEST_ID)).into(ic_profile_warn);
        }else finish();

        btn_warn_user_action.setOnClickListener(v -> {
            if(edit_reason_warn_user.getText().toString().length() <= 0)
                edit_reason_warn_user.setError(getString(R.string.required_field));
            else{
                if(account_id != 0){
                    LoadingDialog loadingDialog = new LoadingDialog(this);
                    loadingDialog.startLoading();

                    DtoAccount account = new DtoAccount();
                    account.setAccount_id_cry(EncryptHelper.encrypt(String.valueOf(account_id)));
                    account.setReason_warn(EncryptHelper.encrypt(edit_reason_warn_user.getText().toString()));

                    PostServices services = retrofit.create(PostServices.class);
                    Call<DtoPost> call = services.warn_an_user(account);

                    call.enqueue(new Callback<DtoPost>() {
                        @Override
                        public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                            loadingDialog.dismissDialog();
                            if(response.code() == 200){
                                finish();
                                if(response.body() != null){
                                    reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE)
                                            .child(account_UID);
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("active", response.body().getActive());

                                    reference.updateChildren(hashMap).addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()) Log.d("Warn", "Update in Realtime database Successful");
                                    });
                                }
                            }
                            else
                                Warnings.showWeHaveAProblem(WarnTheUserActivity.this, ErrorHelper.WARN_USER_SERVER_ERROR);
                        }

                        @Override
                        public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                            loadingDialog.dismissDialog();
                            Warnings.showWeHaveAProblem(WarnTheUserActivity.this, ErrorHelper.WARN_USER_SERVER_ERROR);
                        }
                    });
                }else Warnings.showWeHaveAProblem(this, ErrorHelper.WARN_USER_WITHOUT_ID);
            }
        });

        edit_reason_warn_user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_warn_user_action.setEnabled(s.toString().length() > 5);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    String CheckWarnLevel(long active){
        String action;
        if(active == DtoAccount.ACCOUNT_ACTIVE)
            action = getString(R.string.restricted_and_alerted);
        else if(active == DtoAccount.ACCOUNT_RESTRICTION)
            action = getString(R.string.suspended);
        else action = getString(R.string.reactivated);
        return getString(R.string.warn_reason, action);
    }

    void Ids(){
        btn_warn_user_action = findViewById(R.id.btn_warn_user_action);
        ic_profile_warn = findViewById(R.id.ic_profile_warn);
        txt_name_user = findViewById(R.id.txt_name_user_warn);
        txt_warn_reason = findViewById(R.id.txt_warn_reason);
        edit_reason_warn_user = findViewById(R.id.edit_reason_warn_user);
        btn_warn_user_action.setEnabled(false);
    }
}