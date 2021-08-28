package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Adapters.Posts_Adapters;
import dev.kaua.squash.Adapters.SearchItemArrayAdapter;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
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
    private long size = 0;
    private static DatabaseReference reference_posts;
    private static SearchFragment instance;
    private static DaoFollowing daoFollowing;
    private DtoAccount account;
    private FirebaseUser fUser;

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
            bundle.putString("account_id", EncryptHelper.decrypt(info.getAccount_id_cry()));
            bundle.putInt("control", 0);
            MainActivity.getInstance().GetBundleProfile(bundle);
            MainActivity.getInstance().CallProfile();
            ProfileFragment.getInstance().LoadAnotherUser();
            edit_search.setText(null);
        });

        swipe_post_feed.setOnRefreshListener(this::Apply_ArrayList);

        return view;
    }

    public static SearchFragment getInstance(){ return instance;}

    final List<DtoAccount> mAccounts = new ArrayList<>();
    public void LoadSearch() {
        DatabaseReference ref = myFirebaseHelper.getFirebaseDatabase().getReference().child(myFirebaseHelper.USERS_REFERENCE);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                mAccounts.clear();
                for(DataSnapshot snapshot: datasnapshot.getChildren()){
                    DtoAccount account = snapshot.getValue(DtoAccount.class);
                    if(account != null && fUser != null)
                        if(account.getId() != null && !account.getId().equals(fUser.getUid())
                                && account.getActive() > DtoAccount.ACCOUNT_DISABLE && account.getAccount_id_cry() != null)
                            mAccounts.add(account);
                }
                Apply_ArrayListUsers();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    final List<DtoAccount> FinalAccounts = new ArrayList<>();
    @SuppressLint("ClickableViewAccessibility")
    private void Apply_ArrayListUsers() {
        if(mAccounts.size() > 0){
            Log.d("SearchUser", "Request To Load");
            if(FinalAccounts.size() != mAccounts.size()){
                if(getActivity() != null && !getActivity().isDestroyed() && !getActivity().isFinishing()){
                    Log.d("SearchUser", "Loaded");
                    FinalAccounts.clear();
                    FinalAccounts.addAll(mAccounts);
                    SearchItemArrayAdapter adapter = new SearchItemArrayAdapter(getContext(), R.layout.adapter_search_layout, R.id.txt_user_name_search, FinalAccounts);

                    //edit_search.setDropDownBackgroundDrawable(context.getDrawable(R.drawable.custom_edit_register_new));
                    edit_search.setAdapter(adapter);
                    edit_search.setOnTouchListener((v, event) -> {
                        edit_search.showDropDown();
                        edit_search.requestFocus();
                        return false;
                    });
                }
            }
        }
    }

    final ArrayList<DtoPost> arrayListDto = new ArrayList<>();
    Posts_Adapters posts_adapters = null;
    private void loadFeed() {
        reference_posts = null;
        if(getContext() != null){
            daoFollowing = new DaoFollowing(getContext());
            if(ConnectionHelper.isOnline(getContext())){
                reference_posts = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD);
                reference_posts.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                        arrayListDto.clear();
                        for(DataSnapshot snapshot: datasnapshot.getChildren()){
                            DtoPost post = snapshot.getValue(DtoPost.class);
                            if(post != null && getContext() != null && post.getAccount_id() != null){
                                if(Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post.getAccount_id())))
                                        != MyPrefs.getUserInformation(getContext()).getAccount_id()){
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
                        if(size != arrayListDto.size())
                            swipe_post_feed.setRefreshing(true);
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
        if (visible){
            if(getContext() != null){
                Toolbar toolbar = view.findViewById(R.id.toolbar_search);
                ((AppCompatActivity)requireActivity()).setSupportActionBar(toolbar);
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("");
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(false); // Hide default toolbar title
                toolbar.setNavigationOnClickListener(v -> MainActivity.getInstance().LoadMainFragment());

                Apply_ArrayList();
                Apply_ArrayListUsers();
            }
        }
    }

    final List<DtoPost> finalFeedList = new ArrayList<>();
    int currentSize = 0;
    void Apply_ArrayList() {
        Log.d("FeedNotFollow", "Request -> " + arrayListDto.size());
        if(arrayListDto.size() != finalFeedList.size() && getContext() != null && getActivity() != null){
            finalFeedList.clear();
            for (DtoPost post: arrayListDto){
                if(!daoFollowing.check_if_follow(MyPrefs.getUserInformation(getContext()).getAccount_id(),
                        Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(post.getAccount_id())))))
                    finalFeedList.add(post);
            }
            if(currentSize != finalFeedList.size() && !getActivity().isFinishing() && !getActivity().isDestroyed()){
                currentSize = finalFeedList.size();
                Log.d("FeedNotFollow", "Loaded -> " + currentSize);
                posts_adapters = new Posts_Adapters((ArrayList<DtoPost>) finalFeedList, getActivity(), Posts_Adapters.CAN_NOT_ANIME);
                if(finalFeedList.size() > 0) posts_adapters.notifyItemRangeChanged(0, finalFeedList.size()-1);
                if (finalFeedList.size() <= 0){
                    swipe_post_feed.setVisibility(View.GONE);
                    txt_empty_feed.setVisibility(View.VISIBLE);
                }else{
                    size = finalFeedList.size();
                    recycler_post_feed.setAdapter(posts_adapters);
                    recycler_post_feed.getRecycledViewPool().clear();
                    swipe_post_feed.setVisibility(View.VISIBLE);
                    txt_empty_feed.setVisibility(View.GONE);
                }
            }
            swipe_post_feed.setRefreshing(false);
        }
    }

    private void Ids(@NonNull View view) {
        instance = this;
        fUser = myFirebaseHelper.getFirebaseUser();
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
