package dev.kaua.squash.Activitys.Setting.AccountSetting.Activity.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

import dev.kaua.squash.Data.System.DtoSystem;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.Methods;

public class LinksAdapter extends RecyclerView.Adapter<LinksAdapter.ViewHolder> {

    private final Activity mContext;
    private final List<DtoSystem> mWebSiteList;

    public LinksAdapter(Activity mContext, List<DtoSystem> mWebSiteList){
        this.mContext = mContext;
        this.mWebSiteList = mWebSiteList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_links_visited, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull @NotNull LinksAdapter.ViewHolder holder, int position) {
        final DtoSystem website = mWebSiteList.get(position);
        if(website != null){
            if(website.getTitle().length() > 35) holder.webSite_Title.setText(website.getTitle().substring(0, 32) + "...");
            else holder.webSite_Title.setText(website.getTitle());
            holder.webSite_link.setText(website.getLink_display());
            holder.webSite_date_time.setText(LoadDate(website.getDate_time()));

            Glide.with(mContext)
                    .asBitmap()
                    .load(website.getWebSite_Image())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            RoundedBitmapDrawable img = RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                            img.setCornerRadius(25);
                            holder.link_image.setImageDrawable(img);
                        }

                        @Override
                        public void onLoadCleared( Drawable placeholder) {
                        }
                    });

            holder.itemView.setOnClickListener(v -> {
                Methods.browseTo(mContext, website.getLink());
            });
        }
    }

    private String LoadDate(String date_time) {
        String[] split = date_time.split("/");
        String date_refactor = date_time;
        try {
            if(Calendar.getInstance().get(Calendar.YEAR) > Integer.parseInt(split[2]))
                date_refactor = split[0] + " " + Methods.getMonth(Integer.parseInt(split[1])) + " " + split[2];
            else
                date_refactor = Methods.getMonth(Integer.parseInt(split[1])) + " " + split[0];

        }catch (Exception ignore){}
        return date_refactor;
    }


    @Override
    public int getItemCount() { return mWebSiteList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView webSite_Title, webSite_link, webSite_date_time;
        private final ImageView link_image;

        @SuppressLint("CutPasteId")
        public ViewHolder(View itemView){
            super(itemView);
            webSite_Title = itemView.findViewById(R.id.webSite_Title);
            webSite_link = itemView.findViewById(R.id.webSite_link);
            webSite_date_time = itemView.findViewById(R.id.webSite_date_time);
            link_image = itemView.findViewById(R.id.link_image);
        }
    }

}
