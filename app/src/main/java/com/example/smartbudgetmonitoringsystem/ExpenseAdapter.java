package com.example.smartbudgetmonitoringsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ExpenseAdapter extends ArrayAdapter<Expense> {

    private Context context;
    private List<Expense> expenses;
    private DatabaseHelper db;
    private Runnable budgetRefreshCallback;
    private int userId;

    public ExpenseAdapter(Context context, List<Expense> expenses, DatabaseHelper db, Runnable budgetRefreshCallback) {
        super(context, 0, expenses);
        this.context = context;
        this.expenses = expenses;
        this.db = db;
        this.budgetRefreshCallback = budgetRefreshCallback;

        SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        this.userId = sharedPreferences.getInt("userId", -1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.expense_item_view_only, parent, false);
        }

        Expense currentExpense = expenses.get(position);

        TextView name = listItem.findViewById(R.id.tvName);
        name.setText(currentExpense.getName());

        TextView category = listItem.findViewById(R.id.tvCategory);
        category.setText(currentExpense.getCategory());

        // Set click listener on category text to open CategoryExpensesActivity
        category.setOnClickListener(v -> {
            Intent intent = new Intent(context, CategoryExpensesActivity.class);
            intent.putExtra("category", currentExpense.getCategory());
            context.startActivity(intent);
        });

        TextView amount = listItem.findViewById(R.id.tvAmount);
        amount.setText(String.format("₹%.2f", currentExpense.getAmount()));

        TextView date = listItem.findViewById(R.id.tvDate);
        date.setText(currentExpense.getDate());

        return listItem;
    }
}
