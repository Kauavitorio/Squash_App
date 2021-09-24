package dev.kaua.squash.Activitys.Setting.AccountSetting.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import dev.kaua.squash.Fragments.FragmentPageAdapter;
import dev.kaua.squash.R;

@SuppressLint("StaticFieldLeak")
public class YourActivityFragment extends Fragment {
    private static View view;
    TabLayout tab_layout;
    ViewPager viewPaper;
    static ViewPaperAdapter viewPaperAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_your_activity_account_setting, container, false);
        Ids();

        return view;
    }

    private void Ids() {
        tab_layout = view.findViewById(R.id.tab_layout_your_activity);
        viewPaper = view.findViewById(R.id.viewPaper_your_activity);
        viewPaperAdapter = new ViewPaperAdapter(getChildFragmentManager());
        viewPaperAdapter.addFragment(new Links_YourActivityFragment(), getString(R.string.links));
        //viewPaperAdapter.addFragment(new ActivityTimeFragment(), getString(R.string.time));
        viewPaper.setOffscreenPageLimit(2);
        viewPaper.setAdapter(viewPaperAdapter);
        tab_layout.setupWithViewPager(viewPaper);

    }

    static class ViewPaperAdapter extends FragmentPageAdapter {

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
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

}
