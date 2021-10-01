package dev.kaua.squash.Adapters.User;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class DownloadIdGenerator {
    private static long id = 0;

    @NonNull
    @Contract(pure = true)
    public static String getCurrentId(){
        if(String.valueOf(id).length() < 4)
            return "ID0" + id;
        else return "ID" + id;
    };

    @NonNull
    public static String generateNewId(){
        id++;
        if(String.valueOf(id).length() < 4)
            return "ID0" + id;
        else return "ID" + id;
    }
}
