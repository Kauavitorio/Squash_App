package dev.kaua.squash.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.FollowAccountHelper;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.Warnings;

public class Followers_FollowingAdapter extends RecyclerView.Adapter<Followers_FollowingAdapter.ViewHolder> {

    private final Activity mContext;
    private final List<DtoAccount> mAccounts;
    private static DaoFollowing daoFollowing;

    public Followers_FollowingAdapter(Activity mContext, List<DtoAccount> mAccounts){
        this.mContext = mContext;
        this.mAccounts = mAccounts;
        daoFollowing = new DaoFollowing(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_user_items, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull @NotNull Followers_FollowingAdapter.ViewHolder holder, int position) {
        DtoAccount account = mAccounts.get(position);
        if(account != null){
            try{
                holder.user_name.setText(EncryptHelper.decrypt(account.getUsername()));
                holder.last_seen.setText(EncryptHelper.decrypt(account.getName_user()));
                if(account.getProfile_image() == null || account.getProfile_image().equals(DtoAccount.DEFAULT)) holder.profile_image.setImageResource(R.drawable.pumpkin_default_image);
                else Glide.with(mContext).load(EncryptHelper.decrypt(account.getProfile_image())).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(holder.profile_image);

                holder.itemView.setOnClickListener(v -> {
                    try {
                        final Animation myAnim = AnimationUtils.loadAnimation(mContext,R.anim.click_anim);
                        holder.itemView.startAnimation(myAnim);
                        Bundle bundle = new Bundle();
                        bundle.putString("account_id", String.valueOf(account.getAccount_id()));
                        bundle.putInt("control", 0);
                        MainActivity.getInstance().GetBundleProfile(bundle);
                        MainActivity.getInstance().CallProfile();
                        ProfileFragment.getInstance().LoadAnotherUser();
                        ((Activity)mContext).finish();
                    }catch (Exception exception){
                        Warnings.showWeHaveAProblem(mContext, ErrorHelper.FOLLOWING_FOLLOWERS_CLICK);
                    }
                });

                if(account.getAccount_id() != MyPrefs.getUserInformation(mContext).getAccount_id()){
                    holder.container_follow_adapter_user.setVisibility(View.VISIBLE);
                    if(daoFollowing.check_if_follow(MyPrefs.getUserInformation(mContext).getAccount_id(),
                            account.getAccount_id())){
                        holder.btn_follow_adapter_user.setBackground(mContext.getDrawable(R.drawable.background_button_following));
                        holder.btn_follow_adapter_user.setText(mContext.getString(R.string.following));
                        holder.btn_follow_adapter_user.setTextColor(mContext.getColor(R.color.black));
                    }else{
                        holder.btn_follow_adapter_user.setBackground(mContext.getDrawable(R.drawable.background_button_follow));
                        holder.btn_follow_adapter_user.setText(mContext.getString(R.string.follow));
                        holder.btn_follow_adapter_user.setTextColor(mContext.getColor(R.color.white));
                    }

                    //   Follow / Un Follow Click
                    holder.btn_follow_adapter_user.setOnClickListener(v -> {
                        if(account.getAccount_id() != MyPrefs.getUserInformation(mContext).getAccount_id()){
                            final FollowAccountHelper followAccountHelper = new FollowAccountHelper(mContext);
                            String follow = mContext.getString(R.string.follow);
                            String following = mContext.getString(R.string.following);
                            if(holder.btn_follow_adapter_user.getText().toString().equals(follow)){
                                holder.btn_follow_adapter_user.setBackground(mContext.getDrawable(R.drawable.background_button_following));
                                holder.btn_follow_adapter_user.setText(mContext.getString(R.string.following));
                                holder.btn_follow_adapter_user.setTextColor(mContext.getColor(R.color.black));
                                followAccountHelper.DoFollow(account.getAccount_id(), account.getUsername(), mContext);
                            }
                            else if(holder.btn_follow_adapter_user.getText().toString().equals(following)){
                                holder.btn_follow_adapter_user.setBackground(mContext.getDrawable(R.drawable.background_button_follow));
                                holder.btn_follow_adapter_user.setText(mContext.getString(R.string.follow));
                                holder.btn_follow_adapter_user.setTextColor(mContext.getColor(R.color.white));
                                followAccountHelper.DoUnFollow(account.getAccount_id(), mContext);
                            }
                        }
                    });

                }else holder.container_follow_adapter_user.setVisibility(View.GONE);

                if(account.getVerification_level() != null && Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(account.getVerification_level()))) > 0){
                    holder.verification_ic.setVisibility(View.VISIBLE);
                    int verified = Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(account.getVerification_level())));
                    if (verified == 2)
                        holder.verification_ic.setImageDrawable(mContext.getDrawable(R.drawable.ic_verified_employee_account));
                    else
                        holder.verification_ic.setImageDrawable(mContext.getDrawable(R.drawable.ic_verified_account));
                }else holder.verification_ic.setVisibility(View.GONE);

                holder.img_status.setVisibility(View.GONE);
            }catch (Exception ex){
                Log.d("USER_ADAPTER", ex.toString());
            }
        }else holder.itemView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() { return mAccounts.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView user_name, last_seen;
        private final CircleImageView profile_image;
        private final ConstraintLayout container_follow_adapter_user;
        private final Button btn_follow_adapter_user;
        private final CircleImageView img_status;
        private final ImageView verification_ic;

        @SuppressLint("CutPasteId")
        public ViewHolder(View itemView){
            super(itemView);
            user_name = itemView.findViewById(R.id.user_name_users);
            verification_ic = itemView.findViewById(R.id.verification_ic_user_chat);
            profile_image = itemView.findViewById(R.id.profile_image_users);
            last_seen = itemView.findViewById(R.id.last_seen);
            img_status = itemView.findViewById(R.id.img_status_user);
            container_follow_adapter_user = itemView.findViewById(R.id.container_follow_adapter_user);
            btn_follow_adapter_user = itemView.findViewById(R.id.btn_follow_adapter_user);
            setIsRecyclable(false);
        }
    }
}
