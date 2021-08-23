package dev.kaua.squash.Fragments.Chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.database.FirebaseDatabase;
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
import dev.kaua.squash.Fragments.ChatFragment;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.MyPrefs;

@SuppressLint("StaticFieldLeak")
public class UsersOutstandingFragment extends Fragment {
    private static RecyclerView recycler_view_users;
    private static DaoFollowing daoFollowing;
    private static DtoAccount myAccount;
    private static Context instance;
    private DaoChat chatDB;

    private static UserChatAdapter userChatAdapter;
    private static List<DtoAccount> mAccounts = new ArrayList<>();;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_outstanding, container, false);
        Ids(view);

        return view;
    }

    private void Ids(View view) {
        if(getContext() != null) instance = getContext();
        chatDB = new DaoChat(requireContext());
        daoFollowing = new DaoFollowing(requireContext());
        myAccount = MyPrefs.getUserInformation(requireContext());
        recycler_view_users = view.findViewById(R.id.recycler_view_users_outstanding);
        recycler_view_users.setHasFixedSize(true);
        recycler_view_users.setItemViewCacheSize(20);
        recycler_view_users.setDrawingCacheEnabled(true);
        recycler_view_users.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recycler_view_users.setLayoutManager(new LinearLayoutManager(requireContext()));
        LoadAdapter();
    }

    final static List<DtoAccount> finalList = new ArrayList<>();
    void LoadAdapter(){
        if(getContext() != null){
            if(chatDB != null && mAccounts != null && recycler_view_users != null){
                mAccounts.clear();
                finalList.clear();
                mAccounts = chatDB.get_CHAT_LIST();
                for(DtoAccount account: mAccounts){
                    if(account.getAccount_id_cry() == null){
                        finalList.add(account);
                    }
                }
                userChatAdapter = new UserChatAdapter(getContext(), finalList, true, false);
                ((SimpleItemAnimator) Objects.requireNonNull(recycler_view_users.getItemAnimator())).setSupportsChangeAnimations(false);
                recycler_view_users.setAdapter(userChatAdapter);
            }
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if(visible)
            LoadAdapter();
    }
}