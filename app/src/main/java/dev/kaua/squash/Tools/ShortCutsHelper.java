package dev.kaua.squash.Tools;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.R;

public class ShortCutsHelper extends ContextWrapper {
    public static final String TAG = "ShortCutsHelper";
    public static final String SHORTCUT_TAG = "shortcut";
    public static final String SHORTCUT_IDS = "Shortcut_";
    public static final int NONE_SHORT = 0;
    public static final int SEARCH = 10;
    public static final int NEW_POST = 20;
    public static final int PROFILE = 30;
    private static final List<ShortcutInfo> shortcutInfoList = new ArrayList<>();
    private static final List<String> shortcutInfoIDS = new ArrayList<>();

    public ShortCutsHelper(Context context){
        super(context);

        //  ShortCut To Search Fragment
        ShortcutInfo mShortcutSearch = CreateShortCut(CreateIntent(SEARCH), getString(R.string.search),
                getString(R.string.search), R.drawable.ic_search_shortcut, SEARCH, null);

        //  ShortCut Make a new post
        ShortcutInfo mShortcutCompose = CreateShortCut(CreateIntent(NEW_POST), getString(R.string.new_post),
                getString(R.string.new_post), R.drawable.ic_new_post_shortcut, NEW_POST, null);

        shortcutInfoList.clear();
        shortcutInfoList.addAll(Arrays.asList(mShortcutSearch, mShortcutCompose));

        LoadIds();
    }

    void LoadIds(){
        shortcutInfoIDS.clear();
        for(int i = 0; i < shortcutInfoList.size(); i++){
            if(shortcutInfoList.get(i) != null && shortcutInfoList.get(i).getId() != null)
                shortcutInfoIDS.add(shortcutInfoList.get(i).getId());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public void launchShortcuts() {
        ShortcutManager mShortcutManager = getSystemService(ShortcutManager.class);

        //  ShortCut to go to profile
        Glide.with(this)
                .asBitmap()
                .load(MyPrefs.getUserInformation(getApplicationContext()).getProfile_image())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        ShortcutInfo mShortcutProfile = CreateShortCut(CreateIntent(PROFILE), getString(R.string.view_profile),
                                getString(R.string.view_profile), R.drawable.pumpkin_default_image, PROFILE, resource);
                        shortcutInfoList.add(mShortcutProfile);

                        LoadIds();
                        mShortcutManager.setDynamicShortcuts(shortcutInfoList);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public void removeShorCuts() {
        if(shortcutInfoIDS.size() > 0){
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            shortcutManager.disableShortcuts(shortcutInfoIDS);
            shortcutManager.removeAllDynamicShortcuts();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private ShortcutInfo CreateShortCut(Intent intent, String ShortLabel, String LongLabel, int icon, final int ID, Bitmap
                                        custom_icon) {
        if(custom_icon != null) {
            return new ShortcutInfo.Builder(getApplicationContext(), SHORTCUT_IDS + ID)
                    .setShortLabel(ShortLabel)
                    .setLongLabel(LongLabel)
                    .setIcon(Icon.createWithBitmap(custom_icon))
                    .setIntent(intent)
                    .build();

        }else
            return new ShortcutInfo.Builder(this, SHORTCUT_IDS + ID)
                    .setShortLabel(ShortLabel)
                    .setLongLabel(LongLabel)
                    .setIcon(Icon.createWithResource(this, icon))
                    .setIntent(intent)
                    .build();
    }

    private Intent CreateIntent(int shortCutId){
        return new Intent(this, MainActivity.class)
                .putExtra(SHORTCUT_TAG, shortCutId)
                .putExtra(MainActivity.SHARED_TAG, 0)
                .setAction(Intent.ACTION_VIEW);
    }
}
