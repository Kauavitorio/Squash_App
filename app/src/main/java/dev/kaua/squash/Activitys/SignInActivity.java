package dev.kaua.squash.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import dev.kaua.squash.R;
import dev.kaua.squash.Security.Login;
import dev.kaua.squash.Tools.Warnings;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class SignInActivity extends AppCompatActivity {
    private static SignInActivity instance;
    public static TextView txt_login_title;
    private Button btn_next;
    private ImageView btn_back;
    private TextView txt_forget_password;
    private TextInputEditText edit_login_method, edit_password;
    private Animation myAnim;

    private String login_method, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Ids();
        SetTextWatcher();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
            if(bundle.getInt(SplashActivity.ACCOUNT_DISABLE) == Login.DISABLE_ACCOUNT)
                Warnings.showAccountDisable(this);

        btn_next.setOnClickListener(v -> {
            btn_next.startAnimation(myAnim);
            if(edit_login_method.getText() != null && edit_password.getText() != null){
                login_method = edit_login_method.getText().toString();
                password = edit_password.getText().toString();
                Login.DoLogin(this, login_method, password);
            }
        });

        btn_back.setOnClickListener(v -> goTo_intro());

        txt_forget_password.setOnClickListener(v -> {
            txt_forget_password.startAnimation(myAnim);
            Intent i = new Intent(this, ForgotPasswordActivity.class);
            if(edit_login_method.getText() != null &&
                    Patterns.EMAIL_ADDRESS.matcher(edit_login_method.getText().toString().replace(" ", "")).matches())
            i.putExtra("email", edit_login_method.getText().toString());
            else i.putExtra("email", "");
            startActivity(i);
        });
    }

    private void SetTextWatcher(){
        //  Set login method TextWatcher
        edit_login_method.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {CheckInputs();}
        });
        //  Set password TextWatcher
        edit_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { CheckInputs(); }
        });
    }

    private void CheckInputs(){
        if(edit_login_method.getText() != null && edit_password.getText() != null){
            if(edit_login_method.getText().toString().length() > 0 && edit_password.getText().toString().length() > 0){
                btn_next.setEnabled(true);
                btn_next.setBackgroundResource(R.drawable.custom_button_next);
            }else{
                btn_next.setEnabled(false);
                btn_next.setBackgroundResource(R.drawable.custom_button_disable_next);
            }
        }
    }

    private void Ids() {
        instance = this;
        myAnim = AnimationUtils.loadAnimation(this,R.anim.click_anim);
        edit_login_method = findViewById(R.id.edit_login_method_signIn);
        edit_password = findViewById(R.id.edit_password_signIn);
        btn_next = findViewById(R.id.btn_next_signIn);
        txt_forget_password = findViewById(R.id.txt_forget_password);
        txt_login_title = findViewById(R.id.txt_login_title);
        btn_back = findViewById(R.id.btn_back_signIn);
        getWindow().setStatusBarColor(getColor(R.color.base_color));
        getWindow().setNavigationBarColor(getColor(R.color.base_color));
    }

    public static SignInActivity getInstance(){ return instance; }

    public void Invalid_email_or_password(){
        edit_password.setText(null);
        Warnings.Base_Sheet_Alert(this, getString(R.string.login_or_password_invalid), true);
    }

    @Override
    public void onBackPressed() {
        goTo_intro();
    }

    private void goTo_intro() {
        btn_back.startAnimation(myAnim);
        Intent goTo_intro = new Intent(this, IntroActivity.class);
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.move_to_right_back, R.anim.move_to_right_go);
        ActivityCompat.startActivity(this, goTo_intro, activityOptionsCompat.toBundle());
        finish();
    }
}