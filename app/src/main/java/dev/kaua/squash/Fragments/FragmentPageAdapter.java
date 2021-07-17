package dev.kaua.squash.Fragments;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


public class FragmentPageAdapter extends FragmentPagerAdapter {

    public FragmentPageAdapter(
            FragmentManager fm)
    {
        super(fm);
    }


    @Override
    public Fragment getItem(int position)
    {
        if (position == 0)
            return new ChatFragment();

        else if (position == 1)
            return new MainFragment();

        else if(position == 2)
            return new SearchFragment();

        else if(position == 3)
            return new ProfileFragment();
        else
            return new MainFragment();
    }

    @Override
    public int getCount()
    {
        return 4;
    }
}