package dev.kaua.squash.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ImageView btn_back_password;
    private TextView txt_desc_verify_code;
    private Button btn_next, btn_check_verify_code, btn_change_password;
    private TextInputLayout tl_email_forgot_password, tl_password, tl_confirm_password;
    private TextInputEditText email_forgot_password, edit_password, confirm_password, verify_code_edit;
    private RelativeLayout container_insert_email, verify_code_container, set_new_password;
    private Animation myAnim, myAnimSlide;
    private final int ANIMATION_DURATION = 300;
    private String email, verify_id;

    private final Retrofit retrofit = Methods.GetRetrofitBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Ids();

        Bundle bundle = getIntent().getExtras();
        if(bundle.getString("email") != null && !bundle.getString("email").equals(""))
            email_forgot_password.setText(bundle.getString("email"));

        btn_next.setOnClickListener(v -> {
            btn_next.startAnimation(myAnim);
            if(email_forgot_password.getText() == null ||
                    !Patterns.EMAIL_ADDRESS.matcher(email_forgot_password.getText().toString().replace(" ", "")).matches())
                tl_email_forgot_password.setError(getString(R.string.please_enter_a_valid_email));
            else{
                email = email_forgot_password.getText().toString().replace(" ", "");
                DtoAccount account = new DtoAccount();
                account.setEmail(EncryptHelper.encrypt(email));
                LoadingDialog loadingDialog = new LoadingDialog(this);
                loadingDialog.startLoading();
                AccountServices services = retrofit.create(AccountServices.class);
                Call<DtoAccount> call = services.forgot_password(account);
                call.enqueue(new Callback<DtoAccount>() {
                    @Override
                    public void onResponse(@NonNull Call<DtoAccount> call, @NonNull Response<DtoAccount> response) {
                        loadingDialog.dismissDialog();
                        if(response.code() == 201){
                            txt_desc_verify_code.setText(getString(R.string.we_send_validation_code, email));
                            container_insert_email.startAnimation(myAnimSlide);
                            container_insert_email.setVisibility(View.GONE);
                            new Handler().postDelayed(() -> verify_code_container.setVisibility(View.VISIBLE),ANIMATION_DURATION);
                        }else if(response.code() == 401){
                            Log.d("LoginActions", "Email not validated");
                            ToastHelper.toast(ForgotPasswordActivity.this, getString(R.string.unable_to_login), ToastHelper.LONG_DURATION);
                            finish();
                        }else if(response.code() == 404){
                            email_forgot_password.requestFocus();
                            ToastHelper.toast(ForgotPasswordActivity.this, getString(R.string.user_not_found), ToastHelper.LONG_DURATION);
                        } else Warnings.showWeHaveAProblem(ForgotPasswordActivity.this);
                    }
                    @Override
                    public void onFailure(@NonNull Call<DtoAccount> call, @NonNull Throwable t) {
                        loadingDialog.dismissDialog();
                        Warnings.showWeHaveAProblem(ForgotPasswordActivity.this);
                    }
                });
            }
        });

        btn_check_verify_code.setOnClickListener(v -> {
            btn_check_verify_code.startAnimation(myAnim);
            if(verify_code_edit.getText() == null || verify_code_edit.getText().toString().length() < 7)
                verify_code_edit.setError(getString(R.string.the_validation_code_is_invalid));
            else{
                verify_id = verify_code_edit.getText().toString().replace(" ", "");
                DtoAccount dtoAccount = new DtoAccount();
                dtoAccount.setEmail(EncryptHelper.encrypt(email));
                dtoAccount.setVerify_id(EncryptHelper.encrypt(verify_id));
                AccountServices services = retrofit.create(AccountServices.class);
                Call<DtoAccount> call = services.check_validation_code(dtoAccount);

                LoadingDialog loadingDialog = new LoadingDialog(this);
                loadingDialog.startLoading();
                call.enqueue(new Callback<DtoAccount>() {
                    @Override
                    public void onResponse(@NonNull Call<DtoAccount> call, @NonNull Response<DtoAccount> response) {
                        loadingDialog.dismissDialog();
                        if(response.code() == 200){
                            verify_code_container.startAnimation(myAnimSlide);
                            verify_code_container.setVisibility(View.GONE);
                            new Handler().postDelayed(() -> set_new_password.setVisibility(View.VISIBLE), ANIMATION_DURATION);
                        }else
                            verify_code_edit.setError(getString(R.string.the_validation_code_is_invalid));
                    }

                    @Override
                    public void onFailure(@NonNull Call<DtoAccount> call, @NonNull Throwable t) {
                        loadingDialog.dismissDialog();
                        Warnings.showWeHaveAProblem(ForgotPasswordActivity.this);
                    }
                });
            }
        });

        btn_change_password.setOnClickListener(v -> {
            btn_change_password.startAnimation(myAnim);
            if(edit_password.getText() == null || confirm_password.getText() == null || edit_password.getText().length() < 8 || confirm_password.getText().length() < 8){
                tl_password.setError(getString(R.string.password_needs));
                tl_password.setErrorEnabled(true);
                tl_confirm_password.setError(getString(R.string.passwords_do_not_match));
                tl_confirm_password.setErrorEnabled(true);
            }else if(!edit_password.getText().toString().matches(Methods.PASSWORD_REGEX)){
                tl_password.setError(getString(R.string.password_needs));
                tl_password.setErrorEnabled(true);
            }else if(!confirm_password.getText().toString().equals(edit_password.getText().toString())){
                tl_confirm_password.setError(getString(R.string.passwords_do_not_match));
                tl_confirm_password.setErrorEnabled(true);
            }else{
                DtoAccount account = new DtoAccount();
                account.setEmail(EncryptHelper.encrypt(email));
                account.setVerify_id(EncryptHelper.encrypt(verify_id));
                account.setPassword(EncryptHelper.encrypt(EncryptHelper.encrypt(EncryptHelper.encrypt(edit_password.getText().toString()))));
                AccountServices services = retrofit.create(AccountServices.class);
                Call<DtoAccount> call = services.change_password(account);

                LoadingDialog loadingDialog = new LoadingDialog(this);
                loadingDialog.startLoading();
                call.enqueue(new Callback<DtoAccount>() {
                    @Override
                    public void onResponse(@NonNull Call<DtoAccount> call, @NonNull Response<DtoAccount> response) {
                        loadingDialog.dismissDialog();
                        if(response.code() == 200){
                            ToastHelper.toast(ForgotPasswordActivity.this, getString(R.string.password_has_successfully_been_changed), ToastHelper.LONG_DURATION);
                            finish();
                        }else Warnings.showWeHaveAProblem(ForgotPasswordActivity.this);
                    }
                    @Override
                    public void onFailure(@NonNull Call<DtoAccount> call, @NonNull Throwable t) {
                        loadingDialog.dismissDialog();
                        Warnings.showWeHaveAProblem(ForgotPasswordActivity.this);
                    }
                });
            }
        });

        CreateTextWatcher();
    }

    void CreateTextWatcher(){
        //  Email TextWatcher
        email_forgot_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().replaceAll(" ", "");
                if (!s.toString().equals(result)) {
                    email_forgot_password.setText(result);
                    email_forgot_password.setSelection(result.length());
                }
                if(email_forgot_password.getText() != null && email_forgot_password.getText().length() > 3 && !Patterns.EMAIL_ADDRESS.matcher(email_forgot_password.getText().toString().replace(" ", "")).matches())
                    tl_email_forgot_password.setError(getString(R.string.please_enter_a_valid_email));
                else tl_email_forgot_password.setErrorEnabled(false);
            }
        });

        //  Password TextWatcher
        edit_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(Objects.requireNonNull(edit_password.getText()).toString().length() > 0){
                    if (Objects.requireNonNull(edit_password.getText()).toString().indexOf(' ') > 0){
                        tl_password.setError(getString(R.string.password_cannot_contain_spaces));
                        tl_password.setErrorEnabled(true);
                    }else{
                        if (!edit_password.getText().toString().matches(Methods.PASSWORD_REGEX)){
                            tl_password.setError(getString(R.string.password_needs));
                            tl_password.setErrorEnabled(true);
                        }else
                            tl_password.setErrorEnabled(false);
                    }
                }else
                    tl_password.setErrorEnabled(false);
            }
        });

        //  Confirm Password TextWatcher
        confirm_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(edit_password.getText() != null && confirm_password.getText() != null && confirm_password.getText().toString().length() > 0){
                    if (!confirm_password.getText().toString().equals(edit_password.getText().toString())){
                        tl_confirm_password.setError(getString(R.string.passwords_do_not_match));
                        tl_confirm_password.setErrorEnabled(true);
                    }else
                        tl_confirm_password.setErrorEnabled(false);
                }else{
                    tl_confirm_password.setError(getString(R.string.passwords_do_not_match));
                    tl_confirm_password.setErrorEnabled(true);
                }
            }
        });
    }

    void Ids(){
        getWindow().setStatusBarColor(getColor(R.color.base_color));
        myAnim = AnimationUtils.loadAnimation(this,R.anim.click_anim);
        myAnimSlide = AnimationUtils.loadAnimation(this,R.anim.exit_to_left);
        btn_back_password = findViewById(R.id.btn_back_password);
        tl_password = findViewById(R.id.tl_password_forgot_password);
        verify_code_edit = findViewById(R.id.verify_code_edit);
        set_new_password = findViewById(R.id.set_new_password);
        tl_confirm_password = findViewById(R.id.tl_confirm_password_forgot_password);
        edit_password = findViewById(R.id.password_forgot_password);
        btn_check_verify_code = findViewById(R.id.btn_check_verify_code);
        confirm_password = findViewById(R.id.confirm_password_forgot_password);
        btn_change_password = findViewById(R.id.btn_change_password);
        txt_desc_verify_code = findViewById(R.id.txt_desc_verify_code);
        container_insert_email = findViewById(R.id.container_insert_email);
        verify_code_container = findViewById(R.id.verify_code_container);
        tl_email_forgot_password = findViewById(R.id.tl_email_forgot_password);
        email_forgot_password = findViewById(R.id.email_forgot_password);
        btn_next = findViewById(R.id.btn_next_forgot_password);
        container_insert_email.setVisibility(View.VISIBLE);
        set_new_password.setVisibility(View.GONE);
        verify_code_container.setVisibility(View.GONE);
        btn_back_password.setOnClickListener(v -> {
            btn_back_password.startAnimation(myAnim);
            finish();
        });
    }
}