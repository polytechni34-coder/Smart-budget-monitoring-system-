package com.example.smartbudgetmonitoringsystem;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PeerComparisonAdapter extends RecyclerView.Adapter<PeerComparisonAdapter.ComparisonHolder> {

    private List<PeerComparisonData> comparisonList;

    public PeerComparisonAdapter(List<PeerComparisonData> comparisonList) {
        this.comparisonList = comparisonList;
    }

    @NonNull
    @Override
    public ComparisonHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_peer_comparison, parent, false);
        return new ComparisonHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ComparisonHolder holder, int position) {
        PeerComparisonData data = comparisonList.get(position);
        
        holder.tvPeerName.setText(data.getPeerName());
        holder.tvMyAmount.setText("₹" + (int)data.getMySpent());
        
        String myPctText = data.getMyBudget() > 0 ? 
                (int)((data.getMySpent() / data.getMyBudget()) * 100) + "% of budget" : "No budget set";
        holder.tvMyPercent.setText(myPctText);

        holder.tvPeerAmount.setText("₹" + (int)data.getPeerSpent());
        String peerPctText = data.getPeerBudget() > 0 ? 
                (int)((data.getPeerSpent() / data.getPeerBudget()) * 100) + "% of budget" : "No budget set";
        holder.tvPeerPercent.setText(peerPctText);

        double myPct = data.getMyBudget() > 0 ? (data.getMySpent() / data.getMyBudget()) : 0;
        double peerPct = data.getPeerBudget() > 0 ? (data.getPeerSpent() / data.getPeerBudget()) : 0;

        if (myPct < peerPct) {
            holder.tvWinner.setText("🏆 You saved more this period!");
            holder.tvWinner.setTextColor(Color.parseColor("#00D2A0"));
        } else if (myPct == peerPct) {
            holder.tvWinner.setText("🤝 You both spent equally!");
            holder.tvWinner.setTextColor(Color.parseColor("#9B94FF"));
        } else {
            holder.tvWinner.setText("📈 Peer spent less than you this period");
            holder.tvWinner.setTextColor(Color.parseColor("#FFB347"));
        }

        // Category Breakdown
        holder.categoryContainer.removeAllViews();
        Set<String> allCategories = new HashSet<>();
        allCategories.addAll(data.getMyCategories().keySet());
        allCategories.addAll(data.getPeerCategories().keySet());

        for (String cat : allCategories) {
            LinearLayout row = new LinearLayout(holder.itemView.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 6);
            row.setLayoutParams(lp);

            TextView tvCat = new TextView(holder.itemView.getContext());
            tvCat.setText(cat);
            tvCat.setTextSize(12);
            tvCat.setTextColor(Color.parseColor("#757575"));
            LinearLayout.LayoutParams lpCat = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvCat.setLayoutParams(lpCat);

            double myAmt = data.getMyCategories().getOrDefault(cat, 0.0);
            TextView tvMy = new TextView(holder.itemView.getContext());
            tvMy.setText("₹" + (int)myAmt);
            tvMy.setTextSize(12);
            tvMy.setTextColor(Color.parseColor("#9B94FF"));
            tvMy.setTypeface(android.graphics.Typeface.MONOSPACE);
            tvMy.setMinWidth(dpToPx(70, holder.itemView.getContext()));
            tvMy.setGravity(android.view.Gravity.END);

            TextView tvVs = new TextView(holder.itemView.getContext());
            tvVs.setText(" vs ");
            tvVs.setTextSize(11);
            tvVs.setTextColor(Color.parseColor("#BDBDBD"));
            tvVs.setPadding(dpToPx(4, holder.itemView.getContext()), 0, dpToPx(4, holder.itemView.getContext()), 0);

            double peerAmt = data.getPeerCategories().getOrDefault(cat, 0.0);
            TextView tvPeer = new TextView(holder.itemView.getContext());
            tvPeer.setText("₹" + (int)peerAmt);
            tvPeer.setTextSize(12);
            tvPeer.setTextColor(Color.parseColor("#757575"));
            tvPeer.setTypeface(android.graphics.Typeface.MONOSPACE);
            tvPeer.setMinWidth(dpToPx(70, holder.itemView.getContext()));
            tvPeer.setGravity(android.view.Gravity.END);

            row.addView(tvCat);
            row.addView(tvMy);
            row.addView(tvVs);
            row.addView(tvPeer);
            holder.categoryContainer.addView(row);
        }
    }

    private int dpToPx(int dp, android.content.Context context) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return comparisonList.size();
    }

    static class ComparisonHolder extends RecyclerView.ViewHolder {
        TextView tvPeerName, tvMyAmount, tvMyPercent, tvPeerAmount, tvPeerPercent, tvWinner;
        LinearLayout categoryContainer;

        public ComparisonHolder(@NonNull View itemView) {
            super(itemView);
            tvPeerName = itemView.findViewById(R.id.tvPeerName);
            tvMyAmount = itemView.findViewById(R.id.tvMyAmount);
            tvMyPercent = itemView.findViewById(R.id.tvMyPercent);
            tvPeerAmount = itemView.findViewById(R.id.tvPeerAmount);
            tvPeerPercent = itemView.findViewById(R.id.tvPeerPercent);
            tvWinner = itemView.findViewById(R.id.tvWinner);
            categoryContainer = itemView.findViewById(R.id.categoryBreakdownContainer);
        }
    }
}
