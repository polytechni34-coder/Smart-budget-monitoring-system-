package com.example.smartbudgetmonitoringsystem;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;

public class CategoryItemsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryItemAdapter adapter;
    private ArrayList<String> itemList;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_items);

        category = getIntent().getStringExtra("category");
        TextView tvTitle = findViewById(R.id.tvCategoryTitle);
        tvTitle.setText(category + " Items");

        recyclerView = findViewById(R.id.rvCategoryItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = getPredefinedItems(category);
        adapter = new CategoryItemAdapter(itemList);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<String> getPredefinedItems(String category) {
        String lowerCategory = category.toLowerCase();
        String[] items;

        if (lowerCategory.contains("food")) {
            items = new String[]{"apple", "burger", "biryani", "bread", "banana", "biscuit", "butter", "cake", "chicken", "coffee", "dosa", "egg", "fish", "fries", "juice", "milk", "noodles", "pizza", "rice", "sandwich", "shawarma", "tea"};
        } else if (lowerCategory.contains("transport")) {
            items = new String[]{"bus", "train", "taxi", "auto", "uber", "metro", "petrol", "diesel", "bike fuel", "parking", "toll", "bus ticket", "train ticket"};
        } else if (lowerCategory.contains("shopping")) {
            items = new String[]{"clothes", "shoes", "shirt", "pants", "tshirt", "jacket", "watch", "bag", "mobile case", "electronics", "charger", "headphones"};
        } else if (lowerCategory.contains("entertainment")) {
            items = new String[]{"movie", "netflix", "amazon prime", "hotstar", "game", "concert", "cinema ticket", "theme park", "music subscription"};
        } else if (lowerCategory.contains("health")) {
            items = new String[]{"medicine", "doctor", "hospital", "clinic", "pharmacy", "vitamins", "health checkup", "dental care", "eye checkup"};
        } else if (lowerCategory.contains("education")) {
            items = new String[]{"books", "notebooks", "tuition", "course fee", "exam fee", "stationery", "pen", "pencil", "online course"};
        } else if (lowerCategory.contains("bills")) {
            items = new String[]{"Electricity Bill", "Water Bill", "Internet Bill", "Mobile Recharge", "Rent", "Gas Bill"};
        } else if (lowerCategory.contains("travel")) {
            items = new String[]{"flight ticket", "hotel booking", "resort", "tour package", "luggage", "visa fee"};
        } else if (lowerCategory.contains("gifts")) {
            items = new String[]{"birthday gift", "anniversary gift", "wedding gift", "flowers", "chocolates", "gift card"};
        } else {
            items = new String[]{"donation", "charity", "subscription", "repair", "service", "miscellaneous", "other expense"};
        }

        return new ArrayList<>(Arrays.asList(items));
    }
}
