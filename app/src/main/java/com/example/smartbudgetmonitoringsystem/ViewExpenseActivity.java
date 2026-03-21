package com.example.smartbudgetmonitoringsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewExpenseActivity extends AppCompatActivity {

    DatabaseHelper db;
    private ProgressBar budgetProgressBar;
    private TextView tvBudgetAmount, tvTotalSpent, tvRemainingAmount;
    private RelativeLayout budgetInfoContainer;
    private SharedPreferences sharedPreferences;
    private int userId;
    
    private BarChart barChart;
    private ListView lvExpenses;
    private ExpenseAdapter expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expense);

        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = new DatabaseHelper(this);

        budgetProgressBar = findViewById(R.id.budgetProgressBar);
        tvBudgetAmount = findViewById(R.id.tvBudgetAmount);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        budgetInfoContainer = findViewById(R.id.budgetInfoContainer);
        barChart = findViewById(R.id.barChart);
        lvExpenses = findViewById(R.id.lvExpenses);

        budgetInfoContainer.setOnClickListener(v -> showSpentAmountPopup());

        setupCategoryButtons();
        setupBarChart();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_expenses);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_expenses) {
                    return true;
                //} else if (itemId == R.id.navigation_analytics) {
                    //startActivity(new Intent(this, AnalyticsActivity.class));
                    //finish();
                    //return true;
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
        }
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.getLegend().setEnabled(false);
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
    }

    private void updateBarChart(List<Expense> expenses) {
        String[] categories = {"Food", "Transport", "Shopping", "Entertainment", "Health", "Education", "Bills", "Gifts", "Travel", "Other"};
        Map<String, Float> categoryTotals = new HashMap<>();
        
        for (String cat : categories) {
            categoryTotals.put(cat, 0f);
        }
        
        for (Expense e : expenses) {
            String cat = e.getCategory();
            categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0f) + (float) e.getAmount());
        }
        
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < categories.length; i++) {
            entries.add(new BarEntry(i, categoryTotals.get(categories[i])));
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "Spending by Category");
        dataSet.setColors(new int[]{
            Color.parseColor("#FF6B6B"), Color.parseColor("#4D96FF"), 
            Color.parseColor("#FFD93D"), Color.parseColor("#6BCB77"),
            Color.parseColor("#9C27B0"), Color.parseColor("#4A90E2"),
            Color.parseColor("#FF8A65"), Color.parseColor("#BA68C8"),
            Color.parseColor("#4DB6AC"), Color.parseColor("#90A4AE")
        });
        dataSet.setValueTextSize(10f);
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(categories));
        barChart.getXAxis().setLabelCount(categories.length);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void setupCategoryButtons() {
        findViewById(R.id.btnBills).setOnClickListener(v -> openCategoryExpenses("Bills"));
        findViewById(R.id.btnFood).setOnClickListener(v -> openCategoryExpenses("Food"));
        findViewById(R.id.btnShopping).setOnClickListener(v -> openCategoryExpenses("Shopping"));
        findViewById(R.id.btnTransport).setOnClickListener(v -> openCategoryExpenses("Transport"));
        findViewById(R.id.btnEntertainment).setOnClickListener(v -> openCategoryExpenses("Entertainment"));
        findViewById(R.id.btnHealth).setOnClickListener(v -> openCategoryExpenses("Health"));
        findViewById(R.id.btnEducation).setOnClickListener(v -> openCategoryExpenses("Education"));
        findViewById(R.id.btnGifts).setOnClickListener(v -> openCategoryExpenses("Gifts"));
        findViewById(R.id.btnTravel).setOnClickListener(v -> openCategoryExpenses("Travel"));
        findViewById(R.id.btnOther).setOnClickListener(v -> openCategoryExpenses("Other"));
    }

    private void openCategoryExpenses(String category) {
        Intent intent = new Intent(this, CategoryExpensesActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBudgetProgress();
        loadRecentExpenses();
    }

    private void loadRecentExpenses() {
        List<Expense> expenses = db.getAllExpenses(userId);
        expenseAdapter = new ExpenseAdapter(this, expenses, db, this::refreshBudgetProgress);
        lvExpenses.setAdapter(expenseAdapter);
        updateBarChart(expenses);
    }

    private void refreshBudgetProgress() {
        Cursor budgetCursor = db.getCurrentMonthBudget(userId);
        double budgetAmount = 0;
        if (budgetCursor.moveToFirst()) {
            budgetAmount = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow("monthly_budget"));
        }
        budgetCursor.close();

        double totalSpent = db.getCurrentMonthTotalExpenses(userId);
        double remainingAmount = budgetAmount - totalSpent;

        tvBudgetAmount.setText(String.format(Locale.getDefault(), "Budget: ₹%.2f", budgetAmount));
        tvTotalSpent.setText(String.format(Locale.getDefault(), "Spent: ₹%.2f", totalSpent));
        tvRemainingAmount.setText(String.format(Locale.getDefault(), "Remaining: ₹%.2f", remainingAmount));

        int progress = 0;
        if (budgetAmount > 0) {
            progress = (int) ((totalSpent / budgetAmount) * 100);
        }
        budgetProgressBar.setProgress(progress);

        Drawable progressDrawable;
        if (progress < 80) {
            progressDrawable = ContextCompat.getDrawable(this, R.drawable.custom_progress_bar);
        } else if (progress <= 100) {
            progressDrawable = ContextCompat.getDrawable(this, R.drawable.custom_progress_bar_yellow);
        } else {
            progressDrawable = ContextCompat.getDrawable(this, R.drawable.custom_progress_bar_red);
        }
        budgetProgressBar.setProgressDrawable(progressDrawable);
    }

    private void showSpentAmountPopup() {
        double totalSpent = db.getCurrentMonthTotalExpenses(userId);
        new AlertDialog.Builder(this)
                .setTitle("Total Spent")
                .setMessage(String.format(Locale.getDefault(), "You have spent ₹%.2f so far this month.", totalSpent))
                .setPositiveButton("OK", null)
                .show();
    }
}
