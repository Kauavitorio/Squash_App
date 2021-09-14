package dev.kaua.squash.Tools;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class UserPermissions {
    private static final String TAG = "PermissionsRequest";

    public static void validatePermissions(@NonNull String[] permissions, Activity activity, int requestCode){
        ArrayList<String> PermissionList = new ArrayList<>();

        //  Checks permissions already granted
        for (String permission : permissions) {
            final boolean HavePermission = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
            if (!HavePermission)
                PermissionList.add(permission);
        }
        Log.d(TAG, "Request Permission List -> " + PermissionList.toString());
        if (!PermissionList.isEmpty()){
            String[] PermissionsVector = new String[PermissionList.size()];
            PermissionList.toArray(PermissionsVector);
            ActivityCompat.requestPermissions(activity, PermissionsVector, requestCode);
        }
    }
}
