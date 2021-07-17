package dev.kaua.squash.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.Methods;

public class Posts_Adapters extends RecyclerView.Adapter<Posts_Adapters.MyHolderPosts> {
    ArrayList<DtoPost> list;
    Context context;
    static int selectedItem = 0;

    public Posts_Adapters(ArrayList<DtoPost> ArrayList, Context context) {
        this.list = ArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolderPosts onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_posts, parent, false);
        return new MyHolderPosts(listItem);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull MyHolderPosts holder, int position) {
        if(Integer.parseInt(Objects.requireNonNull(list.get(position).getVerification_level())) != 0){
            holder.ic_account_badge.setVisibility(View.VISIBLE);
            if (Integer.parseInt(Objects.requireNonNull(list.get(position).getVerification_level())) == 1)
                holder.ic_account_badge.setImageDrawable(context.getDrawable(R.drawable.ic_verified_account));
            else
                holder.ic_account_badge.setImageDrawable(context.getDrawable(R.drawable.ic_verified_employee_account));

        }else holder.ic_account_badge.setVisibility(View.GONE);
        holder.img_secondImage_post.setVisibility(View.GONE);
        holder.container_third_img.setVisibility(View.GONE);
        Picasso.get().load(list.get(position).getProfile_image()).into(holder.icon_user_profile_post);
        holder.txt_name_user_post.setText(list.get(position).getName_user());
        holder.txt_username_post.setText( "| @" + list.get(position).getUsername());
        Methods.setTextViewHTML(context, holder.txt_post_content, list.get(position).getPost_content());

        holder.txt_likes_post.setText(Methods.NumberTrick(Integer.parseInt(list.get(position).getPost_likes())));
        holder.txt_comments_post.setText(Methods.NumberTrick(Integer.parseInt(list.get(position).getPost_comments_amount())));

        if(list.get(position).getPost_images() != null && list.get(position).getPost_images().size() > 0 && !list.get(position).getPost_images().get(0).equals("NaN")){
            int ImagesAmount = list.get(position).getPost_images().size();
            if(ImagesAmount < 2){
                for (int i = 0; i < 1; i++){
                    Picasso.get().load(EncryptHelper.decrypt(list.get(position).getPost_images().get(i))).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            holder.img_firstImage_post.setImageBitmap(Methods.getRoundedCornerBitmap(bitmap, 50));
                        }
                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {}
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {}
                    });
                }
            }else{
                Picasso.get().load(EncryptHelper.decrypt(list.get(position).getPost_images().get(0))).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        holder.img_firstImage_post.setImageBitmap(Methods.getRoundedCornerBitmap(bitmap, 50));
                    }
                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {}
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {}
                });
                Picasso.get().load(EncryptHelper.decrypt(list.get(position).getPost_images().get(1))).into(holder.img_secondImage_post);
                holder.img_secondImage_post.setVisibility(View.VISIBLE);
                int width = holder.img_firstImage_post.getWidth();
                holder.img_firstImage_post.getLayoutParams().width = width - 50;
                holder.img_firstImage_post.requestLayout();
                if (ImagesAmount > 2) {
                    holder.container_third_img.setVisibility(View.VISIBLE);
                    Picasso.get().load(EncryptHelper.decrypt(list.get(position).getPost_images().get(2))).into(holder.img_thirdImage_post);
                    if (ImagesAmount == 3) holder.container_blur_post.setVisibility(View.GONE);
                    else {
                        holder.txt_images_amount_post.setText("+" + (ImagesAmount - 2));
                    }
                }
            }
        }else holder.img_firstImage_post.setVisibility(View.GONE);

        holder.icon_user_profile_post.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("account_id", list.get(position).getAccount_id());
            bundle.putInt("control", 0);
            MainActivity.getInstance().GetBundleProfile(bundle);
            MainActivity.getInstance().CallProfile();
            ProfileFragment.getInstance().LoadAnotherUser();
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class MyHolderPosts extends RecyclerView.ViewHolder{
        CircleImageView icon_user_profile_post;
        TextView txt_name_user_post, txt_username_post, txt_post_content, txt_images_amount_post;
        TextView txt_likes_post, txt_comments_post;
        ImageView img_firstImage_post, img_secondImage_post, img_thirdImage_post, ic_account_badge;
        RelativeLayout container_third_img;
        ConstraintLayout container_blur_post;

        public MyHolderPosts(@NonNull View itemView) {
            super(itemView);
            icon_user_profile_post = itemView.findViewById(R.id.icon_user_profile_post);
            txt_name_user_post = itemView.findViewById(R.id.txt_name_user_post);
            txt_username_post = itemView.findViewById(R.id.txt_username_post);
            txt_post_content = itemView.findViewById(R.id.txt_post_content);
            img_firstImage_post = itemView.findViewById(R.id.img_firstImage_post);
            img_secondImage_post = itemView.findViewById(R.id.img_secondImage_post);
            container_third_img = itemView.findViewById(R.id.container_third_img_posts);
            img_thirdImage_post = itemView.findViewById(R.id.img_thirdImage_post);
            ic_account_badge = itemView.findViewById(R.id.ic_account_badge);
            container_blur_post = itemView.findViewById(R.id.container_blur_post);
            txt_images_amount_post = itemView.findViewById(R.id.txt_images_amount_post);
            txt_likes_post = itemView.findViewById(R.id.txt_likes_post);
            txt_comments_post = itemView.findViewById(R.id.txt_comments_post);
        }
    }
}
