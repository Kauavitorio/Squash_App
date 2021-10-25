package dev.kaua.squash.Activities.Medias;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.text.SimpleDateFormat;
import java.util.Date;

import dev.kaua.squash.R;
import dev.kaua.squash.Tools.CapturePhotoUtils;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.ToastHelper;

public class ViewMediaActivity extends AppCompatActivity {
    SubsamplingScaleImageView imageView;
    ImageView btn_back_view_media;
    LottieAnimationView download_btn;
    TextView txt_sent_date, txt_sent_on;
    Bitmap image_bitmap;
    public static final String IMAGE_URL_TAG = "image_url";
    public static final String CHAT_ID_TAG = "chat_id";
    public static final String RECEIVE_TIME_TAG = "receive_time";
    public static final String POST_TAG = "post";
    public static final String TAG = "ViewMediaActivityLog";

    String image_url, chat_id, receive_time_full;
    ConstraintLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_media);
        Ids();

        final Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            image_url = bundle.getString(IMAGE_URL_TAG);
            chat_id = bundle.getString(CHAT_ID_TAG);
            if(bundle.getString(RECEIVE_TIME_TAG).equals(POST_TAG)){
                @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm a").format(new Date());
                receive_time_full = timeStamp;
                txt_sent_date.setVisibility(View.GONE);
                txt_sent_on.setVisibility(View.GONE);
            }else
                receive_time_full = bundle.getString(RECEIVE_TIME_TAG);

            txt_sent_date.setText(receive_time_full);

            final LoadingDialog loadingDialog = new LoadingDialog(this);
            loadingDialog.startLoading();
            Glide.with(this).asBitmap().load(image_url)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            image_bitmap = resource;

                            //  Load dominantColor in background page
                            Palette.from(image_bitmap).generate(palette -> {
                                if(palette != null){
                                    try {
                                        final int dominantColor = palette.getDominantColor(getColor(R.color.background_setting));
                                        root.setBackgroundColor(dominantColor);
                                        getWindow().setNavigationBarColor(dominantColor);
                                    } catch (Exception exception) {
                                        Log.d(TAG, exception.getMessage());
                                    }
                                }
                            });

                            loadingDialog.dismissDialog();
                            imageView.setImage(ImageSource.bitmap(resource));
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });

            download_btn.setOnClickListener(v -> {
                if(image_bitmap != null) {
                    download_btn.playAnimation();
                    CapturePhotoUtils.insertImage(getContentResolver(), image_bitmap, chat_id, "");
                    new Handler().postDelayed(() -> ToastHelper.toast(this, getString(R.string.image_downloaded_successfully), 0), 3200);
                }
                else {
                    download_btn.setAnimation(R.raw.error);
                    download_btn.playAnimation();
                    new Handler().postDelayed(() -> download_btn.setAnimation(R.raw.download_icon), 2500);
                    ToastHelper.toast(this, getString(R.string.unable_to_download_the_image), 0);
                }
            });

        }else finish();
    }

    private void Ids() {
        getWindow().setStatusBarColor(getColor(R.color.base_color));
        root =findViewById(R.id.activity_view_media_base);
        imageView = findViewById(R.id.img_view_media);
        btn_back_view_media = findViewById(R.id.btn_back_view_media);
        txt_sent_date = findViewById(R.id.txt_sent_date);
        txt_sent_on = findViewById(R.id.txt_sent_on);
        download_btn = findViewById(R.id.download_btn);
        btn_back_view_media.setOnClickListener(v -> finish());
    }
}