package com.example.smartbudgetmonitoringsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PeerComparisonActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int userId;
    private boolean isMonthly = true;
    private List<Integer> peerIds = new ArrayList<>();
    private List<String> peerNames = new ArrayList<>();
    private List<PeerComparisonData> comparisonList = new ArrayList<>();
    
    private TextView tvInviteCode, tvConnectStatus, tvNoPeers, tvNoComparison;
    private EditText etPeerCode;
    private Button btnConnect, btnToggleMonth, btnToggleWeek;
    private RecyclerView rvPeers, rvComparison;
    private PeerListAdapter peerListAdapter;
    private PeerComparisonAdapter comparisonAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_comparison);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            finish();
            return;
        }

        db = new DatabaseHelper(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        tvInviteCode = findViewById(R.id.tvInviteCode);
        etPeerCode = findViewById(R.id.etPeerCode);
        btnConnect = findViewById(R.id.btnConnect);
        tvConnectStatus = findViewById(R.id.tvConnectStatus);
        tvNoPeers = findViewById(R.id.tvNoPeers);
        tvNoComparison = findViewById(R.id.tvNoComparison);
        btnToggleMonth = findViewById(R.id.btnToggleMonth);
        btnToggleWeek = findViewById(R.id.btnToggleWeek);
        rvPeers = findViewById(R.id.rvPeers);
        rvComparison = findViewById(R.id.rvComparison);

        btnBack.setOnClickListener(v -> finish());

        // Load or generate invite code
        String code = db.generateInviteCode(userId);
        tvInviteCode.setText(code);

        btnConnect.setOnClickListener(v -> {
            String entered = etPeerCode.getText().toString().trim().toUpperCase();
            if (entered.isEmpty()) {
                tvConnectStatus.setText("Please enter a code");
                tvConnectStatus.setTextColor(Color.parseColor("#FF6B6B"));
                tvConnectStatus.setVisibility(View.VISIBLE);
                return;
            }
            boolean success = db.connectPeers(userId, entered);
            if (success) {
                tvConnectStatus.setText("✅ Connected successfully!");
                tvConnectStatus.setTextColor(Color.parseColor("#00D2A0"));
                etPeerCode.setText("");
                loadPeers();
                loadComparison();
            } else {
                tvConnectStatus.setText("❌ Invalid code or already connected");
                tvConnectStatus.setTextColor(Color.parseColor("#FF6B6B"));
            }
            tvConnectStatus.setVisibility(View.VISIBLE);
        });

        btnToggleMonth.setOnClickListener(v -> {
            isMonthly = true;
            btnToggleMonth.setBackgroundResource(R.drawable.bg_chip_selected);
            btnToggleWeek.setBackgroundResource(R.drawable.bg_chip_default);
            btnToggleMonth.setTextColor(Color.WHITE);
            btnToggleWeek.setTextColor(Color.parseColor("#8892AA"));
            loadComparison();
        });

        btnToggleWeek.setOnClickListener(v -> {
            isMonthly = false;
            btnToggleWeek.setBackgroundResource(R.drawable.bg_chip_selected);
            btnToggleMonth.setBackgroundResource(R.drawable.bg_chip_default);
            btnToggleWeek.setTextColor(Color.WHITE);
            btnToggleMonth.setTextColor(Color.parseColor("#8892AA"));
            loadComparison();
        });

        // Setup RVs
        rvPeers.setLayoutManager(new LinearLayoutManager(this));
        peerListAdapter = new PeerListAdapter(peerNames, peerIds, (peerUserId, position) -> {
            db.removePeer(userId, peerUserId);
            peerIds.remove(position);
            peerNames.remove(position);
            peerListAdapter.notifyItemRemoved(position);
            loadComparison();
            if (peerIds.isEmpty()) tvNoPeers.setVisibility(View.VISIBLE);
        });
        rvPeers.setAdapter(peerListAdapter);

        rvComparison.setLayoutManager(new LinearLayoutManager(this));
        comparisonAdapter = new PeerComparisonAdapter(comparisonList);
        rvComparison.setAdapter(comparisonAdapter);

        loadPeers();
        loadComparison();
    }

    private void loadPeers() {
        peerIds.clear();
        peerNames.clear();
        List<Integer> ids = db.getPeerIds(userId);
        for (int id : ids) {
            peerNames.add(db.getPeerName(id));
        }
        peerIds.addAll(ids);
        peerListAdapter.notifyDataSetChanged();
        tvNoPeers.setVisibility(peerIds.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadComparison() {
        comparisonList.clear();
        if (peerIds.isEmpty()) {
            tvNoComparison.setVisibility(View.VISIBLE);
            comparisonAdapter.notifyDataSetChanged();
            return;
        }
        tvNoComparison.setVisibility(View.GONE);

        double mySpent = db.getTotalSpentForUser(userId, isMonthly);
        double myBudget = db.getBudgetForUser(userId);
        Map<String, Double> myCats = db.getCategoryTotalsForUser(userId, isMonthly);

        for (int peerId : peerIds) {
            double peerSpent = db.getTotalSpentForUser(peerId, isMonthly);
            double peerBudget = db.getBudgetForUser(peerId);
            Map<String, Double> peerCats = db.getCategoryTotalsForUser(peerId, isMonthly);
            String name = db.getPeerName(peerId);
            
            comparisonList.add(new PeerComparisonData(name, mySpent, myBudget, peerSpent, peerBudget, myCats, peerCats));
        }
        comparisonAdapter.notifyDataSetChanged();
    }
}
