package dev.kaua.squash.Fragments.Chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.Notifications.Token;
import dev.kaua.squash.R;

public class ChatsFragment extends Fragment {
    private RecyclerView recycler_myMsg;
    private UserChatAdapter userChatAdapter;
    private List<DtoAccount> mAccounts;
    private EditText search_users;

    FirebaseUser fUser;
    DatabaseReference reference;

    private List<Chatslist> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        Ids(view);

        fUser = ConfFirebase.getFirebaseUser();
        usersList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatslist").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                usersList.clear();
                for(DataSnapshot snapshot : datasnapshot.getChildren()){
                    Chatslist chatlist = snapshot.getValue(Chatslist.class);
                    usersList.add(chatlist);
                }

                chatList();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
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
        FirebaseUser fUser = ConfFirebase.getFirebaseUser();
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
                                mAccounts.add(account);
                            }
                        }
                    }
                }
                userChatAdapter = new UserChatAdapter(getContext(), mAccounts, true, false);
                recycler_myMsg.setAdapter(userChatAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void Ids(View view) {

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

    ArrayList<DtoAccount> mAccountsBase = new ArrayList<>();
    boolean reload = true, base_reload = true;
    private void chatList() {
        mAccounts = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                mAccounts.clear();
                for(DataSnapshot snapshot : datasnapshot.getChildren()){
                    DtoAccount account = snapshot.getValue(DtoAccount.class);
                    if(account != null){
                        for(Chatslist chatList : usersList){
                            if(account.getId() != null && account.getId().equals(chatList.getId())){
                                mAccounts.add(account);
                            }
                        }
                    }
                }

                if(mAccountsBase.size() != mAccounts.size()) mAccountsBase.addAll(mAccounts);
                for (int i = 0; i < mAccounts.size(); i++){
                    if(!mAccountsBase.get(i).getStatus_chat().equals(mAccounts.get(i).getStatus_chat())){
                        reload = true;
                    }
                }
                if(reload){
                    userChatAdapter = new UserChatAdapter(getContext(), mAccounts, true, false);
                    ((SimpleItemAnimator) Objects.requireNonNull(recycler_myMsg.getItemAnimator())).setSupportsChangeAnimations(false);
                    recycler_myMsg.setAdapter(userChatAdapter);
                    mAccountsBase.clear();
                    mAccountsBase.addAll(mAccounts);
                    reload = false;
                }

                if(base_reload){
                    userChatAdapter = new UserChatAdapter(getContext(), mAccounts, true, false);
                    ((SimpleItemAnimator) Objects.requireNonNull(recycler_myMsg.getItemAnimator())).setSupportsChangeAnimations(false);
                    recycler_myMsg.setAdapter(userChatAdapter);
                    mAccountsBase.clear();
                    mAccountsBase.addAll(mAccounts);
                    base_reload = false;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

}