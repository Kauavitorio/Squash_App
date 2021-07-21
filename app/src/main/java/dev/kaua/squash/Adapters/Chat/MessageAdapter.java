package dev.kaua.squash.Adapters.Chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;

@SuppressWarnings("IfStatementWithIdenticalBranches")
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_START = -1;
    private final Context mContext;
    private static List<DtoMessage> mMessages;
    private final String imageURL;
    private final String chat_Username;
    private final String myUsername;
    private final int joinNow;
    private RecyclerView recycler_view_msg;
    FirebaseUser fUser;

    public MessageAdapter(Context mContext, List<DtoMessage> mMessages, String imageURL, int joinNow, RecyclerView recycler_view_msg
    , String myUsername, String chat_Username){
        this.mContext = mContext;
        this.mMessages = mMessages;
        this.imageURL = imageURL;
        this.chat_Username = chat_Username;
        this.myUsername = myUsername;
        this.joinNow = joinNow;
        this.recycler_view_msg = recycler_view_msg;
    }

    @NonNull
    @NotNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT){
            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_chat_item_right, parent, false);
            return new ViewHolder(view);
        }
        else if(viewType == MSG_TYPE_START){
            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_start_chat, parent, false);
            return new ViewHolder(view);
        }
        else{
            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapter.ViewHolder holder, int position) {
        DtoMessage message = mMessages.get(position);
        holder.msg_chat_item.setText(EncryptHelper.decrypt(message.getMessage()));
        holder.msgTime_chat_item.setText(EncryptHelper.decrypt(message.getTime()));

        if(message.getReply_from() != null && !message.getReply_from().equals("noOne") && !message.getReply_content().equals("empty")){
            holder.container_reply.setVisibility(View.VISIBLE);
            holder.card_container_reply_content.setElevation(0);
            if(fUser.getUid().equals(message.getReply_from()))
                holder.txt_reply_from.setText(mContext.getString(R.string.reply_to) + " " + myUsername);
            else
                holder.txt_reply_from.setText(mContext.getString(R.string.reply_to) + " " + chat_Username);

            holder.reply_content.setText(EncryptHelper.decrypt(message.getReply_content()));
        }

        //Disable to future updates
        //if(imageURL == null || imageURL.equals("default")) holder.profile_image_item.setImageResource(R.drawable.pumpkin_default_image);
        //else Picasso.get().load(EncryptHelper.decrypt(imageURL)).into(holder.profile_image_item);

        holder.container_msg.setVisibility(View.VISIBLE);
        if(position == mMessages.size() -1){
            if (message.getIsSeen() == 1) holder.txt_seen.setText(mContext.getString(R.string.seen));
            else holder.txt_seen.setText(mContext.getString(R.string.delivered));

            if(joinNow != 0){
                Animation CartAnim = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
                holder.container_msg.setAnimation(CartAnim);
                holder.txt_seen.setAnimation(CartAnim);
            }
        }else holder.txt_seen.setVisibility(View.GONE);



        holder.container_msg.setOnLongClickListener(v -> {
            if(mMessages.get(holder.getAdapterPosition()).getSender().equals(fUser.getUid()))
                delete(holder.getAdapterPosition(), mMessages.get(holder.getAdapterPosition()).getMessage());
            return false;
        });

        //recycler_view_msg.smoothScrollToPosition(mMessages.size() - 1);
    }

    private void delete(int position, String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(mContext.getString(R.string.delete_message_for_everyone));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.yes),
                (dialog, which) -> {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    Query applesQuery = ref.child("Chats").orderByChild("message").equalTo(msg);

                    applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                appleSnapshot.getRef().removeValue();
                            }
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NotNull DatabaseError databaseError) {
                            Log.e("DeleteMessage", "onCancelled", databaseError.toException());
                        }
                    });
                    dialog.dismiss();
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.cancel), (dialog, which) -> alertDialog.dismiss());
        alertDialog.show();
    }

    @Override
    public int getItemCount() { return mMessages.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView msg_chat_item, txt_seen, msgTime_chat_item, txt_reply_from, reply_content;
        private final CircleImageView profile_image_item;
        private final CardView card_container_reply_content;
        private final LinearLayout container_msg;
        private final LinearLayout container_reply;

        @SuppressLint("CutPasteId")
        public ViewHolder(View itemView){
            super(itemView);
            msg_chat_item = itemView.findViewById(R.id.msg_chat_item);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            reply_content = itemView.findViewById(R.id.reply_content);
            profile_image_item = itemView.findViewById(R.id.profile_image_chat_item);
            txt_reply_from = itemView.findViewById(R.id.txt_reply_from);
            msgTime_chat_item = itemView.findViewById(R.id.msgTime_chat_item);
            card_container_reply_content = itemView.findViewById(R.id.card_container_reply_content);
            container_reply = itemView.findViewById(R.id.container_reply);
            container_msg = itemView.findViewById(R.id.container_msg);
            container_msg.setVisibility(View.GONE);
        }

        public static DtoMessage GetMessage(int pos){
            return mMessages.get(pos);
        }

    }

    @Override
    public int getItemViewType(int position) {
        fUser = ConfFirebase.getFirebaseUser();
        if(mMessages.get(position).getSender().equals(fUser.getUid())) return MSG_TYPE_RIGHT;
        else if(mMessages.get(position).getSender().equals("base_start"))
            return MSG_TYPE_START;
        else return MSG_TYPE_LEFT;
    }
}