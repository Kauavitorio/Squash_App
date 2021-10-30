package dev.kaua.squash.Activities.Profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Adapters.Profile.Profile_Image;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.UserPermissions;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressLint("StaticFieldLeak")
public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileLOG";
    public static CircleImageView ic_edit_ProfileUser;
    TextInputEditText edit_name, edit_username, edit_bio;
    Button btn_edit_profile;
    ImageView close_edit_profile;
    public static DtoAccount user;
    public static String new_image;
    public static StorageReference storageReference;
    public static int PICK_IMAGE_REQUEST = 111;
    private static final int DELAY = 500;
    //public static final int PIC_CROP = 1;
    private boolean username_check = true;
    private String base_username;
    private Timer timer = new Timer();
    private static SharedPreferences mPrefs;
    public static EditProfileActivity instance;
    private DatabaseReference reference;
    private LoadingDialog loadingDialog;
    private final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
    InputMethodManager imm;
    private Animation myAnim;

    final Retrofit retrofit = Methods.GetRetrofitBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Ids();

        btn_edit_profile.setOnClickListener(v -> {
            btn_edit_profile.startAnimation(myAnim);
            if(edit_username.getText() != null && ConnectionHelper.isOnline(this)){
                if(!username_check) showError(edit_username, getString(R.string.username_is_already_in_use));
                else if(!MyPrefs.getUserInformation(this).getUsername().equals(Methods.SQUASH_ORIGINAL_USERNAME)
                        && edit_username.getText().toString().equals(Methods.SQUASH_ORIGINAL_USERNAME))
                    showError(edit_username, getString(R.string.username_is_already_in_use));
            else if(edit_username.getText() == null || edit_username.getText().toString().replace(" ", "").length() < 6)
                    showError(edit_username, getString(R.string.required_field));
                else if(edit_name.getText() == null || edit_name.getText().toString().length() < 2)
                    showError(edit_name, getString(R.string.required_field));
                else{
                    btn_edit_profile.setEnabled(false);
                    AccountServices services = retrofit.create(AccountServices.class);

                    DtoAccount newInfo = new DtoAccount();
                    newInfo.setAccount_id_cry(EncryptHelper.encrypt(user.getAccount_id() + ""));
                    newInfo.setName_user(EncryptHelper.encrypt(edit_name.getText().toString()));
                    newInfo.setUsername(EncryptHelper.encrypt(edit_username.getText().toString().replace(" ", "")));
                    if(edit_bio.getText() == null) newInfo.setBio_user(EncryptHelper.encrypt(""));
                    else newInfo.setBio_user(EncryptHelper.encrypt(edit_bio.getText().toString()));
                    newInfo.setProfile_image(EncryptHelper.encrypt(new_image));

                    loadingDialog = new LoadingDialog(this);
                    loadingDialog.startLoading();
                    services.edit(newInfo).enqueue(new Callback<DtoAccount>() {
                        @Override
                        public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                            loadingDialog.dismissDialog();
                            if(response.code() == 200){
                                btn_edit_profile.setEnabled(false);

                                //  Get user prefs
                                mPrefs = getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);

                                //  Add User prefs
                                MyPrefs.updateProfileImage(EditProfileActivity.this, EncryptHelper.encrypt(new_image));
                                SharedPreferences.Editor editor = mPrefs.edit();
                                if(response.body() != null){
                                    editor.putString("pref_name_user", EncryptHelper.encrypt(edit_name.getText().toString()));
                                    editor.putString("pref_username", EncryptHelper.encrypt(edit_username.getText().toString()));
                                    editor.putString("pref_bio_user", EncryptHelper.encrypt(edit_bio.getText().toString()));
                                    editor.apply();
                                }

                                //  Register new user on Firebase Database
                                reference = myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.USERS_REFERENCE).child(Objects.requireNonNull(myFirebaseHelper.getFirebaseAuth().getUid()));
                                HashMap<String, Object> hashMap = new HashMap<>();

                                hashMap.put("username", edit_username.getText().toString().trim());
                                hashMap.put("name_user", edit_name.getText().toString().trim());
                                hashMap.put("search", edit_username.getText().toString().trim());
                                hashMap.put("imageURL", new_image);

                                reference.updateChildren(hashMap).addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()) Log.d(TAG, "Register in Realtime database Successful");
                                });

                                //  Update all user posts
                                myFirebaseHelper.getFirebaseDatabase().getReference()
                                        .child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD).orderByChild("account_id")
                                        .equalTo(EncryptHelper.encrypt(String.valueOf(MyPrefs.getUserInformation(EditProfileActivity.this)
                                                .getAccount_id())))
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("username", EncryptHelper.encrypt(edit_username.getText().toString().trim()));
                                            hashMap.put("name_user", EncryptHelper.encrypt(edit_name.getText().toString().trim()));
                                            hashMap.put("profile_image", EncryptHelper.encrypt(new_image));
                                            appleSnapshot.getRef().updateChildren(hashMap);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NotNull DatabaseError databaseError) {
                                        Log.e("EditProfile", "onCancelled", databaseError.toException());
                                    }
                                });

                                ProfileFragment.getInstance().GetUserInfo(EditProfileActivity.this);
                                finish();
                            }
                            else if(response.code() == 400) showError(edit_username, getString(R.string.bad_username));
                            else if(response.code() == 405) showError(edit_name, getString(R.string.bad_username));
                            else if(response.code() == 401) showError(edit_username, getString(R.string.username_is_already_in_use));
                            else
                                Warnings.showWeHaveAProblem(EditProfileActivity.this, ErrorHelper.PROFILE_EDIT);
                        }

                        @Override
                        public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                            loadingDialog.dismissDialog();
                            btn_edit_profile.setEnabled(true);
                            Warnings.showWeHaveAProblem(EditProfileActivity.this, ErrorHelper.PROFILE_EDIT);
                        }
                    });
                }
            }else ToastHelper.toast(this, getString(R.string.you_are_without_internet), ToastHelper.SHORT_DURATION);
        });

        ic_edit_ProfileUser.setOnClickListener(v -> {
            UserPermissions.validatePermissions(permissions, this, 1);
            int GalleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (GalleryPermission == PackageManager.PERMISSION_GRANTED)
                OpenGallery();
        });
    }


    private void showError(@NonNull EditText editText, String error){
        btn_edit_profile.setEnabled(true);
        editText.setError(error);
        editText.requestFocus();
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void RunEditWatcher(){
        edit_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(final Editable s) {
                if (edit_username != null && edit_username.getText() != null && s != null && s.toString().contains(" ")) {
                    edit_username.setText(edit_username.getText().toString().replace(" ", ""));
                    edit_username.setSelection(edit_username.getText().length());
                }
                if(s != null){

                    boolean test_username = MyPrefs.getUserInformation(EditProfileActivity.this)
                            .getUsername().equalsIgnoreCase(s.toString().replace(" ", ""));
                    if (!test_username && s.length() >= 5) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(() -> {
                                    try {
                                        if(ConnectionHelper.isOnline(EditProfileActivity.this)){
                                            DtoAccount account = new DtoAccount();
                                            account.setUsername(EncryptHelper.encrypt(s.toString().replace(" ", "")));
                                            AccountServices services = retrofit.create(AccountServices.class);
                                            Call<DtoAccount> call = services.check_username(account);
                                            call.enqueue(new Callback<DtoAccount>() {
                                                @Override
                                                public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                                                    if(response.code() == 401) username_check = false;
                                                    else if (response.code() == 200) username_check = true;
                                                    DoUsernameValidation();
                                                }
                                                @Override
                                                public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {}
                                            });
                                        }
                                    }catch (Exception e){
                                        Log.d(TAG, e.getMessage());
                                    }
                                });
                            }
                        }, DELAY);
                    }else {
                        if(s.toString().length() < 5) edit_username.setError(getString(R.string.your_username_must_contain_at_least));
                        else edit_username.setError(null);
                    }
                }
            }
        });
    }

    private void DoUsernameValidation() {
        if(edit_username.getText() != null){
            boolean test_username = MyPrefs.getUserInformation(EditProfileActivity.this)
                    .getUsername().equalsIgnoreCase(edit_username.getText().toString().replace(" ", ""));
            if(!username_check && !test_username)
                edit_username.setError(getString(R.string.username_is_already_in_use));
            else username_check = true;
        }
    }

    private void OpenGallery() {
        UserPermissions.validatePermissions(permissions, this, UserPermissions.PERMISSIONS_REQUEST);
        int GalleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (GalleryPermission == PackageManager.PERMISSION_GRANTED){
            Intent openGallery = new Intent();
            openGallery.setType("image/*");
            openGallery.setAction(Intent.ACTION_PICK);
            //noinspection deprecation
            startActivityForResult(Intent.createChooser(openGallery, getString(R.string.select_an_image)), PICK_IMAGE_REQUEST);
        }
    }

    File file_upload_to_crop;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadingDialog = new LoadingDialog(this);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri filePath = data.getData();
            LoadingDialog dialog = new LoadingDialog(this);
            dialog.startLoading();
            try {
                dialog.dismissDialog();
                Profile_Image.SendToCrop(this, filePath);
                /*Glide.with(this)
                        .asBitmap()
                        .load(filePath)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                try {
                                    file_upload_to_crop = new File(CapturePhotoUtils.insertImage(getContentResolver(), resource, "user_profile_image", "")));
                                    dialog.dismissDialog();
                                    UCrop.of(filePath, Uri.fromFile(file_upload_to_crop))
                                            .start(EditProfileActivity.this);
                                }
                                catch (Exception ex){
                                    dialog.dismissDialog();
                                    Warnings.showWeHaveAProblem(EditProfileActivity.this);
                                    Log.d(TAG, ex.toString());
                                }
                            }
                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) { }
                        });*/

            }catch (Exception ex){
                dialog.dismissDialog();
                Warnings.showWeHaveAProblem(EditProfileActivity.this, ErrorHelper.PROFILE_EDIT_IMAGE_UPLOAD);
                Log.d(TAG, ex.toString());
            }
        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if(data != null){
                final Uri resultUri = UCrop.getOutput(data);
                Profile_Image.SendToCrop(this, resultUri);
            }else Warnings.showWeHaveAProblem(this, ErrorHelper.PROFILE_EDIT_IMAGE_CROP);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            if(data != null){
                final Throwable cropError = UCrop.getError(data);
                if(cropError != null)
                    Log.d(TAG, cropError.toString());
            }
            Warnings.showWeHaveAProblem(this, ErrorHelper.PROFILE_EDIT_IMAGE_CROP);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == UserPermissions.PERMISSIONS_REQUEST){
            if(grantResults.length > 0){
                int GalleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (GalleryPermission == PackageManager.PERMISSION_GRANTED){
                    OpenGallery();
                }
            }
        }
    }

    private void loadUserInfo() {
        user = MyPrefs.getUserInformation(this);
        new_image = user.getProfile_image();
        Glide.with(this).load(user.getProfile_image()).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ic_edit_ProfileUser);
        edit_name.setText(user.getName_user());
        edit_username.setText(user.getUsername());
        base_username = user.getUsername();
        edit_bio.setText(user.getBio_user());
    }

    private void Ids() {
        myAnim = AnimationUtils.loadAnimation(this, R.anim.click_anim);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        instance = this;
        loadingDialog = new LoadingDialog(this);
        ic_edit_ProfileUser = findViewById(R.id.ic_edit_ProfileUser);
        close_edit_profile = findViewById(R.id.close_edit_profile);
        close_edit_profile.setOnClickListener(v -> finish());
        edit_name = findViewById(R.id.edit_name);
        btn_edit_profile = findViewById(R.id.btn_edit_profile);
        edit_username = findViewById(R.id.edit_username);
        edit_bio = findViewById(R.id.edit_bio);
        loadUserInfo();
        RunEditWatcher();
    }
}