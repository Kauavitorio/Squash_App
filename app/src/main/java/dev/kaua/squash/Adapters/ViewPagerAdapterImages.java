package dev.kaua.squash.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Objects;

import dev.kaua.squash.Activitys.ViewMediaActivity;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ToastHelper;

public class ViewPagerAdapterImages extends PagerAdapter {

    // Context object
    Context context;

    //  Post Info
    DtoPost postInfo;

    // Array of images
    List<String> images;

    // Layout Inflater
    LayoutInflater mLayoutInflater;

    //  Image Config
    final RequestOptions myOptions = new RequestOptions()
            .fitCenter() // or centerCrop
            .override(450, 450);


    // Viewpager Constructor
    public ViewPagerAdapterImages(Context context, DtoPost postInfo, List<String> images) {
        this.context = context;
        this.postInfo = postInfo;
        this.images = images;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // return the number of images
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        // inflating the item.xml
        View itemView = mLayoutInflater.inflate(R.layout.adapter_images_post, container, false);

        if(images.get(position) != null && images.get(position).length() > 5){

            // referencing the image view from the item.xml file
            ImageView imageView = itemView.findViewById(R.id.imageViewMain);

            // setting the image in the imageView
            Glide.with(context)
                    .asBitmap()
                    .apply(myOptions)
                    .load(EncryptHelper.decrypt(images.get(position)))
                    .into(imageView);

            imageView.setOnClickListener(v -> CreateImageViewIntent(postInfo, images.get(position), imageView));

            // Adding the View
            Objects.requireNonNull(container).addView(itemView);
        }

        return itemView;
    }

    private void CreateImageViewIntent(DtoPost post, String url, ImageView img) {
        if(ConnectionHelper.isOnline(context)){
            img.startAnimation(AnimationUtils.loadAnimation(context,R.anim.click_anim));
            Intent intent = new Intent(context, ViewMediaActivity.class);
            intent.putExtra(ViewMediaActivity.IMAGE_URL_TAG, EncryptHelper.decrypt(url));
            intent.putExtra(ViewMediaActivity.RECEIVE_TIME_TAG, ViewMediaActivity.POST_TAG);
            String id = post.getUsername() + "_ID" + post.getPost_id();
            if (id.length() < 11) id += "posts_media";
            intent.putExtra(ViewMediaActivity.CHAT_ID_TAG, id);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.move_to_left_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(context, intent, activityOptionsCompat.toBundle());
        }else ToastHelper.toast((Activity) context, context.getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}

