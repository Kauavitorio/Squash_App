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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.kaua.squash.Adapters.Chat.UserChatAdapter;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.R;

public class UsersFragment extends Fragment {
    private RecyclerView recycler_view_users;
    private EditText search_users;

    private UserChatAdapter userChatAdapter;
    private List<DtoAccount> mAccounts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        Ids(view);

        mAccounts = new ArrayList<>();
        readAccounts();

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

        return view;
    }

    private void Ids(View view) {
        recycler_view_users = view.findViewById(R.id.recycler_view_users);
        search_users = view.findViewById(R.id.search_users);
        recycler_view_users.setHasFixedSize(true);
        recycler_view_users.setItemViewCacheSize(20);
        recycler_view_users.setDrawingCacheEnabled(true);
        recycler_view_users.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recycler_view_users.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                        mAccounts.add(account);
                    }
                }
                userChatAdapter = new UserChatAdapter(getContext(), mAccounts, true, false);
                recycler_view_users.setAdapter(userChatAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void readAccounts() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("status_chat");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                if(search_users.getText().toString().equals("")){
                    mAccounts.clear();
                    for(DataSnapshot snapshot: datasnapshot.getChildren()){
                        DtoAccount account = snapshot.getValue(DtoAccount.class);
                        assert account != null;
                        assert firebaseUser != null;
                        if(account.getId() != null && !account.getId().equals(firebaseUser.getUid())) mAccounts.add(account);
                    }
                    Collections.reverse(mAccounts);
                    userChatAdapter = new UserChatAdapter(getContext(), mAccounts, true, false);
                    recycler_view_users.setAdapter(userChatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}