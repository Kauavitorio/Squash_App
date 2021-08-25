package dev.kaua.squash.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.Warnings;

public class Followers_FollowingAdapter extends RecyclerView.Adapter<Followers_FollowingAdapter.ViewHolder> {

    private Context mContext;
    private List<DtoAccount> mAccounts;
    String theLastMessage;

    public Followers_FollowingAdapter(Context mContext, List<DtoAccount> mAccounts){
        this.mContext = mContext;
        this.mAccounts = mAccounts;
    }

    @NonNull
    @NotNull
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
                        bundle.putString("account_id", mAccounts.get(position).getAccount_id() + "");
                        bundle.putInt("control", 0);
                        MainActivity.getInstance().GetBundleProfile(bundle);
                        MainActivity.getInstance().CallProfile();
                        ProfileFragment.getInstance().LoadAnotherUser();
                        ((Activity)mContext).finish();
                    }catch (Exception exception){
                        Warnings.showWeHaveAProblem(mContext, ErrorHelper.FOLLOWING_FOLLOWERS_CLICK);
                    }
                });

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
        private final CircleImageView img_status;
        private final CardView card_no_read_ic;
        private final ImageView ic_pinned_chat;
        private final ImageView verification_ic;

        @SuppressLint("CutPasteId")
        public ViewHolder(View itemView){
            super(itemView);
            ic_pinned_chat = itemView.findViewById(R.id.ic_pinned_chat);
            user_name = itemView.findViewById(R.id.user_name_users);
            verification_ic = itemView.findViewById(R.id.verification_ic_user_chat);
            profile_image = itemView.findViewById(R.id.profile_image_users);
            last_seen = itemView.findViewById(R.id.last_seen);
            img_status = itemView.findViewById(R.id.img_status_user);
            card_no_read_ic = itemView.findViewById(R.id.card_no_read_ic);
        }
    }

    //  check for last message
    private void lastMessage(String userId, CardView ic_not_seen){
        theLastMessage  = "default";
        FirebaseUser firebaseUser = myFirebaseHelper.getFirebaseUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    DtoMessage message = snapshot.getValue(DtoMessage.class);
                    if(message != null)
                        if(message.getReceiver() != null)
                            if(message.getReceiver().equals(firebaseUser.getUid()) && message.getSender().equals(userId) ||
                                    message.getReceiver().equals(userId) && message.getSender().equals(firebaseUser.getUid())){
                                if(firebaseUser.getUid().equals(message.getReceiver()) && message.getSender().equals(userId) && message.getIsSeen() == 0) ic_not_seen.setVisibility(View.VISIBLE);
                            }
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });

    }
}
