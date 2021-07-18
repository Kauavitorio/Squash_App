package dev.kaua.squash.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageView;

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
    private Button btn_next;
    private ImageView btn_back;
    private TextInputEditText edit_login_method, edit_password;

    private String login_method, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Ids();
        SetTextWatcher();

        btn_next.setOnClickListener(v -> {
            login_method = Objects.requireNonNull(edit_login_method.getText()).toString();
            password = Objects.requireNonNull(edit_password.getText()).toString();
            Login.DoLogin(this, login_method, password);
        });

        btn_back.setOnClickListener(v -> goTo_intro());
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
        if(Objects.requireNonNull(edit_login_method.getText()).toString().length() > 0 && Objects.requireNonNull(edit_password.getText()).toString().length() > 0){
            btn_next.setEnabled(true);
            btn_next.setBackgroundResource(R.drawable.custom_button_next);
        }else{
            btn_next.setEnabled(false);
            btn_next.setBackgroundResource(R.drawable.custom_button_disable_next);
        }
    }

    private void Ids() {
        instance = this;
        edit_login_method = findViewById(R.id.edit_login_method_signIn);
        edit_password = findViewById(R.id.edit_password_signIn);
        btn_next = findViewById(R.id.btn_next_signIn);
        btn_back = findViewById(R.id.btn_back_signIn);
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
        Intent goTo_intro = new Intent(this, IntroActivity.class);
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.move_to_right_back, R.anim.move_to_right_go);
        ActivityCompat.startActivity(this, goTo_intro, activityOptionsCompat.toBundle());
        finish();
    }
}