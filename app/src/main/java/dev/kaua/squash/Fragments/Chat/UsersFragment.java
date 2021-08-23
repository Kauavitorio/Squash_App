package dev.kaua.squash.Fragments.Chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.kaua.squash.Adapters.Chat.UserChatAdapter;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.MyPrefs;

@SuppressLint("StaticFieldLeak")
public class UsersFragment extends Fragment {
    private static RecyclerView recycler_view_users;
    private static SwipeRefreshLayout swipe;
    private static TextView no_user_fount_user_chat;
    private EditText search_users;
    private static DaoFollowing daoFollowing;
    private static DtoAccount myAccount;
    private static Context instance;
    private static  FirebaseUser fUser;

    private static UserChatAdapter userChatAdapter;
    private final static List<DtoAccount> mAccounts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        Ids(view);
        readAccounts();

        //  Search an user
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

        swipe.setOnRefreshListener(this::readAccounts);

        return view;
    }

    private void Ids(View view) {
        fUser = myFirebaseHelper.getFirebaseUser();
        if(getContext() != null) instance = getContext();
        daoFollowing = new DaoFollowing(requireContext());
        myAccount = MyPrefs.getUserInformation(requireContext());
        recycler_view_users = view.findViewById(R.id.recycler_view_users);
        no_user_fount_user_chat = view.findViewById(R.id.no_user_fount_user_chat);
        swipe = view.findViewById(R.id.swipe_following_chat_user);
        search_users = view.findViewById(R.id.search_users);
        recycler_view_users.setHasFixedSize(true);
        recycler_view_users.setItemViewCacheSize(20);
        recycler_view_users.setDrawingCacheEnabled(true);
        recycler_view_users.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recycler_view_users.setLayoutManager(new LinearLayoutManager(requireContext()));
        if(finalList.size() > 0)
            no_user_fount_user_chat.setVisibility(View.GONE);
        else no_user_fount_user_chat.setVisibility(View.VISIBLE);
    }

    private void searchUsers(String str) {
        if(getContext() != null){
            if(ConnectionHelper.isOnline(getContext())){
                Query query = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE).orderByChild("search")
                        .startAt(str)
                        .endAt(str + "\uf8ff");

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                        mAccounts.clear();
                        for (DataSnapshot snapshot : datasnapshot.getChildren()){
                            DtoAccount account = snapshot.getValue(DtoAccount.class);
                            if(account != null)
                                mAccounts.add(account);
                        }
                        LoadAdapter();
                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                });
            }
        }
    }

    final static List<DtoAccount> finalList = new ArrayList<>();
    static void LoadAdapter(){
        if(mAccounts.size() > 0){
            finalList.clear();
            for (DtoAccount accounts : mAccounts){
                if(accounts != null && accounts.getAccount_id_cry() != null && accounts.getActive() > DtoAccount.ACCOUNT_DISABLE)
                if(!accounts.getId().equals(fUser.getUid())
                        && daoFollowing.check_if_follow(myAccount.getAccount_id(),
                        Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(accounts.getAccount_id_cry()))))
                && accounts.getActive() > DtoAccount.ACCOUNT_DISABLE){
                    finalList.add(accounts);
                }
            }
            userChatAdapter = new UserChatAdapter(instance, finalList, true, false);
            recycler_view_users.setAdapter(userChatAdapter);
            if(finalList.size() > 0)
                no_user_fount_user_chat.setVisibility(View.GONE);
            else no_user_fount_user_chat.setVisibility(View.VISIBLE);
        }
        if(finalList.size() > 0)
            no_user_fount_user_chat.setVisibility(View.GONE);
        else no_user_fount_user_chat.setVisibility(View.VISIBLE);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if(visible)
            LoadAdapter();
    }

    private void readAccounts() {
        if(getContext() != null){
            if(ConnectionHelper.isOnline(getContext())){
                Query query = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE).orderByChild("status_chat");

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                        if(search_users.getText().toString().equals("")){
                            mAccounts.clear();
                            for(DataSnapshot snapshot: datasnapshot.getChildren()){
                                DtoAccount account = snapshot.getValue(DtoAccount.class);
                                if(account != null && fUser != null){
                                    if(account.getId() != null && !account.getId().equals(fUser.getUid())
                                            && daoFollowing.check_if_follow(myAccount.getAccount_id(), Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(account.getAccount_id_cry())))))
                                        mAccounts.add(account);
                                }
                            }
                            Collections.reverse(mAccounts);
                            LoadAdapter();
                        }
                        swipe.setRefreshing(false);
                        if(finalList.size() > 0)
                            no_user_fount_user_chat.setVisibility(View.GONE);
                        else no_user_fount_user_chat.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                });
            }
        }
    }
}