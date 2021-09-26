package dev.kaua.squash.Fragments.Chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.kaua.squash.Adapters.Chat.UserChatAdapter;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoAccount;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.MyPrefs;

@SuppressLint("StaticFieldLeak")
public class UsersFragment extends Fragment {
    private static RecyclerView recycler_view_users;
    private static TextView no_user_fount_user_chat;
    private static DaoFollowing daoFollowing;
    private static DtoAccount myAccount;
    private static Activity instance;
    private static  FirebaseUser fUser;

    private static UserChatAdapter userChatAdapter;
    private final static List<DtoAccount> mAccounts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        Ids(view);
        readAccounts();

        return view;
    }

    private void Ids(View view) {
        fUser = myFirebaseHelper.getFirebaseUser();
        if(getActivity() != null) {
            instance = getActivity();
            db = new DaoAccount(instance);
        }
        daoFollowing = new DaoFollowing(requireContext());
        myAccount = MyPrefs.getUserInformation(requireContext());
        recycler_view_users = view.findViewById(R.id.recycler_view_users_following);
        no_user_fount_user_chat = view.findViewById(R.id.no_user_fount_user_chat);
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
    static DaoAccount db;
    static void LoadAdapter(){
        if(mAccounts.size() > 0 && instance != null && db != null){
            DtoAccount account_follow = db.get_followers_following(MyPrefs.getUserInformation(instance).getAccount_id());
            if(Long.parseLong(account_follow.getFollowing()) != finalList.size()){
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

                // FIXME: 9/16/2021 - Following list without users
                //  Without this the list doesn't appear to user
                //   The adapter is working normally with chat list but no here
                DtoAccount a = new DtoAccount();
                a.setEmail("AAAAAA"); // .-.
                finalList.add(a); // I really don't know why

                userChatAdapter = new UserChatAdapter(instance, finalList, false, UserChatAdapter.OFF_CHATS);
                recycler_view_users.setAdapter(userChatAdapter);
            }
            if(finalList.size() > 0)
                no_user_fount_user_chat.setVisibility(View.GONE);
            else no_user_fount_user_chat.setVisibility(View.VISIBLE);
        }
        if(finalList.size() > 0)
            no_user_fount_user_chat.setVisibility(View.GONE);
        else no_user_fount_user_chat.setVisibility(View.VISIBLE);

        if(!ConnectionHelper.isOnline(instance)) no_user_fount_user_chat.setText(instance.getString(R.string.you_are_without_internet));
        else no_user_fount_user_chat.setText(instance.getString(R.string.no_user_found));
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
                Query query = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE).orderByChild("last_seen");

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                        if(!instance.isFinishing() && !instance.isDestroyed()){
                            mAccounts.clear();
                            for(DataSnapshot snapshot: datasnapshot.getChildren()){
                                DtoAccount account = snapshot.getValue(DtoAccount.class);
                                if(account != null && fUser != null){
                                    if(account.getId() != null && !account.getId().equals(fUser.getUid())
                                    && account.getActive() > DtoAccount.ACCOUNT_DISABLE)
                                        mAccounts.add(account);
                                }
                            }
                            //Collections.reverse(mAccounts);
                            LoadAdapter();
                        }
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