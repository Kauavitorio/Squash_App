package dev.kaua.squash.Tools;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class UserPermissions {

    public static void validatePermissions(String[] permissions, Activity activity, int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ArrayList<String> PermissionList = new ArrayList<>();

            //  Checks permissions already granted
            for (String permission : permissions) {
                boolean HavePermission = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
                if (!HavePermission){
                    PermissionList.add(permission);
                }
            }
            if (!PermissionList.isEmpty()){
                String[] PermissionsVector = new String[PermissionList.size()];
                PermissionList.toArray(PermissionsVector);
                ActivityCompat.requestPermissions(activity, PermissionsVector, requestCode);
            }
        }
    }
}
