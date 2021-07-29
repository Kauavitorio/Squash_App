package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Data.Account.AsyncUser_Search;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.AsyncRecommended_Posts_feed;
import dev.kaua.squash.R;
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

    private void loadFeed() {
        AsyncRecommended_Posts_feed async = new AsyncRecommended_Posts_feed(requireContext(), recycler_post_feed, swipe_post_feed, txt_empty_feed);
        async.execute();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) LoadSearch();
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
        loadFeed();
    }
}
