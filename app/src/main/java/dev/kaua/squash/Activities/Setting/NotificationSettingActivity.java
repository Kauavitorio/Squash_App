package dev.kaua.squash.Activities.Setting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.widget.CompoundButton;

import dev.kaua.squash.R;
import dev.kaua.squash.Tools.MyPrefs;

public class NotificationSettingActivity extends AppCompatActivity {
    SwitchCompat pauseAll_Switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);
        Ids();
    }

    void Ids(){
        getWindow().setStatusBarColor(getColor(R.color.background_menu_sheet));
        pauseAll_Switch = findViewById(R.id.pause_all_notification_switch);
        findViewById(R.id.btn_close_notifications_setting_ac)
                .setOnClickListener(v -> finish());

        pauseAll_Switch.setChecked(!MyPrefs.isNotificationActive(this));

        pauseAll_Switch.setOnCheckedChangeListener((buttonView, isChecked) -> MyPrefs.setNotificationActive(NotificationSettingActivity.this, !isChecked));
    }
}