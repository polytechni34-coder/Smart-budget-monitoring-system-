package com.example.smartbudgetmonitoringsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList;
    private DatabaseHelper db;
    private int userId;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sp.getInt("userId", -1);

        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmptyNotifications);
        ImageButton btnBack = findViewById(R.id.btnBackNotifications);

        btnBack.setOnClickListener(v -> finish());

        notificationList = new ArrayList<>();
        loadNotifications();

        adapter = new NotificationsAdapter(notificationList, (notificationId, position) -> {
            if (db.deleteNotification(notificationId)) {
                notificationList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, notificationList.size());
                
                if (notificationList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
                Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete notification", Toast.LENGTH_SHORT).show();
            }
        });
        
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        db.markNotificationsAsRead(userId);
    }

    private void loadNotifications() {
        notificationList.clear();
        Cursor cursor = db.getNotifications(userId);
        if (cursor.moveToFirst()) {
            do {
                notificationList.add(new Notification(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("message")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (notificationList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
