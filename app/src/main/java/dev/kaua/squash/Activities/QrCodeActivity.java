package dev.kaua.squash.Activities;

import static dev.kaua.squash.Activities.MessageActivity.OPEN_CAMERA;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.zxing.EncodeHintType;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.glxn.qrgen.android.QRCode;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Adapters.User.CaptureAct;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.UserPermissions;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class QrCodeActivity extends AppCompatActivity {
    public static final String PROFILE_IMAGE_TAG = "image_profile";
    public static final String PROFILE_NAME_TAG = "name_user";
    public static final String PROFILE_USERNAME_TAG = "username";
    public static final String PROFILE_ID_TAG = "account_id";
    private ImageView btn_close, img_qr_code;
    private CircleImageView ic_qrCode_Profile;
    private TextView qr_code_username;
    private CardView btn_scan_qr;
    private Animation myAnim;
    private static final String MOBILE_URL_INFO = "Squash_QR";
    private static final String TAG = "QRCODE_ACTIVITY";
    private final static DtoAccount account = new DtoAccount();
    private static final String[] permissions = { Manifest.permission.CAMERA };

    final Retrofit retrofit = Methods.GetRetrofitBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        Ids();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            account.setProfile_image(bundle.getString(PROFILE_IMAGE_TAG));
            account.setName_user(bundle.getString(PROFILE_NAME_TAG));
            account.setUsername(bundle.getString(PROFILE_USERNAME_TAG));
            account.setAccount_id(bundle.getLong(PROFILE_ID_TAG));
            LoadQrCode();

            btn_close.setOnClickListener(v -> {
                btn_close.startAnimation(myAnim);
                finish();
            });

            btn_scan_qr.setOnClickListener(v -> {
                btn_scan_qr.startAnimation(myAnim);
                UserPermissions.validatePermissions(permissions, this, OPEN_CAMERA);
                int GalleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                if (GalleryPermission == PackageManager.PERMISSION_GRANTED){
                    IntentIntegrator integrator = new IntentIntegrator(this);
                    integrator.setCaptureActivity(CaptureAct.class);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                    integrator.setBarcodeImageEnabled(true);
                    integrator.setOrientationLocked(false);
                    integrator.setBeepEnabled(false);
                    integrator.setCameraId(0);  // Use a specific camera of the device
                    integrator.setPrompt(getString(R.string.scan_qr_code));
                    integrator.initiateScan();
                }
            });
        }else finish();
    }

    @SuppressLint("SetTextI18n")
    private void LoadQrCode() {
        if(ConnectionHelper.isOnline(this)){
            Glide.with(this).load(account.getProfile_image()).into(ic_qrCode_Profile);

            //  Generate QrCode
            Bitmap QRBitmap = QRCode.from(Methods.BASE_URL_HTTPS + account.getUsername() + "/p" + "?acc="
                    + MOBILE_URL_INFO + "&?r="
                    + account.getAccount_id())
                    .withSize(250, 250)
                    .withErrorCorrection(ErrorCorrectionLevel.L)
                    .withHint(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8).bitmap();

            //  Loading App Logo
            Glide.with(this).asBitmap().load(R.mipmap.ic_launcher)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            img_qr_code.setImageBitmap(Methods.mergeBitmaps(resource, QRBitmap));
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
            qr_code_username.setText("@" + account.getUsername());
        }else{
            finish();
            ToastHelper.toast(this, getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
        }
    }

    void Ids(){
        myAnim = AnimationUtils.loadAnimation(this, R.anim.click_anim);
        btn_close = findViewById(R.id.btn_close_qr_code);
        img_qr_code = findViewById(R.id.img_qr_code);
        ic_qrCode_Profile = findViewById(R.id.ic_qrCode_Profile);
        qr_code_username = findViewById(R.id.qr_code_username);
        btn_scan_qr = findViewById(R.id.btn_scan_qr);

        if(Settings.System.canWrite(this)) {
            try{
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                int brightness = Settings.System.getInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS);
                Log.d(TAG, "Current brightness -> " + brightness);

                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.screenBrightness = 255/(float)255;
                getWindow().setAttributes(lp);
            } catch(Settings.SettingNotFoundException e){
                Log.e(TAG, "Cannot access system brightness");
                Log.e(TAG, "Error -> " + e.toString());
            }
        } else Warnings.Sheet_Setting_Permissions(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents() != null){
                String result_qr = result.getContents();
                if(result_qr.contains(MOBILE_URL_INFO) &&
                        result_qr.startsWith(Methods.BASE_URL_HTTPS) || result_qr.startsWith(Methods.BASE_URL_HTTP)) {
                    String[] split  = result_qr.replace(Methods.BASE_URL_HTTPS, "")
                            .replace(Methods.BASE_URL_HTTP, "").split("/");
                    if(split.length > 0){
                        LoadingDialog loadingDialog = new LoadingDialog(this);
                        loadingDialog.startLoading();
                        try {
                            DtoAccount account = new DtoAccount();
                            account.setUsername(split[0]);
                            AccountServices services = retrofit.create(AccountServices.class);
                            Call<DtoPost> call = services.search_with_username(account);
                            call.enqueue(new Callback<DtoPost>() {
                                @Override
                                public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                                    loadingDialog.dismissDialog();
                                    if(response.code() == 200){
                                        if(response.body() != null){
                                            finish();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("account_id", response.body().getAccount_id());
                                            bundle.putInt("control", 0);
                                            MainActivity.getInstance().GetBundleProfile(bundle);
                                            MainActivity.getInstance().CallProfile();
                                            ProfileFragment.getInstance().LoadAnotherUser();
                                        }
                                    }else ToastHelper.toast(QrCodeActivity.this, getString(R.string.user_not_found), 0);
                                }
                                @Override
                                public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                                    loadingDialog.dismissDialog();
                                    Warnings.showWeHaveAProblem(QrCodeActivity.this, ErrorHelper.QRCODE_GET_USER);
                                }
                            });
                        }catch (Exception ex){
                            loadingDialog.dismissDialog();
                            ToastHelper.toast(this, getString(R.string.no_results), ToastHelper.SHORT_DURATION);
                        }
                    }else ToastHelper.toast(this, getString(R.string.no_results), ToastHelper.SHORT_DURATION);
                } else ToastHelper.toast(this, getString(R.string.no_results), ToastHelper.SHORT_DURATION);
            }
        }else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}