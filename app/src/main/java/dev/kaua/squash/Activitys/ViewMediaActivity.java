package dev.kaua.squash.Activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
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

    String image_url, chat_id, receive_time_full;
    String[] receive_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_media);
        Ids();

        Bundle bundle = getIntent().getExtras();
        image_url = bundle.getString(IMAGE_URL_TAG);
        chat_id = bundle.getString(CHAT_ID_TAG);
        if(bundle.getString(RECEIVE_TIME_TAG).equals(POST_TAG)){
            @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm a").format(new Date());
            receive_time = timeStamp.split("/");
            receive_time_full = timeStamp;
            txt_sent_date.setVisibility(View.GONE);
            txt_sent_on.setVisibility(View.GONE);
        }else{
            receive_time = bundle.getString(RECEIVE_TIME_TAG).split("/");
            receive_time_full = bundle.getString(RECEIVE_TIME_TAG);
        }
        txt_sent_date.setText(receive_time_full);

        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoading();
        Glide.with(this)
                .asBitmap()
                .load(image_url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        image_bitmap = resource;
                        loadingDialog.dismissDialog();
                        imageView.setImage(ImageSource.bitmap(resource));
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });

        download_btn.setOnClickListener(v -> {
            if(image_bitmap != null) {
                download_btn.playAnimation();
                Methods.SaveImage(this, image_bitmap, chat_id, receive_time[0] + receive_time[1] +
                        receive_time[2].replace(" ", "").replace(":", "").substring(0, 8));

                new Handler().postDelayed(() -> ToastHelper.toast(this, getString(R.string.image_downloaded_successfully), 0), 3500);
            }
            else {
                download_btn.setAnimation(R.raw.error);
                download_btn.playAnimation();
                new Handler().postDelayed(() -> download_btn.setAnimation(R.raw.download_icon), 2500);
                ToastHelper.toast(this, getString(R.string.unable_to_download_the_image), 0);
            }
        });
    }

    private void Ids() {
        getWindow().setStatusBarColor(getColor(R.color.base_color));
        imageView = (SubsamplingScaleImageView)findViewById(R.id.img_view_media);
        btn_back_view_media = findViewById(R.id.btn_back_view_media);
        txt_sent_date = findViewById(R.id.txt_sent_date);
        txt_sent_on = findViewById(R.id.txt_sent_on);
        download_btn = findViewById(R.id.download_btn);
        btn_back_view_media.setOnClickListener(v -> finish());
    }
}