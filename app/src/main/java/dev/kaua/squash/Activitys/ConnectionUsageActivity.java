package dev.kaua.squash.Activitys;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dev.kaua.squash.Data.Post.DtoPost;
import dev.kaua.squash.Data.Post.PostServices;
import dev.kaua.squash.Fragments.MainFragment;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.ConnectionHelper;
import dev.kaua.squash.Tools.LoadingDialog;
import dev.kaua.squash.Tools.MyPrefs;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("SetTextI18n")
public class ConnectionUsageActivity extends AppCompatActivity {
    ConnectionHelper connectionHelper;
    private TextView txt_total_usage, txt_sent_amount, txt_received_amount, txt_network_usage_since;
    private TextView txt_last_usage_reset;
    private LinearLayout btn_reset_statistics;
    private Animation myAnim;
    private static final int UID = android.os.Process.myUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_usage);
        Ids();

        Toolbar toolbar = findViewById(R.id.toolbar_connection_usage);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        loadNetworkUsage();

        btn_reset_statistics.setOnClickListener(v -> {
            btn_reset_statistics.startAnimation(myAnim);
            ResetStatistics();
        });
    }

    private void loadNetworkUsage() {
        long [] RX_TX = MyPrefs.get_RX_TX_Subtraction(this);
        long rxBytes = connectionHelper.getUidRxBytes(UID) - RX_TX[0];
        long txBytes = connectionHelper.getUidTxBytes(UID) - RX_TX[1];
        txt_total_usage.setText(ConnectionHelper.humanReadableByteCountSI(rxBytes + txBytes));
        txt_sent_amount.setText(ConnectionHelper.humanReadableByteCountSI(txBytes));
        txt_received_amount.setText(ConnectionHelper.humanReadableByteCountSI(rxBytes));
        txt_network_usage_since.setText(getString(R.string.network_usage_since) + " " + MyPrefs.get_NetWorkStartCount(this));
        String last_reset = MyPrefs.get_NetWorkLastReset(this);
        if(last_reset == null) last_reset = getString(R.string.never);
        txt_last_usage_reset.setText(getString(R.string.last_reset_time) + ": " + last_reset);
    }

    void ResetStatistics(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle(getString(R.string.delete_post))
                .setMessage(getString(R.string.delete_post_message))
                .setPositiveButton(getString(R.string.reset), (dialog, which) -> {
                    MyPrefs.InsertNetworkCount(ConnectionUsageActivity.this);
                    long rxBytes = connectionHelper.getUidRxBytes(UID);
                    long txBytes = connectionHelper.getUidTxBytes(UID);
                    MyPrefs.InsertNetworkStatisticsReset(ConnectionUsageActivity.this, rxBytes, txBytes);
                    loadNetworkUsage();
                    dialog.dismiss();
                })
                .setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
        Dialog mDialog = alert.create();
        mDialog.getWindow().getAttributes().windowAnimations = R.style.MyAlertDialogStyle;
        mDialog.show();
    }

    private void Ids() {
        myAnim = AnimationUtils.loadAnimation(this,R.anim.click_anim);
        getWindow().setStatusBarColor(getColor(R.color.black_intro));
        connectionHelper = new ConnectionHelper(this);
        btn_reset_statistics = findViewById(R.id.btn_reset_statistics);
        txt_total_usage = findViewById(R.id.txt_total_usage);
        txt_sent_amount = findViewById(R.id.txt_sent_amount);
        txt_last_usage_reset = findViewById(R.id.txt_last_usage_reset);
        txt_sent_amount = findViewById(R.id.txt_sent_amount);
        txt_received_amount = findViewById(R.id.txt_received_amount);
        txt_network_usage_since = findViewById(R.id.txt_network_usage_since);
    }


}