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

import dev.kaua.squash.Activitys.EditProfileActivity;
import dev.kaua.squash.Firebase.ConfFirebase;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Warnings;

public class Profile_Image extends EditProfileActivity {

    public static void SendToCrop(Activity context, Intent data) {
        Uri filePath = data.getData();
        try {
            //getting image from gallery
            if(filePath != null) {
                try {
                    Intent cropIntent = new Intent("com.android.camera.action.CROP");
                    // indicate image type and Uri
                    cropIntent.setDataAndType(filePath, "image/*");
                    // set crop properties here
                    cropIntent.putExtra("crop", true);
                    // indicate aspect of desired crop
                    cropIntent.putExtra("aspectX", 1);
                    cropIntent.putExtra("aspectY", 1);
                    // indicate output X and Y
                    cropIntent.putExtra("outputX", 158);
                    cropIntent.putExtra("outputY", 158);
                    // retrieve data on return
                    cropIntent.putExtra("return-data", true);
                    // start the activity - we handle returning in onActivityResult
                    context.startActivityForResult(cropIntent, PIC_CROP);
                }
                // respond to users whose devices do not support the crop action
                catch (ActivityNotFoundException ex) {
                    // display an error message
                    String errorMessage = "Whoops - your device doesn't support the crop action!";
                    Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        } catch (Exception ex) {
            Warnings.showWeHaveAProblem(context);
            Log.d("DEBUG_CHAT", ex.toString());
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
