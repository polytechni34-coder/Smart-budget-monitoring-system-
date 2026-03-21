package com.example.smartbudgetmonitoringsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    Spinner spinnerFilter;
    PieChart pieChart;
    LineChart lineChart;
    DatabaseHelper db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            finish();
            return;
        }

        spinnerFilter = findViewById(R.id.spinnerFilter);
        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart);

        db = new DatabaseHelper(this);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = parent.getItemAtPosition(position).toString();
                if (filter.equals("Week")) {
                    updateCharts(db.getExpensesLastWeek(userId));
                } else if (filter.equals("Month")) {
                    updateCharts(db.getExpensesCurrentMonth(userId));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
           // bottomNavigationView.setSelectedItemId(R.id.navigation_analytics);
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
               // } else if (itemId == R.id.navigation_analytics) {
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

    private void updateCharts(List<Expense> expenses) {
        // --- Pie Chart ---
        Map<String, Float> categorySum = new HashMap<>();
        for (Expense e : expenses) {
            float prev = categorySum.getOrDefault(e.getCategory(), 0f);
            categorySum.put(e.getCategory(), prev + (float) e.getAmount());
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categorySum.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Expense Categories");
        pieDataSet.setColors(new int[]{Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN});
        pieDataSet.setValueTextSize(14f);
        pieDataSet.setSliceSpace(2f);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setCenterText("Expenses");
        pieChart.animateY(1000);
        pieChart.invalidate();

        // --- Line Chart ---
        List<Entry> lineEntries = new ArrayList<>();
        for (int i = 0; i < expenses.size(); i++) {
            lineEntries.add(new Entry(i, (float) expenses.get(i).getAmount()));
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Expenses Over Time");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setValueTextSize(12f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

}
