package com.example.smartbudgetmonitoringsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName;
    private ImageView ivProfilePic;
    private RelativeLayout sectionUser, itemEditProfile, itemChangePassword, itemLanguage, btnLogout, itemNotifications, itemPeerComparison;
    private SwitchMaterial switchDarkMode, switchNotifications;
    private DatabaseHelper db;
    private SharedPreferences sharedPreferences;
    private SharedPreferences settingsPrefs;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isDarkMode = settingsPrefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            redirectToLogin();
            return;
        }

        tvUserName = findViewById(R.id.tvUserName);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        sectionUser = findViewById(R.id.sectionUser);
        itemEditProfile = findViewById(R.id.itemEditProfile);
        itemChangePassword = findViewById(R.id.itemChangePassword);
        itemLanguage = findViewById(R.id.itemLanguage);
        btnLogout = findViewById(R.id.btnLogout);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotifications = findViewById(R.id.switchNotifications);
        itemNotifications = findViewById(R.id.itemNotifications);
        itemPeerComparison = findViewById(R.id.itemPeerComparison);

        loadUserData();
        setupSettings();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        Cursor cursor = db.getUserById(userId);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            tvUserName.setText(name);
            cursor.close();
        }
    }

    private void setupSettings() {
        switchDarkMode.setChecked(settingsPrefs.getBoolean("dark_mode", false));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        switchNotifications.setChecked(settingsPrefs.getBoolean("notifications", true));
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putBoolean("notifications", isChecked);
            editor.apply();
        });

        itemNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        itemPeerComparison.setOnClickListener(v -> {
            startActivity(new Intent(this, PeerComparisonActivity.class));
        });

        View.OnClickListener editProfileListener = v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        };

        sectionUser.setOnClickListener(editProfileListener);
        itemEditProfile.setOnClickListener(editProfileListener);

        itemChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        itemLanguage.setOnClickListener(v -> {
            Toast.makeText(this, "Language selection coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            redirectToLogin();
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_expenses) {
                    startActivity(new Intent(this, ViewExpenseActivity.class));
                    finish();
                    return true;
                //} else if (itemId == R.id.navigation_analytics) {
                    //startActivity(new Intent(this, AnalyticsActivity.class));
                    //finish();
                    //return true;
                } else if (itemId == R.id.navigation_peers) {
                    startActivity(new Intent(this, PeerComparisonActivity.class));
                    finish();
                    return true;
                }
                return itemId == R.id.navigation_profile;
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
