package dev.kaua.squash.Adapters.Chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

import dev.kaua.squash.Activities.MessageActivity;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.LocalDataBase.DaoChat;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.UserPermissions;
import dev.kaua.squash.Tools.Warnings;

@SuppressLint("StaticFieldLeak")
public abstract class BackgroundHelper extends MessageActivity {
    private static BottomSheetDialog bottomSheetDialog;
    private static final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE };

    public static void OpenGallery() {
        bottomSheetDialog = new BottomSheetDialog(instance, R.style.BottomSheetTheme);
        bottomSheetDialog.setCancelable(false);
        //  Creating View for SheetMenu
        View sheetView = LayoutInflater.from(instance).inflate(R.layout.adapter_sheet_menu_base,
                instance.findViewById(R.id.sheet_menu_base));
        LinearLayout btn_negative_sheet = sheetView.findViewById(R.id.btn_negative_sheet);
        btn_negative_sheet.setVisibility(View.VISIBLE);
        TextView txt_positive_button_sheet = sheetView.findViewById(R.id.txt_positive_button_sheet);
        txt_positive_button_sheet.setText(instance.getString(R.string.yes));

        //  Set Main Message
        TextView txt_main_text_sheet = sheetView.findViewById(R.id.txt_main_text_sheet);
        txt_main_text_sheet.setText(instance.getString(R.string.msg_background_chat_under_development));

        sheetView.findViewById(R.id.btn_positive_sheet).setOnClickListener(v -> {
            UserPermissions.validatePermissions(permissions, instance, 189);
            int GalleryPermission = ContextCompat.checkSelfPermission(instance, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (GalleryPermission == PackageManager.PERMISSION_GRANTED){
                Intent openGallery = new Intent();
                openGallery.setType("image/*");
                openGallery.setAction(Intent.ACTION_PICK);
                instance.startActivityForResult(Intent.createChooser(openGallery, "Select Image"), PICK_IMAGE_REQUEST);
                bottomSheetDialog.dismiss();
            }
        });

        btn_negative_sheet.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    public static void SendToCrop(Activity context, Intent data) {
        Uri filePath = data.getData();
        try {
            //getting image from gallery
            if(filePath != null) {

                //uploading the image
                storageReference = myFirebaseHelper.getFirebaseStorage().child("user").child("chat").child("background").child("bg_" + fUser.getUid() + "_"
                        + user_im_chat.getId());
                storageReference .putFile(filePath).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("ProfileUpload", Objects.requireNonNull(task.getException()).toString());
                    }
                    return storageReference.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        LoadBackground(downloadUri.toString());
                        DaoChat daoChat = new DaoChat(instance);
                        daoChat.REGISTER_BG("bg_" + fUser.getUid() + "_"
                                + user_im_chat.getId(), downloadUri.toString());

                    } else {
                        ToastHelper.toast(context, task.getException().toString(), 0);
                        Log.d("ProfileUpload", Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
            }
            else
                ToastHelper.toast(context, context.getString(R.string.select_an_image), 0);
        } catch (Exception ex) {
            Warnings.showWeHaveAProblem(context, ErrorHelper.BACKGROUND_HELPER_UPLOAD);
            Log.d("ProfileUpload", ex.toString());
        }
    }

    public static void uploadFile(Bitmap bitmap) {
        LoadingDialog loadingDialog = new LoadingDialog(instance);
        loadingDialog.startLoading();


       /* ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storageReference = myFirebaseHelper.getFirebaseStorage().child("user").child("chat").child("background").child("bg_" + fUser.getUid() + "_"
                + user_im_chat.getId());
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
                        Log.d("DEBUG_CHAT",  imageUrl);
                        LoadBackground(imageUrl);
                        DaoChat daoChat = new DaoChat(instance);
                        daoChat.REGISTER_BG("bg_" + fUser.getUid() + "_"
                                + user_im_chat.getId(), imageUrl);
                    });
                }else
                    loadingDialog.dismissDialog();
            }
        });*/
    }

    public static void LoadBackground(String imageUrl) {
        Log.d("DEBUG_CHAT",  imageUrl);
        Glide.with(instance).load(imageUrl).into(background_chat);
    }

}
