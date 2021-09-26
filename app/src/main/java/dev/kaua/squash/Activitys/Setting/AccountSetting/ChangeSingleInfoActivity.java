package dev.kaua.squash.Activitys.Setting.AccountSetting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import dev.kaua.squash.Data.Account.AccountServices;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Security.Login;
import dev.kaua.squash.Tools.ErrorHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.Methods;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressLint("UseCompatLoadingForDrawables")
public class ChangeSingleInfoActivity extends AppCompatActivity {
    public static final int CHANGE_PHONE = 11;
    public static final int CHANGE_BIRTHDAY = 22;
    public static final String CHANGE_REQUEST = "change_request_tag";
    public static String CONTENT_ORIGINAL;
    public static int age_user;
    TextView txt_change_single_title, txt_age_error_change;
    EditText edit_phone_number, edit_birthday;
    Button btn_change_phone_number, btn_change_birthday;
    LinearLayout container_phone_change, container_birthday_change;
    private final Calendar myCalendar = Calendar.getInstance();
    private static DatePickerDialog.OnDateSetListener date;
    private final Retrofit retrofit = Methods.GetRetrofitBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_single_info);
        txt_change_single_title = findViewById(R.id.txt_change_single_title);
        edit_phone_number = findViewById(R.id.edit_phone_number_change_single);
        btn_change_phone_number = findViewById(R.id.btn_change_phone_number);
        container_phone_change = findViewById(R.id.container_phone_change);
        container_birthday_change = findViewById(R.id.container_birthday_change);
        edit_birthday = findViewById(R.id.edit_birthday_change_single);
        btn_change_birthday = findViewById(R.id.btn_change_birthday);
        txt_age_error_change = findViewById(R.id.txt_age_error_change);

        final Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.getInt(CHANGE_REQUEST) != 0){
            final int REQUEST = bundle.getInt(CHANGE_REQUEST);

            if(REQUEST == CHANGE_BIRTHDAY){
                container_birthday_change.setVisibility(View.VISIBLE);
                txt_change_single_title.setText(getString(R.string.birthday));
                CONTENT_ORIGINAL = MyPrefs.getUserInformation(this).getBorn_date();
                edit_birthday.setText(MyPrefs.getUserInformation(this).getBorn_date());

            }else if(REQUEST == CHANGE_PHONE){
                txt_change_single_title.setText(getString(R.string.phone_number));
                edit_phone_number.setText(MyPrefs.getUserInformation(this).getPhone_user());
                CONTENT_ORIGINAL = MyPrefs.getUserInformation(this).getPhone_user();
                CheckPhoneNumber();
                container_phone_change.setVisibility(View.VISIBLE);
                edit_phone_number.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        CheckPhoneNumber();
                    }
                });
            }

        }else finish();

        //  Creating Calendar
        date = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        //  Select Birth date Click
        edit_birthday.setOnClickListener(v -> ShowCalendar());

        btn_change_phone_number.setOnClickListener(v -> {
            btn_change_phone_number.startAnimation(AnimationUtils.loadAnimation(this,R.anim.click_anim));
            UpdateInfo(edit_phone_number.getText().toString(), String.valueOf(CHANGE_PHONE));
        });

        btn_change_birthday.setOnClickListener(v -> {
            btn_change_birthday.startAnimation(AnimationUtils.loadAnimation(this,R.anim.click_anim));
            UpdateInfo(edit_birthday.getText().toString(), String.valueOf(CHANGE_BIRTHDAY));
        });

        findViewById(R.id.btn_back_change_single_info).setOnClickListener(v -> finish());
    }

    final int min = 30;
    final int max = 70;
    void UpdateInfo(String content, String type){
        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoading();

        final DtoAccount new_info = new DtoAccount();
        final String place = Methods.RandomCharacters((int)Math.floor(Math.random()*(max-min+1)+min));
        new_info.setPlaced(EncryptHelper.encrypt(place));
        new_info.setContent( place + EncryptHelper.encrypt(content));
        new_info.setAdds( EncryptHelper.encrypt(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id())) + place);
        new_info.setType_acc(type);

        AccountServices services = retrofit.create(AccountServices.class);
        Call<DtoAccount> call = services.update_base_info(new_info);
        call.enqueue(new Callback<DtoAccount>() {
            @Override
            public void onResponse(@NonNull Call<DtoAccount> call, @NonNull Response<DtoAccount> response) {
                loadingDialog.dismissDialog();
                if(response.code() == 200){
                    Login.ReloadUserinfo(ChangeSingleInfoActivity.this, MyPrefs.getUserInformation(ChangeSingleInfoActivity.this).getEmail(),
                            MyPrefs.getUserInformation(ChangeSingleInfoActivity.this).getPassword());
                    new Handler().postDelayed(() -> {
                        LoadingDialog loadingDialog2 = new LoadingDialog(ChangeSingleInfoActivity.this);
                        loadingDialog2.startLoading();

                        new Handler().postDelayed(() -> {
                            loadingDialog2.dismissDialog();
                            finish();
                        }, 2500);
                    }, 500);

                }else
                    Warnings.showWeHaveAProblem(ChangeSingleInfoActivity.this, ErrorHelper.UPDATE_BASE_INFO_REQUEST);
            }
            @Override
            public void onFailure(@NonNull Call<DtoAccount> call, @NonNull Throwable t) {
                loadingDialog.dismissDialog();
                Warnings.showWeHaveAProblem(ChangeSingleInfoActivity.this, ErrorHelper.UPDATE_BASE_INFO_FAILURE);
            }
        });
    }

    private void updateLabel() {
        final String myFormat = "dd/MM/yyyy"; //In which you need put here
        final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        final String dateSelected = sdf.format(myCalendar.getTime());

        final String[] dateSplit = dateSelected.split("/");

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        if((year - Integer.parseInt(dateSplit[2])) < 13 )
            txt_age_error_change.setVisibility(View.VISIBLE);
        else txt_age_error_change.setVisibility(View.GONE);

        age_user = (year - Integer.parseInt(dateSplit[2]));

        edit_birthday.setText(dateSelected);
        CheckCalendar();
    }

    void ShowCalendar(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    void CheckPhoneNumber() {
        btn_change_phone_number.setEnabled(false);
        if(!CONTENT_ORIGINAL.equals(edit_phone_number.getText().toString())){
            btn_change_phone_number.setEnabled(true);
            btn_change_phone_number.setBackground(getDrawable(R.drawable.background_button_follow));
        }else
            btn_change_phone_number.setBackground(getDrawable(R.drawable.custom_button_disable_next));
    }

    void CheckCalendar() {
        btn_change_birthday.setEnabled(false);
        if(!CONTENT_ORIGINAL.equals(edit_birthday.getText().toString()) && age_user > 13){
            btn_change_birthday.setEnabled(true);
            btn_change_birthday.setBackground(getDrawable(R.drawable.background_button_follow));
        }else
            btn_change_birthday.setBackground(getDrawable(R.drawable.custom_button_disable_next));
    }
}