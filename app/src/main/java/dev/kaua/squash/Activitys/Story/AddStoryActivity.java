package dev.kaua.squash.Activitys.Story;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.UserPermissions;

@SuppressWarnings({"rawtypes", "FieldCanBeLocal"})
@SuppressLint("SimpleDateFormat")
public class AddStoryActivity extends AppCompatActivity {
    private static final String TAG = "AddStory_Ac";
    private static final int CAMERA_REQUEST = 333;
    private static final int GALLERY_REQUEST = 222;
    private static final int _REQUEST = 1254;
    private static final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA };
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

        TestPerm();
        storageReference = myFirebaseHelper.getFirebaseStorageInstance().getReference(myFirebaseHelper.USERS_REFERENCE)
                .child(myFirebaseHelper.STORY_REFERENCE);

    }

    PickImageDialog pickImageDialog;
    boolean asked = false;
    private void TestPerm() {
        final int GalleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        final int CAMERAPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if(GalleryPermission == PackageManager.PERMISSION_DENIED || CAMERAPermission == PackageManager.PERMISSION_DENIED ){
            if(!asked){
                UserPermissions.validatePermissions(permissions, this, _REQUEST);
                asked = true;
            }else{
                if (GalleryPermission == PackageManager.PERMISSION_GRANTED || CAMERAPermission == PackageManager.PERMISSION_GRANTED ){
                    OpenPick();
                }else UserPermissions.validatePermissions(permissions, this, _REQUEST);
            }
        }else OpenPick();
    }

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
        if(requestCode == _REQUEST) {
            asked = true;
            TestPerm();
        }
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