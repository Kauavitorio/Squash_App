package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Adapters.Posts_Adapters;
import dev.kaua.squash.Data.Account.AsyncUser_Search;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.MyPrefs;

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("StaticFieldLeak")
public class SearchFragment extends Fragment {
    private static final int PROFILE_TARGET = 0;

    private AutoCompleteTextView edit_search;
    private SwipeRefreshLayout swipe_post_feed;
    private RecyclerView recycler_post_feed;
    private TextView txt_empty_feed;
    private View view;
    private boolean control_visible = false;
    private boolean base_load = true;
    private static DatabaseReference reference_posts;
    private static SearchFragment instance;
    private DtoAccount account;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_activity_search, container, false);
        Ids(view);

        //  Click search item
        edit_search.setOnItemClickListener((parent, view, position, id) -> {
            //noinspection AccessStaticViaInstance
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(requireActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit_search.getWindowToken(), 0);
            DtoAccount info = (DtoAccount) parent.getItemAtPosition(position);
            Bundle bundle = new Bundle();
            bundle.putString("account_id", info.getAccount_id_cry());
            bundle.putInt("control", 0);
            MainActivity.getInstance().GetBundleProfile(bundle);
            MainActivity.getInstance().CallProfile();
            ProfileFragment.getInstance().LoadAnotherUser();
            edit_search.setText(null);
        });

        swipe_post_feed.setOnRefreshListener(this::loadFeed);

        return view;
    }

    public static SearchFragment getInstance(){ return instance;}

    public void LoadSearch() {
        AsyncUser_Search asyncProductsSearchMain = new AsyncUser_Search(edit_search, getActivity());
        //noinspection unchecked
        asyncProductsSearchMain.execute();
    }

    Posts_Adapters posts_adapters = null;
    private void loadFeed() {
        reference_posts = null;
        final ArrayList<DtoPost> arrayListDto = new ArrayList<>();
        if(getContext() != null){
            if(ConnectionHelper.isOnline(getContext())){
                reference_posts = myFirebaseHelper.getFirebaseDatabase().getReference("Posts").child("Published");
                reference_posts.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                        base_load = false;
                        if(control_visible){
                            swipe_post_feed.setRefreshing(true);
                            arrayListDto.clear();
                            for(DataSnapshot snapshot: datasnapshot.getChildren()){
                                DtoPost post = snapshot.getValue(DtoPost.class);
                                if(post != null && getContext() != null){
                                    if(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post.getAccount_id()))) != MyPrefs.getUserInformation(getContext()).getAccount_id()){
                                        post.setPost_id(EncryptHelper.decrypt(post.getPost_id()));
                                        post.setAccount_id(EncryptHelper.decrypt(post.getAccount_id()));
                                        post.setVerification_level(EncryptHelper.decrypt(post.getVerification_level()));
                                        post.setName_user(EncryptHelper.decrypt(post.getName_user()));
                                        post.setUsername(EncryptHelper.decrypt(post.getUsername()));
                                        post.setProfile_image(EncryptHelper.decrypt(post.getProfile_image()));
                                        post.setPost_date(EncryptHelper.decrypt(post.getPost_date()));
                                        post.setPost_time(EncryptHelper.decrypt(post.getPost_time()));
                                        post.setPost_content(EncryptHelper.decrypt(post.getPost_content()));
                                        if(post.getPost_images() != null && post.getPost_images().size() != 0) post.setPost_images(post.getPost_images());
                                        else post.setPost_images(null);
                                        post.setPost_likes(EncryptHelper.decrypt(post.getPost_likes()));
                                        post.setPost_comments_amount(EncryptHelper.decrypt(post.getPost_comments_amount()));
                                        post.setPost_topic(EncryptHelper.decrypt(post.getPost_topic()));
                                        post.setSuggestion(false);
                                        arrayListDto.add(post);
                                    }
                                }
                            }
                            Collections.shuffle(arrayListDto);
                            posts_adapters = new Posts_Adapters(arrayListDto, getContext());
                            if(arrayListDto.size() > 0) posts_adapters.notifyItemRangeChanged(0, arrayListDto.size()-1);
                            if (arrayListDto.size() <= 0){
                                swipe_post_feed.setVisibility(View.GONE);
                                txt_empty_feed.setVisibility(View.VISIBLE);
                            }else{
                                recycler_post_feed.setAdapter(posts_adapters);
                                recycler_post_feed.getRecycledViewPool().clear();
                                swipe_post_feed.setVisibility(View.VISIBLE);
                                txt_empty_feed.setVisibility(View.GONE);
                                new Handler().postDelayed(() -> swipe_post_feed.setRefreshing(false), 500);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        control_visible = visible;
        if(!base_load) loadFeed();
        if (visible){
            if(getContext() != null){
                Toolbar toolbar = view.findViewById(R.id.toolbar_search);
                ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("");
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(false); // Hide default toolbar title
                toolbar.setNavigationOnClickListener(v -> MainActivity.getInstance().LoadMainFragment());
            }
        }
    }

    private void Ids(@NonNull View view) {
        instance = this;
        account = MyPrefs.getUserInformation(requireContext());
        edit_search = view.findViewById(R.id.edit_Search_Main);
        swipe_post_feed = view.findViewById(R.id.swipe_post_feed);
        txt_empty_feed = view.findViewById(R.id.txt_empty_feed);
        recycler_post_feed = view.findViewById(R.id.recycler_post_feed);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recycler_post_feed.setLayoutManager(linearLayoutManager);
        swipe_post_feed.setVisibility(View.GONE);
        txt_empty_feed.setVisibility(View.VISIBLE);
        loadFeed();
        LoadSearch();
    }
}
