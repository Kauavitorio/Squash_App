package dev.kaua.squash.Fragments;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;


public class FragmentPageAdapter extends FragmentPagerAdapter  {

    public FragmentPageAdapter(FragmentManager fm) {
        super(fm);
    }


    ChatFragment chatFragment;
    MainFragment mainFragment;
    SearchFragment searchFragment;
    ProfileFragment profileFragment;
    
    @Override
    public @NotNull Fragment getItem(int position)
    {
        if (position == 0){
            if(chatFragment == null)
                chatFragment = new ChatFragment();
            return chatFragment;
        }

        else if (position == 1){
            if(mainFragment == null)
                mainFragment = new MainFragment();
            return mainFragment;
        }

        else if(position == 2){
            if(searchFragment == null)
                searchFragment = new SearchFragment();
            return searchFragment;
        }

        else if(position == 3){
            if(profileFragment == null)
                profileFragment = new ProfileFragment();
            return profileFragment;
        }
        else{
            if(profileFragment == null)
                profileFragment = new ProfileFragment();
            return profileFragment;
        }
    }

    @Override
    public int getCount()
    {
        return 4;
    }
}