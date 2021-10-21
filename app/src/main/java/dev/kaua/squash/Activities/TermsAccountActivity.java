package dev.kaua.squash.Activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import dev.kaua.squash.R;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.MyPrefs;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class TermsAccountActivity extends AppCompatActivity {

    private ImageView btn_back_terms;
    private Button btn_next_terms;
    private LoadingDialog loadingDialog;
    private final Handler timer = new Handler();
    private SharedPreferences mPrefs;
    private String terms_list = "Terms 01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_account);
        Ids();

        btn_back_terms.setOnClickListener(v -> finish());

        btn_next_terms.setOnClickListener(v -> {
            loadingDialog.startLoading();
            SignUpActivity.getInstance().EnableSignUpButton();
            mPrefs = getSharedPreferences(MyPrefs.PREFS_TERMS, MODE_PRIVATE);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString("terms_list", terms_list);
            editor.apply();
            loadingDialog.dismissDialog();
            finish();
        });
    }

    private void Ids() {
        loadingDialog = new LoadingDialog(this);
        btn_back_terms = findViewById(R.id.btn_back_terms);
        btn_next_terms = findViewById(R.id.btn_next_terms);
    }

    @Override
    public void onBackPressed() { finish(); }

    @SuppressLint("NonConstantResourceId")
    public void DoPreferences(View view){
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_receive_email:
                if (checked) terms_list = (terms_list + " Terms 01").replaceAll("^ +| +$|( )+", "$1");
                else terms_list =  terms_list.replace(" Terms 01", "");
                break;
            case R.id.checkbox_personalize_inferred:
                if (checked) terms_list = (terms_list + " Terms 02").replaceAll("^ +| +$|( )+", "$1");
                else terms_list =  terms_list.replace("Terms 02", "");
                break;
        }
    }
}