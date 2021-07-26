package dev.kaua.squash.Adapters.Profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;


import androidx.core.graphics.BitmapCompat;

import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import dev.kaua.squash.Activitys.EditProfileActivity;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.CompressImage;
import dev.kaua.squash.Tools.LoadingDialog;
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

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), filePath);
                Log.d("UPDATE_PROFILE_IMAGE", "Default size -> " + BitmapCompat.getAllocationByteCount(bitmap));
                ic_edit_ProfileUser.setCircleBackgroundColor(context.getColor(R.color.black));
                ic_edit_ProfileUser.setImageBitmap(bitmap);
                ic_edit_ProfileUser.setDrawingCacheEnabled(true);
                ic_edit_ProfileUser.buildDrawingCache();
                Bitmap bitmapUpload = ic_edit_ProfileUser.getDrawingCache();
                Bitmap bitMapSendUpload = CompressImage.compressImageFromBitmap(bitmapUpload, bitmap.getWidth(), bitmap.getHeight());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitMapSendUpload.compress(Bitmap.CompressFormat.PNG, 100, baos);
                Log.d("UPDATE_PROFILE_IMAGE", "Resize size -> " + BitmapCompat.getAllocationByteCount(bitMapSendUpload));
                byte[] dataUpload = baos.toByteArray();
                ic_edit_ProfileUser.setCircleBackgroundColor(context.getColor(R.color.base_color));
                storageReference = ConfFirebase.getFirebaseStorage().child("user").child("profile").child("User_" + user.getAccount_id() +
                        "_" + ConfFirebase.getFirebaseAuth().getUid());
                storageReference.putBytes(dataUpload).addOnCompleteListener(task -> {
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
