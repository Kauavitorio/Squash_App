package dev.kaua.squash.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.AsyncLikes_Posts;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Data.Post.PostServices;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.LocalDataBase.DaoPosts;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressLint("UseCompatLoadingForDrawables")
public class Posts_Adapters extends RecyclerView.Adapter<Posts_Adapters.MyHolderPosts> {
    ArrayList<DtoPost> list;
    static Context mContext;
    static int selectedItem = 0;
    static DaoPosts daoPosts;

    final Retrofit retrofit = Methods.GetRetrofitBuilder();

    public Posts_Adapters(ArrayList<DtoPost> ArrayList, Context mContext) {
        this.list = ArrayList;
        Posts_Adapters.mContext = mContext;
        daoPosts = new DaoPosts(mContext);
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
                holder.ic_account_badge.setImageDrawable(mContext.getDrawable(R.drawable.ic_verified_account));
            else
                holder.ic_account_badge.setImageDrawable(mContext.getDrawable(R.drawable.ic_verified_employee_account));

        }else holder.ic_account_badge.setVisibility(View.GONE);
        holder.img_secondImage_post.setVisibility(View.GONE);
        holder.container_third_img.setVisibility(View.GONE);
        Picasso.get().load(list.get(position).getProfile_image()).into(holder.icon_user_profile_post);
        holder.txt_name_user_post.setText(list.get(position).getName_user());
        holder.txt_username_post.setText( "| @" + list.get(position).getUsername());
        Methods.setTextViewHTML(mContext, holder.txt_post_content, list.get(position).getPost_content());

        Check_Like(holder, position);

        holder.txt_likes_post.setText(Methods.NumberTrick(Long.parseLong(list.get(position).getPost_likes())));
        holder.txt_comments_post.setText(Methods.NumberTrick(Long.parseLong(list.get(position).getPost_comments_amount())));

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

        holder.btn_like_post.setOnClickListener(v -> Like_Un_Like_A_Post(holder, position, list.get(position).getPost_id()));
    }

    private void Check_Like(@NotNull MyHolderPosts holder, int position) {
        DtoAccount user = MainActivity.getInstance().getUserInformation();
        boolean result_like = daoPosts.get_A_Like(Long.parseLong(list.get(position).getPost_id()), Long.parseLong(user.getAccount_id() + ""));
        if(result_like) holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.red_heart));
        else holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.ic_heart));
    }

    private void Like_Un_Like_A_Post(@NotNull MyHolderPosts holder, long position, String post_id) {
        //  Get User info
        DtoAccount user = MainActivity.getInstance().getUserInformation();

        boolean result_like = daoPosts.get_A_Like(Long.parseLong(post_id), Long.parseLong(user.getAccount_id() + ""));
        if(result_like) {
            holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.ic_heart));
            long like_now = Long.parseLong(list.get((int) position).getPost_likes());
            like_now = like_now - 1;
            holder.txt_likes_post.setText(Methods.NumberTrick(like_now));
        }else{
            holder.img_heart_like.setImageDrawable(mContext.getDrawable(R.drawable.red_heart));
            long like_now = Long.parseLong(list.get((int) position).getPost_likes());
            like_now = like_now + 1;
            holder.txt_likes_post.setText(Methods.NumberTrick(like_now));
        }

        //  Do Like or Un Like
        DtoPost dtoPost = new DtoPost();
        dtoPost.setPost_id(EncryptHelper.encrypt(post_id));
        dtoPost.setAccount_id_cry(EncryptHelper.encrypt(user.getAccount_id() + ""));
        PostServices services = retrofit.create(PostServices.class);
        Call<DtoPost> call = services.like_Un_Like_A_Post(dtoPost);
        call.enqueue(new Callback<DtoPost>() {
            @Override
            public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                AsyncLikes_Posts async = new AsyncLikes_Posts((Activity) mContext , Long.parseLong(user.getAccount_id() + ""));
                //noinspection unchecked
                async.execute();
            }
            @Override
            public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) { Warnings.showWeHaveAProblem(mContext); }
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class MyHolderPosts extends RecyclerView.ViewHolder{
        CircleImageView icon_user_profile_post;
        TextView txt_name_user_post, txt_username_post, txt_post_content, txt_images_amount_post;
        TextView txt_likes_post, txt_comments_post;
        ImageView img_firstImage_post, img_secondImage_post, img_thirdImage_post, ic_account_badge, img_heart_like;
        RelativeLayout container_third_img;
        ConstraintLayout container_blur_post;
        LinearLayout btn_like_post;

        public MyHolderPosts(@NonNull View itemView) {
            super(itemView);
            icon_user_profile_post = itemView.findViewById(R.id.icon_user_profile_post);
            txt_name_user_post = itemView.findViewById(R.id.txt_name_user_post);
            img_heart_like = itemView.findViewById(R.id.img_heart_like_post);
            txt_username_post = itemView.findViewById(R.id.txt_username_post);
            txt_post_content = itemView.findViewById(R.id.txt_post_content);
            img_firstImage_post = itemView.findViewById(R.id.img_firstImage_post);
            img_secondImage_post = itemView.findViewById(R.id.img_secondImage_post);
            container_third_img = itemView.findViewById(R.id.container_third_img_posts);
            btn_like_post = itemView.findViewById(R.id.btn_like_post);
            img_thirdImage_post = itemView.findViewById(R.id.img_thirdImage_post);
            ic_account_badge = itemView.findViewById(R.id.ic_account_badge);
            container_blur_post = itemView.findViewById(R.id.container_blur_post);
            txt_images_amount_post = itemView.findViewById(R.id.txt_images_amount_post);
            txt_likes_post = itemView.findViewById(R.id.txt_likes_post);
            txt_comments_post = itemView.findViewById(R.id.txt_comments_post);
        }
    }
}
