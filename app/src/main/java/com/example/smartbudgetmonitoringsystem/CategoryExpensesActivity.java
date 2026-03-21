package com.example.smartbudgetmonitoringsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryExpensesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryExpenseAdapter adapter;
    private DatabaseHelper db;
    private int userId;
    private String category;
    private TextView tvCategoryHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_expenses);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            finish();
            return;
        }

        category = getIntent().getStringExtra("category");
        if (category == null) {
            category = "Other";
        }

        tvCategoryHeader = findViewById(R.id.tvCategoryHeader);
        tvCategoryHeader.setText(category + " Expenses");

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.rvCategoryExpenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadCategoryExpenses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategoryExpenses();
    }

    private void loadCategoryExpenses() {
        List<Expense> expenses = db.getExpensesByCategory(userId, category);
        adapter = new CategoryExpenseAdapter(this, expenses, db, userId, this::loadCategoryExpenses);
        recyclerView.setAdapter(adapter);
    }
}
