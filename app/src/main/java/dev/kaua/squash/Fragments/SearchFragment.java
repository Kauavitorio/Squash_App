package dev.kaua.squash.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Data.Account.AsyncUser_Search;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("StaticFieldLeak")
public class SearchFragment extends Fragment {
    private static final int PROFILE_TARGET = 0;

    private AutoCompleteTextView edit_search;
    private View view;
    private static SearchFragment instance;
    private DtoAccount account;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_activity_search, container, false);
        Ids(view);

        LoadSearch();

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

        return view;
    }

    public static SearchFragment getInstance(){ return instance;}

    public void LoadSearch() {
        AsyncUser_Search asyncProductsSearchMain = new AsyncUser_Search(edit_search, getActivity());
        //noinspection unchecked
        asyncProductsSearchMain.execute();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) LoadSearch();
    }

    @Override
    public void onResume() {
        super.onResume();
        LoadSearch();
    }

    private void Ids(View view) {
        instance = this;
        account = MainActivity.getInstance().getUserInformation();
        edit_search = view.findViewById(R.id.edit_Search_Main);
    }
}
