package dev.kaua.squash.Activitys;

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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.UserPermissions;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressLint("StaticFieldLeak")
public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfile";
    public static CircleImageView ic_edit_ProfileUser;
    TextInputEditText edit_name, edit_username, edit_bio;
    Button btn_edit_profile;
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

    final Retrofit retrofit = Methods.GetRetrofitBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Ids();

        btn_edit_profile.setOnClickListener(v -> {
            if(!username_check) showError(edit_username, getString(R.string.username_is_already_in_use));
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
                newInfo.setUsername(EncryptHelper.encrypt(edit_username.getText().toString()));
                if(edit_bio.getText() == null) newInfo.setBio_user(EncryptHelper.encrypt(""));
                else newInfo.setBio_user(EncryptHelper.encrypt(edit_bio.getText().toString()));
                newInfo.setProfile_image(EncryptHelper.encrypt(new_image));

                loadingDialog = new LoadingDialog(this);
                loadingDialog.startLoading();
                Call<DtoAccount> call = services.edit(newInfo);
                call.enqueue(new Callback<DtoAccount>() {
                    @Override
                    public void onResponse(@NotNull Call<DtoAccount> call, @NotNull Response<DtoAccount> response) {
                        loadingDialog.dismissDialog();
                        if(response.code() == 200){

                            //  Clear all prefs before login user
                            mPrefs = getSharedPreferences(MyPrefs.PREFS_USER, MODE_PRIVATE);

                            //  Add User prefs
                            SharedPreferences.Editor editor = mPrefs.edit();
                            assert response.body() != null;
                            editor.putString("pref_name_user", EncryptHelper.encrypt(edit_name.getText().toString()));
                            editor.putString("pref_username", EncryptHelper.encrypt(edit_username.getText().toString()));
                            editor.putString("pref_profile_image", EncryptHelper.encrypt(new_image));
                            editor.putString("pref_bio_user", EncryptHelper.encrypt(edit_bio.getText().toString()));
                            editor.apply();

                            //  Register new user on Firebase Database
                            reference = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(myFirebaseHelper.getFirebaseAuth().getUid()));
                            HashMap<String, Object> hashMap = new HashMap<>();

                            hashMap.put("username", edit_username.getText().toString().trim());
                            hashMap.put("name_user", edit_name.getText().toString().trim());
                            hashMap.put("search", edit_username.getText().toString().trim());
                            hashMap.put("imageURL", new_image);

                            reference.updateChildren(hashMap).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()) Log.d(TAG, "Register in Realtime database Successful");
                            });

                            ProfileFragment.getInstance().GetUserInfo(EditProfileActivity.this);
                            finish();
                        }
                        else if(response.code() == 400) showError(edit_username, getString(R.string.bad_username));
                        else if(response.code() == 405) showError(edit_name, getString(R.string.bad_username));
                        else if(response.code() == 401) showError(edit_username, getString(R.string.username_is_already_in_use));
                        else Warnings.showWeHaveAProblem(EditProfileActivity.this);
                    }

                    @Override
                    public void onFailure(@NotNull Call<DtoAccount> call, @NotNull Throwable t) {
                        loadingDialog.dismissDialog();
                        btn_edit_profile.setEnabled(true);
                        Warnings.showWeHaveAProblem(EditProfileActivity.this);
                    }
                });
            }
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
                if (s.length() >= 6 && !base_username.equals(s.toString())) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> {
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
                            });
                        }
                    }, DELAY);
                }
            }
        });
    }

    private void DoUsernameValidation() {
        if(!username_check)
            edit_username.setError(getString(R.string.username_is_already_in_use));
    }

    private void OpenGallery() {
        UserPermissions.validatePermissions(permissions, this, 189);
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
        if(requestCode == 189){
            UserPermissions.validatePermissions(permissions, this, 189);
            int GalleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (GalleryPermission == PackageManager.PERMISSION_GRANTED){
                Intent openGallery = new Intent();
                openGallery.setType("image/*");
                openGallery.setAction(Intent.ACTION_PICK);
                //noinspection deprecation
                startActivityForResult(Intent.createChooser(openGallery, getString(R.string.select_an_image)), PICK_IMAGE_REQUEST);
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri filePath = data.getData();
            LoadingDialog dialog = new LoadingDialog(this);
            dialog.startLoading();
            try {
                Glide.with(this)
                        .asBitmap()
                        .load(filePath)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                try {
                                    @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
                                    file_upload_to_crop = Methods.SaveImage(EditProfileActivity.this, resource, "user_profile_image", timeStamp);
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
                        });

            }catch (Exception ex){
                dialog.dismissDialog();
                Warnings.showWeHaveAProblem(EditProfileActivity.this);
                Log.d(TAG, ex.toString());
            }
        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if(data != null){
                final Uri resultUri = UCrop.getOutput(data);
                Profile_Image.SendToCrop(this, resultUri);
            }else Warnings.showWeHaveAProblem(this);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            if(data != null){
                final Throwable cropError = UCrop.getError(data);
                if(cropError != null)
                    Log.d(TAG, cropError.toString());
            }
            Warnings.showWeHaveAProblem(this);
        }
    }


    private void loadUserInfo() {
        user = MyPrefs.getUserInformation(this);
        new_image = user.getProfile_image();
        Glide.with(this).load(user.getProfile_image()).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(ic_edit_ProfileUser);
        edit_name.setText(user.getName_user());
        edit_username.setText(user.getUsername());
        base_username = user.getUsername();
        edit_bio.setText(user.getBio_user());
    }

    private void Ids() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        instance = this;
        loadingDialog = new LoadingDialog(this);
        ic_edit_ProfileUser = findViewById(R.id.ic_edit_ProfileUser);
        edit_name = findViewById(R.id.edit_name);
        btn_edit_profile = findViewById(R.id.btn_edit_profile);
        edit_username = findViewById(R.id.edit_username);
        edit_bio = findViewById(R.id.edit_bio);
        loadUserInfo();
        RunEditWatcher();
    }
}