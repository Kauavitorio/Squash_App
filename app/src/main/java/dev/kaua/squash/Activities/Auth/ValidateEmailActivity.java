package dev.kaua.squash.Activities.Auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Validation.ValidationServices;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Security.Login;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class ValidateEmailActivity extends AppCompatActivity {
    private TextView txt_who_is_sent;
    private TextInputEditText edit_verification_code;
    private Button btn_next;
    private ImageView btn_back;
    private TextView txt_didNot_receive_email_validate;

    private static String account_id, password, login_method_user;

    final Retrofit retrofitUser = Methods.GetRetrofitBuilder();

    public static final String LOGIN_METHOD_ID = "email_user";
    public static final String PASSWORD_ID = "password";
    public static final String TYPE_VALIDATE_ID = "type_validate";
    public static final String ACCOUNT_ID_ID = "account_id";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_email);
        Ids();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, new IntentFilter("validate_email_intent"));

        //  Get User Information form SignUp
        Bundle bundle = getIntent().getExtras();
        account_id = bundle.getString(ACCOUNT_ID_ID);
        password = bundle.getString(PASSWORD_ID);
        login_method_user = bundle.getString(LOGIN_METHOD_ID);
        int type_validate = bundle.getInt(TYPE_VALIDATE_ID);
        if(type_validate == 0){
            //  Show Account Created Alert
            Warnings.Base_Sheet_Alert(this, getString(R.string.account_was_created), false);
            txt_who_is_sent.setText(getString(R.string.the_code_has_been_sent, login_method_user.replace(" ", "")));
        }
        else if(type_validate == 2){
            txt_who_is_sent.setVisibility(View.GONE);
            edit_verification_code.setText(bundle.getString("verify_id"));
            TryValidate();
        }
        else{
            Warnings.Base_Sheet_Alert(this, getString(R.string.unable_to_login), false);
            txt_who_is_sent.setText(getString(R.string.the_code_has_been_sent_no_email));
        }

        //  Set back click
        btn_back.setOnClickListener(v -> Warnings.Sheet_Really_want_to_leave_emailValidation(this));


        //  Set Didn't receive email click
        txt_didNot_receive_email_validate.setOnClickListener(v -> Warnings.DidNot_receive_email(this, getString(R.string.resend_code_desc), getString(R.string.resend_code), account_id,0));

        //  Set Edit Code TextWatcher
        edit_verification_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 6){
                    btn_next.setEnabled(true);
                    btn_next.setBackgroundResource(R.drawable.custom_button_next);
                }else{
                    btn_next.setEnabled(false);
                    btn_next.setBackgroundResource(R.drawable.custom_button_disable_next);
                }
            }
        });

        //  Button Next click
        btn_next.setOnClickListener(v -> TryValidate());

    }

    private void TryValidate() {
        LoadingDialog loadingDialog = new LoadingDialog(ValidateEmailActivity.this);
        loadingDialog.startLoading();
        DtoAccount account = new DtoAccount();
        account.setAccount_id_cry(EncryptHelper.encrypt(account_id));
        account.setVerify_id(EncryptHelper.encrypt(Objects.requireNonNull(edit_verification_code.getText()).toString()));
        ValidationServices services = retrofitUser.create(ValidationServices.class);
        Call<DtoAccount> call = services.validate_email(account);
        call.enqueue(new Callback<DtoAccount>() {
            @Override
            public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                loadingDialog.dismissDialog();
                //  User's email has been successfully confirmed, now login will be performed
                if(response.code() == 200) Login.DoLogin(ValidateEmailActivity.this, login_method_user, password);

                //  Validation Code is Invalid
                else if(response.code() == 203) Warnings.Base_Sheet_Alert(ValidateEmailActivity.this, getString(R.string.the_validation_code_is_invalid), true);

                //  API Server had an error
                else Warnings.showWeHaveAProblem(ValidateEmailActivity.this, ErrorHelper.EMAIL_VALIDATION_TRY_VALIDATE);
            }

            @Override
            public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                loadingDialog.dismissDialog();
                Warnings.showWeHaveAProblem(ValidateEmailActivity.this, ErrorHelper.EMAIL_VALIDATION_TRY_VALIDATE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If this activity is destroyed in some other way,
        // you won't need to finish it by activity B,
        // because it will already be destroyed
        // So here the BroadcastReceiver is removed
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        edit_verification_code.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onBackPressed() {
        Warnings.Sheet_Really_want_to_leave_emailValidation(this);
    }

    public static String TestIntent(){ return account_id; }

    private void Ids() {
        txt_who_is_sent = findViewById(R.id.txt_who_is_sent_validate_email);
        edit_verification_code = findViewById(R.id.edit_verification_code_validate_email);
        txt_didNot_receive_email_validate = findViewById(R.id.txt_didNot_receive_email_validate_email);
        btn_back = findViewById(R.id.btn_back_validate_email);
        btn_next = findViewById(R.id.btn_next_validate_email);
        btn_next.setEnabled(false);
        btn_next.setBackgroundResource(R.drawable.custom_button_disable_next);
    }
}