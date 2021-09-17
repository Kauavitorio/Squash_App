package dev.kaua.squash.Adapters.Chat;

import static android.content.Context.AUDIO_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.kaua.squash.Activitys.MessageActivity;
import dev.kaua.squash.Activitys.ViewMediaActivity;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;

@SuppressWarnings({"IfStatementWithIdenticalBranches", "ConstantConditions"})
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_RIGHT_AUDIO = 2;
    public static final int MSG_TYPE_LEFT_AUDIO = 3;
    public static final int MSG_TYPE_RIGHT_MEDIA = 4;
    public static final int MSG_TYPE_LEFT_MEDIA = 5;
    public static final int MSG_TYPE_START = -1;
    private final MessageActivity mContext;
    private static List<DtoMessage> mMessages;
    private final String imageURL;
    private final String chat_Username;
    private final String myUsername;
    private final String chat_id;
    FirebaseStorage firebaseStorage;
    private RecyclerView recycler_view_msg;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable runnable;
    private final Animation myAnim;
    private final DaoChat daoChat;
    private static int LayoutType;
    FirebaseUser fUser = myFirebaseHelper.getFirebaseUser();

    public MessageAdapter(MessageActivity mContext, List<DtoMessage> mMessages, String imageURL, RecyclerView recycler_view_msg
    , String myUsername, String chat_Username, String chat_id){
        this.mContext = mContext;
        MessageAdapter.mMessages = mMessages;
        this.imageURL = imageURL;
        this.chat_Username = chat_Username;
        this.myUsername = myUsername;
        this.recycler_view_msg = recycler_view_msg;
        this.chat_id = chat_id;
        this.daoChat = new DaoChat(mContext);
        myAnim = AnimationUtils.loadAnimation(mContext, R.anim.click_anim);
        checkMessagesSize();
    }

    private void checkMessagesSize() {
        MessageActivity.ShowOrNot_noMessage(mMessages.size() <= 0);
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutType = viewType;
        Context context = parent.getContext();
        View view;
        if(LayoutType == MSG_TYPE_RIGHT || LayoutType == MSG_TYPE_RIGHT_MEDIA){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_chat_item_right, parent, false);
            return new ViewHolder(view);
        }
        else if(LayoutType == MSG_TYPE_START){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_start_chat, parent, false);
            return new ViewHolder(view);
        }else if(LayoutType == MSG_TYPE_RIGHT_AUDIO){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_chat_audio_right, parent, false);
            return new ViewHolder(view);
        }else if(LayoutType == MSG_TYPE_LEFT_AUDIO){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_chat_audio_left, parent, false);
            return new ViewHolder(view);
        }
        else{
            view = LayoutInflater.from(context).inflate(R.layout.adapter_chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        final Context context = holder.itemView.getContext();
        final ViewHolder viewHolder = holder;
        final DtoMessage message = mMessages.get(position);

        if(LayoutType != MSG_TYPE_START){
            if(LayoutType == MSG_TYPE_RIGHT || LayoutType == MSG_TYPE_LEFT || LayoutType == MSG_TYPE_LEFT_MEDIA || LayoutType == MSG_TYPE_RIGHT_MEDIA){
                if(viewHolder.container_msg != null) viewHolder.container_msg.setVisibility(View.VISIBLE);
                if(viewHolder.msg_chat_item != null){
                    viewHolder.msg_chat_item.setText(EncryptHelper.decrypt(message.getMessage()));
                    //  Apply all url on Texts Views
                    Linkify.addLinks(viewHolder.msg_chat_item, Linkify.WEB_URLS);

                    //  URL CLICK'S listener
                    viewHolder.msg_chat_item.setMovementMethod(BetterLinkMovementMethod.newInstance().setOnLinkClickListener((textView, url) -> {
                        if (Patterns.WEB_URL.matcher(url).matches()) {
                            //An web url is detected
                            Methods.browseTo(context, url);
                            return true;
                        }
                        return false;
                    }));

                    if(message.getReply_from() != null && !message.getReply_from().equals(DtoMessage.NO_ONE) && !message.getReply_content().equals(DtoMessage.EMPTY)){
                        viewHolder.container_reply.setVisibility(View.VISIBLE);
                        if(fUser.getUid().equals(message.getReply_from()))
                            viewHolder.txt_reply_from.setText(mContext.getString(R.string.reply_to) + " " + myUsername);
                        else
                            viewHolder.txt_reply_from.setText(mContext.getString(R.string.reply_to) + " " + chat_Username);

                        viewHolder.reply_content.setText(EncryptHelper.decrypt(message.getReply_content()));

                        viewHolder.container_reply.setOnClickListener(v -> {
                            viewHolder.container_reply.startAnimation(myAnim);
                            String current_message = EncryptHelper.decrypt(message.getReply_content());
                            for (int i = 0; i < mMessages.size(); i++){
                                String get_reply = EncryptHelper.decrypt(mMessages.get(i).getReply_content());
                                if(current_message.equals(get_reply))
                                    MessageActivity.ScrollRecycler(position, i);

                                //viewHolder.itemView.setBackgroundColor(mContext.getColor(R.color.color_hover));
                            }
                        });
                    }

                    if(EncryptHelper.decrypt(mMessages.get(position).getMessage()) == null || EncryptHelper.decrypt(message.getMessage()).length() <= 0)
                        if(viewHolder.msg_chat_item != null) viewHolder.msg_chat_item.setVisibility(View.GONE);

                    viewHolder.msg_chat_item.setOnLongClickListener(v -> {
                        if(viewHolder.container_msg != null) viewHolder.container_msg.startAnimation(myAnim);
                        if(viewHolder.voicePlayerView != null) viewHolder.voicePlayerView.startAnimation(myAnim);
                        message_Action(fUser.getUid(), viewHolder.getAdapterPosition(), mMessages.get(viewHolder.getAdapterPosition()).getId_msg());
                        return false;
                    });
                }

                if(LayoutType == MSG_TYPE_LEFT_MEDIA || LayoutType == MSG_TYPE_RIGHT_MEDIA){
                    if(viewHolder.container_msg != null) viewHolder.container_msg.setVisibility(View.VISIBLE);
                    viewHolder.container_media_img_chat.setVisibility(View.VISIBLE);
                    viewHolder.media_img.setVisibility(View.VISIBLE);
                    Glide.with(context).load(message.getMedia().get(0)).dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL).thumbnail(0.5f).into(holder.media_img);

                    viewHolder.container_media_img_chat.setOnClickListener(v -> {
                        if(viewHolder.container_media_img_chat != null) viewHolder.container_media_img_chat.startAnimation(myAnim);
                        Intent i = new Intent(mContext, ViewMediaActivity.class);
                        i.putExtra("image_url", message.getMedia().get(0));
                        i.putExtra("receive_time", EncryptHelper.decrypt(message.getTime()).replace("-", "/"));
                        i.putExtra("chat_id", chat_id);
                        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(mContext, R.anim.move_to_left_go, R.anim.move_to_right_go);
                        ActivityCompat.startActivity(mContext, i, activityOptionsCompat.toBundle());
                    });
                }else if(viewHolder.container_media_img_chat != null) viewHolder.container_media_img_chat.setVisibility(View.GONE);

            }else if(LayoutType == MSG_TYPE_RIGHT_AUDIO || LayoutType == MSG_TYPE_LEFT_AUDIO){
                if(viewHolder.voicePlayerView != null && viewHolder.audio_timer != null
                        && viewHolder.play_button != null && viewHolder.pause_button != null && viewHolder.audio_seek_bar != null){
                    viewHolder.voicePlayerView.setVisibility(View.VISIBLE);

                    viewHolder.audio_timer.setText(convertFormat(MediaPlayer.create(mContext, Uri.parse(message.getMedia().get(0))).getDuration()));

                    viewHolder.play_button.setOnClickListener(v -> {
                        AudioManager am = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                        if(am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0)
                            ToastHelper.toast(mContext, mContext.getString(R.string.increase_media_volume), ToastHelper.SHORT_DURATION);

                        ResetHandlers();
                        mediaPlayer = MediaPlayer.create(context, Uri.parse(message.getMedia().get(0)));
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                //  Set progress on SeekBar
                                holder.audio_seek_bar.setProgress(mediaPlayer.getCurrentPosition());
                                //  Handler post delay for 0.5 seconds
                                handler.postDelayed(this, 500);
                            }
                        };

                        //  Get audio duration
                        int duration = mediaPlayer.getDuration();
                        // Convert Duration to minute and second
                        String sDuration = convertFormat(duration);
                        holder.audio_timer.setText(sDuration);

                        holder.pause_button.setVisibility(View.VISIBLE);
                        holder.play_button.setVisibility(View.GONE);

                        // Start Audio
                        mediaPlayer.start();

                        //Set max on Seek bar
                        holder.audio_seek_bar.setMax(mediaPlayer.getDuration());
                        handler.postDelayed(runnable, 0);
                    });

                    viewHolder.pause_button.setOnClickListener(v -> {
                        holder.pause_button.setVisibility(View.GONE);
                        holder.play_button.setVisibility(View.VISIBLE);

                        mediaPlayer.pause();

                        handler.removeCallbacks(runnable);
                    });

                    viewHolder.audio_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if(fromUser){
                                if(mediaPlayer != null)
                                    mediaPlayer.seekTo(progress);
                            }
                            if(mediaPlayer != null){

                                //  Get audio duration
                                int duration = mediaPlayer.getDuration();
                                // Convert Duration to minute and second
                                String sDuration = convertFormat(duration);
                                holder.audio_timer.setText(convertFormat(mediaPlayer.getCurrentPosition()) + " / " + sDuration);

                                if(mediaPlayer.getCurrentPosition() == duration){
                                    holder.pause_button.setVisibility(View.GONE);
                                    holder.play_button.setVisibility(View.VISIBLE);

                                    mediaPlayer.pause();

                                    handler.removeCallbacks(runnable);
                                    mediaPlayer.seekTo(0);
                                    holder.audio_seek_bar.setProgress(0);
                                    holder.audio_timer.setText(convertFormat(duration));
                                }
                            }
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                }
            }

            if(viewHolder.container_msg != null){
                if(position == mMessages.size() - 1)
                    viewHolder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up));
            }

            //  Default Action
            //  This action and items contain in all adapters
            if(message.getSender().equals(fUser.getUid())){
                viewHolder.message_container.setPadding(2, 2, 20, 2);
                if(position == mMessages.size() - 1){
                    DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference();
                    Query seen_last = ref.child(myFirebaseHelper.CHATS_REFERENCE).child(EncryptHelper.decrypt(chat_id)).orderByChild(DtoMessage.ID_MSG).equalTo(message.getId_msg());
                    seen_last.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!mContext.isDestroyed() && !mContext.isFinishing()){
                                for (DataSnapshot appleSnapshot: snapshot.getChildren()) {
                                    DtoMessage finalMessage = appleSnapshot.getValue(DtoMessage.class);
                                    if(finalMessage != null && finalMessage.getId_msg() != null){
                                        if (finalMessage.getIsSeen() == DtoMessage.SEEN) viewHolder.img_seen.setImageDrawable(context.getDrawable(R.drawable.ic_seen));
                                        else viewHolder.img_seen.setImageDrawable(context.getDrawable(R.drawable.ic_delivered));
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }else if(viewHolder.img_seen != null) viewHolder.img_seen.setVisibility(View.GONE);
            if(message.getTime() != null){
                String[] time = EncryptHelper.decrypt(message.getTime()).replace("-", "/").split("/");
                viewHolder.msgTime_chat_item.setText(time[2].substring(4));
            }
            viewHolder.message_container.setOnLongClickListener(v -> {
                if(viewHolder.container_msg != null) viewHolder.container_msg.startAnimation(myAnim);
                if(viewHolder.voicePlayerView != null) viewHolder.voicePlayerView.startAnimation(myAnim);
                message_Action(fUser.getUid(), viewHolder.getAdapterPosition(), mMessages.get(viewHolder.getAdapterPosition()).getId_msg());
                return false;
            });
        }
    }


    private void ResetHandlers() {
        handler.removeCallbacks(runnable);
        handler = new Handler();
        runnable = null;
        mediaPlayer = null;
    }

    @SuppressLint("DefaultLocale")
    private String convertFormat(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    private void message_Action(String user_id, int position, String id_msg) {
        Dialog myDialog = new Dialog(mContext);
        myDialog.setContentView(R.layout.message_action_adapter);
        LinearLayout delete_message = myDialog.findViewById(R.id.delete_message_container);
        LinearLayout copy_message = myDialog.findViewById(R.id.copy_message_container);
        if(!mMessages.get(position).getSender().equals(user_id)) delete_message.setVisibility(View.GONE);
        if(EncryptHelper.decrypt(mMessages.get(position).getMessage()).length() <= 0) copy_message.setVisibility(View.GONE);

        //  Delete message click
        delete_message.setOnClickListener(v -> {
            delete_message.startAnimation(myAnim);
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setTitle(mContext.getString(R.string.delete_message_for_everyone));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.yes),
                    (dialog, which) -> {
                        if(ConnectionHelper.isOnline(mContext)){
                            DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference();
                            Query applesQuery = ref.child(myFirebaseHelper.CHATS_REFERENCE).child(EncryptHelper.decrypt(chat_id)).orderByChild(DtoMessage.ID_MSG).equalTo(id_msg);

                            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                        appleSnapshot.getRef().removeValue();
                                    }
                                    firebaseStorage = FirebaseStorage.getInstance();
                                    if(mMessages != null && mMessages.get(position).getMedia() != null && mMessages.get(position).getMedia().get(0) != null && !mMessages.get(position).getMedia().get(0).equals("")){
                                        StorageReference photoRef = firebaseStorage.getReferenceFromUrl(mMessages.get(position).getMedia().get(0));
                                        photoRef.delete().addOnSuccessListener(aVoid -> {
                                            // File deleted successfully
                                            Log.d("DeleteMessage", "onSuccess: deleted file");
                                        }).addOnFailureListener(exception -> {
                                            // Uh-oh, an error occurred!
                                            Log.d("DeleteMessage", "onFailure: did not delete file");
                                        });
                                    }
                                    daoChat.delete_message(id_msg);
                                    mMessages.remove(position);
                                    notifyItemRemoved(position);
                                }

                                @Override
                                public void onCancelled(@NotNull DatabaseError databaseError) {
                                    Log.e("DeleteMessage", "onCancelled", databaseError.toException());
                                }
                            });
                            dialog.dismiss();
                        }else ToastHelper.toast(mContext, mContext.getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.cancel), (dialog, which) -> alertDialog.dismiss());
            alertDialog.show();
            myDialog.dismiss();
        });

        //  Copy message click
        copy_message.setOnClickListener(v -> {
            copy_message.startAnimation(myAnim);
            myDialog.dismiss();
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("MESSAGE_" + mMessages.get(position).getId_msg(), EncryptHelper.decrypt(mMessages.get(position).getMessage()));
            clipboard.setPrimaryClip(clip);
            ToastHelper.toast(mContext, mContext.getString(R.string.copied_message), ToastHelper.SHORT_DURATION);
        });

        myDialog.show();
    }

    public static int getSize(){
        return mMessages.size();
    }

    @Override
    public int getItemCount() { return mMessages.size(); }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView msg_chat_item, txt_reply_from, reply_content;
        ConstraintLayout container_msg;
        ConstraintLayout container_reply;
        ConstraintLayout container_media_img_chat;

        //  Base Items
        private final ImageView media_img;
        private final ImageView img_seen;
        private final TextView msgTime_chat_item;
        private final ConstraintLayout message_container;

        //  Audio Items
        private TextView audio_timer;
        private SeekBar audio_seek_bar;
        private CardView play_button, pause_button;
        private LinearLayout voicePlayerView;

        @SuppressLint("CutPasteId")
        public ViewHolder(View itemView){
            super(itemView);
            setIsRecyclable(true);
            //  Base Items
            media_img = itemView.findViewById(R.id.media_img);
            img_seen = itemView.findViewById(R.id.img_seen);
            msgTime_chat_item = itemView.findViewById(R.id.msgTime_chat_item);
            message_container = itemView.findViewById(R.id.message_container);

            msg_chat_item = itemView.findViewById(R.id.msg_chat_item);
            reply_content = itemView.findViewById(R.id.reply_content);
            container_media_img_chat = itemView.findViewById(R.id.container_media_img_chat);
            txt_reply_from = itemView.findViewById(R.id.txt_reply_from);
            container_reply = itemView.findViewById(R.id.container_reply);
            container_msg = itemView.findViewById(R.id.container_msg);


            if(LayoutType == MSG_TYPE_LEFT_AUDIO || LayoutType == MSG_TYPE_RIGHT_AUDIO){
                audio_timer = itemView.findViewById(R.id.audio_timer);
                voicePlayerView = itemView.findViewById(R.id.voicePlayerView);
                pause_button = itemView.findViewById(R.id.pause_button);
                play_button = itemView.findViewById(R.id.play_button);
                audio_seek_bar = itemView.findViewById(R.id.audio_seek_bar);
            }
        }

        public static DtoMessage GetMessage(int pos){
            return mMessages.get(pos);
        }

    }

    @Override
    public int getItemViewType(int position) {
        final DtoMessage message = mMessages.get(position);
        if(message.getSender().equals(fUser.getUid())) {
            if(message.getMedia() != null && message.getMedia() != null && message.getMedia().size() > 0
                    && !message.getMedia().get(0).equals("")){
                String extension = message.getMedia().get(0).substring(message.getMedia().get(0).lastIndexOf("."));
                Log.d("Audio_Loader", extension.substring(0, 4));
                if(extension.startsWith(".3gp")) return MSG_TYPE_RIGHT_AUDIO;
                else return MSG_TYPE_RIGHT_MEDIA;
            }
            else
                return MSG_TYPE_RIGHT;
        }
        else if(message.getSender().equals("base_start"))
            return MSG_TYPE_START;
        else {
            if(message.getMedia() != null && message.getMedia() != null && message.getMedia().size() > 0
                    && !message.getMedia().get(0).equals("")){
                String extension = message.getMedia().get(0).substring(message.getMedia().get(0).lastIndexOf("."));
                Log.d("Audio_Loader", extension.substring(0, 4));
                if(extension.startsWith(".3gp")) return MSG_TYPE_LEFT_AUDIO;
                else return MSG_TYPE_LEFT_MEDIA;
            }
            else
                return MSG_TYPE_LEFT;
        }
    }
}