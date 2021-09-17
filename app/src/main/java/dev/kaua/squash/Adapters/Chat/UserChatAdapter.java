package dev.kaua.squash.Adapters.Chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.ViewHolder> {

    private final Activity mContext;
    private final List<DtoAccount> mAccounts;
    private final boolean share;
    private final boolean CHAT;
    DaoChat daoChat;
    String theLastMessage;

    public static final boolean CHATS = true;
    public static final boolean OFF_CHATS = false;

    public UserChatAdapter(Activity mContext, List<DtoAccount> mAccounts, boolean share, boolean CHAT){
        this.mContext = mContext;
        this.mAccounts = mAccounts;
        this.share = share;
        this.CHAT = CHAT;
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

                holder.itemView.setOnLongClickListener(v -> {
                    if(CHAT){
                        final int PositionFinal = position;
                        holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_anim));
                        AlertDialog.Builder alert = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle)
                                .setTitle(mContext.getString(R.string.delete_this_conversation))
                                .setMessage(mContext.getString(R.string.do_you_really_want_to_delete_this_conversation))
                                .setPositiveButton(mContext.getString(R.string.yes), (dialog, which) -> {
                                    dialog.dismiss();
                                    if(ConnectionHelper.isOnline(mContext)){
                                        DaoChat db = new DaoChat(mContext);
                                        db.delete_user_from_chat(account.getId());
                                        LoadingDialog loadingDialog = new LoadingDialog((Activity) mContext);
                                        loadingDialog.startLoading();

                                        final DatabaseReference chatRef = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.CHAT_LIST_REFERENCE)
                                                .child(myFirebaseHelper.getFirebaseUser().getUid())
                                                .child(account.getId());

                                        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                if(snapshot.exists()){
                                                    snapshot.getRef().removeValue();
                                                    mAccounts.remove(PositionFinal);
                                                    notifyItemRemoved(PositionFinal);
                                                    loadingDialog.dismissDialog();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                                        });

                                    }else ToastHelper.toast((Activity)mContext , mContext.getString(R.string.you_are_without_internet), 0);
                                })
                                .setNeutralButton(mContext.getString(R.string.no), (dialogInterface, i) -> dialogInterface.dismiss());
                        Dialog mDialog = alert.create();
                        mDialog.getWindow().getAttributes().windowAnimations = R.style.MyAlertDialogStyle;
                        mDialog.show();
                    }
                    return false;
                });

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
                holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.click_anim));
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra(MessageActivity.USER_ID, account.getId());
                intent.putExtra(MessageActivity.CHAT_ID, account.getChat_id());
                if(share){
                    final int shareType = ShareContentActivity.getInstance().GetShareType();
                    intent.putExtra(MessageActivity.SHARE_ID, MainActivity.SHARED_ID);
                    intent.putExtra(MessageActivity.SHARE_TYPE_ID, shareType);
                    if(shareType == MainActivity.SHARED_PLAIN_TEXT)
                        intent.putExtra(MessageActivity.SHARE_CONTENT_ID, (String) ShareContentActivity.getInstance().GetShareContent());
                    else if(shareType == MainActivity.SHARED_IMAGE)
                        intent.putExtra(MessageActivity.SHARE_CONTENT_ID, (Uri) ShareContentActivity.getInstance().GetShareContent());
                    mContext.finish();
                }else intent.putExtra(MessageActivity.SHARE_ID, 0);
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
            final boolean isChat = account.getStatus_chat() != null && account.getStatus_chat().equals(Methods.ONLINE);
            holder.img_status.setVisibility(View.VISIBLE);

            if(isChat){
                if(ConnectionHelper.isOnline(mContext)){
                    if (account.getStatus_chat() != null && account.getStatus_chat().equals(Methods.ONLINE)){
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
}
