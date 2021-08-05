package dev.kaua.squash.Activitys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Adapters.AudioRecorder;
import dev.kaua.squash.Adapters.Chat.BackgroundHelper;
import dev.kaua.squash.Adapters.Chat.MessageAdapter;
import dev.kaua.squash.Adapters.Chat.SwipeReply;
import dev.kaua.squash.Adapters.Chat.ViewProxy;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.Chatslist;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.Fragments.Chat.ChatsFragment;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.Notifications.APIService;
import dev.kaua.squash.Notifications.Client;
import dev.kaua.squash.Notifications.Data;
import dev.kaua.squash.Notifications.MyResponse;
import dev.kaua.squash.Notifications.Sender;
import dev.kaua.squash.Notifications.Token;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.KeyboardUtils;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.UserPermissions;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

@SuppressLint("SetTextI18n")
@SuppressWarnings({"deprecation", "StaticFieldLeak", "FieldCanBeLocal"})
public class MessageActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    public static ConstraintLayout container_no_message_yet;
    public static ImageView background_chat;
    public static ImageView btn_more_medias;
    public static LinearLayout container_edit_text;
    public static MessageActivity instance;
    private TextView txt_user_name, txt_isOnline_chat, txtQuotedMsg;
    private static RecyclerView recycler_view_msg;
    private EditText text_send;
    private CardView btn_send, btn_rec_audio;
    private String message_to_reply;
    private ImageView verification_ic;
    private String reply_from;
    private ConstraintLayout reply_layout;
    private ImageView cancelButton;
    private ValueEventListener seenListener;
    public static DtoAccount user_im_chat;
    private static final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
    private static final String[] permissions_audio = { Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    public static FirebaseUser fUser;

    private static DaoChat chatDB;
    private static DatabaseReference reference;
    static MessageAdapter messageAdapter;
    private static List<DtoMessage> mMessage;
    private List<String> medias_pin = new ArrayList<>();
    private static String userId;
    private static String chat_id;
    String another_user_image = "";
    private Animation myAnim;

    private APIService apiService;

    boolean notify = false;
    public static final int PIC_CROP = 111;
    public static final int PICK_IMAGE_REQUEST = 222;
    public static final int PICK_IMAGE_REQUEST_MEDIA = 333;
    public static final int OPEN_CAMERA = 444;
    private static final String TAG = "MESSAGE_ACTIVITY";
    public static StorageReference storageReference;

    Intent intent;

    private TextView recordTimeText;
    private View recordPanel;
    private View slideText;
    private boolean recording;
    private float startedDraggingX = -1;
    private float distCanMove = dp(80);
    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private Timer timer;
    private float x1,x2;
    static final int MIN_DISTANCE = dp(400);
    private static AudioRecorder audioRecorder;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Ids();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        apiService = Client.getClient(Methods.FCM_URL).create(APIService.class);

        intent = getIntent();
        userId = intent.getStringExtra("userId");
        chat_id = intent.getStringExtra("chat_id");
        fUser = ConfFirebase.getFirebaseUser();
        reference = ConfFirebase.getFirebaseDatabase().getReference("Users").child(userId);

        CheckShared();
        GenerateChatID();

        String img = chatDB.get_BG("bg_" + fUser.getUid() + "_"
                + userId);
        if(img != null) BackgroundHelper.LoadBackground(img);

        //  Send msg click
        btn_send.setOnClickListener(v -> {
            btn_send.startAnimation(myAnim);
            notify = true;
            String msg = text_send.getText().toString();
            if(!msg.equals("") && msg.trim().replaceAll(" +", "").length() > 0)
                sendMessage(fUser.getUid(), userId, msg);
            else ToastHelper.toast(this, getString(R.string.the_message_cannot_be_empty), 0);
            text_send.setText("");
        });

        //  Loop to get user who user having a chat information
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                user_im_chat = snapshot.getValue(DtoAccount.class);
                if(user_im_chat != null){

                    txt_user_name.setText(user_im_chat.getName_user());
                    if(!another_user_image.equals(user_im_chat.getImageURL())){
                        another_user_image = user_im_chat.getImageURL();
                        if(user_im_chat.getImageURL().equals("default")) profile_image.setImageResource(R.drawable.pumpkin_default_image);
                        else Picasso.get().load(EncryptHelper.decrypt(another_user_image)).into(profile_image);
                    }
                    //readMessage(fUser.getUid(), userId, user_im_chat.getImageURL());
                    checkChatList(userId);

                    //  check typing status
                    if(user_im_chat.getTypingTo().equals(fUser.getUid())){
                        txt_isOnline_chat.setVisibility(View.VISIBLE);
                        txt_isOnline_chat.setText(getString(R.string.typing));
                    }
                    else{
                        if(user_im_chat.getStatus_chat().equals("online")){
                            txt_isOnline_chat.setVisibility(View.VISIBLE);
                            txt_isOnline_chat.setText(getString(R.string.online));
                        }
                        else txt_isOnline_chat.setText(Methods.loadLastSeen(MessageActivity.this, user_im_chat.getLast_seen()));
                    }

                    if(user_im_chat.getVerification_level() != null && Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(user_im_chat.getVerification_level()))) > 0){
                        verification_ic.setVisibility(View.VISIBLE);
                        int verified = Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(user_im_chat.getVerification_level())));
                        if (verified == 2)
                            verification_ic.setImageDrawable(getDrawable(R.drawable.ic_verified_employee_account));
                        else
                            verification_ic.setImageDrawable(getDrawable(R.drawable.ic_verified_account));
                    }else verification_ic.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });

        //  check edittext is typing
        text_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0) {
                    Methods.typingTo_chat_Status("noOne");

                    if(reply_layout.getVisibility() == ConstraintLayout.VISIBLE){
                        btn_send.setVisibility(View.VISIBLE);
                        btn_rec_audio.setVisibility(View.GONE);
                    }else{
                        if(btn_send.getVisibility() != CardView.GONE)
                            btn_send.startAnimation(AnimationUtils.loadAnimation(MessageActivity.this ,R.anim.slide_donw));
                        btn_send.setVisibility(View.GONE);
                        if(btn_rec_audio.getVisibility() != CardView.VISIBLE)
                            btn_rec_audio.startAnimation(AnimationUtils.loadAnimation(MessageActivity.this ,R.anim.slide_up));
                        btn_rec_audio.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    recordPanel.setVisibility(View.GONE);
                    if(btn_send.getVisibility() != CardView.VISIBLE)
                        btn_send.startAnimation(AnimationUtils.loadAnimation(MessageActivity.this ,R.anim.slide_up));
                    btn_send.setVisibility(View.VISIBLE);
                    if(btn_rec_audio.getVisibility() != CardView.GONE)
                        btn_rec_audio.startAnimation(AnimationUtils.loadAnimation(MessageActivity.this ,R.anim.slide_donw));
                    btn_rec_audio.setVisibility(View.GONE);
                    container_edit_text.setVisibility(View.VISIBLE);
                    btn_more_medias.setVisibility(View.VISIBLE);
                    Methods.typingTo_chat_Status(userId);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        //  Reply close button click
        cancelButton.setOnClickListener(v -> hideReplayLayout());

        //  Btn medias click
        btn_more_medias.setOnClickListener(v -> {
            btn_more_medias.startAnimation(myAnim);
            BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetTheme);
            View sheetView = LayoutInflater.from(this).inflate(R.layout.adapter_sheet_media_action,
                    findViewById(R.id.adapter_sheet_media));

            sheetView.findViewById(R.id.close_media_action).setOnClickListener(view -> dialog.dismiss());

            sheetView.findViewById(R.id.container_btn_image).setOnClickListener(view -> {
                OpenGallery_ACTION();
                dialog.dismiss();
            });

            sheetView.findViewById(R.id.container_btn_video).setOnClickListener(view -> {
                dialog.dismiss();
                ToastHelper.toast(this, getString(R.string.under_development), 0);
            });
            dialog.setContentView(sheetView);
            dialog.show();
        });

        //  Profile Image click
        profile_image.setOnClickListener(v -> OpenUserProfile());

        //  Rec audio click
        btn_rec_audio.setOnTouchListener((view, motionEvent) -> {
            if(!recording) btn_rec_audio.startAnimation(myAnim);
            UserPermissions.validatePermissions(permissions_audio, instance, REQUEST_RECORD_AUDIO_PERMISSION);
            int RECORD_PERMISSION = ContextCompat.checkSelfPermission(instance, Manifest.permission.RECORD_AUDIO);
            if (RECORD_PERMISSION == PackageManager.PERMISSION_GRANTED){

                x2 = motionEvent.getX();
                float deltaX = x2 - x1;
                Log.d(TAG, "MIN -> " + MIN_DISTANCE);
                Log.d(TAG, "CURRENT -> " + deltaX);

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(!recording){
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                                .getLayoutParams();
                        params.leftMargin = dp(30);
                        slideText.setLayoutParams(params);
                        ViewProxy.setAlpha(slideText, 1);
                        startedDraggingX = -1;
                        // startRecording();
                        StartRecord();
                        btn_rec_audio.getParent()
                                .requestDisallowInterceptTouchEvent(true);
                    }
                } else if (Math.abs(deltaX) >= MIN_DISTANCE && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(recording){
                        startedDraggingX = -1;
                        Log.d(TAG, "CANCEL ACTION");
                        StopRecord(true);
                        // stopRecording(true);
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    float x = motionEvent.getX();
                    /*if (x < -distCanMove) {
                        StopRecord(false);
                        // stopRecording(false);
                    }*/
                    x = x + ViewProxy.getX(btn_rec_audio);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                            .getLayoutParams();
                    if (startedDraggingX != -1) {
                        float dist = (x - startedDraggingX);
                        params.leftMargin = dp(120) + (int) dist;
                        slideText.setLayoutParams(params);
                        float alpha = 1.0f + dist / distCanMove;
                        if (alpha > 1) alpha = 1;
                        else if (alpha < 0)
                            alpha = 0;
                        ViewProxy.setAlpha(slideText, alpha);
                    }
                    if (x <= ViewProxy.getX(slideText) + slideText.getWidth()
                            + dp(30)) {
                        if (startedDraggingX == -1) {
                            startedDraggingX = x;
                            distCanMove = (recordPanel.getMeasuredWidth()
                                    - slideText.getMeasuredWidth() - dp(48)) / 2.0f;
                            if (distCanMove <= 0)
                                distCanMove = dp(80);
                            else if (distCanMove > dp(80))
                                distCanMove = dp(80);
                        }
                    }
                    if (params.leftMargin > dp(30)) {
                        params.leftMargin = dp(30);
                        slideText.setLayoutParams(params);
                        ViewProxy.setAlpha(slideText, 1);
                        startedDraggingX = -1;
                    }
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if(recording){
                        startedDraggingX = -1;
                        Log.d(TAG, "SENT ACTION");
                        StopRecord(false);
                    }
                }
                view.onTouchEvent(motionEvent);
                return true;
            }
            return true;
        });

        seenMessage(userId);
        setRecyclerSwipe();
    }

    private static String fileName = null;
    private MyTimerTask myTimerTask;
    private void StartRecord() {
        UserPermissions.validatePermissions(permissions_audio, instance, REQUEST_RECORD_AUDIO_PERMISSION);
        int RECORD_PERMISSION = ContextCompat.checkSelfPermission(instance, Manifest.permission.RECORD_AUDIO);
        if (RECORD_PERMISSION == PackageManager.PERMISSION_GRANTED){
            try {
                recording = true;
                // Record to the external cache directory for visibility
                fileName = getExternalCacheDir().getAbsolutePath();
                @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                fileName += "/squash_" + timeStamp + ".3gp";
                audioRecorder = null;
                audioRecorder = new AudioRecorder(fileName);
                audioRecorder.start();
                recordPanel.setVisibility(View.VISIBLE);
                container_edit_text.setVisibility(View.GONE);
                btn_more_medias.setVisibility(View.GONE);
                startTime = SystemClock.uptimeMillis();
                timer = null;
                timer = new Timer();
                if(myTimerTask != null) myTimerTask = null;
                myTimerTask = new MyTimerTask();
                timer.schedule(myTimerTask, 1000, 1000);
                Methods.vibrate(this, Methods.VIBRATE_SHORT);
            }catch (Exception ex){
                recording = false;
                Log.d(TAG, ex.getMessage());
                StopRecord(true);
                Warnings.showWeHaveAProblem(this);
            }
        }
    }

    private void StopRecord(boolean cancel) {
        recording = false;
        try {
            if (!cancel) {
                recordPanel.setVisibility(View.GONE);
                container_edit_text.setVisibility(View.VISIBLE);
                btn_more_medias.setVisibility(View.VISIBLE);
                String audio_path = audioRecorder.stop();
                String record_time = recordTimeText.getText().toString();
                String[] record_time_split = record_time.split(":");
                if (timer != null && !record_time.equals("00:00") && Integer.parseInt(record_time_split[1]) > 0) {
                    @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    Uri uriAudio = Uri.fromFile(new File(audio_path).getAbsoluteFile());
                    LoadingDialog loadingDialog = new LoadingDialog(this);
                    loadingDialog.startLoading();
                    storageReference = ConfFirebase.getFirebaseStorage().child("user").child("chat").child("medias").child(fUser.getUid())
                            .child("audios").child("squash_audio_" + timeStamp + ".3gp");
                    storageReference.putFile(uriAudio).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            loadingDialog.dismissDialog();
                            Log.d(TAG, Objects.requireNonNull(task.getException()).toString());
                        }
                        if (task.getResult().getMetadata() != null) {
                            if (task.getResult().getMetadata().getReference() != null) {
                                Task<Uri> result = task.getResult().getStorage().getDownloadUrl();
                                result.addOnSuccessListener(uri -> {
                                    loadingDialog.dismissDialog();
                                    String audio_download = uri.toString();
                                    medias_pin = new ArrayList<>();
                                    medias_pin.add(audio_download);
                                    sendMessage(fUser.getUid(), userId, text_send.getText().toString());
                                    Log.d(TAG, "OK UPLOAD");
                                });
                            } else
                                loadingDialog.dismissDialog();
                        }
                    });
                }

                if(timer != null)
                    timer.cancel();

                recordTimeText.setText("00:00");
                Methods.vibrate(this, Methods.VIBRATE_SHORT);
            } else {
                recordPanel.setVisibility(View.GONE);
                container_edit_text.setVisibility(View.VISIBLE);
                btn_more_medias.setVisibility(View.VISIBLE);
                String audio_path = audioRecorder.stop();
                Log.d(TAG, "CANCEL -> " + audio_path);

                if(timer != null)
                    timer.cancel();

                recordTimeText.setText("00:00");
                Methods.vibrate(this, Methods.VIBRATE_SHORT);
            }
        }catch (Exception ex){
            Log.d(TAG, ex.toString());
            Warnings.showWeHaveAProblem(this);
        }
    }

    public static int dp(float value) {
        return (int) Math.ceil(1 * value);
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            @SuppressLint("DefaultLocale") final String hms = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(updatedTime)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                            .toHours(updatedTime)),
                    TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                            .toMinutes(updatedTime)));
            long lastSec = TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                    .toMinutes(updatedTime));
            System.out.println(lastSec + " hms " + hms);
            runOnUiThread(() -> {
                try {
                    if (recordTimeText != null)
                        recordTimeText.setText(hms);
                } catch (Exception e) {
                    Log.d(TAG, "TimerTask -> " + e.toString());
                }

            });
        }
    }

    private void checkChatList(String userId) {
        reference = FirebaseDatabase.getInstance().getReference("Chatslist").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                for(DataSnapshot snapshot : datasnapshot.getChildren()){
                    Chatslist chatList = snapshot.getValue(Chatslist.class);
                    if(chatList != null)
                    if(chatList.getId().equals(userId)){
                        if(chatList.getChat_id() != null)
                            UpdateChat_Id(chatList.getChat_id());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private void GenerateChatID() {
        if(chat_id == null){
            String first_sequence = Methods.RandomCharactersWithoutSpecials(20);
            String second_sequence = Methods.RandomCharactersWithoutSpecials(5);
            chat_id = "CHATID" + fUser.getUid() + first_sequence + userId + second_sequence + "SQUASH";
            chat_id = EncryptHelper.encrypt(Methods.shuffle(chat_id));
        }
    }

    public static void ShowOrNot_noMessage(boolean status){
        if(status) container_no_message_yet.setVisibility(View.VISIBLE);
        else container_no_message_yet.setVisibility(View.GONE);
    }

    private void CheckShared() {
        Bundle bundle = getIntent().getExtras();
        if(bundle.getInt("shared") == 1){
            if(bundle.getInt("shared_type") == 1)
                text_send.setText(bundle.getString("shared_content"));
        }
    }

    private void Ids() {
        myAnim = AnimationUtils.loadAnimation(this,R.anim.click_anim);
        instance = MessageActivity.this;
        chatDB = new DaoChat(MessageActivity.this);
        profile_image = findViewById(R.id.profile_image_chat);
        recordPanel = findViewById(R.id.record_panel);
        recordTimeText = findViewById(R.id.recording_time_text);
        btn_more_medias = findViewById(R.id.btn_more_medias);
        TextView textView = findViewById(R.id.slideToCancelTextView);
        textView.setText(getString(R.string.slide_to_cancel));
        container_no_message_yet = findViewById(R.id.container_no_message_yet);
        txt_user_name = findViewById(R.id.txt_username_chat);
        slideText = findViewById(R.id.slideText);
        recycler_view_msg = findViewById(R.id.recycler_view_msg);
        verification_ic = findViewById(R.id.verification_ic_message);
        txt_isOnline_chat = findViewById(R.id.txt_isOnline_chat);
        btn_rec_audio = findViewById(R.id.container_btn_rec_audio);
        reply_layout = findViewById(R.id.reply_layout);
        background_chat = findViewById(R.id.background_chat);
        container_edit_text = findViewById(R.id.container_edit_text_chat);
        txtQuotedMsg = findViewById(R.id.txtQuotedMsg);
        cancelButton = findViewById(R.id.cancelButton);
        text_send = findViewById(R.id.text_send);
        btn_send = findViewById(R.id.container_btn_send);
        btn_send.setElevation(0);
        recycler_view_msg.setHasFixedSize(false);
        recycler_view_msg.setNestedScrollingEnabled(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recycler_view_msg.setLayoutManager(linearLayoutManager);
        getWindow().setStatusBarColor(getColor(R.color.black_intro));
        recordPanel.setVisibility(View.GONE);
        container_edit_text.setVisibility(View.VISIBLE);
    }

    private void seenMessage(String userUid){
        reference = FirebaseDatabase.getInstance().getReference().child("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                for(DataSnapshot snapshot : datasnapshot.getChildren()){
                    DtoMessage message = snapshot.getValue(DtoMessage.class);
                    if(message != null)
                        if(message.getReceiver() != null)
                            if(message.getReceiver().equals(fUser.getUid()) && message.getSender().equals(userUid)){
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("isSeen", 1);
                                snapshot.getRef().updateChildren(hashMap);
                            }
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private void sendMessage(String sender, String receiver, String message){
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("dd-MM-yyyy HH:mm a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time_id = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String formattedDate = df_time.format(c.getTime());
        String formattedDate_id = df_time_id.format(c.getTime());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("id_msg", Methods.RandomCharactersWithoutSpecials(8) + formattedDate_id.replace("-","")
                .replace(" ","").replace(":","") + fUser.getUid());
        if(message_to_reply != null && message_to_reply.length() > 0){
            hashMap.put("reply_from", reply_from);
            hashMap.put("reply_content", EncryptHelper.encrypt(message_to_reply));
        }
        else{
            hashMap.put("reply_from", "noOne");
            hashMap.put("reply_content", "empty");
        }
        hashMap.put("message", EncryptHelper.encrypt(message));
        hashMap.put("isSeen", 0);
        hashMap.put("time", EncryptHelper.encrypt(formattedDate));
        hashMap.put("media", medias_pin);

        reference.child("Chats").child(Objects.requireNonNull(EncryptHelper.decrypt(chat_id))).push().setValue(hashMap);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time_last_chat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        user_im_chat.setLast_chat(df_time_last_chat.format(c.getTime()));
        chatDB.UPDATE_A_CHAT(user_im_chat, 1);

        //  add User to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatslist")
                .child(fUser.getUid())
                .child(userId);

        final DatabaseReference chatRefANOTHER_USER = FirebaseDatabase.getInstance().getReference("Chatslist")
                .child(userId)
                .child(fUser.getUid());

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("id").setValue(userId);
                    chatRef.child("chat_id").setValue(chat_id);
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
        chatRefANOTHER_USER.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRefANOTHER_USER.child("id").setValue(fUser.getUid());
                    chatRefANOTHER_USER.child("chat_id").setValue(chat_id);
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });

        if(medias_pin != null && medias_pin.size() > 0){
            String extension = medias_pin.get(0).substring(medias_pin.get(0).lastIndexOf("."));
            if(extension.startsWith(".3gp")) message = getString(R.string.audio);
            medias_pin.clear();
        }

        if(notify) sendNotification(receiver, MyPrefs.getUserInformation(this).getUsername(), message, EncryptHelper.decrypt(chat_id));
        notify = false;

        text_send.setText("");
        /*reference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                DtoAccount account = snapshot.getValue(DtoAccount.class);
                //if(account != null)
                //if(notify) sendNotification(receiver, account.getUsername(), msg, EncryptHelper.decrypt(chat_id));
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });*/

        try {
            ChatsFragment.getInstance().chatList();
        }catch (Exception exception){
            Log.d(TAG, "Cant load list");
        }

        hideReplayLayout();
    }

    private void currentUser(String userId){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getSharedPreferences( MyPrefs.PREFS_NOTIFICATION, MODE_PRIVATE).edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }

    private void sendNotification(String receiver, String username, String message, String chat_id){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fUser.getUid(), R.drawable.pumpkin_default_image, username+": "+ message, getString(R.string.new_message), userId, EncryptHelper.encrypt(chat_id));

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {
                            if(response.code() == 200){
                                assert response.body() != null;
                                if(response.body().success != 1)
                                    Log.w(TAG, "Send Message Notification -> Failed");
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<MyResponse> call, @NotNull Throwable t) {
                            Warnings.showWeHaveAProblem(MessageActivity.this);
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    public static void UpdateChat_Id(String id){
        chat_id = id;
        base_load = true;
        readMessage(fUser.getUid(), userId, user_im_chat.getImageURL());
    }

    static int joinNow = 0;
    static boolean base_load = true;
    static List<DtoMessage> mMessageFinal = new ArrayList<>();
    private static void readMessage(String myID, String userId, String imageURl){
        Calendar c = Calendar.getInstance();
        mMessage = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference().child("Chats").child(Objects.requireNonNull(EncryptHelper.decrypt(chat_id)));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                mMessage.clear();
                DtoMessage messageBase = new DtoMessage();
                messageBase.setSender("base_start");
                mMessage.add(messageBase);
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    DtoMessage message = snapshot.getValue(DtoMessage.class);
                    if(message != null)
                        if(message.getReceiver() != null)
                            if (message.getReceiver().equals(myID) && message.getSender().equals(userId)
                                    || message.getReceiver().equals(userId) && message.getSender().equals(myID)){
                                mMessage.add(message);
                    }
                }
                if(mMessage.size() > 1){
                    if(mMessageFinal.size() != mMessage.size()) {
                        Log.d(TAG, "OKAY not need to reset");
                        if(joinNow <= 100) joinNow++;
                        else joinNow = 1;
                    } else joinNow = 0;

                    if(mMessageFinal.size() > 0 && !mMessageFinal.get(mMessageFinal.size() -1).getMessage().equals(mMessage.get(mMessage.size() -1).getMessage())
                            || mMessageFinal.size() > 0 && mMessageFinal.get(mMessageFinal.size() -1).getIsSeen() != mMessage.get(mMessage.size() -1).getIsSeen()){
                        Log.d(TAG, "OKAY NEW MSG OR IS SEEN");
                        LoadAdapter(imageURl);
                        base_load = false;

                        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time_last_chat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        user_im_chat.setLast_chat(df_time_last_chat.format(c.getTime()));
                        chatDB.UPDATE_A_CHAT(user_im_chat, 1);
                    }

                    if(base_load) LoadAdapter(imageURl);

                    if(!base_load && mMessageFinal.size() != mMessage.size()) LoadAdapter(imageURl);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private static void LoadAdapter(String imageURl) {
        mMessageFinal.clear();
        mMessageFinal.addAll(mMessage);
        messageAdapter = new MessageAdapter(instance, mMessage, imageURl, joinNow, recycler_view_msg,
                MyPrefs.getUserInformation(instance).getUsername(), user_im_chat.getUsername(), chat_id);
        recycler_view_msg.setAdapter(messageAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    public void setRecyclerSwipe(){
        SwipeReply swipeReplyController = new SwipeReply(this, position -> {
            //GET YOUR ADAPTER ITEM ON WHICH THE SWIPE REPLY IS CALLED
            DtoMessage message = MessageAdapter.ViewHolder.GetMessage(position);
            showQuotedMessage(EncryptHelper.decrypt(message.getMessage()), message.getSender());
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeReplyController);
        itemTouchHelper.attachToRecyclerView(recycler_view_msg);
    }

    private void showQuotedMessage(String msg, String from){
        message_to_reply = msg;
        reply_from = from;
        text_send.requestFocus();
        KeyboardUtils.showKeyboard(this);
        txtQuotedMsg.setText(msg);
        reply_layout.setVisibility(View.VISIBLE);
        btn_rec_audio.setVisibility(View.GONE);
        btn_send.setVisibility(View.VISIBLE);
    }

    private void hideReplayLayout() {
        message_to_reply = null;
        reply_from = null;
        if(reply_layout.getVisibility() == ConstraintLayout.VISIBLE)
            KeyboardUtils.hideKeyboard(this);
        reply_layout.setVisibility(View.GONE);
        text_send.requestFocus();

        if(text_send.getText() != null && text_send.getText().toString().length() > 0 ){
            btn_rec_audio.setVisibility(View.GONE);
            btn_send.setVisibility(View.VISIBLE);
        }else{
            if(btn_send.getVisibility() != CardView.GONE)
                btn_send.startAnimation(AnimationUtils.loadAnimation(MessageActivity.this ,R.anim.slide_donw));
            btn_send.setVisibility(View.GONE);
            if(btn_rec_audio.getVisibility() != CardView.VISIBLE)
                btn_rec_audio.startAnimation(AnimationUtils.loadAnimation(MessageActivity.this ,R.anim.slide_up));
            btn_rec_audio.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.see_profile:
                OpenUserProfile();
                return true;
            case R.id.medias_profile:
                ToastHelper.toast(this, getString(R.string.under_development), 0);
                return true;
            case R.id.pin_message:
                ToastHelper.toast(this, getString(R.string.under_development), 0);
                return true;
            case R.id.wallpaper_profile:
                ToastHelper.toast(this, getString(R.string.under_development), 1);
                //BackgroundHelper.OpenGallery();
                return true;
        }
        return false;
    }

    private void OpenUserProfile(){
        finish();
        try {
            Bundle bundle = new Bundle();
            bundle.putString("account_id", EncryptHelper.decrypt(user_im_chat.getAccount_id_cry()));
            bundle.putInt("control", 0);
            MainActivity.getInstance().GetBundleProfile(bundle);
            MainActivity.getInstance().CallProfile();
            ProfileFragment.getInstance().LoadAnotherUser();
        }catch (Exception ex){
            Intent goto_main = new Intent(this, MainActivity.class);
            goto_main.putExtra("shortcut", 0);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.move_to_left_go, R.anim.move_to_right_go);
            ActivityCompat.startActivity(this, goto_main, activityOptionsCompat.toBundle());
            finishAffinity();
            Handler timer = new Handler();
            timer.postDelayed(() -> {
                Bundle bundle_profile = new Bundle();
                bundle_profile.putString("account_id", EncryptHelper.decrypt(user_im_chat.getAccount_id_cry()));
                bundle_profile.putInt("control", 0);
                MainActivity.getInstance().GetBundleProfile(bundle_profile);
                MainActivity.getInstance().CallProfile();
                ProfileFragment.getInstance().LoadAnotherUser();
            },500);
        }
    }

    File file_upload_to_crop;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIC_CROP && data != null) {
            // get the returned data
            Bundle extras = data.getExtras();
            // get the cropped bitmap
            Bitmap selectedBitmap = extras.getParcelable("data");
            BackgroundHelper.uploadFile(selectedBitmap);
        }

        if(requestCode == OPEN_CAMERA) OpenGallery_ACTION();

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
            BackgroundHelper.SendToCrop(this, data);

        if (requestCode == PICK_IMAGE_REQUEST_MEDIA && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            LoadingDialog dialog = new LoadingDialog(this);
            dialog.startLoading();
            try {
                Glide.with(this)
                        .asBitmap()
                        .load(filePath)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                try {
                                    @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
                                    file_upload_to_crop = Methods.SaveImage(MessageActivity.this, resource, chat_id, timeStamp);
                                    dialog.dismissDialog();
                                    UCrop.of(filePath, Uri.fromFile(file_upload_to_crop))
                                            .start(MessageActivity.this);
                                }
                                catch (Exception ex){
                                    dialog.dismissDialog();
                                    Warnings.showWeHaveAProblem(MessageActivity.this);
                                    Log.d(TAG, ex.toString());
                                }
                            }
                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) { }
                        });

            }catch (Exception ex){
                dialog.dismissDialog();
                Warnings.showWeHaveAProblem(MessageActivity.this);
                Log.d(TAG, ex.toString());
            }
        }

        LoadingDialog loadingDialog = new LoadingDialog(this);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            loadingDialog.startLoading();
            final Uri resultUri = UCrop.getOutput(data);
            Upload_Image(loadingDialog, resultUri);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            loadingDialog.dismissDialog();
            if(data != null){
                final Throwable cropError = UCrop.getError(data);
                if(cropError != null)
                Log.d(TAG, cropError.toString());
            }
            Warnings.showWeHaveAProblem(this);
        }
    }

    private void Upload_Image(LoadingDialog loadingDialog, Uri resultUri) {
        try {
            //getting image from gallery
            if(resultUri != null) {

                //uploading the image
                storageReference = ConfFirebase.getFirebaseStorage().child("user").child("chat").child("medias").child(fUser.getUid()).child("chat__"
                        + getFileName(resultUri).replace(" ", "") + "_" + Methods.RandomCharactersWithoutSpecials(3));
                storageReference.putFile(resultUri).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, Objects.requireNonNull(task.getException()).toString());
                    }
                    return storageReference.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        medias_pin = new ArrayList<>();
                        medias_pin.add(downloadUri.toString());
                        sendMessage(fUser.getUid(), userId, text_send.getText().toString());

                    } else {
                        loadingDialog.dismissDialog();
                        Warnings.showWeHaveAProblem(MessageActivity.this);
                        Log.d(TAG, Objects.requireNonNull(task.getException()).toString());
                    }
                });
            }
            else{
                ToastHelper.toast(this, getString(R.string.select_an_image), 0);
                loadingDialog.dismissDialog();
            }
        } catch (Exception ex) {
            loadingDialog.dismissDialog();
            Warnings.showWeHaveAProblem(this);
            Log.d(TAG, ex.toString());
        }
    }

    public String getFileName(@NonNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private void OpenGallery_ACTION() {
        UserPermissions.validatePermissions(permissions, instance, OPEN_CAMERA);
        int GalleryPermission = ContextCompat.checkSelfPermission(instance, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (GalleryPermission == PackageManager.PERMISSION_GRANTED){
            Intent openGallery = new Intent();
            openGallery.setType("image/*");
            openGallery.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(openGallery, "Select Image"), PICK_IMAGE_REQUEST_MEDIA);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Methods.status_chat("online", this);
        currentUser(userId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        Methods.status_chat("offline", this);
        Methods.typingTo_chat_Status("noOne");
        currentUser("none");
    }
}