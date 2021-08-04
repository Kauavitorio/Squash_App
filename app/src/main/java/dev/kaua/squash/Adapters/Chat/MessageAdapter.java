package dev.kaua.squash.Adapters.Chat;

import static android.content.Context.AUDIO_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MessageActivity;
import dev.kaua.squash.Adapters.DownloadIdGenerator;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;

@SuppressWarnings({"IfStatementWithIdenticalBranches", "ConstantConditions"})
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_START = -1;
    private final MessageActivity mContext;
    private static List<DtoMessage> mMessages;
    private final String imageURL;
    private final String chat_Username;
    private final String myUsername;
    private final String chat_id;
    FirebaseStorage firebaseStorage;
    private final int joinNow;
    private RecyclerView recycler_view_msg;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable runnable;
    private Animation myAnim;
    FirebaseUser fUser = ConfFirebase.getFirebaseUser();

    public MessageAdapter(MessageActivity mContext, List<DtoMessage> mMessages, String imageURL, int joinNow, RecyclerView recycler_view_msg
    , String myUsername, String chat_Username, String chat_id){
        this.mContext = mContext;
        MessageAdapter.mMessages = mMessages;
        this.imageURL = imageURL;
        this.chat_Username = chat_Username;
        this.myUsername = myUsername;
        this.joinNow = joinNow;
        this.recycler_view_msg = recycler_view_msg;
        this.chat_id = chat_id;
        myAnim = AnimationUtils.loadAnimation(mContext,R.anim.click_anim);
        checkMessagesSize();
    }

    private void checkMessagesSize() {
        MessageActivity.ShowOrNot_noMessage(mMessages.size() <= 0);
    }

    @NonNull
    @NotNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view;
        if(viewType == MSG_TYPE_RIGHT){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_chat_item_right, parent, false);
            return new ViewHolder(view);
        }
        else if(viewType == MSG_TYPE_START){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_start_chat, parent, false);
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
        Context context = holder.itemView.getContext();
        final ViewHolder viewHolder = holder;
        DtoMessage message = mMessages.get(position);
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
        if(message.getTime() != null){
            String[] time = EncryptHelper.decrypt(message.getTime()).replace("-", "/").split("/");
            viewHolder.msgTime_chat_item.setText(time[2].substring(4));
        }


        if(message.getReply_from() != null && !message.getReply_from().equals("noOne") && !message.getReply_content().equals("empty")){
            viewHolder.container_reply.setVisibility(View.VISIBLE);
            if(fUser.getUid().equals(message.getReply_from()))
                viewHolder.txt_reply_from.setText(mContext.getString(R.string.reply_to) + " " + myUsername);
            else
                viewHolder.txt_reply_from.setText(mContext.getString(R.string.reply_to) + " " + chat_Username);

            viewHolder.reply_content.setText(EncryptHelper.decrypt(message.getReply_content()));
        }

        viewHolder.container_msg.setVisibility(View.VISIBLE);
        holder.voicePlayerView.setVisibility(View.GONE);
        if(mMessages.get(position).getMedia() != null && message.getMedia() != null && message.getMedia().size() > 0){
            if(EncryptHelper.decrypt(mMessages.get(position).getMessage()) == null || EncryptHelper.decrypt(mMessages.get(position).getMessage()).length() <= 0)
                viewHolder.msg_chat_item.setVisibility(View.GONE);

            holder.voicePlayerView.setVisibility(View.VISIBLE);
            viewHolder.container_msg.setVisibility(View.GONE);
            String extension = mMessages.get(position).getMedia().get(0).substring(mMessages.get(position).getMedia().get(0).lastIndexOf("."));
            Log.d("Audio_Loader", extension.substring(0, 4));
            if(extension.startsWith(".3gp")){

                holder.audio_timer.setText(convertFormat(MediaPlayer.create(mContext, Uri.parse(mMessages.get(position).getMedia().get(0))).getDuration()));

                holder.play_button.setOnClickListener(v -> {
                    AudioManager am = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                    if(am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0)
                        ToastHelper.toast(mContext, mContext.getString(R.string.increase_media_volume), 0);

                    ResetHandlers();
                    mediaPlayer = MediaPlayer.create(context, Uri.parse(mMessages.get(position).getMedia().get(0)));
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

                holder.pause_button.setOnClickListener(v -> {
                    holder.pause_button.setVisibility(View.GONE);
                    holder.play_button.setVisibility(View.VISIBLE);

                    mediaPlayer.pause();

                    handler.removeCallbacks(runnable);
                });

                holder.audio_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser){
                            if(mediaPlayer != null)
                            mediaPlayer.seekTo(progress);
                        }

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
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }else{
                viewHolder.container_msg.setVisibility(View.VISIBLE);
                holder.voicePlayerView.setVisibility(View.GONE);
                viewHolder.container_media_img_chat.setVisibility(View.VISIBLE);
                viewHolder.media_img.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getMedia().get(0)).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        viewHolder.progress_media_img_chat.setVisibility(View.GONE);
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        viewHolder.progress_media_img_chat.setVisibility(View.GONE);
                        return false;
                    }
                }).into(holder.media_img);

                viewHolder.container_media_img_chat.setOnClickListener(v -> {
                    Glide.with(context)
                            .asBitmap()
                            .load(message.getMedia().get(0))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    String[] receive_time = EncryptHelper.decrypt(message.getTime()).replace("-", "/").split("/");
                                    if(resource != null) Methods.SaveImage(mContext, resource, chat_id, receive_time[0] + receive_time[1] +
                                            receive_time[2].replace(" ", "").replace(":", "").substring(0, 8));
                                }
                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) { }
                            });
                });
            }
        }else{
            viewHolder.container_media_img_chat.setVisibility(View.GONE);
            viewHolder.progress_media_img_chat.setVisibility(View.GONE);
        }

        //Disable to future updates
        //if(imageURL == null || imageURL.equals("default")) holder.profile_image_item.setImageResource(R.drawable.pumpkin_default_image);
        //else Picasso.get().load(EncryptHelper.decrypt(imageURL)).into(holder.profile_image_item);

        if(message.getSender().equals(fUser.getUid())){
            if (message.getIsSeen() == 1) viewHolder.img_seen.setImageDrawable(mContext.getDrawable(R.drawable.ic_seen));
            else viewHolder.img_seen.setImageDrawable(mContext.getDrawable(R.drawable.ic_delivered));

            if(joinNow != 0){
                Animation CartAnim = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
                viewHolder.container_msg.setAnimation(CartAnim);
            }
        }else viewHolder.img_seen.setVisibility(View.GONE);

        viewHolder.message_container.setOnLongClickListener(v -> {
            viewHolder.container_msg.startAnimation(myAnim);
            viewHolder.voicePlayerView.startAnimation(myAnim);
            if(mMessages.get(viewHolder.getAdapterPosition()).getSender().equals(fUser.getUid()))
                delete(viewHolder.getAdapterPosition(), mMessages.get(viewHolder.getAdapterPosition()).getId_msg());
            return false;
        });

        viewHolder.msg_chat_item.setOnLongClickListener(v -> {
            viewHolder.container_msg.startAnimation(myAnim);
            viewHolder.voicePlayerView.startAnimation(myAnim);
            if(mMessages.get(viewHolder.getAdapterPosition()).getSender().equals(fUser.getUid()))
                delete(viewHolder.getAdapterPosition(), mMessages.get(viewHolder.getAdapterPosition()).getId_msg());
            return false;
        });

        //recycler_view_msg.smoothScrollToPosition(mMessages.size() - 1);
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

    private void delete(int position, String id_msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(mContext.getString(R.string.delete_message_for_everyone));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.yes),
                (dialog, which) -> {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    Query applesQuery = ref.child("Chats").child(EncryptHelper.decrypt(chat_id)).orderByChild("id_msg").equalTo(id_msg);

                    applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                appleSnapshot.getRef().removeValue();
                            }
                            firebaseStorage = FirebaseStorage.getInstance();
                            if(mMessages != null && mMessages.get(position).getMedia() != null && mMessages.get(position).getMedia().get(0) != null){
                                StorageReference photoRef = firebaseStorage.getReferenceFromUrl(mMessages.get(position).getMedia().get(0));
                                photoRef.delete().addOnSuccessListener(aVoid -> {
                                    // File deleted successfully
                                    Log.d("DeleteMessage", "onSuccess: deleted file");
                                }).addOnFailureListener(exception -> {
                                    // Uh-oh, an error occurred!
                                    Log.d("DeleteMessage", "onFailure: did not delete file");
                                });
                            }
                            mMessages.remove(position);
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

        private final TextView msg_chat_item, msgTime_chat_item, txt_reply_from, reply_content;
        private final CircleImageView profile_image_item;
        private final ConstraintLayout container_msg, message_container;
        private final ConstraintLayout container_reply;
        private final ConstraintLayout container_media_img_chat;
        private final ImageView media_img;
        private final ImageView img_seen;
        private final TextView audio_timer;
        private final SeekBar audio_seek_bar;
        private final CardView play_button, pause_button;
        private final LinearLayout voicePlayerView;
        private final ProgressBar progress_media_img_chat;

        @SuppressLint("CutPasteId")
        public ViewHolder(View itemView){
            super(itemView);
            setIsRecyclable(false);
            msg_chat_item = itemView.findViewById(R.id.msg_chat_item);
            img_seen = itemView.findViewById(R.id.img_seen);
            play_button = itemView.findViewById(R.id.play_button);
            pause_button = itemView.findViewById(R.id.pause_button);
            audio_timer = itemView.findViewById(R.id.audio_timer);
            audio_seek_bar = itemView.findViewById(R.id.audio_seek_bar);
            message_container = itemView.findViewById(R.id.message_container);
            media_img = itemView.findViewById(R.id.media_img);
            voicePlayerView = itemView.findViewById(R.id.voicePlayerView);
            progress_media_img_chat = itemView.findViewById(R.id.progress_media_img_chat);
            reply_content = itemView.findViewById(R.id.reply_content);
            container_media_img_chat = itemView.findViewById(R.id.container_media_img_chat);
            profile_image_item = itemView.findViewById(R.id.profile_image_chat_item);
            txt_reply_from = itemView.findViewById(R.id.txt_reply_from);
            msgTime_chat_item = itemView.findViewById(R.id.msgTime_chat_item);
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
        if(mMessages.get(position).getSender().equals(fUser.getUid())) return MSG_TYPE_RIGHT;
        else if(mMessages.get(position).getSender().equals("base_start"))
            return MSG_TYPE_START;
        else return MSG_TYPE_LEFT;
    }
}