package dev.kaua.squash.Tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;

import com.airbnb.lottie.LottieAnimationView;

import dev.kaua.squash.R;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class LoadingDialog {

    private final Activity activity;
    private Dialog dialog;

    public LoadingDialog(Activity myActivity){
        activity = myActivity;
    }

    @SuppressLint("InflateParams")
    public void startLoading(){
        try {
            dialog = new Dialog(activity);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.adapter_custom_loading);
            final LottieAnimationView progressBarDialog = dialog.findViewById(R.id.progressBarDialog);
            new Handler().postDelayed(() -> progressBarDialog.setSpeed((float) 2.5),6000);

            new Handler().postDelayed(() -> {
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                    Warnings.showWeHaveAProblem(activity, ErrorHelper.LOAD_SOME_FILE_ERROR);
                }
            }, 36000);

            dialog.show();
        } catch (Exception ex){
            ToastHelper.toast(activity, ex.getMessage(), ToastHelper.SHORT_DURATION);
        }
    }

   public void dismissDialog(){
        try {
            if(dialog != null && dialog.isShowing()) dialog.dismiss();
        }catch (Exception ignore){}
    }
}
