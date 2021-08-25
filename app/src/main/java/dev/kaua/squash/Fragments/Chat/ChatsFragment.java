package dev.kaua.squash.Fragments.Chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.kaua.squash.Adapters.Chat.UserChatAdapter;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.Chatslist;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.Notifications.Token;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;

public class ChatsFragment extends Fragment {
    private RecyclerView recycler_myMsg;
    private TextView txt_not_start_conversation;
    private UserChatAdapter userChatAdapter;
    private List<DtoAccount> First_List_Accounts;
    private EditText search_users;
    private DaoChat chatDB;
    private static ChatsFragment instance;

    FirebaseUser fUser;
    DatabaseReference reference;

    private final List<Chatslist> ChatList_List = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        Ids(view);
        fUser = myFirebaseHelper.getFirebaseUser();

        chatList();
        if(ConnectionHelper.isOnline(requireContext())){
            reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.CHAT_LIST_REFERENCE).child(fUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                    ChatList_List.clear();
                    for(DataSnapshot snapshot : datasnapshot.getChildren()){
                        Chatslist chatList = snapshot.getValue(Chatslist.class);
                        if(chatList != null && chatList.getChat_id() != null)
                            ChatList_List.add(chatList);
                    }
                    chatList();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {}
            });

            myFirebaseHelper.getFirebaseMessaging().getToken().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    String token = task.getResult();
                    updateToken(token);
                }
            });
        }

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
        Query query = myFirebaseHelper.getFirebaseDatabase().getReference("Users").orderByChild("search")
                .startAt(str)
                .endAt(str + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                First_List_Accounts.clear();
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    DtoAccount account = snapshot.getValue(DtoAccount.class);
                    assert account != null;
                    if(!account.getId().equals(fUser.getUid())){
                        for (int i = 0; i < ChatList_List.size(); i++){
                            if(ChatList_List.get(i).getId().equals(account.getId())){
                                account.setChat_id(ChatList_List.get(i).getChat_id());
                                First_List_Accounts.add(account);
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
        txt_not_start_conversation = view.findViewById(R.id.txt_not_start_conversation);
        recycler_myMsg.setHasFixedSize(true);
        recycler_myMsg.setItemViewCacheSize(20);
        ((SimpleItemAnimator) Objects.requireNonNull(recycler_myMsg.getItemAnimator())).setSupportsChangeAnimations(false);
        recycler_myMsg.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void updateToken(String token){
        DatabaseReference reference1 = myFirebaseHelper.getFirebaseDatabase().getReference("Tokens");
        Token token1 = new Token(token);
        reference1.child(fUser.getUid()).setValue(token1);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }

    public static ChatsFragment getInstance(){ return instance; }

    ArrayList<DtoAccount> mAccountsBase = new ArrayList<>();
    boolean reload = true, base_reload = true, can_load = true;
    public void chatList() {
        LoadChatRecycler();

        if(getContext() != null)
            if(ConnectionHelper.isOnline(getContext())){
                First_List_Accounts = new ArrayList<>();
                reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                        First_List_Accounts.clear();
                        can_load = false;
                        for(DataSnapshot snapshot : datasnapshot.getChildren()){
                            DtoAccount account = snapshot.getValue(DtoAccount.class);
                            if(account != null && account.getId() != null && account.getName_user() != null && account.getName_user().length() > 0){
                                for(Chatslist chatList : ChatList_List){
                                    if(account.getId().equals(chatList.getId())){
                                        account.setChat_id(chatList.getChat_id());
                                        First_List_Accounts.add(account);
                                    }
                                }
                            }
                        }

                        if(mAccountsBase.size() != First_List_Accounts.size()) {
                            mAccountsBase.clear();
                            mAccountsBase.addAll(First_List_Accounts);

                            chatDB.REGISTER_CHAT_LIST(mAccountsBase);
                            new Handler().postDelayed(() -> LoadChatRecycler(), 200);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                });
            }
    }

    final static List<DtoAccount> finalList = new ArrayList<>();
    public static int Outstanding;
    private static Parcelable recyclerViewState;
    public void LoadChatRecycler() {
        if(chatDB != null && First_List_Accounts != null && recycler_myMsg != null){
            if(recycler_myMsg.getLayoutManager() != null) recyclerViewState = recycler_myMsg.getLayoutManager().onSaveInstanceState();
            First_List_Accounts.clear();
            finalList.clear();
            Outstanding = 0;
            First_List_Accounts = chatDB.get_CHAT_LIST();
            for(DtoAccount account: First_List_Accounts){
                if(account.getId() != null && account.getActive() > DtoAccount.ACCOUNT_DISABLE){
                    finalList.add(account);
                }else Outstanding++;
            }
            if(finalList.size() > 0){
                recycler_myMsg.setVisibility(View.VISIBLE);
                txt_not_start_conversation.setVisibility(View.GONE);
            }else{
                recycler_myMsg.setVisibility(View.GONE);
                txt_not_start_conversation.setVisibility(View.VISIBLE);
            }
            userChatAdapter = new UserChatAdapter(getActivity(), finalList, false);
            if(recycler_myMsg.getItemAnimator() != null) ((SimpleItemAnimator) recycler_myMsg.getItemAnimator()).setSupportsChangeAnimations(false);
            recycler_myMsg.setAdapter(userChatAdapter);
            mAccountsBase.clear();
            mAccountsBase.addAll(First_List_Accounts);
            reload = false;
            if(recyclerViewState != null && recycler_myMsg.getLayoutManager() != null) recycler_myMsg.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }
    }

}