package dev.kaua.squash.Activities.Auth;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.System.DtoSystem;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoSystem;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Security.GoogleAuthHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
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

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

@SuppressLint("StaticFieldLeak")
@SuppressWarnings("ConstantConditions")
public class SignUpActivity extends AppCompatActivity {
    private ImageView btn_back_signUp;
    private TextInputEditText edit_name, edit_email, edit_phone, edit_bornDate, edit_password;
    private TextInputLayout email_tl_signUp, phone_tl_signUp, bornDate_tl_signUp, password_tl_signUp;
    private Button btn_next, btn_signUp;
    private static FirebaseAnalytics mFirebaseAnalytics;
    private Timer timer = new Timer();
    private final Handler timerHandler = new Handler();
    private final long DELAY = 1000; // in ms
    private final Calendar myCalendar = Calendar.getInstance();
    private static DatePickerDialog.OnDateSetListener date;
    private LoadingDialog loadingDialog;
    private Animation myAnim;
    private static SignUpActivity instance;
    private static SharedPreferences mPrefs;
    private TextView txt_policy_and_privacy;
    private CheckBox policy_and_privacy_check;

    int age_user = 0;
    boolean policy_and_privacy = false;
    String name_user, email, phone;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    final Retrofit retrofitUser = Methods.GetRetrofitBuilder();

    private static final String TAG = "signUP_TAG";
    public static final String ERROR_CODE_TAG = "error_code";
    public static final String NAME_USER_TAG = "name_user";
    public static final String EMAIL_TAG = "email_user";
    public static final String PHONE_TAG = "phone_user";
    public static final String BIRTH_TAG = "date_birth";
    public static final String PASSWORD_TAG = "password";
    public static final String AGE_TAG = "age";
    public static final String GOOGLE_ID_TAG = "googleID_USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Ids();
        RunEditTextErrors();
        checking_password_have_minimum_characters();
        final Bundle bundle = getIntent().getExtras();
        Log.d(TAG, GoogleAuthHelper.isGoogleLogin(this) + "");
        if(bundle != null){
            if(GoogleAuthHelper.getGoogleId() != null && GoogleAuthHelper.getGoogleId().length() > 3 && bundle.getInt(ERROR_CODE_TAG) == 0){
                edit_name.setText(GoogleAuthHelper.GOOGLE_NAME);
                edit_email.setText(GoogleAuthHelper.GOOGLE_EMAIL);
                edit_email.setEnabled(false);
            }else{
                CheckErrorCode(bundle.getInt(ERROR_CODE_TAG));
                edit_name.setText(bundle.getString(NAME_USER_TAG));
                edit_email.setText(bundle.getString(EMAIL_TAG));
                edit_phone.setText(bundle.getString(PHONE_TAG));
                edit_bornDate.setText(bundle.getString(BIRTH_TAG));
                edit_password.setText(bundle.getString(PASSWORD_TAG));
                age_user = bundle.getInt(AGE_TAG);
            }
        }

        //  Creating Calendar
        date = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        //  Privacy Policy click
        txt_policy_and_privacy.setOnClickListener(v -> Methods.browseTo(this, getString(R.string.privacy_policy_url)));

        //  Privacy Policy check status
        policy_and_privacy_check.setOnClickListener(v -> policy_and_privacy = policy_and_privacy_check.isChecked());

        //  Select Birth date Click
        edit_bornDate.setOnClickListener(v -> ShowCalendar());

        //  Back button click
        btn_back_signUp.setOnClickListener(v -> Back_to_intro());

        //  Next button click
        btn_next.setOnClickListener(v -> {
            btn_next.startAnimation(myAnim);
            try {
                String name_user_full = Objects.requireNonNull(edit_name.getText()).toString();
                name_user = name_user_full.replaceAll("^ +| +$|( )+", "$1");
                String[] split_name = name_user.split(" ");
                if(split_name[1].length() <= 0)
                    edit_name.setError(getString(R.string.it_is_necessary_last_name));
                else if (!Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(edit_email.getText()).toString()).matches())
                    email_tl_signUp.setError(getString(R.string.please_enter_a_valid_email));
                else if(!Methods.isValidPhoneNumber(Objects.requireNonNull(edit_phone.getText()).toString()))
                    phone_tl_signUp.setError(getString(R.string.please_enter_a_valid_phone_number));
                else if(Objects.requireNonNull(edit_bornDate.getText()).toString().length() <= 0)
                    bornDate_tl_signUp.setError(getString(R.string.age_warning));
                else if(age_user < 13)
                    bornDate_tl_signUp.setError(getString(R.string.age_warning));
                else if(!Objects.requireNonNull(edit_password.getText()).toString().matches(Methods.PASSWORD_REGEX))
                    password_tl_signUp.setError(getString(R.string.password_needs));
                else if(!policy_and_privacy) ToastHelper.toast(this, getString(R.string.required_to_accept_the_privacy_policy_term), 0);
                else{
                    loadingDialog.startLoading();
                    timerHandler.postDelayed(() -> {
                        startActivity(new Intent(this, TermsAccountActivity.class));
                        loadingDialog.dismissDialog();
                    }, 300);
                }
            }catch (Exception ex){
                edit_name.setError(getString(R.string.it_is_necessary_last_name));
            }
        });

        //  Sign Up button click
        btn_signUp.setOnClickListener(v -> DoRegister());

    }

    void DoRegister() {
        btn_signUp.startAnimation(myAnim);
        if(!policy_and_privacy) ToastHelper.toast(this, getString(R.string.required_to_accept_the_privacy_policy_term), 0);
        else{

            loadingDialog = new LoadingDialog(this);
            loadingDialog.startLoading();

            //  Getting joined date before send user information to API
            String joined_date = String.valueOf(System.currentTimeMillis());

            // User information storage on your Dto
            final DtoAccount account = new DtoAccount();
            account.setName_user(EncryptHelper.encrypt(Methods.RemoveSpace(Objects.requireNonNull(edit_name.getText()).toString())));
            account.setEmail(EncryptHelper.encrypt(Methods.RemoveSpace(Objects.requireNonNull(edit_email.getText()).toString().replace(" ", ""))));
            account.setPhone_user(EncryptHelper.encrypt(Methods.RemoveSpace(Objects.requireNonNull(edit_phone.getText()).toString())));
            account.setPassword(EncryptHelper.encrypt(edit_password.getText().toString()));
            account.setBorn_date(EncryptHelper.encrypt(Methods.RemoveSpace(Objects.requireNonNull(edit_bornDate.getText()).toString())));
            account.setJoined_date(EncryptHelper.encrypt(joined_date));
            final String device_login = Build.BRAND + ", " + Build.MODEL;
            account.setLogin_info(EncryptHelper.encrypt(device_login));
            account.setBio_user(EncryptHelper.encrypt(getString(R.string.default_bio)));

            // Generate token to user can be logged in firebase services
            final String token = Methods.RandomCharacters(50);
            account.setToken(EncryptHelper.encrypt(token));

            //  Register User in Firebase
            mAuth = myFirebaseHelper.getFirebaseAuth();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null) mAuth.signOut();

            mFirebaseAnalytics = myFirebaseHelper.getFirebaseAnalytics(this);

            final AccountServices services = retrofitUser.create(AccountServices.class);
            Call<DtoAccount> call;
            if(GoogleAuthHelper.isGoogleLogin(this)) {
                account.setProfile_image(EncryptHelper.encrypt(GoogleAuthHelper.GOOGLE_PHOTO));
                account.setGoogle_auth(EncryptHelper.encrypt(GoogleAuthHelper.GOOGLE_ID));
                call = services.registerUserGoogle(account);
            }else
                call = services.registerUser(account);

            call.enqueue(new Callback<DtoAccount>() {
                @Override
                public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                    if(response.code() == 201){
                        FirebaseRegister(response, account, token);
                    }else if(response.code() == 401)
                        //  Email is already used
                        ReloadPage(401);
                    else if(response.code() == 423)
                        //  Phone is already used
                        ReloadPage(423);
                    else if(response.code() == 406)
                        //  BadWord in user name
                        ReloadPage(406);
                    else
                        Warnings.showWeHaveAProblem(SignUpActivity.this, ErrorHelper.SIGN_UP_ACTION);
                }

                @Override
                public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                    loadingDialog.dismissDialog();
                    Warnings.showWeHaveAProblem(SignUpActivity.this, ErrorHelper.SIGN_UP_ACTION);
                }
            });
        }
    }

    private void FirebaseRegister(@NonNull Response<DtoAccount> response, DtoAccount account, String token) {
        mAuth.createUserWithEmailAndPassword(EncryptHelper.decrypt(account.getEmail()), token)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadingDialog.dismissDialog();

                        Privacy_PolicyCheck();

                        // Sign in success, now go to register user into API
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.w("Auth", "OK" + user);

                        String userId = user.getUid();

                        //  Creating analytic for sign up event
                        Bundle bundle_Analytics = new Bundle();
                        bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_ID, userId);
                        bundle_Analytics.putString(FirebaseAnalytics.Param.ITEM_NAME, EncryptHelper.decrypt(response.body().getUsername()));
                        bundle_Analytics.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle_Analytics);

                        Calendar c = Calendar.getInstance();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("dd MMMM yyyy HH:mm a");
                        String formattedDate = df_time.format(c.getTime());

                        //  Register new user on Firebase Database
                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", userId);
                        assert response.body() != null;
                        hashMap.put("username", EncryptHelper.decrypt(response.body().getUsername()));
                        hashMap.put("name_user", edit_name.getText().toString());
                        hashMap.put("search", EncryptHelper.decrypt(response.body().getUsername()));
                        hashMap.put("account_id_cry", response.body().getAccount_id_cry());
                        hashMap.put("imageURL", "default");
                        hashMap.put("status_chat", "offline");
                        hashMap.put("last_seen", formattedDate);
                        hashMap.put("typingTo", "noOne");

                        DtoAccount follow = new DtoAccount();
                        follow.setAccount_id_cry(response.body().getAccount_id_cry());
                        follow.setAccount_id_following(EncryptHelper.encrypt("25"));
                        AccountServices services = retrofitUser.create(AccountServices.class);
                        Call<DtoAccount> call_follow = services.follow_a_user(follow);
                        call_follow.enqueue(new Callback<DtoAccount>() {
                            @Override
                            public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {}
                            @Override
                            public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {}
                        });

                        reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()) Log.d("User", "Register in Realtime database Successful");
                        });

                        //  User has been created so now go to the Email Validation
                        final Intent i = new Intent(SignUpActivity.this, ValidateEmailActivity.class);
                        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),R.anim.move_to_left_go, R.anim.move_to_right_go);
                        mPrefs = getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putString("pref_account_id", response.body().getAccount_id_cry());
                        editor.putString("pref_email", EncryptHelper.encrypt(edit_email.getText().toString()));
                        editor.putString("pref_password", EncryptHelper.encrypt(edit_password.getText().toString()));
                        editor.apply();
                        i.putExtra("account_id", EncryptHelper.decrypt(response.body().getAccount_id_cry()));
                        i.putExtra("email_user", edit_email.getText().toString());
                        i.putExtra("password", edit_password.getText().toString());
                        i.putExtra("type_validate", 0);
                        ActivityCompat.startActivity(SignUpActivity.this, i, activityOptionsCompat.toBundle());
                        finish();

                    } else {
                        loadingDialog.dismissDialog();
                        //  Email is already used
                        ReloadPage(401);
                        Log.d("Auth", "Error " + task.getException());
                    }
                });
    }

    //  Method to get last version of Privacy Policy
    private void Privacy_PolicyCheck(){
        if(ConnectionHelper.isOnline(SignUpActivity.this)){
            reference = myFirebaseHelper.getFirebaseDatabase().getReference("System");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(ConnectionHelper.isOnline(SignUpActivity.this)){
                        DtoSystem system = snapshot.getValue(DtoSystem.class);
                        if(system != null) {
                            DaoSystem daoSystem = new DaoSystem(SignUpActivity.this);
                            daoSystem.setPrivacyPolicy(system.getPrivacy_policy());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    //  Method to check what is the error the register got
    private void CheckErrorCode(int error_code) {
        switch (error_code){
            case 406:
                Warnings.Base_Sheet_Alert(SignUpActivity.this, getString(R.string.bad_username), true);
                break;
            case 423:
                Warnings.Base_Sheet_Alert(SignUpActivity.this, getString(R.string.phone_is_already_used), true);
                break;
            case 401:
                Warnings.Base_Sheet_Alert(SignUpActivity.this, getString(R.string.email_is_already_used), true);
                break;
        }
    }

    // Method to reload Pag when register get error
    private void ReloadPage(int error_code){
        if(loadingDialog != null) loadingDialog.dismissDialog();
        final Intent goTo_SignUp = new Intent(this, SignUpActivity.class);
        final ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),R.anim.move_to_left_go, R.anim.move_to_right_go);
        goTo_SignUp.putExtra(ERROR_CODE_TAG, error_code);
        goTo_SignUp.putExtra(NAME_USER_TAG, Objects.requireNonNull(edit_name.getText()).toString());
        goTo_SignUp.putExtra(EMAIL_TAG, Objects.requireNonNull(edit_email.getText()).toString());
        goTo_SignUp.putExtra(PHONE_TAG, Objects.requireNonNull(edit_phone.getText()).toString());
        goTo_SignUp.putExtra(BIRTH_TAG, Objects.requireNonNull(edit_bornDate.getText()).toString());
        goTo_SignUp.putExtra(PASSWORD_TAG, Objects.requireNonNull(edit_password.getText()).toString());
        goTo_SignUp.putExtra(AGE_TAG, age_user);
        ActivityCompat.startActivity(this, goTo_SignUp, activityOptionsCompat.toBundle());
        finish();
    }

    private void RunEditTextErrors() {
        //  Email Text Watcher
        edit_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { all_filled(); }
            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) { if(timer != null) timer.cancel(); all_filled();}
            @Override
            public void afterTextChanged(final Editable s) {
                all_filled();
                //avoid triggering event when text is too short
                email_tl_signUp.setErrorEnabled(false);
                if (s.length() >= 3) {

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            email = s.toString();
                            runOnUiThread(() -> {
                                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                                    email_tl_signUp.setError(getString(R.string.please_enter_a_valid_email));
                                    email_tl_signUp.setErrorEnabled(true);
                                }
                            });
                        }
                    }, DELAY);
                }
            }
        });

        //  Phone Text Watcher
        edit_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { all_filled(); }
            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) { if(timer != null) timer.cancel();
                all_filled();}
            @Override
            public void afterTextChanged(final Editable s) {
                all_filled();
                //avoid triggering event when text is too short
                phone_tl_signUp.setErrorEnabled(false);
                if (s.length() >= 3) {

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            phone = s.toString();
                            runOnUiThread(() -> {
                                if(!Methods.isValidPhoneNumber(phone)){
                                    phone_tl_signUp.setError(getString(R.string.please_enter_a_valid_phone_number));
                                    phone_tl_signUp.setErrorEnabled(true);
                                }
                            });
                        }
                    }, DELAY);
                }
            }
        });

        //  Name Text Watcher
        edit_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {all_filled();}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {all_filled();}
            @Override
            public void afterTextChanged(Editable s) {all_filled();}
        });
    }

    private void Ids() {
        myAnim = AnimationUtils.loadAnimation(this,R.anim.click_anim);
        instance = this;
        loadingDialog = new LoadingDialog(this);
        btn_back_signUp = findViewById(R.id.btn_back_signUp);
        edit_name = findViewById(R.id.edit_name_signUp);
        edit_email = findViewById(R.id.edit_email_signUp);
        txt_policy_and_privacy = findViewById(R.id.txt_policy_and_privacy);
        edit_phone = findViewById(R.id.edit_phone_signUp);
        email_tl_signUp = findViewById(R.id.email_tl_signUp);
        policy_and_privacy_check = findViewById(R.id.policy_and_privacy_check);
        phone_tl_signUp = findViewById(R.id.phone_tl_signUp);
        edit_bornDate = findViewById(R.id.edit_bornDate_signUp);
        bornDate_tl_signUp = findViewById(R.id.bornDate_tl_signUp);
        edit_password = findViewById(R.id.edit_password_signUp);
        password_tl_signUp = findViewById(R.id.password_tl_signUp);
        btn_next = findViewById(R.id.btn_next_signUp);
        btn_signUp = findViewById(R.id.btn_signUp_signUp);
        btn_signUp.setVisibility(View.GONE);
        getWindow().setStatusBarColor(getColor(R.color.base_color));
        getWindow().setNavigationBarColor(getColor(R.color.base_color));
    }

    private void ShowCalendar(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        String dateSelected = sdf.format(myCalendar.getTime());

        String[] dateSplit = dateSelected.split("/");

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        if((year - Integer.parseInt(dateSplit[2])) < 13 )
            bornDate_tl_signUp.setError(getString(R.string.age_warning));
        else
            bornDate_tl_signUp.setErrorEnabled(false);

        age_user = (year - Integer.parseInt(dateSplit[2]));

        all_filled();
        edit_bornDate.setText(dateSelected);
    }

    private void checking_password_have_minimum_characters() {
        Objects.requireNonNull(password_tl_signUp.getEditText()).addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {
                all_filled();
                if(Objects.requireNonNull(edit_password.getText()).toString().length() > 0){
                    if (Objects.requireNonNull(edit_password.getText()).toString().indexOf(' ') > 0){
                        password_tl_signUp.setError(getString(R.string.password_cannot_contain_spaces));
                        password_tl_signUp.setErrorEnabled(true);
                    }else{
                        if (!edit_password.getText().toString().matches(Methods.PASSWORD_REGEX)){
                            password_tl_signUp.setError(getString(R.string.password_needs));
                            password_tl_signUp.setErrorEnabled(true);
                        }else
                            password_tl_signUp.setErrorEnabled(false);
                    }
                }else
                    password_tl_signUp.setErrorEnabled(false);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void all_filled(){
        if(edit_name.getText().toString().length() > 0 && Patterns.EMAIL_ADDRESS.matcher(edit_email.getText().toString()).matches()
        && Methods.isValidPhoneNumber(edit_phone.getText().toString()) && edit_bornDate.getText().toString().length() > 0 && edit_password.getText().toString().length() >= 8
        && edit_password.getText().toString().matches(Methods.PASSWORD_REGEX)){
            btn_next.setEnabled(true);
            btn_next.setBackgroundResource(R.drawable.custom_button_next);
        }else{
            btn_next.setEnabled(false);
            btn_next.setBackgroundResource(R.drawable.custom_button_disable_next);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Objects.requireNonNull(edit_name.getText()).length() <= 0)
            edit_name.requestFocus();
        else if(Objects.requireNonNull(edit_email.getText()).length() <= 0)
            edit_email.requestFocus();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static SignUpActivity getInstance() { return instance; }

    public void EnableSignUpButton(){
        btn_next.setVisibility(View.GONE);
        btn_signUp.setVisibility(View.VISIBLE);
        btn_signUp.startAnimation(myAnim);
        btn_next.setEnabled(false);
        edit_name.setEnabled(false);
        edit_email.setEnabled(false);
        edit_phone.setEnabled(false);
        edit_bornDate.setEnabled(false);
        edit_password.setEnabled(false);
        DoRegister();
    }

    @Override
    public void onBackPressed() {
        Back_to_intro();
    }

    private void Back_to_intro() {
        if(!GoogleAuthHelper.isGoogleLogin(this)) GoToIntro();
        else{
            final AlertDialog.Builder alert = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.cancel_registration))
            .setMessage(getString(R.string.cancel_your_registration))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                final LoadingDialog loadingDialog = new LoadingDialog(SignUpActivity.this);
                loadingDialog.startLoading();
                GoogleAuthHelper.getGoogleSignInClient(SignUpActivity.this).signOut()
                        .addOnCompleteListener(SignUpActivity.this, task -> {
                            loadingDialog.dismissDialog();
                            GoogleAuthHelper.ResetVariable();
                            GoToIntro();
                        });
            }).setNeutralButton(getString(R.string.no), null);
            final Dialog mDialog = alert.create();
            mDialog.getWindow().getAttributes().windowAnimations = R.style.MyAlertDialogStyle;
            mDialog.show();
        }
    }

    void GoToIntro(){
        final ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.move_to_right_back, R.anim.move_to_right_go);
        ActivityCompat.startActivity(this, new Intent(this, IntroActivity.class), activityOptionsCompat.toBundle());
        finish();
    }
}