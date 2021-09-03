package dev.kaua.squash.Notifications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.kaua.squash.Activitys.MainActivity;
import dev.kaua.squash.Adapters.NotificationAdapter;
import dev.kaua.squash.LocalDataBase.Notification.DaoNotification;
import dev.kaua.squash.R;

public class NotificationActivity extends AppCompatActivity {
    RecyclerView recycler_notification;
    TextView txt_no_notification;
    SwipeRefreshLayout swipe_notification;
    DaoNotification daoNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Ids();

        swipe_notification.setOnRefreshListener(this::LoadNotification);

        LoadNotification();
    }

    void Ids() {
        getWindow().setStatusBarColor(getColor(R.color.black_intro));
        recycler_notification = findViewById(R.id.recycler_notification);
        swipe_notification = findViewById(R.id.swipe_notification);
        txt_no_notification = findViewById(R.id.txt_no_notification);
        daoNotification = new DaoNotification(this);
        Toolbar toolbar = findViewById(R.id.toolbar_notifications);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default toolbar title
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    void ReadAllNotification() {
        daoNotification.Read_All_Notification();
    }

    void LoadNotification() {
        List<Data> list = daoNotification.getNotifications();
        recycler_notification.setLayoutManager(new LinearLayoutManager(this));
        if(list.size() > 0){
            NotificationAdapter notificationAdapter = new NotificationAdapter(this, list);
            recycler_notification.setAdapter(notificationAdapter);
            txt_no_notification.setVisibility(View.GONE);
            ReadAllNotification();
        }else {
            txt_no_notification.setVisibility(View.VISIBLE);
        }
        swipe_notification.setRefreshing(false);
    }
}