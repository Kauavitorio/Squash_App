package dev.kaua.squash.Adapters.Profile;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import dev.kaua.squash.Activitys.ComposeActivity;
import dev.kaua.squash.Activitys.EditProfileActivity;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;

public class Profile_Image extends EditProfileActivity {

    public static void SendToCrop(Activity context, Intent data) {
        LoadingDialog loadingDialog = new LoadingDialog(context);
        loadingDialog.startLoading();
        Uri filePath = data.getData();
        try {
            //getting image from gallery
            if(filePath != null) {

                //uploading the image
                storageReference = ConfFirebase.getFirebaseStorage().child("user").child("profile").child("User_" + user.getAccount_id() +
                        "_" + ConfFirebase.getFirebaseAuth().getUid());
                storageReference.putFile(filePath).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("MediaUpload", Objects.requireNonNull(task.getException()).toString());
                    }
                    return storageReference.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        loadingDialog.dismissDialog();
                        String imageUrl = downloadUri.toString();
                        new_image = imageUrl;
                        Log.d("DEBUG_CHAT",  imageUrl);
                        Picasso.get().load(imageUrl).into(ic_edit_ProfileUser);
                    } else {
                        loadingDialog.dismissDialog();
                        Warnings.showWeHaveAProblem(context);
                        Log.d("MediaUpload", Objects.requireNonNull(task.getException()).toString());
                    }
                });
            }
            else{
                ToastHelper.toast(context, context.getString(R.string.select_an_image), 0);
                loadingDialog.dismissDialog();
            }
        } catch (Exception ex) {
            loadingDialog.dismissDialog();
            Warnings.showWeHaveAProblem(context);
            Log.d("MediaUpload", ex.toString());
        }
    }

    public static void uploadFile(Bitmap bitmap) {
        LoadingDialog loadingDialog = new LoadingDialog(instance);
        loadingDialog.startLoading();

       ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        storageReference = ConfFirebase.getFirebaseStorage().child("user").child("profile").child("User_" + user.getAccount_id() +
                "_" + ConfFirebase.getFirebaseAuth().getUid());
        storageReference.putBytes(data).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                loadingDialog.dismissDialog();
                Log.d("DEBUG_CHAT", Objects.requireNonNull(task.getException()).toString());
            }
            if (task.getResult().getMetadata() != null) {
                if (task.getResult().getMetadata().getReference() != null) {
                    Task<Uri> result = task.getResult().getStorage().getDownloadUrl();
                    result.addOnSuccessListener(uri -> {
                        loadingDialog.dismissDialog();
                        String imageUrl = uri.toString();
                        new_image = imageUrl;
                        Log.d("DEBUG_CHAT",  imageUrl);
                        Picasso.get().load(imageUrl).into(ic_edit_ProfileUser);
                    });
                }else
                    loadingDialog.dismissDialog();
            }
        });
    }

}
