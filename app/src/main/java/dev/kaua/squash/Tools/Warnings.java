package dev.kaua.squash.Tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

import dev.kaua.squash.Activitys.SignInActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Validation.ValidationServices;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

@SuppressLint("StaticFieldLeak")
public class Warnings {
    private static BottomSheetDialog bottomSheetDialog;
    private static Dialog Dialog;
    private static LoadingDialog loadingDialog;
    static final Retrofit retrofitUser = new Retrofit.Builder()
            .baseUrl("https://dev-river-api.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static void showNeedUpdate(Context context, String versionName, long versionCode, int needUpdate){
        if(bottomSheetDialog != null) bottomSheetDialog.dismiss();

        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        //  Creating View for SheetMenu

        bottomSheetDialog.setCancelable(false);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.adapter_appneedupdate,
                ((Activity)context).findViewById(R.id.sheet_app_update_menu));
        TextView txt_version_app = sheetView.findViewById(R.id.txt_version_app);
        txt_version_app.setText(context.getString(R.string.have_new_version, versionName));

        sheetView.findViewById(R.id.btn_update_now).setOnClickListener(v -> {
            String url = "https://play.google.com/store/apps/details?id=dev.kaua.squash";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
            bottomSheetDialog.dismiss();
        });
        if(needUpdate != 0) sheetView.findViewById(R.id.btn_update_later).setVisibility(View.GONE);
        sheetView.findViewById(R.id.btn_update_later).setOnClickListener(v -> {
            MyPrefs.setUpdateRequest_Show(context , 1);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
        Log.d("MobileVersion", "Need update: " + versionCode);
    }

    //  Create Show To Base Message
    public static void Base_Sheet_Alert(Activity context, String msg, boolean cancelable) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        bottomSheetDialog.setCancelable(cancelable);
        //  Creating View for SheetMenu
        View sheetView = LayoutInflater.from(context).inflate(R.layout.adapter_sheet_menu_base,
                context.findViewById(R.id.sheet_menu_base));
        LinearLayout btn_negative_sheet = sheetView.findViewById(R.id.btn_negative_sheet);
        btn_negative_sheet.setVisibility(View.GONE);
        TextView txt_positive_button_sheet = sheetView.findViewById(R.id.txt_positive_button_sheet);
        txt_positive_button_sheet.setText(context.getString(R.string.ok));

        //  Set Main Message
        TextView txt_main_text_sheet = sheetView.findViewById(R.id.txt_main_text_sheet);
        txt_main_text_sheet.setText(msg);

        sheetView.findViewById(R.id.btn_positive_sheet).setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    //  Create Show To did not receive email
    public static void DidNot_receive_email(Activity context, String msg_main, String msg, String account_id, int case_type) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        //  Creating View for SheetMenu
        View sheetView = LayoutInflater.from(context).inflate(R.layout.adapter_sheet_menu_did_not_receive_email,
                context.findViewById(R.id.sheet_menu_did_not_receive_email));
        LinearLayout btn_negative_sheet = sheetView.findViewById(R.id.btn_negative_sheet);
        btn_negative_sheet.setVisibility(View.GONE);
        TextView txt_positive_button_sheet = sheetView.findViewById(R.id.txt_positive_button_sheet);
        txt_positive_button_sheet.setText(msg);

        //  Set Main Message
        TextView txt_main_text_sheet_did_not_receive_email = sheetView.findViewById(R.id.txt_main_text_sheet_did_not_receive_email);
        txt_main_text_sheet_did_not_receive_email.setText(msg_main);

        sheetView.findViewById(R.id.btn_positive_sheet).setOnClickListener(v -> {
            loadingDialog = new LoadingDialog(context);
            loadingDialog.startLoading();
            ValidationServices services = retrofitUser.create(ValidationServices.class);
            DtoAccount account = new DtoAccount();
            account.setAccount_id_cry(EncryptHelper.encrypt(account_id));
            Call<DtoAccount> call = services.resend_validate_email(account);
            call.enqueue(new Callback<DtoAccount>() {
                @Override
                public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                    loadingDialog.dismissDialog();
                    bottomSheetDialog.dismiss();
                }
                @Override
                public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                    loadingDialog.dismissDialog();
                    Warnings.showWeHaveAProblem(context);
                }
            });
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    //  Create Show To Really want to leave?
    public static void Sheet_Really_want_to_leave_emailValidation(Activity context) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        //  Creating View for SheetMenu
        View sheetView = LayoutInflater.from(context).inflate(R.layout.adapter_sheet_menu_base,
                context.findViewById(R.id.sheet_menu_base));
        TextView txt_positive_button_sheet = sheetView.findViewById(R.id.txt_positive_button_sheet);
        txt_positive_button_sheet.setText(context.getString(R.string.yes));

        //  Set Main Message
        TextView txt_main_text_sheet = sheetView.findViewById(R.id.txt_main_text_sheet);
        txt_main_text_sheet.setText(context.getString(R.string.account_will_not_be_validated_leave));

        sheetView.findViewById(R.id.btn_positive_sheet).setOnClickListener(v -> {
            Intent goTo_intro = new Intent(context, SignInActivity.class);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.move_to_right_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(context, goTo_intro, activityOptionsCompat.toBundle());
            ((Activity) context).finish();
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.btn_negative_sheet).setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    public static void showWeHaveAProblem(Context context){
        Dialog = new Dialog(context);

        CardView btnOk_WeHaveAProblem;
        Dialog.setContentView(R.layout.adapter_wehaveaproblem);
        Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnOk_WeHaveAProblem = Dialog.findViewById(R.id.btnOk_WeHaveAProblem);
        btnOk_WeHaveAProblem.setElevation(10);
        Dialog.setCancelable(false);

        btnOk_WeHaveAProblem.setOnClickListener(v -> Dialog.dismiss());

        Dialog.show();
    }

    //  Create Show Need Login Message but with shortCut
    public static void NeedLoginWithShortCutAlert(Activity context, int shortCutId) {
        Dialog = new Dialog(context);
        SharedPreferences mPrefs;
        mPrefs = context.getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);

        TextView txtMsg_alert, txtPositiveBtn_alert, txtCancel_alert;
        CardView PositiveBtn_alert;
        Dialog.setContentView(R.layout.adapter_comum_alert_need_login);
        Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        txtMsg_alert = Dialog.findViewById(R.id.txtMsg_alert);
        txtCancel_alert = Dialog.findViewById(R.id.txtCancel_alert);
        PositiveBtn_alert = Dialog.findViewById(R.id.PositiveBtn_alert);
        txtPositiveBtn_alert = Dialog.findViewById(R.id.txtPositiveBtn_alert);
        PositiveBtn_alert.setElevation(20);

        txtMsg_alert.setText(context.getString(R.string.need_login_msg));
        txtCancel_alert.setText(context.getString(R.string.no));
        txtPositiveBtn_alert.setText(context.getString(R.string.yes));

        PositiveBtn_alert.setOnClickListener(v -> {
            PositiveBtn_alert.setElevation(0);
            Intent goTo_SignIn = new Intent(context, SignInActivity.class);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(context,R.anim.move_to_left_go, R.anim.move_to_right_go);
            goTo_SignIn.putExtra("shortcut", shortCutId);
            ActivityCompat.startActivity(context, goTo_SignIn, activityOptionsCompat.toBundle());
            context.finish();
            mPrefs.edit().clear().apply();
            Dialog.dismiss();
        });

        txtCancel_alert.setOnClickListener(c -> Dialog.dismiss());

        Dialog.show();
    }
}
