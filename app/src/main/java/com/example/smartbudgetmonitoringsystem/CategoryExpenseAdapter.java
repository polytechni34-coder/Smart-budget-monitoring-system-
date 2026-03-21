package com.example.smartbudgetmonitoringsystem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryExpenseAdapter extends RecyclerView.Adapter<CategoryExpenseAdapter.ViewHolder> {

    private Context context;
    private List<Expense> expenses;
    private DatabaseHelper db;
    private int userId;
    private Runnable onDeleteCallback;

    public CategoryExpenseAdapter(Context context, List<Expense> expenses, DatabaseHelper db, int userId, Runnable onDeleteCallback) {
        this.context = context;
        this.expenses = expenses;
        this.db = db;
        this.userId = userId;
        this.onDeleteCallback = onDeleteCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.expense_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.tvName.setText(expense.getName());
        holder.tvCategory.setText(expense.getCategory());
        holder.tvAmount.setText(String.format("₹%.2f", expense.getAmount()));
        holder.tvDate.setText(expense.getDate());

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddExpenseActivity.class);
            intent.putExtra("expense_id", expense.getId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (db.deleteExpense(expense.getId(), userId)) {
                expenses.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, expenses.size());
                Toast.makeText(context, "Expense deleted", Toast.LENGTH_SHORT).show();
                if (onDeleteCallback != null) {
                    onDeleteCallback.run();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvAmount, tvDate;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
