package dev.kaua.squash.Activitys;

import static dev.kaua.squash.Activitys.MessageActivity.OPEN_CAMERA;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.EncodeHintType;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.glxn.qrgen.android.QRCode;

import org.jetbrains.annotations.NotNull;

import dev.kaua.squash.Adapters.CaptureAct;
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
    private ImageView btn_close, img_qr_code;
    private TextView qr_code_username;
    private CardView btn_scan_qr;
    private Animation myAnim;
    private static final String MOBILE_URL_INFO = "Mobile_QR";
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
        account.setProfile_image(bundle.getString("image_profile"));
        account.setName_user(bundle.getString("name_user"));
        account.setUsername(bundle.getString("username"));
        account.setAccount_id(bundle.getLong("account_id"));
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
    }

    @SuppressLint("SetTextI18n")
    private void LoadQrCode() {
        if(ConnectionHelper.isOnline(this)){
            Bitmap myBitmap = QRCode.from(Methods.BASE_URL_HTTPS + account.getUsername() + "/profile" + "?access=" + MOBILE_URL_INFO + "&?request="
                    + Methods.RandomCharactersWithoutSpecials(10) + "_" + account.getAccount_id())
                    .withHint(EncodeHintType.CHARACTER_SET, "UTF-8").bitmap();
            img_qr_code.setImageBitmap(myBitmap);
            qr_code_username.setText("@" + account.getUsername());
        }else{
            finish();
            ToastHelper.toast(this, getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
        }
    }

    void Ids(){
        myAnim = AnimationUtils.loadAnimation(this,R.anim.click_anim);
        btn_close = findViewById(R.id.btn_close_qr_code);
        img_qr_code = findViewById(R.id.img_qr_code);
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
            }
            catch(Settings.SettingNotFoundException e){
                Log.e(TAG, "Cannot access system brightness");
                e.printStackTrace();
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