package dev.kaua.squash.Activities.Setting.AccountSetting.Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import dev.kaua.squash.Activities.Setting.AccountSettingActivity;
import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.Data.Account.DtoVerification;
import dev.kaua.squash.Firebase.myFirebaseHelper;
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

@SuppressLint("UseCompatLoadingForDrawables")
public class RequestVerificationFragment extends Fragment {
    private static final String TAG = "PERSONAL_INFO_LOG";
    static View view;

    TextInputEditText edit_username, edit_fullName, edit_audience, edit_link_1, edit_link_2, edit_link_3;
    LinearLayout select_document, selected_document_container, verification_from_container;
    RelativeLayout already_verified;
    Button btn_choose_file, btn_submit;
    TextView txt_title_document_type, edit_birthday, txt_selected_document_type,
            txt_document_title, txt_remove_document_file;
    BottomSheetDialog sheet_document;
    View sheetView_document;
    CheckBox rg, passport, driver, bill, article;
    public static final int OPEN_CAMERA = 444;
    public static final int PICK_IMAGE_REQUEST_MEDIA = 333;
    private static final String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
    public static StorageReference storageReference;
    static final Retrofit retrofit = Methods.GetRetrofitBuilder();

    //  FROM VARIABLES
    final static int NONE_DOCUMENT = -111;
    final static int RG_DOCUMENT = 10;
    final static int PASSPORT_DOCUMENT = 20;
    final static int DRIVER_DOCUMENT = 30;
    final static int BILL_DOCUMENT = 40;
    final static int ARTICLE_DOCUMENT = 50;
    static int document_type = NONE_DOCUMENT;
    static String document_name = null;
    static String DOCUMENT_URL = "";
    static String link1 = "";
    static String link2 = "";
    static String link3 = "";
    static Uri document = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account_request_verification, container, false);
        Ids();

        //  Select Document Sheet Dialog
        select_document.setOnClickListener(v -> {
            select_document.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.click_anim));
            if(sheet_document == null) {
                sheet_document = new BottomSheetDialog(requireContext(), R.style.BottomSheetTheme);

                //  Creating View for SheetMenu
                sheetView_document = LayoutInflater.from(requireContext()).inflate(R.layout.adapter_sheet_document_type_selector,
                        view.findViewById(R.id.sheet_document_selector));

                rg = sheetView_document.findViewById(R.id.document_rg);
                passport = sheetView_document.findViewById(R.id.document_passport);
                driver = sheetView_document.findViewById(R.id.document_drivers_license);
                bill = sheetView_document.findViewById(R.id.document_bill);
                article = sheetView_document.findViewById(R.id.document_incorporation);
            }

            rg.setOnClickListener(v1 -> {
                document_type = RG_DOCUMENT;
                ApplyFilter();
            });

            passport.setOnClickListener(v1 -> {
                document_type = PASSPORT_DOCUMENT;
                ApplyFilter();
            });

            driver.setOnClickListener(v1 -> {
                document_type = DRIVER_DOCUMENT;
                ApplyFilter();
            });

            bill.setOnClickListener(v1 -> {
                document_type = BILL_DOCUMENT;
                ApplyFilter();
            });

            article.setOnClickListener(v1 -> {
                document_type = ARTICLE_DOCUMENT;
                ApplyFilter();
            });

            sheet_document.setContentView(sheetView_document);
            sheet_document.show();
        });

        //  Choose Document File Click
        btn_choose_file.setOnClickListener(v -> {
            btn_choose_file.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.click_anim));
            OpenGallery();
        });

        //  Remove Document File
        txt_remove_document_file.setOnClickListener(v -> {
            txt_remove_document_file.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.click_anim));
            document_name = null;
            document = null;
            CheckDocumentImage();
        });

        //  Submit Click
        btn_submit.setOnClickListener(v -> {
            if(edit_fullName.getText() != null && edit_audience.getText() != null){
                final String full_name = edit_fullName.getText().toString().trim();
                final String audience = edit_audience.getText().toString().trim();

                if(full_name.length() <= 0) DoToast(getString(R.string.full_name_required));
                else if(audience.length() < 15) DoToast(getString(R.string.need_to_enter_description_audience));
                else if(document_name == null || document_name.length() <= 0)
                    DoToast(getString(R.string.need_to_select_a_document));
                else if(!TestLinks()) DoToast(warn_link);
                else {

                    LoadingDialog loadingDialog = new LoadingDialog(requireActivity());
                    loadingDialog.startLoading();

                    //uploading the image
                    storageReference = myFirebaseHelper.getFirebaseStorage().child(myFirebaseHelper.USERS_REFERENCE)
                            .child(myFirebaseHelper.VERIFICATION_REFERENCE).child(myFirebaseHelper.MEDIAS_REFERENCE)
                            .child(myFirebaseHelper.getFirebaseUser().getUid()).child(Methods.shuffle(Methods.RandomCharactersWithoutSpecials(20)
                                    + getFileName(document).replace(" ", "") + "_" + Methods.RandomCharactersWithoutSpecials(3)));
                    storageReference.putFile(document).continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, Objects.requireNonNull(task.getException()).toString());
                        }
                        return storageReference.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loadingDialog.dismissDialog();
                            final Uri downloadUri = task.getResult();

                            new Handler().postDelayed(() -> {
                                final LoadingDialog loading = new LoadingDialog(requireActivity());
                                loading.startLoading();

                                new Handler().postDelayed(() -> {

                                    DOCUMENT_URL = downloadUri.toString();

                                    String placedB = Methods.shuffle(Methods.RandomCharacters(Methods.getRandomAmount()));
                                    String placedC = Methods.shuffle(Methods.RandomCharactersWithoutSpecials(Methods.getRandomAmount())
                                            + "SQUASH" + "VERIFICATION" + "REQUEST");
                                    final DtoVerification dto = new DtoVerification();
                                    dto.setuNe(placedB + EncryptHelper.encrypt(MyPrefs.getUserInformation(requireContext()).getUsername()));
                                    dto.setNm(placedC + EncryptHelper.encrypt(audience));
                                    dto.setDoc_t(document_type);
                                    dto.setDoc_u(placedC + EncryptHelper.encrypt(DOCUMENT_URL) + placedB);
                                    dto.setAd_nc(placedC + EncryptHelper.encrypt(audience));
                                    dto.setUrl1(placedC + EncryptHelper.encrypt(link1));
                                    dto.setUrl2(placedB + EncryptHelper.encrypt(link2));
                                    dto.setUrl3(placedC + EncryptHelper.encrypt(link3));

                                    AccountServices services = retrofit.create(AccountServices.class);
                                    Call<DtoVerification> call = services.request_verification(dto);
                                    call.enqueue(new Callback<DtoVerification>() {
                                        @Override
                                        public void onResponse(@NonNull Call<DtoVerification> call, @NonNull Response<DtoVerification> response) {
                                            loading.dismissDialog();
                                            if(response.code() == 200){
                                                ToastHelper.toast(requireActivity(), getString(R.string.your_request_was_successful), ToastHelper.LONG_DURATION);
                                                AccountSettingActivity.getInstance().LoadOptions();
                                            }else Warnings.showWeHaveAProblem(requireContext(), ErrorHelper.REQUEST_VERIFICATION_REQUEST);
                                        }
                                        @Override
                                        public void onFailure(@NonNull Call<DtoVerification> call, @NonNull Throwable t) {
                                            loading.dismissDialog();
                                            Warnings.showWeHaveAProblem(requireContext(), ErrorHelper.REQUEST_VERIFICATION_FAILURE);
                                        }
                                    });
                                }, 1000);
                            }, 500);
                        } else {
                            loadingDialog.dismissDialog();
                            ToastHelper.toast(requireActivity(), getString(R.string.there_was_a_communication_problem), ToastHelper.LONG_DURATION);
                            Log.d(TAG, Objects.requireNonNull(task.getException()).toString());
                        }
                    });
                }
            }else DoToast(getString(R.string.required_to_enter_full_name_and_public));
        });

        return view;
    }

    String warn_link = "";
    boolean TestLinks(){
        boolean test = true;
        if(edit_link_3.getText() != null && edit_link_3.getText().toString().trim().length() > 0){
            if(!Patterns.WEB_URL.matcher(edit_link_3.getText().toString()).matches()) {
                warn_link = getString(R.string.link_is_inserted_incorrectly, String.valueOf(3));
                test = false;
            }else link3 = edit_link_3.getText().toString().trim();
        }
        if(edit_link_2.getText() != null && edit_link_2.getText().toString().trim().length() > 0){
            if(!Patterns.WEB_URL.matcher(edit_link_2.getText().toString()).matches()) {
                warn_link = getString(R.string.link_is_inserted_incorrectly, String.valueOf(2));
                test = false;
            }else link2 = edit_link_2.getText().toString().trim();
        }
        if(edit_link_1.getText() != null && edit_link_1.getText().toString().trim().length() > 0){
            if(!Patterns.WEB_URL.matcher(edit_link_1.getText().toString()).matches()) {
                warn_link = getString(R.string.link_is_inserted_incorrectly, String.valueOf(1));
                test = false;
            }else link1 = edit_link_1.getText().toString().trim();
        }
        return test;
    }

    void DoToast(final String CONTENT){
        ToastHelper.toast(requireActivity(), CONTENT, ToastHelper.SHORT_DURATION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_MEDIA && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                PreSetDocument(filePath);
            }catch (Exception ex){
                ToastHelper.toast(requireActivity(), getString(R.string.unable_to_locate_the_image), ToastHelper.SHORT_DURATION);
                Log.d(TAG, ex.toString());
            }
        }
    }

    void PreSetDocument(Uri filePath) {
        if(filePath != null){
            document = filePath;
            document_name = getFileName(filePath);
            txt_document_title.setText(document_name);
            CheckDocumentImage();
        }
        AllFilled();
    }

    void CheckDocumentImage() {
        if(document == null && document_name == null){
            btn_choose_file.setVisibility(View.VISIBLE);
            selected_document_container.setVisibility(View.GONE);
        }else{
            btn_choose_file.setVisibility(View.GONE);
            selected_document_container.setVisibility(View.VISIBLE);
        }
        AllFilled();
    }

    public String getFileName(@NonNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
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

    private void ChangeChecks() {
        if(rg != null && passport != null && driver != null && bill != null && article != null){
            if(document_type == NONE_DOCUMENT){
                rg.setChecked(false);
                passport.setChecked(false);
                driver.setChecked(false);
                bill.setChecked(false);
                article.setChecked(false);
            }
            else if(document_type == RG_DOCUMENT){
                rg.setChecked(true);
                passport.setChecked(false);
                driver.setChecked(false);
                bill.setChecked(false);
                article.setChecked(false);
            }
            else if(document_type == PASSPORT_DOCUMENT){
                rg.setChecked(false);
                passport.setChecked(true);
                driver.setChecked(false);
                bill.setChecked(false);
                article.setChecked(false);
            }
            else if(document_type == DRIVER_DOCUMENT){
                rg.setChecked(false);
                passport.setChecked(false);
                driver.setChecked(true);
                bill.setChecked(false);
                article.setChecked(false);
            }
            else if(document_type == BILL_DOCUMENT){
                rg.setChecked(false);
                passport.setChecked(false);
                driver.setChecked(false);
                bill.setChecked(true);
                article.setChecked(false);
            }
            else if(document_type == ARTICLE_DOCUMENT){
                rg.setChecked(false);
                passport.setChecked(false);
                driver.setChecked(false);
                bill.setChecked(false);
                article.setChecked(true);
            }
        }
        AllFilled();
    }

    private void ApplyFilter() {
        ChangeChecks();
        CheckDocumentSelected();
        if(document_type == RG_DOCUMENT) txt_selected_document_type.setText(getString(R.string.general_registry_rg));
        else if(document_type == PASSPORT_DOCUMENT) txt_selected_document_type.setText(getString(R.string.passport));
        else if(document_type == DRIVER_DOCUMENT) txt_selected_document_type.setText(getString(R.string.drivers_license));
        else if(document_type == BILL_DOCUMENT) txt_selected_document_type.setText(getString(R.string.recent_utility_bill));
        else if(document_type == ARTICLE_DOCUMENT) txt_selected_document_type.setText(getString(R.string.article_of_incorporation));
    }

    void Ids() {
        edit_username = view.findViewById(R.id.edit_username_verification_form);
        edit_fullName = view.findViewById(R.id.edit_FullName_verification_form);
        edit_birthday = view.findViewById(R.id.edit_birthday_verification_form);
        select_document = view.findViewById(R.id.select_document_verification_form);
        txt_title_document_type = view.findViewById(R.id.txt_title_document_type_verification_form);
        txt_selected_document_type = view.findViewById(R.id.txt_selected_document_type_verification_form);
        btn_choose_file = view.findViewById(R.id.btn_choose_file_verification_form);
        selected_document_container = view.findViewById(R.id.selected_document_container_verification_from);
        txt_document_title = view.findViewById(R.id.txt_document_title_verification_form);
        txt_remove_document_file = view.findViewById(R.id.txt_remove_document_file_verification_form);
        verification_from_container = view.findViewById(R.id.verification_from_container);
        already_verified = view.findViewById(R.id.already_verified);
        edit_audience = view.findViewById(R.id.edit_audience_verification_form);
        btn_submit = view.findViewById(R.id.btn_submit_verification_from);
        edit_link_1 = view.findViewById(R.id.edit_link_1_verification_form);
        edit_link_2 = view.findViewById(R.id.edit_link_2_verification_form);
        edit_link_3 = view.findViewById(R.id.edit_link_3_verification_form);

        if(Methods.getUserLevel(requireContext()) > DtoAccount.NORMAL_ACCOUNT){
            verification_from_container.setVisibility(View.GONE);
            already_verified.setVisibility(View.VISIBLE);
        }else{
            verification_from_container.setVisibility(View.VISIBLE);
            already_verified.setVisibility(View.GONE);
            LoadUserInfo();
            CheckDocumentSelected();
            RunTextWatcher();
        }
    }

    private void RunTextWatcher() {
        edit_audience.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {AllFilled();}
            @Override
            public void afterTextChanged(Editable s) {AllFilled();}
        });
        edit_fullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {AllFilled();}
            @Override
            public void afterTextChanged(Editable s) {AllFilled();}
        });
    }

    private void OpenGallery() {
        UserPermissions.validatePermissions(permissions, requireActivity(), OPEN_CAMERA);
        int GalleryPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (GalleryPermission == PackageManager.PERMISSION_GRANTED){
            Intent openGallery = new Intent();
            openGallery.setType("image/*");
            openGallery.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(openGallery, getString(R.string.select_an_image)), PICK_IMAGE_REQUEST_MEDIA);
        }
    }

    void CheckDocumentSelected() {
        if(document_type == NONE_DOCUMENT){
            txt_title_document_type.setTextSize(17);
            txt_selected_document_type.setVisibility(View.GONE);
            btn_choose_file.setEnabled(false);
            btn_choose_file.setBackground(requireContext().getDrawable(R.drawable.btn_simple_disable));
            btn_choose_file.setTextColor(requireContext().getColor(R.color.hint));
        }else{
            txt_title_document_type.setTextSize(13);
            txt_selected_document_type.setVisibility(View.VISIBLE);
            btn_choose_file.setEnabled(true);
            btn_choose_file.setBackground(requireContext().getDrawable(R.drawable.btn_simple_enable));
            btn_choose_file.setTextColor(requireContext().getColor(R.color.black));
        }
        AllFilled();
    }

    void AllFilled(){
        if(edit_fullName.getText() != null && edit_audience.getText() != null && document_name != null
                && document_type != NONE_DOCUMENT){
            final String full_name = edit_fullName.getText().toString();
            final String audience = edit_audience.getText().toString();

            if(full_name.length() > 2 && audience.length() >= 15 && document_name.length() > 2) EnableSubmit();
            else DisableSubmit();
        }else DisableSubmit();
    }

    void DisableSubmit(){
        btn_submit.setEnabled(false);
        btn_submit.setBackground(requireContext().getDrawable(R.drawable.background_button_follow_disable));
    }

    void EnableSubmit(){
        btn_submit.setEnabled(true);
        btn_submit.setBackground(requireContext().getDrawable(R.drawable.background_button_follow));
    }

    void LoadUserInfo() {
        DtoAccount account = MyPrefs.getUserInformation(requireContext());
        edit_username.setText(account.getUsername());
        edit_birthday.setText(account.getBorn_date());
        AllFilled();
    }
}
