package com.example.smartbudgetmonitoringsystem;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    int expenseId = -1;
    AutoCompleteTextView etName;
    TextView tvCategory;
    EditText etAmount, etDate;
    ImageButton btnAddCategory;
    Button btnSave;
    LinearLayout categoryBarLayout;
    DatabaseHelper db;
    SharedPreferences sharedPreferences;
    int userId;
    
    private List<String> categoryList;
    private static final String CHANNEL_ID = "budget_notifications";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notification permission denied. You won't receive budget alerts.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        createNotificationChannel();
        checkNotificationPermission();

        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Use Singleton instance
        db = DatabaseHelper.getInstance(this);

        etName = findViewById(R.id.etExpenseName);
        tvCategory = findViewById(R.id.tvSelectedCategory);
        etAmount = findViewById(R.id.etExpenseAmount);
        etDate = findViewById(R.id.etExpenseDate);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnSave = findViewById(R.id.btnSaveExpense);
        categoryBarLayout = findViewById(R.id.categoryBarLayout);

        categoryList = new ArrayList<>(Arrays.asList(
                "🍔 Food", "🚌 Transport", "🛒 Shopping", "🎬 Entertainment",
                "🏥 Health", "🎓 Education", "🏠 Bills", "🎁 Gifts",
                "✈ Travel", "📦 Other"
        ));

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis());
        etDate.setText(currentDate);

        etDate.setOnClickListener(v -> showDatePicker());
        btnAddCategory.setOnClickListener(v -> showCategoryDialog());
        tvCategory.setOnClickListener(v -> showCategoryDialog());

        setupAutoComplete();
        populateCategoryBar();

        btnSave.setOnClickListener(v -> handleSaveExpense());

        setupBottomNavigation();

        if (getIntent().hasExtra("expense_id")) {
            expenseId = getIntent().getIntExtra("expense_id", -1);
            loadExpenseData(expenseId);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void handleSaveExpense() {
        String name = etName.getText().toString().trim();
        String category = tvCategory.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double budgetLimit = 0;
        Cursor budgetCursor = db.getCurrentMonthBudget(userId);
        if (budgetCursor.moveToFirst()) {
            budgetLimit = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow("monthly_budget"));
        }
        budgetCursor.close();

        if (budgetLimit <= 0) {
            Toast.makeText(this, "Please set your budget before adding expenses.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("OPEN_BUDGET_DIALOG", true);
            startActivity(intent);
            finish();
            return;
        }

        double currentTotal = db.getCurrentMonthTotalExpenses(userId);
        double totalAfterAdd = currentTotal + amount;
        if (expenseId != -1) {
            Cursor oldExp = db.getExpenseById(expenseId, userId);
            if (oldExp.moveToFirst()) {
                totalAfterAdd -= oldExp.getDouble(oldExp.getColumnIndexOrThrow("amount"));
            }
            oldExp.close();
        }

        if (totalAfterAdd > budgetLimit) {
            showExceededDialog();
            sendSystemNotification("Budget Alert", "Attempted expense exceeds 100% of your monthly budget!", true);
            return;
        }

        checkThresholds(totalAfterAdd, budgetLimit);
        saveToDatabase(name, amount, category, date);
    }

    private void checkThresholds(double totalAfterAdd, double budgetLimit) {
        double percentage = (totalAfterAdd / budgetLimit) * 100;
        if (percentage >= 100) {
            String msg = "You have used 100% of your monthly budget!";
            db.addNotification(userId, "Budget Exhausted", msg);
            sendSystemNotification("Budget Exhausted", msg, true);
        } else if (percentage >= 75) {
            String msg = String.format(Locale.getDefault(), "You have reached %.1f%% of your monthly budget.", percentage);
            db.addNotification(userId, "Budget Warning", msg);
            sendSystemNotification("Budget Warning", msg, true);
        }
    }

    private void sendSystemNotification(String title, String message, boolean showBadge) {
        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setNumber(showBadge ? 1 : 0)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            // Permission catch
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Budget Notifications";
            String description = "Alerts for budget usage thresholds";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showExceededDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Budget Exceeded")
                .setMessage("You cannot add this expense as it exceeds your monthly budget. Please increase your budget or reduce expenses.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void saveToDatabase(String name, double amount, String category, String date) {
        boolean success;
        if (expenseId != -1) {
            success = db.updateExpense(expenseId, userId, name, amount, category, date);
        } else {
            success = db.addExpense(userId, name, amount, category, date);
        }

        if (success) {
            Toast.makeText(this, expenseId != -1 ? "Expense updated" : "Expense added", Toast.LENGTH_SHORT).show();
            // Start Dashboard activity and clear the stack to prevent app from "closing"
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_add);
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
                } else if (itemId == R.id.navigation_add) {
                    return true;
                } else if (itemId == R.id.navigation_analytics) {
                    startActivity(new Intent(this, AnalyticsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        }
    }

    private void populateCategoryBar() {
        if (categoryBarLayout == null) return;
        categoryBarLayout.removeAllViews();
        for (String category : categoryList) {
            Button btn = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);
            btn.setText(category);
            btn.setAllCaps(false);
            btn.setBackgroundResource(android.R.drawable.btn_default);
            btn.setOnClickListener(v -> {
                tvCategory.setText(category);
                updateNameSuggestions(category);
            });
            categoryBarLayout.addView(btn);
        }
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    etDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_select, null);
        builder.setView(dialogView);

        EditText etSearch = dialogView.findViewById(R.id.etSearchCategory);
        ListView lvCategories = dialogView.findViewById(R.id.lvCategories);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryList);
        lvCategories.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        lvCategories.setOnItemClickListener((parent, view, position, id) -> {
            String selected = adapter.getItem(position);
            tvCategory.setText(selected);
            updateNameSuggestions(selected);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupAutoComplete() {
        etName.setThreshold(1);
    }

    private void updateNameSuggestions(String category) {
        String[] suggestions;
        String lowerCategory = category.toLowerCase();

        if (lowerCategory.contains("food")) {
            suggestions = new String[]{"apple", "burger", "biryani", "bread", "banana", "biscuit", "butter", "cake", "chicken", "coffee", "dosa", "egg", "fish", "fries", "juice", "milk", "noodles", "pizza", "rice", "sandwich", "shawarma", "tea"};
        } else if (lowerCategory.contains("transport")) {
            suggestions = new String[]{"bus", "train", "taxi", "auto", "uber", "metro", "petrol", "diesel", "bike fuel", "parking", "toll", "bus ticket", "train ticket"};
        } else if (lowerCategory.contains("shopping")) {
            suggestions = new String[]{"clothes", "shoes", "shirt", "pants", "tshirt", "jacket", "watch", "bag", "mobile case", "electronics", "charger", "headphones"};
        } else if (lowerCategory.contains("entertainment")) {
            suggestions = new String[]{"movie", "netflix", "amazon prime", "hotstar", "game", "concert", "cinema ticket", "theme park", "music subscription"};
        } else if (lowerCategory.contains("health")) {
            suggestions = new String[]{"medicine", "doctor", "hospital", "clinic", "pharmacy", "vitamins", "health checkup", "dental care", "eye checkup"};
        } else if (lowerCategory.contains("education")) {
            suggestions = new String[]{"books", "notebooks", "tuition", "course fee", "exam fee", "stationery", "pen", "pencil", "online course"};
        } else if (lowerCategory.contains("bills")) {
            suggestions = new String[]{"electricity bill", "water bill", "internet bill", "wifi bill", "mobile recharge", "rent", "gas bill", "maintenance"};
        } else if (lowerCategory.contains("travel")) {
            suggestions = new String[]{"flight ticket", "hotel booking", "resort", "tour package", "luggage", "visa fee"};
        } else if (lowerCategory.contains("gifts")) {
            suggestions = new String[]{"birthday gift", "anniversary gift", "wedding gift", "flowers", "chocolates", "gift card"};
        } else if (lowerCategory.contains("other")) {
            suggestions = new String[]{"donation", "charity", "subscription", "repair", "service", "miscellaneous", "other expense"};
        } else {
            suggestions = new String[]{};
        }

        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        etName.setAdapter(nameAdapter);
    }

    private void loadExpenseData(int id) {
        Cursor cursor = db.getExpenseById(id, userId);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String amount = cursor.getString(cursor.getColumnIndexOrThrow("amount"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            
            etName.setText(name);
            etAmount.setText(amount);
            tvCategory.setText(category);
            etDate.setText(date);
            updateNameSuggestions(category);
        }
        cursor.close();
    }
}
