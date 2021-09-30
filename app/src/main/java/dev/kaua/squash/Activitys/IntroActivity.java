package dev.kaua.squash.Activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Security.GoogleAuthHelper;
import dev.kaua.squash.Security.Login;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class IntroActivity extends AppCompatActivity {
    private CardView btn_create_account;
    private Button btn_login_intro;
    private final Handler timer = new Handler();
    private Animation myAnim;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButtonGoogle;
    private static final String TAG = "INTRO_LOG";
    private static final int RC_SIGN_IN = 333;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Ids();
        getWindow().setStatusBarColor(getColor(R.color.header_intro_color));
        getWindow().setNavigationBarColor(getColor(R.color.bottom_intro_color));

        //  Set create account btn click
        btn_create_account.setOnClickListener(v -> {
            btn_create_account.setCardBackgroundColor(getColor(R.color.color_hover));
            btn_create_account.startAnimation(myAnim);
            timer.postDelayed(() -> btn_create_account.setCardBackgroundColor(getColor(R.color.base_color)),300);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),R.anim.move_to_left_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(this, new Intent(this, SignUpActivity.class), activityOptionsCompat.toBundle());
            finish();
        });

        //  Set Login btn click
        btn_login_intro.setOnClickListener(v -> {
            btn_login_intro.startAnimation(myAnim);
            final ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),R.anim.move_to_left_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(this, new Intent(this, SignInActivity.class), activityOptionsCompat.toBundle());
            finish();
        });
    }

    private void Ids() {
        myAnim = AnimationUtils.loadAnimation(this,R.anim.click_anim);
        btn_create_account = findViewById(R.id.btn_create_account_into);
        btn_login_intro = findViewById(R.id.btn_login_intro);
        signInButtonGoogle = findViewById(R.id.sign_in_google_button);
        signInButtonGoogle.setSize(SignInButton.SIZE_WIDE);
        signInButtonGoogle.setOnClickListener(v -> signIn());

        mGoogleSignInClient = GoogleAuthHelper.getGoogleSignInClient(this);

    }

    private void signIn() {
        signInButtonGoogle.startAnimation(myAnim);
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK ) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data));
        }else ToastHelper.toast(this, getString(R.string.need_to_select_your_Google_account), ToastHelper.SHORT_DURATION);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Signed in successfully, try to login into Squash.
            TryToLogin(completedTask.getResult(ApiException.class));
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code= " + e.getStatusCode());
            TryToLogin(null);
        }
    }

    private void TryToLogin(final GoogleSignInAccount account) {
        if(account != null){
            GoogleAuthHelper.GOOGLE_ID = account.getId();
            GoogleAuthHelper.GOOGLE_EMAIL = account.getEmail();
            GoogleAuthHelper.GOOGLE_NAME = account.getDisplayName();
            if(account.getPhotoUrl() != null) GoogleAuthHelper.GOOGLE_PHOTO = account.getPhotoUrl().toString();

            final LoadingDialog loadingDialog = new LoadingDialog(this);
            loadingDialog.startLoading();

            final AccountServices services = Methods.GetRetrofitBuilder().create(AccountServices.class);

            final DtoAccount DTO = new DtoAccount();
            final String placed = Methods.shuffle(Methods.RandomCharacters(Methods.getRandomAmount()));
            DTO.setEmail( placed + EncryptHelper.encrypt(account.getEmail()) );
            DTO.setPlaced(EncryptHelper.encrypt(placed));

            final Call<DtoAccount> call = services.test_google_account(DTO);
            call.enqueue(new Callback<DtoAccount>() {
                @Override
                public void onResponse(@NonNull Call<DtoAccount> call, @NonNull Response<DtoAccount> response) {
                    loadingDialog.dismissDialog();
                    if(response.code() == GoogleAuthHelper.HAS_ACCOUNT){
                        if(GoogleAuthHelper.isGoogleLogin(IntroActivity.this))
                            Login.DoLogin(IntroActivity.this,
                                    Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(IntroActivity.this)).getEmail(), "");
                        else{
                            Warnings.Base_Sheet_Alert(IntroActivity.this, getString(R.string.our_servers_fail_to_communicate_with_google_servers), true);
                            GoogleAuthHelper.getGoogleSignInClient(IntroActivity.this).signOut()
                                    .addOnCompleteListener((Activity) IntroActivity.this, task -> GoogleAuthHelper.ResetVariable());
                        }

                    }else if(response.code() == GoogleAuthHelper.ACCOUNT_NEW){
                        final LoadingDialog lg = new LoadingDialog(IntroActivity.this);

                        final DtoAccount dto = new DtoAccount();
                        dto.setEmail(EncryptHelper.encrypt(Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(IntroActivity.this)).getEmail()));
                        dto.setGoogle_auth(EncryptHelper.encrypt(Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(IntroActivity.this)).getId()));

                        lg.startLoading();
                        AccountServices servicesAc = Methods.GetRetrofitBuilder().create(AccountServices.class);
                        Call<DtoAccount> call1 = servicesAc.active_google_login(dto);
                        call1.enqueue(new Callback<DtoAccount>() {
                            @Override
                            public void onResponse(@NonNull Call<DtoAccount> call, @NonNull Response<DtoAccount> response) {
                                lg.dismissDialog();
                                if(response.code() != GoogleAuthHelper.HAS_ACCOUNT){
                                    Warnings.Base_Sheet_Alert(IntroActivity.this, getString(R.string.our_servers_fail_to_communicate_with_google_servers), true);
                                    GoogleAuthHelper.getGoogleSignInClient(IntroActivity.this).signOut()
                                            .addOnCompleteListener((Activity) IntroActivity.this, task -> GoogleAuthHelper.ResetVariable());
                                }
                                else{
                                    if(GoogleAuthHelper.isGoogleLogin(IntroActivity.this))
                                        Login.DoLogin(IntroActivity.this,
                                                Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(IntroActivity.this)).getEmail(), "");
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<DtoAccount> call, @NonNull Throwable t) {
                                lg.dismissDialog();
                                Warnings.Base_Sheet_Alert(IntroActivity.this, getString(R.string.our_servers_fail_to_communicate_with_google_servers), true);
                                GoogleAuthHelper.getGoogleSignInClient(IntroActivity.this).signOut()
                                        .addOnCompleteListener((Activity) IntroActivity.this, task -> GoogleAuthHelper.ResetVariable());
                            }
                        });

                    }else if(response.code() == GoogleAuthHelper.NO_ACCOUNT){
                        Intent i = new Intent(IntroActivity.this, SignUpActivity.class);
                        i.putExtra(SignUpActivity.GOOGLE_ID_TAG, account.getId());
                        i.putExtra(SignUpActivity.EMAIL_TAG, account.getEmail());
                        i.putExtra(SignUpActivity.NAME_USER_TAG, account.getDisplayName());
                        i.putExtra(SignUpActivity.PHONE_TAG, account.getPhotoUrl());
                        startActivity(i);
                        finish();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DtoAccount> call, @NonNull Throwable t) {
                    loadingDialog.dismissDialog();
                    Warnings.showWeHaveAProblem(IntroActivity.this, ErrorHelper.GOOGLE_TEST_FAILURE);
                }
            });
        }
        else Warnings.Base_Sheet_Alert(IntroActivity.this, getString(R.string.our_servers_fail_to_communicate_with_google_servers), true);
    }
}