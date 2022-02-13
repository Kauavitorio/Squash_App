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
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import dev.kaua.squash.Activities.Auth.PrivacyPolicyUpdateActivity;
import dev.kaua.squash.Activities.Setting.AccountSettingActivity;
import dev.kaua.squash.Activities.Setting.SettingActivity;
import dev.kaua.squash.Activities.Auth.SignInActivity;
import dev.kaua.squash.Activities.Admin.WarnTheUserActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Validation.ValidationServices;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

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
    static final Retrofit retrofitUser = Methods.GetRetrofitBuilder();

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
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(Methods.GOOGLE_PLAY_APP_LINK));
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
        bottomSheetDialog = null;
        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        bottomSheetDialog.setCancelable(cancelable);
        //  Creating View for SheetMenu
        View sheetView = LayoutInflater.from(context).inflate(R.layout.adapter_sheet_menu_base,
                context.findViewById(R.id.sheet_menu_base_layout));
        LinearLayout btn_negative_sheet = sheetView.findViewById(R.id.btn_negative_sheet);
        btn_negative_sheet.setVisibility(View.GONE);
        TextView txt_positive_button_sheet = sheetView.findViewById(R.id.txt_positive_button_sheet_base);
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
                    Warnings.showWeHaveAProblem(context, ErrorHelper.EMAIL_SYSTEM_RE_SEND);
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
                context.findViewById(R.id.sheet_menu_base_layout));
        TextView txt_positive_button_sheet = sheetView.findViewById(R.id.txt_positive_button_sheet_base);
        txt_positive_button_sheet.setText(context.getString(R.string.yes));

        //  Set Main Message
        TextView txt_main_text_sheet = sheetView.findViewById(R.id.txt_main_text_sheet);
        txt_main_text_sheet.setText(context.getString(R.string.account_will_not_be_validated_leave));

        sheetView.findViewById(R.id.btn_positive_sheet).setOnClickListener(v -> {
            Intent goTo_intro = new Intent(context, SignInActivity.class);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.move_to_right_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(context, goTo_intro, activityOptionsCompat.toBundle());
            context.finish();
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.btn_negative_sheet).setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    //  Create Show To Really want to leave?
    public static void Sheet_Setting_Permissions(Activity context) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        bottomSheetDialog.setCancelable(false);
        //  Creating View for SheetMenu
        View sheetView = LayoutInflater.from(context).inflate(R.layout.adapter_sheet_menu_base,
                context.findViewById(R.id.sheet_menu_base_layout));
        TextView txt_positive_button_sheet = sheetView.findViewById(R.id.txt_positive_button_sheet_base);
        txt_positive_button_sheet.setText(context.getString(R.string.yes));

        //  Set Main Message
        TextView txt_main_text_sheet = sheetView.findViewById(R.id.txt_main_text_sheet);
        txt_main_text_sheet.setText(context.getString(R.string.better_display_qr_code));

        sheetView.findViewById(R.id.btn_positive_sheet).setOnClickListener(v -> {
            context.finish();
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.btn_negative_sheet).setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    public static void showWeHaveAProblem(Context context, String ERROR_CODE){
        Dialog = new Dialog(context);

        TextView btnOk_WeHaveAProblem;
        Dialog.setContentView(R.layout.adapter_wehaveaproblem);
        Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnOk_WeHaveAProblem = Dialog.findViewById(R.id.btnOk_WeHaveAProblem);
        Dialog.setCancelable(false);
        ((TextView)Dialog.findViewById(R.id.base_error_title_problem_adapter)).setText(context.getString(R.string.error_code, ERROR_CODE));

        btnOk_WeHaveAProblem.setOnClickListener(v -> {
            btnOk_WeHaveAProblem.startAnimation(AnimationUtils.loadAnimation(context,R.anim.click_anim));
            Dialog.dismiss();
        });

        Dialog.getWindow().getAttributes().windowAnimations = R.style.MyAlertDialogStyle;
        Dialog.show();
    }

    public static void showAccountDisable(Context context){
        Dialog = new Dialog(context);

        TextView btnOk_WeHaveAProblem;
        Dialog.setContentView(R.layout.adapter_wehaveaproblem);
        Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnOk_WeHaveAProblem = Dialog.findViewById(R.id.btnOk_WeHaveAProblem);
        Dialog.setCancelable(false);
        ((TextView)Dialog.findViewById(R.id.base_error_title_problem_adapter)).setText(context.getString(R.string.suspended_account));
        ((TextView)Dialog.findViewById(R.id.txt_base_err_adapter)).setText(context.getString(R.string.your_account_has_been_suspended));

        btnOk_WeHaveAProblem.setOnClickListener(v -> {
            btnOk_WeHaveAProblem.startAnimation(AnimationUtils.loadAnimation(context,R.anim.click_anim));
            Dialog.dismiss();
        });

        Dialog.getWindow().getAttributes().windowAnimations = R.style.MyAlertDialogStyle;
        Dialog.show();
    }

    public static void showProfilePicUpdated(Context context){
        Dialog = new Dialog(context);

        TextView btnOk;
        Dialog.setContentView(R.layout.adapter_wehaveaproblem);
        Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnOk = Dialog.findViewById(R.id.btnOk_WeHaveAProblem);
        Dialog.setCancelable(false);
        ((TextView)Dialog.findViewById(R.id.base_error_title_problem_adapter)).setText(context.getString(R.string.updated_photo));
        ((TextView)Dialog.findViewById(R.id.txt_base_err_adapter)).setText(context.getString(R.string.updated_photo_desc_time));

        btnOk.setOnClickListener(v -> Dialog.dismiss());

        Dialog.getWindow().getAttributes().windowAnimations = R.style.MyAlertDialogStyle;
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

    public static void goToUpdateInPrivacyPolicy(Activity context, long privacy_policy) {
        Intent i = new Intent(context, PrivacyPolicyUpdateActivity.class);
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(context,R.anim.move_to_left_go, R.anim.move_to_right_go);
        i.putExtra(PrivacyPolicyUpdateActivity.PRIVACY_POLICY_TAG, privacy_policy);
        ActivityCompat.startActivity(context, i, activityOptionsCompat.toBundle());
    }

    //  Create Contact Profile Sheet
    public static void Sheet_Contact_Profile(Activity context, DtoAccount account) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        //  Creating View for SheetMenu
        View sheetView = LayoutInflater.from(context).inflate(R.layout.adapter_sheet_contact_profile,
                context.findViewById(R.id.adapter_sheet_contact_profile));
        ImageView close = sheetView.findViewById(R.id.close_contact_profile);

        TextView txt_email = sheetView.findViewById(R.id.txt_email_contact_profile);
        txt_email.setText(account.getEmail());

        txt_email.setOnClickListener(v -> {
            txt_email.startAnimation(AnimationUtils.loadAnimation(context, R.anim.click_anim));
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + account.getEmail()));
            intent.putExtra(Intent.EXTRA_EMAIL, account.getEmail());
            if (intent.resolveActivity(context.getPackageManager()) != null)
                context.startActivity(intent);
        });

        close.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    //  Create Menu Profile
    public static void Sheet_Menu_Profile(Activity context, String username, final long id) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        //  Creating View for SheetMenu
        View sheetView = LayoutInflater.from(context).inflate(R.layout.adapter_menu_profile_sheet,
                context.findViewById(R.id.adapter_sheet_menu_profile));
        sheetView.findViewById(R.id.close_ic_profile_sheet).setElevation(0);

        sheetView.findViewById(R.id.btn_report_profile).setVisibility(View.GONE);
        if(id == MyPrefs.getUserInformation(context).getAccount_id()){
            sheetView.findViewById(R.id.btn_your_activity_profile).setVisibility(View.VISIBLE);
        }else
            sheetView.findViewById(R.id.btn_your_activity_profile).setVisibility(View.GONE);

        if(Methods.getUserLevel(context) == DtoAccount.ACCOUNT_IS_STAFF)
            sheetView.findViewById(R.id.btn_options_profile).setVisibility(View.VISIBLE);
        else
            sheetView.findViewById(R.id.btn_options_profile).setVisibility(View.GONE);

        // Setting Click
        sheetView.findViewById(R.id.btn_setting_profile).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent i = new Intent(context, SettingActivity.class);
            context.startActivity(i);
        });

        // Setting Click
        sheetView.findViewById(R.id.btn_your_activity_profile).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent i = new Intent(context, AccountSettingActivity.class);
            i.putExtra(AccountSettingActivity.REQUEST_TAG, AccountSettingActivity.ACTIVITY);
            context.startActivity(i);
        });

        // Report Click
        sheetView.findViewById(R.id.btn_report_profile).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            BottomSheetDialog bottomSheetDialogReport = new BottomSheetDialog(context, R.style.BottomSheetTheme);
            //  Creating View for SheetMenu
            View sheetViewReport = LayoutInflater.from(context).inflate(R.layout.adapter_user_report ,
                    context.findViewById(R.id.sheet_report_user));

            sheetViewReport.findViewById(R.id.txt_report_post_message_or_comment).setOnClickListener(v1 -> {
                ProfileFragment.ReportUser(context, EncryptHelper.encrypt(context.getString(R.string.report_post_message_or_comment)));
                bottomSheetDialogReport.dismiss();
            });


            sheetViewReport.findViewById(R.id.txt_report_account).setOnClickListener(v1 -> {
                ProfileFragment.ReportUser(context, EncryptHelper.encrypt(context.getString(R.string.report_account)));
                bottomSheetDialogReport.dismiss();
            });

            bottomSheetDialogReport.setContentView(sheetViewReport);
            bottomSheetDialogReport.show();
        });

        // Options Click
        sheetView.findViewById(R.id.btn_options_profile).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
            //  Creating View for SheetMenu
            View sheetViewOptions = LayoutInflater.from(context).inflate(R.layout.adapter_profile_actions ,
                    context.findViewById(R.id.sheet_profile_action));

            sheetViewOptions.findViewById(R.id.btn_warn_user).setOnClickListener(v1 -> {
                LoadingDialog loadingDialog = new LoadingDialog(context);
                loadingDialog.startLoading();
                DtoAccount account_warn = new DtoAccount();
                DatabaseReference reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot fullSnapshot) {
                        for(DataSnapshot snapshot: fullSnapshot.getChildren()){
                            DtoAccount account = snapshot.getValue(DtoAccount.class);
                            if(account != null){
                                if(account.getUsername().equals(username)){
                                    if(account_warn.getAccount_id_cry() == null){
                                        account_warn.setId(account.getId());
                                        account_warn.setUsername(account.getUsername());
                                        account_warn.setAccount_id_cry(account.getId());
                                    }
                                }
                            }
                        }

                        if(account_warn.getAccount_id_cry() != null){
                            if(account_warn.getName_user() == null || !account_warn.getName_user().equals("go")){
                                if(account_warn.getUsername().equals(username)){
                                    loadingDialog.dismissDialog();
                                    ProfileFragment.UID_USER_WARN = account_warn.getId();
                                    Intent i = new Intent(context, WarnTheUserActivity.class);
                                    i.putExtra(WarnTheUserActivity.ACCOUNT_ID_REQUEST_ID, ProfileFragment.warn_id);
                                    i.putExtra(WarnTheUserActivity.ACCOUNT_ACTIVE_REQUEST_ID, ProfileFragment.active_level);
                                    i.putExtra(WarnTheUserActivity.ACCOUNT_NAME_REQUEST_ID, ProfileFragment.txt_user_name.getText().toString());
                                    i.putExtra(WarnTheUserActivity.ACCOUNT_USERNAME_REQUEST_ID, ProfileFragment.txt_username_name.getText().toString());
                                    i.putExtra(WarnTheUserActivity.ACCOUNT_IMAGE_REQUEST_ID, ProfileFragment.user_image);
                                    i.putExtra(WarnTheUserActivity.ACCOUNT_ACTIVE_REQUEST_UID, ProfileFragment.UID_USER_WARN);
                                    context.startActivity(i);
                                }else ToastHelper.toast(context, context.getString(R.string.user_not_found), ToastHelper.SHORT_DURATION);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                });

                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.setContentView(sheetViewOptions);
            bottomSheetDialog.show();
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}
