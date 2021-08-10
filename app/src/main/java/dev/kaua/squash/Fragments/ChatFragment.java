package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Activitys.SettingActivity;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Message.DtoMessage;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.Chat.ChatsFragment;
import dev.kaua.squash.Fragments.Chat.UsersFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.Login;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;

@SuppressLint("StaticFieldLeak")
public class ChatFragment extends Fragment {
    DtoAccount account_info;
    private View view;
    private static ChatFragment instance;
    CircleImageView profile_image;
    TextView txt_username_chat;
    private TabLayout tab_layout_chat;
    private ViewPager view_paper_chat;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    ViewPaperAdapter viewPaperAdapter;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_activity_chat, container, false);
        Ids(view);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("");


        firebaseUser = myFirebaseHelper.getFirebaseUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        viewPaperAdapter = new ViewPaperAdapter(requireActivity().getSupportFragmentManager());

        return view;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible)
            if(getContext() != null){
                txt_username_chat.setText(MyPrefs.getUserInformation(getContext()).getName_user());
                Picasso.get().load(MyPrefs.getUserInformation(getContext()).getProfile_image()).into(profile_image);
            }
    }

    private void loadViewAdapter() {

        if(getContext() != null){
            if(ConnectionHelper.isOnline(getContext())){
                reference = FirebaseDatabase.getInstance().getReference("Chats");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(getActivity() != null){
                            viewPaperAdapter = new ViewPaperAdapter(requireActivity().getSupportFragmentManager());
                            int unread = 0;
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                DtoMessage message = snapshot1.getValue(DtoMessage.class);
                                if(message != null)
                                    if(message.getReceiver() != null)
                                        if(message.getReceiver().equals(firebaseUser.getUid()) && message.getIsSeen() == 0)
                                            unread++;
                            }

                            if(unread == 0)
                                viewPaperAdapter.addFragment(new ChatsFragment(), getString(R.string.chats));
                            else
                                viewPaperAdapter.addFragment(new ChatsFragment(), "(" + unread + ") " + getString(R.string.chats));

                            viewPaperAdapter.addFragment(new UsersFragment(), getString(R.string.following));
                            view_paper_chat.setAdapter(viewPaperAdapter);
                            tab_layout_chat.setupWithViewPager(view_paper_chat);
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

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
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
            case R.id.setting_chat:
                Intent i = new Intent(requireActivity(), SettingActivity.class);
                startActivity(i);
                return true;
            case R.id.logout:
                Login.LogOut(requireContext(), 0);
                return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void Ids(@NonNull View view) {
        account_info = MyPrefs.getUserInformation(requireContext());
        instance = this;
        txt_username_chat = view.findViewById(R.id.txt_username_chat);
        profile_image = view.findViewById(R.id.profile_image_chat);
        tab_layout_chat = view.findViewById(R.id.tab_layout_chat);
        view_paper_chat = view.findViewById(R.id.view_paper_chat);
        loadViewAdapter();

        if(getContext() != null){
            txt_username_chat.setText(MyPrefs.getUserInformation(getContext()).getName_user());
            Picasso.get().load(MyPrefs.getUserInformation(getContext()).getProfile_image()).into(profile_image);
        }
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
