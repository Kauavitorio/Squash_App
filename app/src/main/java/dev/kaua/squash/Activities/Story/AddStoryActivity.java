package dev.kaua.squash.Activities.Story;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickClick;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.UserPermissions;

@SuppressWarnings({"rawtypes", "FieldCanBeLocal", "ConstantConditions"})
@SuppressLint("SimpleDateFormat")
public class AddStoryActivity extends AppCompatActivity {
    private static final String TAG = "AddStory_Ac";
    private static final int CAMERA_REQUEST = 333;
    private static final int GALLERY_REQUEST = 222;
    private static final int _REQUEST = 1254;
    private static final String[] permission_CAMERA = { Manifest.permission.CAMERA };
    private static final String[] permission_GALERY = { Manifest.permission.READ_EXTERNAL_STORAGE };
    private static final SimpleDateFormat df_time = new SimpleDateFormat("dd/MM/yyyy HH:mm a");

    private Uri mImageUri;
    String  myUrl = "";
    private StorageTask storageTask;
    private static final long ONE_DAY = 86400000;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        checkAndRequestPermissions();
        storageReference = myFirebaseHelper.getFirebaseStorageInstance().getReference(myFirebaseHelper.USERS_REFERENCE)
                .child(myFirebaseHelper.STORY_REFERENCE);

    }

    private void checkAndRequestPermissions() {
        final int camera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        final int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (camera != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        if (read != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        if (!listPermissionsNeeded.isEmpty()) {
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            UserPermissions.validatePermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), this, _REQUEST);
        }else OpenPick();
    }


    PickImageDialog pickImageDialog;
    void OpenPick(){
        if(pickImageDialog != null)
            pickImageDialog.dismiss();
        pickImageDialog = PickImageDialog.build(new PickSetup());
        pickImageDialog.setOnClick(new IPickClick() {
            @Override
            public void onGalleryClick() {
                pickImageDialog.dismiss();
                final int GalleryPermission = ContextCompat.checkSelfPermission(AddStoryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if(GalleryPermission == PackageManager.PERMISSION_GRANTED)
                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI) , GALLERY_REQUEST);
                else UserPermissions.validatePermissions(permission_GALERY, AddStoryActivity.this, _REQUEST);
            }

            @Override
            public void onCameraClick() {
                pickImageDialog.dismiss();
                final int CAMERAPermission = ContextCompat.checkSelfPermission(AddStoryActivity.this, Manifest.permission.CAMERA);
                if(CAMERAPermission == PackageManager.PERMISSION_GRANTED)
                    takePhotoFromCamera();
                else UserPermissions.validatePermissions(permission_CAMERA, AddStoryActivity.this, _REQUEST);
            }
        }).setOnPickCancel(this::finish)
                .show(this).setCancelable(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == _REQUEST) {
            final Map<String, Integer> perms = new HashMap<>();
            // Initialize the map with both permissions
            perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            // Fill with actual results from user
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for both permissions
                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.d("in fragment on request", "CAMERA & READ_EXTERNAL_STORAGE permission granted");
                    // process the normal flow
                    //else any one or both the permissions are not granted
                    OpenPick();
                } else {
                    Log.d("in fragment on request", "Some permissions are not granted ask again ");
                    //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                    //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showDialogOK(getString(R.string.camera_and_storage_permission_required),
                                (dialog, which) -> {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            checkAndRequestPermissions();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            finish();
                                            // proceed with logic by disabling the related features or quit the app.
                                            break;
                                    }
                                });
                    }
                    //permission is denied (and never ask again is  checked)
                    //shouldShowRequestPermissionRationale will return false
                    else {
                        ToastHelper.toast(this, getString(R.string.go_to_setting_story_perm_request),
                                ToastHelper.LONG_DURATION);
                        finish();
                    }
                }
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), okListener)
                .setNegativeButton(getString(R.string.cancel), okListener)
                .create()
                .show();
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void publishStory(){
        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoading();

        if(mImageUri != null){
            final String myId = String.valueOf(MyPrefs.getUserInformation(AddStoryActivity.this).getAccount_id());
            final StorageReference imageReference = storageReference.child(System.currentTimeMillis()
                    + myId + "SQUASH"
                    + "." + getFileExtension(mImageUri));
            storageTask = imageReference.putFile(mImageUri);
            //noinspection unchecked
            storageTask.continueWithTask(task -> {
                if(!task.isSuccessful()){
                    throw Objects.requireNonNull(task.getException());
                }
                return imageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Uri downloadUri = (Uri) task.getResult();
                    myUrl = downloadUri.toString();

                    final DatabaseReference reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.STORY_REFERENCE)
                            .child(myId);

                    final Calendar c = Calendar.getInstance();

                    final String storyId = reference.push().getKey();

                    if(storyId != null){
                        long timeEnd = System.currentTimeMillis() + ONE_DAY;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageUrl", EncryptHelper.encrypt(myUrl));
                        hashMap.put("timeStart", ServerValue.TIMESTAMP);
                        hashMap.put("timeEnd", timeEnd);
                        hashMap.put("storyId", storyId);
                        hashMap.put("userId", myId);
                        hashMap.put("userName", MyPrefs.getUserInformation(this).getUsername());
                        hashMap.put("uploadTime", df_time.format(c.getTime()));

                        reference.child(storyId).setValue(hashMap);
                    }else ToastHelper.toast(AddStoryActivity.this, getString(R.string.there_was_a_problem_registering_your_story), ToastHelper.LONG_DURATION);

                    loadingDialog.dismissDialog();
                }else
                    ToastHelper.toast(AddStoryActivity.this, getString(R.string.there_was_a_communication_problem), ToastHelper.LONG_DURATION);
                finish();
            }).addOnFailureListener(e -> Log.d(TAG, e.getMessage()));
        }else{
            ToastHelper.toast(AddStoryActivity.this, getString(R.string.unable_to_get_the_image_from_your_device), ToastHelper.LONG_DURATION);
            loadingDialog.dismissDialog();
            finish();
        }
    }

    private void takePhotoFromCamera() {
        final Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        String path =
                MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,
                        "SquashStory_" + Methods.RandomCharactersWithoutSpecials(5), null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case CAMERA_REQUEST:
                if(resultCode == RESULT_OK){
                    if(data != null){
                        mImageUri = data.getData();
                        if(mImageUri == null)
                            mImageUri = getImageUri(AddStoryActivity.this, (Bitmap)data.getExtras().get("data"));
                        publishStory();
                    }else ToastHelper.toast(AddStoryActivity.this, getString(R.string.unable_to_get_the_image_from_your_device), ToastHelper.LONG_DURATION);
                }else ToastHelper.toast(AddStoryActivity.this, getString(R.string.unable_to_get_the_image_from_your_device), ToastHelper.LONG_DURATION);
                break;
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    if(data != null){
                        mImageUri = data.getData();
                        publishStory();
                    }else ToastHelper.toast(AddStoryActivity.this, getString(R.string.unable_to_get_the_image_from_your_device), ToastHelper.LONG_DURATION);
                }else ToastHelper.toast(AddStoryActivity.this, getString(R.string.unable_to_get_the_image_from_your_device), ToastHelper.LONG_DURATION);
                break;
        }
    }
}