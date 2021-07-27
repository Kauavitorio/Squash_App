package dev.kaua.squash.Activitys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Adapters.Chat.BackgroundHelper;
import dev.kaua.squash.Adapters.Chat.MessageAdapter;
import dev.kaua.squash.Adapters.Chat.SwipeReply;
import dev.kaua.squash.Data.Account.DtoAccount;
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

@SuppressWarnings({"deprecation", "StaticFieldLeak", "FieldCanBeLocal"})
public class MessageActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    public static ImageView background_chat;
    public static ImageView btn_more_medias;
    public static MessageActivity instance;
    private TextView txt_user_name, txt_isOnline_chat, txtQuotedMsg;
    private static RecyclerView recycler_view_msg;
    private EditText text_send;
    private CardView btn_send;
    private String message_to_reply;
    private ImageView verification_ic;
    private String reply_from;
    private ConstraintLayout reply_layout;
    private ImageView cancelButton;
    private ValueEventListener seenListener;
    public static DtoAccount user_im_chat;
    public static DtoAccount mUser = new DtoAccount();
    private static final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE };

    public static FirebaseUser fUser;

    private DaoChat chatDB;
    private DatabaseReference reference;
    private MessageAdapter messageAdapter;
    private List<DtoMessage> mMessage;
    private List<String> medias_pin = new ArrayList<>();
    private String userId;
    String another_user_image = "";

    APIService apiService;

    boolean notify = false;
    public static final int PIC_CROP = 1;
    public static final int PICK_IMAGE_REQUEST = 111;
    public static final int PICK_IMAGE_REQUEST_MEDIA = 222;
    public static StorageReference storageReference;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Ids();
        DaoChat daoChat = new DaoChat(MessageActivity.this);
        chatDB = new DaoChat(MessageActivity.this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        intent = getIntent();
        userId = intent.getStringExtra("userId");
        fUser = ConfFirebase.getFirebaseUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        CheckShared();

        String img = daoChat.get_BG("bg_" + fUser.getUid() + "_"
                + userId);
        if(img != null) BackgroundHelper.LoadBackground(img);

        //  Send msg click
        btn_send.setOnClickListener(v -> {
            notify = true;
            String msg = text_send.getText().toString();
            if(!msg.equals("")) sendMessage(fUser.getUid(), userId, msg);
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
                    readMessage(fUser.getUid(), userId, user_im_chat.getImageURL());

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
                if(s.toString().trim().length() == 0) Methods.typingTo_chat_Status("noOne");
                else Methods.typingTo_chat_Status(userId);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        cancelButton.setOnClickListener(v -> hideReplayLayout());

        btn_more_medias.setOnClickListener(v -> {
            UserPermissions.validatePermissions(permissions, instance, 189);
            int GalleryPermission = ContextCompat.checkSelfPermission(instance, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (GalleryPermission == PackageManager.PERMISSION_GRANTED){
                Intent openGallery = new Intent();
                openGallery.setType("image/*");
                openGallery.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(openGallery, "Select Image"), PICK_IMAGE_REQUEST_MEDIA);
            }
        });

        seenMessage(userId);
        setRecyclerSwipe();
    }

    private void CheckShared() {
        Bundle bundle = getIntent().getExtras();
        if(bundle.getInt("shared") == 1){
            if(bundle.getInt("shared_type") == 1)
                text_send.setText(bundle.getString("shared_content"));
        }
    }

    private void Ids() {
        instance = this;
        profile_image = findViewById(R.id.profile_image_chat);
        btn_more_medias = findViewById(R.id.btn_more_medias);
        txt_user_name = findViewById(R.id.txt_username_chat);
        recycler_view_msg = findViewById(R.id.recycler_view_msg);
        verification_ic = findViewById(R.id.verification_ic_message);
        txt_isOnline_chat = findViewById(R.id.txt_isOnline_chat);
        reply_layout = findViewById(R.id.reply_layout);
        background_chat = findViewById(R.id.background_chat);
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

        reference.child("Chats").push().setValue(hashMap);
        medias_pin.clear();
        text_send.setText("");

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
                if(!snapshot.exists())
                    chatRef.child("id").setValue(userId);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
        chatRefANOTHER_USER.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                    chatRefANOTHER_USER.child("id").setValue(fUser.getUid());
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });

        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                DtoAccount account = snapshot.getValue(DtoAccount.class);
                if(account != null)
                if(notify) sendNotification(receiver, account.getUsername(), msg);
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });

        try {
            ChatsFragment.getInstance().chatList();
        }catch (Exception exception){
            Log.d("Message", "Cant load list");
        }

        hideReplayLayout();
    }

    private void currentUser(String userId){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getSharedPreferences( MyPrefs.PREFS_NOTIFICATION, MODE_PRIVATE).edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }

    private void sendNotification(String receiver, String username, String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fUser.getUid(), R.drawable.pumpkin_default_image, username+": "+ message, getString(R.string.new_message), userId);

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {
                            if(response.code() == 200){
                                assert response.body() != null;
                                if(response.body().success != 1)
                                    Log.w("Send Message Notification", "Failed");
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
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    int joinNow = 0;
    boolean base_load = true;
    List<DtoMessage> mMessageFinal = new ArrayList<>();
    private void readMessage(String myID, String userId, String imageURl){
        Calendar c = Calendar.getInstance();
        mMessage = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference().child("Chats");
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
                        Log.d("Chat", "OKAY not need to reset");
                        if(joinNow <= 100) joinNow++;
                        else joinNow = 1;
                    } else joinNow = 0;

                    if(mMessageFinal.size() > 0 && !mMessageFinal.get(mMessageFinal.size() -1).getMessage().equals(mMessage.get(mMessage.size() -1).getMessage())
                            || mMessageFinal.size() > 0 && mMessageFinal.get(mMessageFinal.size() -1).getIsSeen() != mMessage.get(mMessage.size() -1).getIsSeen()){
                        Log.d("Chat", "OKAY NEW MSG OR IS SEEN");
                        LoadAdapter(imageURl);
                        base_load = false;

                        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time_last_chat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        user_im_chat.setLast_chat(df_time_last_chat.format(c.getTime()));
                        chatDB.UPDATE_A_CHAT(user_im_chat, 1);
                    }

                    if(base_load) LoadAdapter(imageURl);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private void LoadAdapter(String imageURl) {
        mMessageFinal.clear();
        mMessageFinal.addAll(mMessage);
        messageAdapter = new MessageAdapter(MessageActivity.this, mMessage, imageURl, joinNow, recycler_view_msg, getUserInformation().getUsername(), user_im_chat.getUsername());
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
    }

    private void hideReplayLayout() {
        message_to_reply = null;
        reply_from = null;
        if(reply_layout.getVisibility() == ConstraintLayout.VISIBLE)
            KeyboardUtils.hideKeyboard(this);
        reply_layout.setVisibility(View.GONE);
        text_send.requestFocus();
    }

    @SuppressWarnings("ConstantConditions")
    public DtoAccount getUserInformation(){
        SharedPreferences sp = getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);
        mUser.setAccount_id(Long.parseLong(EncryptHelper.decrypt(sp.getString("pref_account_id", null))));
        mUser.setName_user(EncryptHelper.decrypt(sp.getString("pref_name_user", null)));
        mUser.setUsername(EncryptHelper.decrypt(sp.getString("pref_username", null)));
        mUser.setEmail(EncryptHelper.decrypt(sp.getString("pref_email", null)));
        mUser.setPhone_user(EncryptHelper.decrypt(sp.getString("pref_phone_user", null)));
        mUser.setBanner_user(EncryptHelper.decrypt(sp.getString("pref_banner_user", null)));
        mUser.setPhone_user(EncryptHelper.decrypt(sp.getString("pref_phone_user", null)));
        mUser.setProfile_image(EncryptHelper.decrypt(sp.getString("pref_profile_image", null)));
        mUser.setBio_user(EncryptHelper.decrypt(sp.getString("pref_bio_user", null)));
        mUser.setUrl_user(EncryptHelper.decrypt(sp.getString("pref_url_user", null)));
        mUser.setFollowing(EncryptHelper.decrypt(sp.getString("pref_following", null)));
        mUser.setFollowers(EncryptHelper.decrypt(sp.getString("pref_followers", null)));
        mUser.setBorn_date(EncryptHelper.decrypt(sp.getString("pref_born_date", null)));
        mUser.setJoined_date(EncryptHelper.decrypt(sp.getString("pref_joined_date", null)));
        mUser.setPassword(EncryptHelper.decrypt(sp.getString("pref_password", null)));
        mUser.setToken(EncryptHelper.decrypt(sp.getString("pref_token", null)));
        mUser.setVerification_level(EncryptHelper.decrypt(sp.getString("pref_verification_level", null)));
        return mUser;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.see_profile:
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
                return true;
            case R.id.medias_profile:
                return true;
            case R.id.pin_message:
                Methods.PinAUser_Chat(MessageActivity.this, userId);
                return true;
            case R.id.wallpaper_profile:
                BackgroundHelper.OpenGallery();
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIC_CROP) {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                Bitmap selectedBitmap = extras.getParcelable("data");
                BackgroundHelper.uploadFile(selectedBitmap);
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
            BackgroundHelper.SendToCrop(this, data);

        if (requestCode == PICK_IMAGE_REQUEST_MEDIA && resultCode == RESULT_OK && data != null && data.getData() != null) {
            LoadingDialog loadingDialog = new LoadingDialog(this);
            loadingDialog.startLoading();
            Uri filePath = data.getData();
            try {
                //getting image from gallery
                if(filePath != null) {

                    //uploading the image
                    storageReference = ConfFirebase.getFirebaseStorage().child("user").child("chat").child("medias").child(fUser.getUid()).child("chat__"
                            + getFileName(filePath).replace(" ", "") + "_" + Methods.RandomCharactersWithoutSpecials(3));
                    storageReference.putFile(filePath).continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            Log.d("MediaUpload", Objects.requireNonNull(task.getException()).toString());
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
                            Log.d("MediaUpload", Objects.requireNonNull(task.getException()).toString());
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
                Log.d("MediaUpload", ex.toString());
            }
        }
    }

    public String getFileName(Uri uri) {
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