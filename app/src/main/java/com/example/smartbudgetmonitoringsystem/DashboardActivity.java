package com.example.smartbudgetmonitoringsystem;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private PieChart pieChart;
    private DatabaseHelper db;
    private int budgetId = -1;
    private double monthlyBudget = 0;
    private double totalSpent = 0;
    private int userId;
    private boolean isMonthlyView = true;

    private TextView tvBudgetAmount, tvSpentAmount, tvRemainingAmount, tvUsername, tvBudgetLabel;
    private Button btnSetBudget;
    private ImageButton btnEditBudget;
    private MaterialButtonToggleGroup toggleGroup;

    private RelativeLayout advisorCardContainer;
    private LinearLayout advisorCard;
    private TextView tvAdvisorEmoji, tvAdvisorTitle, tvAdvisorMessage, tvUsedPercent;
    private ImageButton btnDismissAdvisor;
    
    private View categoryBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        db = new DatabaseHelper(this);

        pieChart = findViewById(R.id.pieChart);
        tvBudgetAmount = findViewById(R.id.tv_total_budget_amount);
        tvSpentAmount = findViewById(R.id.tv_spent_amount);
        tvRemainingAmount = findViewById(R.id.tv_remaining_amount);
        tvUsername = findViewById(R.id.tv_username);
        tvBudgetLabel = findViewById(R.id.tv_total_budget_label);
        btnSetBudget = findViewById(R.id.btn_set_budget);
        btnEditBudget = findViewById(R.id.btn_edit_budget);
        toggleGroup = findViewById(R.id.toggleGroup);
        categoryBadge = findViewById(R.id.categoryBadge);

        // Categories Button logic
        findViewById(R.id.btnCategories).setOnClickListener(v -> 
                startActivity(new Intent(this, ViewExpenseActivity.class)));

        // Peer Comparison Entry Point
        findViewById(R.id.btnOpenPeerComparison).setOnClickListener(v -> 
                startActivity(new Intent(this, PeerComparisonActivity.class)));

        // Smart Advisor Views
        advisorCardContainer = findViewById(R.id.advisorCardContainer);
        advisorCard = findViewById(R.id.advisorCard);
        tvAdvisorEmoji = findViewById(R.id.tvAdvisorEmoji);
        tvAdvisorTitle = findViewById(R.id.tvAdvisorTitle);
        tvAdvisorMessage = findViewById(R.id.tvAdvisorMessage);
        btnDismissAdvisor = findViewById(R.id.btnDismissAdvisor);
        tvUsedPercent = findViewById(R.id.tv_used_percent);

        btnDismissAdvisor.setOnClickListener(v -> advisorCardContainer.setVisibility(View.GONE));

        loadDashboardData();
        setupPieChart();

        btnSetBudget.setOnClickListener(v -> showBudgetDialog(false));
        btnEditBudget.setOnClickListener(v -> showBudgetDialog(true));

        if (getIntent().getBooleanExtra("OPEN_BUDGET_DIALOG", false)) {
            showBudgetDialog(false);
        }

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnWeekly) {
                    isMonthlyView = false;
                } else if (checkedId == R.id.btnMonthly) {
                    isMonthlyView = true;
                }
                loadDashboardData();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_expenses) {
                startActivity(new Intent(this, ViewExpenseActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_analytics) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_peers) {
                startActivity(new Intent(this, PeerComparisonActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });

        // Clear notifications/badges when app is opened
        clearNotifications();
    }

    private void clearNotifications() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
        updateCategoryBadge();
    }

    private void updateCategoryBadge() {
        if (categoryBadge == null) return;
        int count = db.getExpenseCount(userId);
        if (count > 0) {
            categoryBadge.setVisibility(View.VISIBLE);
        } else {
            categoryBadge.setVisibility(View.GONE);
        }
    }

    private void loadDashboardData() {
        // Set Greeting
        TextView tvGreeting = findViewById(R.id.tv_greeting);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) tvGreeting.setText("Good morning 👋");
        else if (hour < 17) tvGreeting.setText("Good afternoon 👋");
        else tvGreeting.setText("Good evening 👋");

        // Load User Name
        Cursor userCursor = db.getUserById(userId);
        if (userCursor.moveToFirst()) {
            String name = userCursor.getString(userCursor.getColumnIndexOrThrow("name"));
            tvUsername.setText(name);
        }
        userCursor.close();

        // Load Monthly Budget
        Cursor budgetCursor = db.getCurrentMonthBudget(userId);
        if (budgetCursor.moveToFirst()) {
            budgetId = budgetCursor.getInt(budgetCursor.getColumnIndexOrThrow("id"));
            monthlyBudget = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow("monthly_budget"));
            btnSetBudget.setVisibility(View.GONE);
            btnEditBudget.setVisibility(View.VISIBLE);
        } else {
            budgetId = -1;
            monthlyBudget = 0;
            btnSetBudget.setVisibility(View.VISIBLE);
            btnEditBudget.setVisibility(View.GONE);
        }
        budgetCursor.close();

        double displayedBudget;
        List<Expense> expenses;
        if (isMonthlyView) {
            totalSpent = db.getCurrentMonthTotalExpenses(userId);
            displayedBudget = monthlyBudget;
            tvBudgetLabel.setText("Monthly Budget");
            expenses = db.getExpensesCurrentMonth(userId);
        } else {
            totalSpent = db.getWeeklyTotalExpenses(userId);
            displayedBudget = monthlyBudget / 4.0;
            tvBudgetLabel.setText("Estimated Weekly Budget");
            expenses = db.getExpensesLastWeek(userId);
        }

        tvBudgetAmount.setText(String.format(Locale.getDefault(), "₹%.2f", displayedBudget));
        tvSpentAmount.setText(String.format(Locale.getDefault(), "₹%.2f", totalSpent));
        tvRemainingAmount.setText(String.format(Locale.getDefault(), "₹%.2f", displayedBudget - totalSpent));

        // Update Used Percent
        if (tvUsedPercent != null) {
            int usedPct = displayedBudget > 0 ? (int) ((totalSpent / displayedBudget) * 100) : 0;
            tvUsedPercent.setText(usedPct + "%");
        }
        
        pieChart.setCenterText((isMonthlyView ? "Monthly" : "Weekly") + "\nSpent\n" + String.format(Locale.getDefault(), "₹%.2f", totalSpent));
        
        updateChartsData(expenses);

        // Show Smart Advisor
        showSmartAdvisor(totalSpent, displayedBudget, !isMonthlyView);
    }

    private void showSmartAdvisor(double spent, double budget, boolean isWeekly) {
        SmartAdvisor.Advice advice = SmartAdvisor.getAdvice(spent, budget, isWeekly);
        if (advice == null) {
            advisorCardContainer.setVisibility(View.GONE);
            return;
        }

        int bgRes;
        int titleColor;

        switch (advice.colorType) {
            case 0: // info
                bgRes = R.drawable.bg_advisor_info;
                titleColor = Color.parseColor("#9B94FF");
                break;
            case 1: // warning
                bgRes = R.drawable.bg_advisor_warning;
                titleColor = Color.parseColor("#FFB347");
                break;
            case 2: // danger
                bgRes = R.drawable.bg_advisor_danger;
                titleColor = Color.parseColor("#FF6B6B");
                break;
            case 3: // good
                bgRes = R.drawable.bg_advisor_good;
                titleColor = Color.parseColor("#00D2A0");
                break;
            default:
                bgRes = R.drawable.bg_advisor_info;
                titleColor = Color.parseColor("#9B94FF");
        }

        advisorCard.setBackgroundResource(bgRes);
        tvAdvisorEmoji.setText(advice.emoji);
        tvAdvisorTitle.setText(advice.title);
        tvAdvisorTitle.setTextColor(titleColor);
        tvAdvisorMessage.setText(advice.message);
        advisorCardContainer.setVisibility(View.VISIBLE);

        if (!db.advisorNotificationExistsToday(userId, advice.title)) {
            db.addAdvisorNotification(userId, advice.title, advice.message, advice.emoji, 1, advice.colorType);
        }

        checkAndStorePeriodSummary(spent, budget, isWeekly);
    }

    private void checkAndStorePeriodSummary(double spent, double budget, boolean isWeekly) {
        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        boolean isMonthEnd = dayOfMonth >= totalDays - 2;
        boolean isWeekEnd = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;

        if (!((isWeekly && isWeekEnd) || (!isWeekly && isMonthEnd))) return;

        SmartAdvisor.Advice summary = SmartAdvisor.getSummaryAdvice(spent, budget, isWeekly);
        if (summary == null) return;

        String summaryTitle = summary.title + (isWeekly ? " [W]" : " [M]");
        if (!db.advisorNotificationExistsToday(userId, summaryTitle)) {
            db.addAdvisorNotification(userId, summaryTitle, summary.message, summary.emoji, 1, summary.colorType);
        }
    }

    private void updateChartsData(List<Expense> expenses) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense expense : expenses) {
            String category = expense.getCategory();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + expense.getAmount());
        }

        updatePieChartData(categoryTotals);
    }

    private void showBudgetDialog(boolean isUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isUpdate ? "Update Monthly Budget" : "Set Monthly Budget");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (isUpdate) input.setText(String.valueOf(monthlyBudget));
        builder.setView(input);

        builder.setPositiveButton(isUpdate ? "Update" : "Set", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());
                if (isUpdate) {
                    db.updateBudget(budgetId, userId, amount);
                } else {
                    db.addBudget(userId, amount, month);
                }
                loadDashboardData();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        if (isUpdate) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                db.deleteBudget(budgetId, userId);
                loadDashboardData();
            });
        }
        builder.show();
    }

    private void setupPieChart() {
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        pieChart.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        pieChart.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setDrawInside(false);
    }

    private void updatePieChartData(Map<String, Double> categoryTotals) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (categoryTotals.isEmpty()) {
            entries.add(new PieEntry(1, "No Data"));
        } else {
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#FF6B6B"), Color.parseColor("#4D96FF"), Color.parseColor("#FFD93D"), Color.parseColor("#6BCB77"), Color.parseColor("#9C27B0")});
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }
}
