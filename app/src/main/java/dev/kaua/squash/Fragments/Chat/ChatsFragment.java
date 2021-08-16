package dev.kaua.squash.Fragments.Chat;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.kaua.squash.Adapters.Chat.UserChatAdapter;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.Chatslist;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.ChatFragment;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.Notifications.Token;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;

public class ChatsFragment extends Fragment {
    private RecyclerView recycler_myMsg;
    private UserChatAdapter userChatAdapter;
    private List<DtoAccount> mAccounts;
    private EditText search_users;
    private DaoChat chatDB;
    private static ChatsFragment instance;

    FirebaseUser fUser;
    DatabaseReference reference;

    private List<Chatslist> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        Ids(view);

        fUser = myFirebaseHelper.getFirebaseUser();
        usersList = new ArrayList<>();

        chatList();
        reference = FirebaseDatabase.getInstance().getReference("Chatslist").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                usersList.clear();
                for(DataSnapshot snapshot : datasnapshot.getChildren()){
                    Chatslist chatList = snapshot.getValue(Chatslist.class);
                    usersList.add(chatList);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token = task.getResult();
                updateToken(token);
            }
        });

        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s != null && s.length() > 0)
                searchUsers(s.toString().toLowerCase());
                else{
                    base_reload = true;
                    chatList();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
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
                LoadChatRecycler();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {}
        });
    }

    private void Ids(View view) {
        instance = this;
        chatDB = new DaoChat(requireContext());
        recycler_myMsg = view.findViewById(R.id.recycler_myMsg);
        search_users = view.findViewById(R.id.search_users);
        recycler_myMsg.setHasFixedSize(true);
        recycler_myMsg.setItemViewCacheSize(20);
        ((SimpleItemAnimator) Objects.requireNonNull(recycler_myMsg.getItemAnimator())).setSupportsChangeAnimations(false);
        recycler_myMsg.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void updateToken(String token){
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference1.child(fUser.getUid()).setValue(token1);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if(visible)
            LoadChatRecycler();
    }

    public static ChatsFragment getInstance(){ return instance; }

    ArrayList<DtoAccount> mAccountsBase = new ArrayList<>();
    boolean reload = true, base_reload = true, can_load = true;
    public void chatList() {
        LoadChatRecycler();

        if(getContext() != null)
            if(ConnectionHelper.isOnline(getContext())){
                mAccounts = new ArrayList<>();
                reference = myFirebaseHelper.getFirebaseDatabase().getReference("Users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                        mAccounts.clear();
                        can_load = false;
                        for(DataSnapshot snapshot : datasnapshot.getChildren()){
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

                        if(mAccountsBase.size() != mAccounts.size()) mAccountsBase.addAll(mAccounts);

                        //  Check if recyclerView need to be updated
                        try {
                            for (int i = 0; i < mAccounts.size(); i++){
                                if(mAccountsBase != null && mAccountsBase.get(i) != null)
                                    if(!mAccountsBase.get(i).getStatus_chat().equals(mAccounts.get(i).getStatus_chat()) ||
                                            !mAccountsBase.get(i).getName_user().equals(mAccounts.get(i).getName_user()) ||
                                            !mAccountsBase.get(i).getSearch().equals(mAccounts.get(i).getSearch()) ||
                                            !mAccountsBase.get(i).getImageURL().equals(mAccounts.get(i).getImageURL()) ||
                                            mAccountsBase.get(i).getVerification_level() != null && !mAccountsBase.get(i).getVerification_level().equals(mAccounts.get(i).getVerification_level())){
                                        reload = true;
                                    }
                            }
                        }catch (Exception ex){
                            Log.d("CHAT", ex.toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                });

                if(reload){
                    chatDB.REGISTER_CHAT_LIST(mAccounts);
                    new Handler().postDelayed(() -> {
                        if(ChatFragment.getInstance() != null) ChatFragment.getInstance().loadViewAdapter();
                    }, 1000);
                    LoadChatRecycler();
                }

                if(base_reload){
                    chatDB.REGISTER_CHAT_LIST(mAccounts);
                    LoadChatRecycler();
                    base_reload = false;
                }
            }
    }

    final static List<DtoAccount> finalList = new ArrayList<>();
    public static int Outstanding;
    public void LoadChatRecycler() {
        if(chatDB != null && mAccounts != null && recycler_myMsg != null){
            mAccounts.clear();
            finalList.clear();
            Outstanding = 0;
            mAccounts = chatDB.get_CHAT_LIST();
            for(DtoAccount account: mAccounts){
                if(account.getAccount_id_cry() != null){
                    finalList.add(account);
                }else Outstanding++;
            }
            userChatAdapter = new UserChatAdapter(getContext(), finalList, true, false);
            ((SimpleItemAnimator) Objects.requireNonNull(recycler_myMsg.getItemAnimator())).setSupportsChangeAnimations(false);
            recycler_myMsg.setAdapter(userChatAdapter);
            mAccountsBase.clear();
            mAccountsBase.addAll(mAccounts);
            reload = false;

        }
    }

}