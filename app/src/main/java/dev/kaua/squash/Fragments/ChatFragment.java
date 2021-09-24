package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.Setting.SettingActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.Chat.ChatsFragment;
import dev.kaua.squash.Fragments.Chat.UsersFragment;
import dev.kaua.squash.Fragments.Chat.UsersOutstandingFragment;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;

@SuppressLint("StaticFieldLeak")
public class ChatFragment extends Fragment {
    DtoAccount account_info;
    private View view;
    private static ChatFragment instance;
    private static Activity instance_activity;
    CircleImageView profile_image;
    TextView txt_username_chat;
    private TabLayout tab_layout_chat;
    private ViewPager view_paper_chat;
    private static DaoChat chatDB;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    static ViewPaperAdapter viewPaperAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_activity_chat, container, false);
        Ids(view);

        firebaseUser = myFirebaseHelper.getFirebaseUser();
        reference = myFirebaseHelper.getFirebaseDatabase().getReference("Users").child(firebaseUser.getUid());
        viewPaperAdapter = new ViewPaperAdapter(requireActivity().getSupportFragmentManager());

        return view;
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible){
            if(getContext() != null){
                Toolbar toolbar = view.findViewById(R.id.toolbar_chat);
                ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("");
                txt_username_chat.setText(MyPrefs.getUserInformation(getContext()).getName_user());
                Picasso.get().load(MyPrefs.getUserInformation(getContext()).getProfile_image()).into(profile_image);
            }
        }
    }

    static int unread = 0;
    static int Outstanding = 0;
    static int Outstanding_base = 0;
    static boolean base_load = true;
    public void loadViewAdapter() {

        if(getContext() != null){
            if(ConnectionHelper.isOnline(getContext())){
                reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.CHATS_REFERENCE);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(getActivity() != null){
                            if(!getActivity().isFinishing() && !getActivity().isDestroyed()){
                                LoadLocalMessageOutstanding();

                                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                    DtoMessage message = snapshot1.getValue(DtoMessage.class);
                                    if(message != null)
                                        if(message.getReceiver() != null)
                                            if(message.getReceiver().equals(firebaseUser.getUid()) && message.getIsSeen() == 0)
                                                unread++;
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                });
            }else{
                viewPaperAdapter = new ViewPaperAdapter(requireActivity().getSupportFragmentManager());
                viewPaperAdapter.addFragment(new ChatsFragment(), getString(R.string.chats));
                viewPaperAdapter.addFragment(new UsersFragment(), getString(R.string.following));
                view_paper_chat.setAdapter(viewPaperAdapter);
                tab_layout_chat.setupWithViewPager(view_paper_chat);
            }
        }
    }

    public void LoadLocalMessageOutstanding() {
        Outstanding = 0;
        List<DtoAccount> mAccounts = chatDB.get_CHAT_LIST();
        for(DtoAccount account: mAccounts){
            if(account.getAccount_id_cry() == null)
                Outstanding++;
        }
        if(Outstanding_base != Outstanding || base_load){
            base_load = false;
            viewPaperAdapter = new ViewPaperAdapter(requireActivity().getSupportFragmentManager());
            if(unread == 0)
                viewPaperAdapter.addFragment(new ChatsFragment(), instance_activity.getString(R.string.chats));
            else
                viewPaperAdapter.addFragment(new ChatsFragment(), "(" + unread + ") " + instance_activity.getString(R.string.chats));

            viewPaperAdapter.addFragment(new UsersFragment(), instance_activity.getString(R.string.following));

            if(Outstanding > 0) viewPaperAdapter.addFragment(new UsersOutstandingFragment(), "(" + Outstanding + ") " + instance_activity.getString(R.string.outstanding));
            view_paper_chat.setAdapter(viewPaperAdapter);
            tab_layout_chat.setupWithViewPager(view_paper_chat);
        }
    }

    public static ChatFragment getInstance(){ return instance; }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        requireActivity().getMenuInflater().inflate(R.menu.menu_chat, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.account_chat:
                MainActivity.getInstance().CallProfile();
                return true;
            case R.id.starred_messages_chat:
                ToastHelper.toast(requireActivity(), getString(R.string.under_development), 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void Ids(@NonNull View view) {
        account_info = MyPrefs.getUserInformation(requireContext());
        instance = this;
        chatDB = new DaoChat(requireContext());
        if(getActivity() != null) instance_activity = requireActivity();
        txt_username_chat = view.findViewById(R.id.txt_username_chat);
        profile_image = view.findViewById(R.id.profile_image_chat);
        tab_layout_chat = view.findViewById(R.id.tab_layout_chat);
        view_paper_chat = view.findViewById(R.id.view_paper_chat);
        loadViewAdapter();

        if(getContext() != null){
            txt_username_chat.setText(MyPrefs.getUserInformation(getContext()).getName_user());
            Picasso.get().load(MyPrefs.getUserInformation(getContext()).getProfile_image()).into(profile_image);
        }
        viewPaperAdapter = new ViewPaperAdapter(requireActivity().getSupportFragmentManager());
        viewPaperAdapter.addFragment(new ChatsFragment(), getString(R.string.chats));
        viewPaperAdapter.addFragment(new UsersFragment(), getString(R.string.following));
        view_paper_chat.setAdapter(viewPaperAdapter);
        tab_layout_chat.setupWithViewPager(view_paper_chat);
    }

    static class ViewPaperAdapter extends FragmentPageAdapter{

        private final ArrayList<Fragment> fragments;
        private final ArrayList<String> titles;

        ViewPaperAdapter(FragmentManager fm){
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public @NotNull Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
