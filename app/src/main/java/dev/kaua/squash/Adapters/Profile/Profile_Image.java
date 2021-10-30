package dev.kaua.squash.Adapters.Profile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import dev.kaua.squash.Activities.Medias.ViewMediaActivity;
import dev.kaua.squash.Activities.Profile.EditProfileActivity;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class Profile_Image extends EditProfileActivity {
    private static Dialog dialog;

    public static void SendToCrop(Activity mContext, Uri filePath) {
        final LoadingDialog loadingDialog = new LoadingDialog(mContext);
        loadingDialog.startLoading();
        try {
            //getting image from gallery
            if(filePath != null) {
                final String mId = myFirebaseHelper.getFirebaseAuth().getUid();
                if(mId != null){
                    ic_edit_ProfileUser.setCircleBackgroundColor(mContext.getColor(R.color.black));

                    final Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), filePath);
                    int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
                    ic_edit_ProfileUser.setImageBitmap(scaled);
                    ic_edit_ProfileUser.setDrawingCacheEnabled(true);
                    ic_edit_ProfileUser.buildDrawingCache();

                    ByteArrayOutputStream aOutStream = new ByteArrayOutputStream();
                    scaled.compress(Bitmap.CompressFormat.PNG, 100, aOutStream);
                    byte[] dataUpload = aOutStream.toByteArray();

                    ic_edit_ProfileUser.setCircleBackgroundColor(mContext.getColor(R.color.base_color));
                    storageReference = myFirebaseHelper.getFirebaseStorage()
                            .child(myFirebaseHelper.PROFILE_REFERENCE).child(mId);
                    storageReference.putBytes(dataUpload).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            loadingDialog.dismissDialog();
                            Log.d("DEBUG_CHAT", Objects.requireNonNull(task.getException()).toString());
                        }
                        if (task.getResult().getMetadata() != null) {
                            if (task.getResult().getMetadata().getReference() != null) {
                                final Task<Uri> result = task.getResult().getStorage().getDownloadUrl();
                                result.addOnSuccessListener(uri -> {
                                    loadingDialog.dismissDialog();
                                    new_image = uri.toString();
                                    Log.d("DEBUG_CHAT",  new_image);

                                    if(!MyPrefs.getUserInformation(mContext).getProfile_image()
                                            .contains(myFirebaseHelper.USERS_REFERENCE)){

                                        MyPrefs.updateProfileImage(mContext, EncryptHelper.encrypt(new_image));

                                        Glide.with(mContext).load(new_image)
                                                .signature(new ObjectKey(MyPrefs.getUserInformation(mContext).getProfile_image()))
                                                .into(ic_edit_ProfileUser);

                                        new Handler().postDelayed(() -> {
                                            try {
                                                ProfileFragment.getInstance().GetUserInfo(mContext);
                                                Warnings.showProfilePicUpdated(mContext);
                                            }catch (Exception ignore){}
                                        }, 350);
                                    }else{
                                        Glide.with(mContext).load(new_image)
                                                .signature(new ObjectKey(MyPrefs.getUserInformation(mContext).getProfile_image()))
                                                .into(ic_edit_ProfileUser);
                                    }
                                });
                            }else
                                loadingDialog.dismissDialog();
                        }
                    });
                }else Warnings.showWeHaveAProblem(mContext, ErrorHelper.PROFILE_NO_ID);
            }
            else{
                ToastHelper.toast(mContext, mContext.getString(R.string.select_an_image), ToastHelper.SHORT_DURATION);
                loadingDialog.dismissDialog();
            }
        } catch (Exception ex) {
            loadingDialog.dismissDialog();
            Warnings.showWeHaveAProblem(mContext, ErrorHelper.PROFILE_IMAGE_UPLOAD);
            Log.d("MediaUpload", ex.toString());
        }
    }

    public static void showUserProfile(final Activity mContext, String imageUrl, final String userName){
        try {
            dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.adapter_user_image);

            final ImageView imageUser = dialog.findViewById(R.id.user_image_adapter);
            final TextView txt_name = dialog.findViewById(R.id.txt_userName_Image_adapter);
            txt_name.setText(userName);
            Glide.with(mContext).load(imageUrl).into(imageUser);

            imageUser.setOnClickListener(v -> {
                if(ConnectionHelper.isOnline(mContext)){
                    imageUser.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.click_anim));
                    Intent intent = new Intent(mContext, ViewMediaActivity.class);
                    intent.putExtra(ViewMediaActivity.IMAGE_URL_TAG, EncryptHelper.decrypt(imageUrl));
                    intent.putExtra(ViewMediaActivity.RECEIVE_TIME_TAG, ViewMediaActivity.POST_TAG);
                    String id = "IMG-" + userName + "-";
                    intent.putExtra(ViewMediaActivity.CHAT_ID_TAG, id);
                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(mContext, R.anim.move_to_left_go, R.anim.move_to_right_go);
                    ActivityCompat.startActivity(mContext, intent, activityOptionsCompat.toBundle());
                }else ToastHelper.toast(mContext, mContext.getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
            });

            dialog.show();
        } catch (Exception ex){
            ToastHelper.toast(mContext, ex.getMessage(), ToastHelper.SHORT_DURATION);
        }
    }
}
