package dev.kaua.squash.Activities.Auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.util.HashMap;

import dev.kaua.squash.Firebase.myFirebaseHelper;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.MyPrefs;

public class PrivacyPolicyUpdateActivity extends AppCompatActivity {
    public static String PRIVACY_POLICY_TAG = "privacy_policy";
    TextView txt_include_information, txt_please_accept;
    CardView btn_accept;
    boolean accept = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy_update);
        Ids();

        Bundle bundle = getIntent().getExtras();
        final long version = bundle.getLong(PRIVACY_POLICY_TAG);

        btn_accept.setOnClickListener(v -> {
            accept = true;

            final HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("privacyPolicy", version);

            final LoadingDialog loadingDialog = new LoadingDialog(this);
            loadingDialog.startLoading();

            myFirebaseHelper.getFirebaseDatabase().getReference(myFirebaseHelper.SYSTEM_REFERENCE)
                    .child(myFirebaseHelper.PRIVACY_POLICY_VERSION_REFERENCE)
                    .child(String.valueOf(MyPrefs.getUserInformation(this).getAccount_id()))
                    .updateChildren(hashMap).addOnCompleteListener(task -> {
                        if(MyPrefs.setPrivacyPolicy(PrivacyPolicyUpdateActivity.this, version)){
                            loadingDialog.dismissDialog();
                            finish();
                        }
                    });
        });

    }

    private void Ids() {
        btn_accept = findViewById(R.id.btn_accept);
        txt_include_information = findViewById(R.id.txt_include_information);
        txt_please_accept = findViewById(R.id.txt_please_accept);
        txt_include_information.setMovementMethod(LinkMovementMethod.getInstance());
        txt_please_accept.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!accept) finishAffinity();
        else finish();
    }
}