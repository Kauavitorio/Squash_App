package dev.kaua.squash.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import dev.kaua.squash.R;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class IntroActivity extends AppCompatActivity {
    CardView btn_create_account;
    ConstraintLayout btn_login_intro;
    Handler timer = new Handler();
    private Animation myAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Ids();
        getWindow().setStatusBarColor(getColor(R.color.header_intro_color));
        getWindow().setNavigationBarColor(getColor(R.color.bottom_intro_color));

        //  Set create account btn click
        btn_create_account.setOnClickListener(v -> {
            btn_create_account.setCardBackgroundColor(getColor(R.color.base_color_click));
            btn_create_account.startAnimation(myAnim);
            timer.postDelayed(() -> btn_create_account.setCardBackgroundColor(getColor(R.color.base_color)),300);
            Intent goTo_SignUp = new Intent(this, SignUpActivity.class);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),R.anim.move_to_left_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(this, goTo_SignUp, activityOptionsCompat.toBundle());
            finish();
        });

        //  Set Login btn click
        btn_login_intro.setOnClickListener(v -> {
            btn_login_intro.startAnimation(myAnim);
            Intent goTo_SignIn = new Intent(this, SignInActivity.class);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),R.anim.move_to_left_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(this, goTo_SignIn, activityOptionsCompat.toBundle());
            finish();
        });
    }

    private void Ids() {
        myAnim = AnimationUtils.loadAnimation(this,R.anim.click_anim);
        btn_create_account = findViewById(R.id.btn_create_account_into);
        btn_login_intro = findViewById(R.id.btn_login_intro);
    }
}