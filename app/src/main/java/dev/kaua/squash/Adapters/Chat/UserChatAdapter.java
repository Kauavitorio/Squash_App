package dev.kaua.squash.Adapters.Chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.MessageActivity;
import dev.kaua.squash.Activitys.ShareContentActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.ViewHolder> {

    private Activity mContext;
    private List<DtoAccount> mAccounts;
    private boolean isChat;
    private final boolean share;
    DaoChat daoChat;
    String theLastMessage;

    public UserChatAdapter(Activity mContext, List<DtoAccount> mAccounts, boolean share){
        this.mContext = mContext;
        this.mAccounts = mAccounts;
        this.share = share;
        this.daoChat = new DaoChat(mContext);
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
    public void onBindViewHolder(@NonNull @NotNull UserChatAdapter.ViewHolder holder, int position) {
        DtoAccount account = mAccounts.get(position);
        if(account != null){
            try{

                /*if(isChat)
                    lastMessage(account.getId(), holder.card_no_read_ic);
                else holder.last_seen.setVisibility(View.GONE);*/

                LoadProfileImage(holder, account);
                CheckVerification(account, holder);
                CheckUserStatus(account, holder);
                ChatClick(holder, account);

                if(ConnectionHelper.isOnline(mContext)){
                    final DatabaseReference holder_DB = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE).child(account.getId());
                    holder_DB.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!mContext.isDestroyed() && !mContext.isFinishing()){
                                if(ConnectionHelper.isOnline(mContext)){
                                    DtoAccount DB_account = snapshot.getValue(DtoAccount.class);
                                    if(DB_account != null && DB_account.getId() != null){
                                        if(DB_account.getActive() == DtoAccount.ACCOUNT_DISABLE){
                                            holder.itemView.setOnClickListener(v -> {
                                                final Animation myAnim = AnimationUtils.loadAnimation(mContext,R.anim.click_anim);
                                                holder.itemView.startAnimation(myAnim);
                                                ToastHelper.toast(mContext, mContext.getString(R.string.this_account_has_been_disabled)
                                                        , ToastHelper.SHORT_DURATION );
                                            });
                                        }else{
                                            LoadProfileImage(holder, DB_account);
                                            CheckVerification(DB_account, holder);
                                            CheckUserStatus(DB_account, holder);
                                            ChatClick(holder, account);
                                        }
                                        daoChat.Update_ChatList_Item(DB_account);
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }

            }catch (Exception ex){
                Log.d("USER_ADAPTER", ex.toString());
            }
        }else holder.itemView.setVisibility(View.GONE);
    }

    private void ChatClick(@NonNull ViewHolder holder, DtoAccount account) {
        holder.itemView.setOnClickListener(v -> {
            try {
                final Animation myAnim = AnimationUtils.loadAnimation(mContext,R.anim.click_anim);
                holder.itemView.startAnimation(myAnim);
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId", account.getId());
                intent.putExtra("chat_id", account.getChat_id());
                if(share){
                    int shareType = ShareContentActivity.getInstance().GetShareType();
                    intent.putExtra("shared", MainActivity.SHARED_ID);
                    intent.putExtra("shared_type", shareType);
                    if(shareType == MainActivity.SHARED_PLAIN_TEXT)
                        intent.putExtra("shared_content", (String) ShareContentActivity.getInstance().GetShareContent());
                    else if(shareType == MainActivity.SHARED_IMAGE)
                        intent.putExtra("shared_content", (Uri) ShareContentActivity.getInstance().GetShareContent());
                    ((Activity)mContext).finish();
                }else intent.putExtra("shared", 0);
                mContext.startActivity(intent);
            }catch (Exception exception){
                Warnings.showWeHaveAProblem(mContext, ErrorHelper.USER_CHAT_ITEM_CLICK);
            }
        });
    }

    private void LoadProfileImage(@NonNull ViewHolder holder, DtoAccount account) {
        holder.user_name.setText(account.getName_user().trim());
        if(!mContext.isDestroyed()){
            if(account.getImageURL() == null || account.getImageURL().equals(DtoAccount.DEFAULT)) holder.profile_image.setImageResource(R.drawable.pumpkin_default_image);
            else Glide.with(mContext).load(EncryptHelper.decrypt(account.getImageURL())).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(holder.profile_image);
        }
    }

    private void CheckUserStatus(DtoAccount account, @NonNull ViewHolder holder) {
        if(!mContext.isDestroyed()){
            isChat = account.getStatus_chat() != null && account.getStatus_chat().equals("online");
            holder.img_status.setVisibility(View.VISIBLE);

            if(isChat){
                if(ConnectionHelper.isOnline(mContext)){
                    if (account.getStatus_chat() != null && account.getStatus_chat().equals("online")){
                        holder.last_seen.setText(mContext.getString(R.string.online));
                        holder.img_status.setBorderColor(mContext.getColor(R.color.status_on));
                    }
                }else {
                    if(account.getLast_seen() != null)
                        holder.last_seen.setText(Methods.loadLastSeenUser(mContext, account.getLast_seen()));
                    holder.img_status.setBorderColor(mContext.getColor(R.color.status_off));
                }
            }
            else{
                if(account.getLast_chat() != null && account.getLast_chat().equals(mContext.getString(R.string.waiting_for_reply))){
                    holder.last_seen.setText(mContext.getString(R.string.waiting_for_reply));
                    holder.last_seen.setVisibility(View.VISIBLE);
                } else
                    holder.last_seen.setText(Methods.loadLastSeenUser(mContext, account.getLast_seen()));
                holder.img_status.setBorderColor(mContext.getColor(R.color.status_off));
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void CheckVerification(DtoAccount account, @NonNull ViewHolder holder) {
        if(!mContext.isDestroyed()){
            if(account.getVerification_level() != null && Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(account.getVerification_level()))) > 0){
                holder.verification_ic.setVisibility(View.VISIBLE);
                int verified = Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(account.getVerification_level())));
                if (verified == DtoAccount.ACCOUNT_IS_ADM)
                    holder.verification_ic.setImageDrawable(mContext.getDrawable(R.drawable.ic_verified_employee_account));
                else
                    holder.verification_ic.setImageDrawable(mContext.getDrawable(R.drawable.ic_verified_account));
            }else holder.verification_ic.setVisibility(View.GONE);
        }
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
        theLastMessage  = DtoAccount.DEFAULT;
        FirebaseUser firebaseUser = myFirebaseHelper.getFirebaseUser();
        DatabaseReference reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.CHATS_REFERENCE);
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
