package dev.kaua.squash.Activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Adapters.Chat.MessageAdapter;
import dev.kaua.squash.Adapters.Chat.SwipeReply;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.Notifications.APIService;
import dev.kaua.squash.Notifications.Client;
import dev.kaua.squash.Notifications.Data;
import dev.kaua.squash.Notifications.MyResponse;
import dev.kaua.squash.Notifications.Sender;
import dev.kaua.squash.Notifications.Token;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.KeyboardUtils;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
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

public class MessageActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    private TextView txt_user_name, txt_isOnline_chat, txtQuotedMsg;
    private static RecyclerView recycler_view_msg;
    private EditText text_send;
    private CardView btn_send;
    private String message_to_reply;
    private String reply_from;
    private ConstraintLayout reply_layout;
    ConstraintLayout container_bottom_msg;
    private ImageView cancelButton;
    private ValueEventListener seenListener;
    private DtoAccount user_im_chat;

    FirebaseUser fUser;
    DatabaseReference reference;
    MessageAdapter messageAdapter;
    List<DtoMessage> mMessage;
    String userId;

    APIService apiService;

    boolean notify = false;

    Intent intent;

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

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        intent = getIntent();
        userId = intent.getStringExtra("userId");
        fUser = ConfFirebase.getFirebaseUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        btn_send.setOnClickListener(v -> {
            notify = true;
            String msg = text_send.getText().toString();
            if(!msg.equals("")) sendMessage(fUser.getUid(), userId, msg);
            else ToastHelper.toast(this, getString(R.string.the_message_cannot_be_empty), 0);
            text_send.setText("");
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                user_im_chat = snapshot.getValue(DtoAccount.class);
                if(user_im_chat != null){
                    txt_user_name.setText(user_im_chat.getName_user());
                    if(user_im_chat.getImageURL().equals("default")) profile_image.setImageResource(R.mipmap.ic_launcher);
                    else Picasso.get().load(EncryptHelper.decrypt(user_im_chat.getImageURL())).into(profile_image);
                    readMessage(fUser.getUid(), userId, user_im_chat.getImageURL());

                    //  check typing status
                    if(user_im_chat.getTypingTo().equals(fUser.getUid())){
                        txt_isOnline_chat.setVisibility(View.VISIBLE);
                        txt_isOnline_chat.setText(getString(R.string.typing));
                    }
                    else{
                        Animation Anim = AnimationUtils.loadAnimation(MessageActivity.this, R.anim.slide_donw);
                        if(user_im_chat.getStatus_chat().equals("online")){
                            txt_isOnline_chat.setVisibility(View.VISIBLE);
                            txt_isOnline_chat.setText(getString(R.string.online));
                        }
                        else txt_isOnline_chat.setText(Methods.loadLastSeen(MessageActivity.this, user_im_chat.getLast_seen()));
                    }
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

        seenMessage(userId);
        setRecyclerSwipe();

    }

    private void Ids() {
        profile_image = findViewById(R.id.profile_image_chat);
        txt_user_name = findViewById(R.id.txt_username_chat);
        recycler_view_msg = findViewById(R.id.recycler_view_msg);
        txt_isOnline_chat = findViewById(R.id.txt_isOnline_chat);
        reply_layout = findViewById(R.id.reply_layout);
        container_bottom_msg = findViewById(R.id.container_bottom_msg);
        txtQuotedMsg = findViewById(R.id.txtQuotedMsg);
        cancelButton = findViewById(R.id.cancelButton);
        //noinspection deprecation
        container_bottom_msg.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        text_send = findViewById(R.id.text_send);
        btn_send = findViewById(R.id.container_btn_send);
        btn_send.setElevation(0);
        recycler_view_msg.setHasFixedSize(true);
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
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("HH:mm a");
        String formattedDate = df_time.format(c.getTime());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
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

        reference.child("Chats").push().setValue(hashMap);

        //  add User to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatslist")
                .child(fUser.getUid())
                .child(userId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
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
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        hideReplayLayout();
    }

    private void currentUser(String userId){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
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
                    Data data = new Data(fUser.getUid(), R.mipmap.ic_launcher, username+": "+ message, getString(R.string.new_message), userId);

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
    private void readMessage(String myID, String userId, String imageURl){
        mMessage = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference().child("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                mMessage.clear();
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    DtoMessage message = snapshot.getValue(DtoMessage.class);
                    if(message != null)
                        if(message.getReceiver() != null)
                            if (message.getReceiver().equals(myID) && message.getSender().equals(userId)
                                    || message.getReceiver().equals(userId) && message.getSender().equals(myID)){
                                mMessage.add(message);
                    }
                }
                messageAdapter = new MessageAdapter(MessageActivity.this, mMessage, imageURl, joinNow, recycler_view_msg, MainActivity.getInstance().getUserInformation().getUsername(), user_im_chat.getUsername());
                if(joinNow <= 100) joinNow++;
                else joinNow = 1;
                recycler_view_msg.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
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
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()){
            case R.id.pin_message:
                Methods.PinAUser_Chat(MessageActivity.this, userId);
                return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Methods.status_chat("online");
        currentUser(userId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        Methods.status_chat("offline");
        Methods.typingTo_chat_Status("noOne");
        currentUser("none");
    }
}