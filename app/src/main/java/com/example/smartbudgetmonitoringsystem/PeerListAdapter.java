package com.example.smartbudgetmonitoringsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PeerListAdapter extends RecyclerView.Adapter<PeerListAdapter.PeerHolder> {

    private List<String> peerNames;
    private List<Integer> peerIds;
    private OnPeerRemoveListener removeListener;

    public interface OnPeerRemoveListener {
        void onRemove(int peerUserId, int position);
    }

    public PeerListAdapter(List<String> peerNames, List<Integer> peerIds, OnPeerRemoveListener removeListener) {
        this.peerNames = peerNames;
        this.peerIds = peerIds;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public PeerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_peer_list, parent, false);
        return new PeerHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PeerHolder holder, int position) {
        holder.tvName.setText(peerNames.get(position));
        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(peerIds.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return peerNames.size();
    }

    static class PeerHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnRemove;

        public PeerHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPeerListName);
            btnRemove = itemView.findViewById(R.id.btnRemovePeer);
        }
    }
}
