package dev.kaua.squash.Tools;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import dev.kaua.squash.R;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

@SuppressWarnings({"deprecation", "UseCompatLoadingForDrawables"})
public abstract class ToastHelper {
    public static final int LONG_DURATION = 1;
    public static final int SHORT_DURATION = 0;

    private static Toast toast_item;
    public static void toast(@NonNull Activity activity, String msg, int TIME){
        try{
            final LayoutInflater inflater = activity.getLayoutInflater();
            final View layout = inflater.inflate(R.layout.adapter_custom_toast, activity.findViewById(R.id.custom_toast_layout));
            layout.setBackgroundDrawable(activity.getDrawable(R.drawable.background_custom_toast));
            TextView tv = layout.findViewById(R.id.txt_custom_toast);
            tv.setText(msg);
            if(toast_item != null) toast_item.cancel();
            toast_item = new Toast(activity.getApplicationContext());
            toast_item.setGravity(Gravity.CENTER, 0, 0);
            toast_item.setDuration(TIME);
            toast_item.setView(layout);
            toast_item.show();
        }catch (Exception ignore){}
    }
}
