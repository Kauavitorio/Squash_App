package dev.kaua.squash.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import dev.kaua.squash.Adapters.Chat.UserChatAdapter;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.Chatslist;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.R;

public class ShareContentActivity extends AppCompatActivity {
    private RecyclerView recycler_view_users;
    private EditText search_users;
    private static ShareContentActivity instance;
    private int ShareType;
    private Object ShareContent;
    private DaoChat chatDB;

    FirebaseUser fUser;
    DatabaseReference reference;

    private UserChatAdapter userChatAdapter;
    private List<DtoAccount> mAccounts;

    private List<Chatslist> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_content);
        Ids();
        mAccounts = new ArrayList<>();
        usersList = new ArrayList<>();
        fUser = myFirebaseHelper.getFirebaseUser();

        reference = FirebaseDatabase.getInstance().getReference("Chatslist").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                usersList.clear();
                for(DataSnapshot snapshot : datasnapshot.getChildren()){
                    Chatslist chatList = snapshot.getValue(Chatslist.class);
                    usersList.add(chatList);
                }
                readAccounts();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });

        Bundle bundle = getIntent().getExtras();
        ShareType = bundle.getInt("shared_type");
        ShareContent = bundle.get("shared_content");

        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    public static ShareContentActivity getInstance(){ return instance; }

    public int GetShareType(){
        return ShareType;
    }

    public Object GetShareContent(){
        return ShareContent;
    }

    private void searchUsers(String str) {
        FirebaseUser fUser = myFirebaseHelper.getFirebaseUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(str)
                .endAt(str + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                mAccounts.clear();
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    DtoAccount account = snapshot.getValue(DtoAccount.class);
                    assert account != null;
                    if(!account.getId().equals(fUser.getUid())){
                        for (int i = 0; i < usersList.size(); i++){
                            if(usersList.get(i).getId().equals(account.getId())){
                                account.setChat_id(usersList.get(i).getChat_id());
                                mAccounts.add(account);
                            }
                        }
                    }
                }
                userChatAdapter = new UserChatAdapter(ShareContentActivity.this, mAccounts, false, UserChatAdapter.OFF_CHATS);
                recycler_view_users.setAdapter(userChatAdapter);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private void readAccounts() {
        mAccounts = chatDB.get_CHAT_LIST();
        userChatAdapter = new UserChatAdapter(ShareContentActivity.this, mAccounts, true, UserChatAdapter.OFF_CHATS);
        recycler_view_users.setAdapter(userChatAdapter);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot fullSnapshot) {
                if(search_users.getText().toString().equals("")){
                    mAccounts.clear();
                    for(DataSnapshot snapshot : fullSnapshot.getChildren()){
                        DtoAccount account = snapshot.getValue(DtoAccount.class);
                        if(account != null){
                            for(Chatslist chatList : usersList){
                                if(account.getId() != null && account.getId().equals(chatList.getId())){
                                    account.setChat_id(chatList.getChat_id());
                                    mAccounts.add(account);
                                }
                            }
                        }
                    }

                    userChatAdapter = new UserChatAdapter(ShareContentActivity.this, mAccounts, true, UserChatAdapter.OFF_CHATS);
                    recycler_view_users.setAdapter(userChatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private void Ids() {
        instance = this;
        chatDB = new DaoChat(this);
        recycler_view_users = findViewById(R.id.recycler_view_users_share);
        search_users = findViewById(R.id.search_users_share);
        recycler_view_users.setHasFixedSize(true);
        recycler_view_users.setItemViewCacheSize(20);
        recycler_view_users.setDrawingCacheEnabled(true);
        recycler_view_users.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recycler_view_users.setLayoutManager(new LinearLayoutManager(this));
    }
}