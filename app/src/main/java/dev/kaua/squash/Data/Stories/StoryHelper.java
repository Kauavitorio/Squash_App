package dev.kaua.squash.Data.Stories;

import java.util.ArrayList;
import java.util.List;

import dev.kaua.squash.Activitys.Story.StoryActivity;

public class StoryHelper extends StoryActivity {
    public static final String TAG = "StoryHelperLOG";
    public static List<String> storyViewsList;

    public static List<String> getStoryViewsList(){
        if(storyViewsList == null) storyViewsList = new ArrayList<>();
        return storyViewsList;
    }
}
