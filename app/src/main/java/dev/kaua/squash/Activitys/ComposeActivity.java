package dev.kaua.squash.Activitys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Data.Post.PostServices;
import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.Fragments.MainFragment;
import dev.kaua.squash.Fragments.ProfileFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
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

/**
 *  Copyright (c) 2021 Kauã Vitório
 *  Official repository https://github.com/Kauavitorio/Squash_App
 *  Responsible developer: https://github.com/Kauavitorio
 *  @author Kaua Vitorio
 **/

public class ComposeActivity extends AppCompatActivity {
    EditText edit_compose_msg;
    CardView btn_post, btn_add_image;
    List<String> post_image = new ArrayList<>();
    TextView txt_user_name, txt_username_name_compose;
    ImageView ic_account_badge_profile;
    ImageView compose_img01, compose_img02, compose_img03, compose_img04;
    CircleImageView ic_ProfileUser_profile;
    public static FirebaseUser fUser;
    FirebaseStorage firebaseStorage;

    DtoAccount userAccount;

    private static final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
    public static final int PICK_IMAGE_REQUEST_MEDIA = 111;
    final Retrofit retrofit = Methods.GetRetrofitBuilder();
    public static StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        Ids();
        SetRemoveImageClick();

        btn_post.setOnClickListener(v -> {
            String compose_text = edit_compose_msg.getText().toString();
            if(post_image.size() > 0 || compose_text.length() > 0){
                LoadingDialog loadingDialog = new LoadingDialog(this);
                loadingDialog.startLoading();
                Calendar c = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat df_date = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat df_time = new SimpleDateFormat("HH:mm a");
                String date = df_date.format(c.getTime());
                String time = df_time.format(c.getTime());

                //  Set on DtoPost post information
                final DtoPost post = new DtoPost();
                post.setAccount_id(EncryptHelper.encrypt(String.valueOf(userAccount.getAccount_id())));
                post.setPost_time(EncryptHelper.encrypt(time));
                post.setPost_date(EncryptHelper.encrypt(date));
                compose_text = compose_text.trim();
                post.setPost_content(EncryptHelper.encrypt(compose_text));
                post.setPost_topic(EncryptHelper.encrypt(""));
                post.setPost_images(post_image);

                PostServices services = retrofit.create(PostServices.class);
                Call<DtoPost> call = services.do_new_post(post);
                final String finalCompose_text = compose_text;
                call.enqueue(new Callback<DtoPost>() {
                    @Override
                    public void onResponse(@NotNull Call<DtoPost> call, @NotNull Response<DtoPost> response) {
                        loadingDialog.dismissDialog();
                        if(response.code() == 201){
                            if(response.body() != null){
                                final String post_id = response.body().getPost_id();

                                final HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("post_id", post_id);
                                hashMap.put("account_id", EncryptHelper.encrypt(String.valueOf(userAccount.getAccount_id())));
                                hashMap.put("post_time", EncryptHelper.encrypt(time));
                                hashMap.put("post_date", EncryptHelper.encrypt(date));
                                hashMap.put("post_content", EncryptHelper.encrypt(finalCompose_text));
                                hashMap.put("post_topic", EncryptHelper.encrypt(""));
                                hashMap.put("post_images", post_image);

                                hashMap.put("name_user", EncryptHelper.encrypt(MyPrefs.getUserInformation(ComposeActivity.this).getName_user()));
                                hashMap.put("username", EncryptHelper.encrypt(MyPrefs.getUserInformation(ComposeActivity.this).getUsername()));
                                hashMap.put("verification_level", EncryptHelper.encrypt(String.valueOf(Methods.getUserLevel(ComposeActivity.this))));
                                hashMap.put("profile_image", EncryptHelper.encrypt(MyPrefs.getUserInformation(ComposeActivity.this).getProfile_image()));
                                hashMap.put("post_likes", EncryptHelper.encrypt(String.valueOf(0)));
                                hashMap.put("post_comments_amount", EncryptHelper.encrypt(String.valueOf(0)));
                                hashMap.put("active", MyPrefs.getUserInformation(ComposeActivity.this).getActive());
                                myFirebaseHelper.getFirebaseDatabase().getReference()
                                        .child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.PUBLISHED_CHILD)
                                        .push().setValue(hashMap);

                                MainFragment.RefreshRecycler();
                                ProfileFragment.getInstance().ReloadRecycler();
                                finish();
                            }
                        }else Warnings.showWeHaveAProblem(ComposeActivity.this, ErrorHelper.NEW_POST_COMPOSE);
                    }

                    @Override
                    public void onFailure(@NotNull Call<DtoPost> call, @NotNull Throwable t) {
                        loadingDialog.dismissDialog();
                        Warnings.showWeHaveAProblem(ComposeActivity.this, ErrorHelper.NEW_POST_COMPOSE);
                    }
                });
            }

        });

        btn_add_image.setOnClickListener(v -> {
            if(post_image.size() < 4){
                UserPermissions.validatePermissions(permissions, this, 189);
                int GalleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (GalleryPermission == PackageManager.PERMISSION_GRANTED){
                    Intent openGallery = new Intent();
                    openGallery.setType("image/*");
                    openGallery.setAction(Intent.ACTION_PICK);
                    //noinspection deprecation
                    startActivityForResult(Intent.createChooser(openGallery, "Select Image"), PICK_IMAGE_REQUEST_MEDIA);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_MEDIA && resultCode == RESULT_OK && data != null && data.getData() != null) {
            LoadingDialog loadingDialog = new LoadingDialog(this);
            loadingDialog.startLoading();
            Uri filePath = data.getData();
            try {
                //getting image from gallery
                if(filePath != null) {

                    //uploading the image
                    storageReference = myFirebaseHelper.getFirebaseStorage().child(myFirebaseHelper.USERS_REFERENCE)
                            .child(myFirebaseHelper.POSTS_REFERENCE).child(myFirebaseHelper.MEDIAS_REFERENCE).child(fUser.getUid()).child("post_"
                            + getFileName(filePath).replace(" ", "") + "_" + Methods.RandomCharactersWithoutSpecials(5));
                    storageReference.putFile(filePath).continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            Log.d("MediaUpload", Objects.requireNonNull(task.getException()).toString());
                        }
                        return storageReference.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        loadingDialog.dismissDialog();
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            post_image.add(EncryptHelper.encrypt(downloadUri.toString()));
                            checkPost_Images();

                        } else {
                            loadingDialog.dismissDialog();
                            Warnings.showWeHaveAProblem(ComposeActivity.this, ErrorHelper.NEW_POST_COMPOSE_IMAGE_UPLOAD);
                            Log.d("MediaUpload", Objects.requireNonNull(task.getException()).toString());
                        }
                    });
                }
                else{
                    ToastHelper.toast(this, getString(R.string.select_an_image), 0);
                    loadingDialog.dismissDialog();
                }
            } catch (Exception ex) {
                loadingDialog.dismissDialog();
                Warnings.showWeHaveAProblem(this, ErrorHelper.NEW_POST_COMPOSE_IMAGE_UPLOAD);
                Log.d("MediaUpload", ex.toString());
            }
        }
    }

    private void checkPost_Images() {
        SetImagesGONE();
        if(post_image.size() > 0){
            btn_add_image.setVisibility(View.VISIBLE);
            for (int i = 0; i < post_image.size(); i++){
                if(i == 0){
                    Picasso.get().load(EncryptHelper.decrypt(post_image.get(0))).into(compose_img01);
                    compose_img01.setVisibility(View.VISIBLE);
                }else if(i == 1){
                    Picasso.get().load(EncryptHelper.decrypt(post_image.get(1))).into(compose_img02);
                    compose_img02.setVisibility(View.VISIBLE);
                }else if(i == 2){
                    Picasso.get().load(EncryptHelper.decrypt(post_image.get(2))).into(compose_img03);
                    compose_img03.setVisibility(View.VISIBLE);
                }else if(i == 3){
                    Picasso.get().load(EncryptHelper.decrypt(post_image.get(3))).into(compose_img04);
                    compose_img04.setVisibility(View.VISIBLE);
                    btn_add_image.setVisibility(View.GONE);
                }
            }
        }
    }

    private void SetRemoveImageClick(){
        LoadingDialog loadingDialog = new LoadingDialog(this);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.remove_image));
        alert.setMessage(getString(R.string.desc_remove_image_post));
        alert.setNegativeButton(getString(R.string.no), null);
        compose_img01.setOnClickListener(v -> {
            alert.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                loadingDialog.startLoading();
                RemoveImage(loadingDialog, 0);
            });
            alert.show();
        });
        compose_img02.setOnClickListener(v -> {
            alert.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                loadingDialog.startLoading();
                RemoveImage(loadingDialog, 1);
            });
            alert.show();
        });
        compose_img03.setOnClickListener(v -> {
            alert.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                loadingDialog.startLoading();
                RemoveImage(loadingDialog, 2);
            });
            alert.show();
        });
        compose_img04.setOnClickListener(v -> {
            alert.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                loadingDialog.startLoading();
                RemoveImage(loadingDialog, 3);
            });
            alert.show();
        });
    }

    private void RemoveImage(LoadingDialog loadingDialog, int position) {
        firebaseStorage = myFirebaseHelper.getFirebaseStorageInstance();
        StorageReference photoRef = firebaseStorage.getReferenceFromUrl(Objects.requireNonNull(EncryptHelper.decrypt(post_image.get(position))));
        photoRef.delete().addOnSuccessListener(aVoid -> {
            // File deleted successfully
            loadingDialog.dismissDialog();
            post_image.remove(position);
            checkPost_Images();
            Log.d("Compose", "onSuccess: deleted file");
        }).addOnFailureListener(exception -> {
            // Uh-oh, an error occurred!
            Log.d("Compose", "onFailure: did not delete file");
        });
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private void Ids() {
        getWindow().setStatusBarColor(getColor(R.color.background_menu_sheet));
        fUser = myFirebaseHelper.getFirebaseUser();
        userAccount = MyPrefs.getUserInformation(this);
        btn_post = findViewById(R.id.btn_post);
        compose_img01 = findViewById(R.id.compose_img01);
        compose_img02 = findViewById(R.id.compose_img02);
        compose_img03 = findViewById(R.id.compose_img03);
        compose_img04 = findViewById(R.id.compose_img04);
        btn_add_image = findViewById(R.id.btn_add_image_compose);
        txt_username_name_compose = findViewById(R.id.txt_username_name_compose);
        txt_user_name = findViewById(R.id.txt_user_name_compose);
        ic_ProfileUser_profile = findViewById(R.id.ic_ProfileUser_profile_compose);
        ic_account_badge_profile = findViewById(R.id.ic_account_badge_profile_compose);
        ic_account_badge_profile.setVisibility(View.GONE);
        btn_post.setElevation(0);
        edit_compose_msg = findViewById(R.id.edit_compose_msg);
        SetImagesGONE();

        LoadUserInfo();

        Toolbar toolbar = findViewById(R.id.toolbar_compose);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        edit_compose_msg.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void SetImagesGONE() {
        compose_img01.setVisibility(View.GONE);
        compose_img02.setVisibility(View.GONE);
        compose_img03.setVisibility(View.GONE);
        compose_img04.setVisibility(View.GONE);
        btn_add_image.setVisibility(View.VISIBLE);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void LoadUserInfo() {
        Picasso.get().load(userAccount.getProfile_image()).into(ic_ProfileUser_profile);
        txt_user_name.setText(userAccount.getName_user());
        txt_username_name_compose.setText( " | " + userAccount.getUsername());

        int verified = Integer.parseInt(Objects.requireNonNull(EncryptHelper.decrypt(userAccount.getVerification_level())));
        if(verified != 0){
            if (verified == 2)
                ic_account_badge_profile.setImageDrawable(getDrawable(R.drawable.ic_verified_employee_account));
            else
                ic_account_badge_profile.setImageDrawable(getDrawable(R.drawable.ic_verified_account));
            ic_account_badge_profile.setVisibility(View.VISIBLE);
        }
    }
}