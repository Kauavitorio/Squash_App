package dev.kaua.squash.Adapters.Chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MessageActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.ViewHolder> {

    private Context mContext;
    private List<DtoAccount> mAccounts;
    private boolean isChat;
    String theLastMessage;

    public UserChatAdapter(Context mContext, List<DtoAccount> mAccounts, boolean isChat){
        this.mContext = mContext;
        this.mAccounts = mAccounts;
        this.isChat = isChat;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_user_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UserChatAdapter.ViewHolder holder, int position) {
        DtoAccount account = mAccounts.get(position);
        holder.user_name.setText(account.getName_user());
        if(account.getImageURL().equals("default")) holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        else Picasso.get().load(EncryptHelper.decrypt(account.getImageURL())).into(holder.profile_image);


        if(isChat)
            lastMessage(account.getId(), holder.last_msg, holder.card_no_read_ic);
        else holder.last_msg.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, MessageActivity.class);
            intent.putExtra("userId", account.getId());
            mContext.startActivity(intent);
        });

        if(isChat){
            if (account.getStatus_chat().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }else{
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return mAccounts.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView user_name, last_msg;
        private CircleImageView profile_image;
        private CircleImageView img_on, img_off;
        private CardView card_no_read_ic;
        private ImageView ic_pinned_chat;

        @SuppressLint("CutPasteId")
        public ViewHolder(View itemView){
            super(itemView);
            ic_pinned_chat = itemView.findViewById(R.id.ic_pinned_chat);
            user_name = itemView.findViewById(R.id.user_name_users);
            profile_image = itemView.findViewById(R.id.profile_image_users);
            img_on = itemView.findViewById(R.id.img_on);
            last_msg = itemView.findViewById(R.id.last_msg);
            img_off = itemView.findViewById(R.id.img_off);
            card_no_read_ic = itemView.findViewById(R.id.card_no_read_ic);
        }
    }

    //  check for last message
    private void lastMessage(String userId, TextView last_msg, CardView ic_not_seen){
        theLastMessage  = "default";
        FirebaseUser firebaseUser = ConfFirebase.getFirebaseUser();
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
                                theLastMessage = message.getMessage();
                            }
                }

                switch (theLastMessage){
                    case "default":
                        last_msg.setVisibility(View.GONE);
                        break;
                    default:
                        if(Objects.requireNonNull(EncryptHelper.decrypt(theLastMessage)).length() > 35)
                        last_msg.setText(Objects.requireNonNull(EncryptHelper.decrypt(theLastMessage)).substring(0, 35) + "…");
                        else
                            last_msg.setText(EncryptHelper.decrypt(theLastMessage));
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }
}
