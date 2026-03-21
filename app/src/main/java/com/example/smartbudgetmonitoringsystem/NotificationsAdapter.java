package com.example.smartbudgetmonitoringsystem;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
    private OnNotificationDeleteListener deleteListener;

    public interface OnNotificationDeleteListener {
        void onDelete(int notificationId, int position);
    }

    public NotificationsAdapter(List<Notification> notificationList, OnNotificationDeleteListener deleteListener) {
        this.notificationList = notificationList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvEmoji.setText(notification.getEmoji());

        String titleColor;
        String bubbleBg;
        switch (notification.getColorType()) {
            case 0: titleColor = "#9B94FF"; bubbleBg = "#1A6C63FF"; break;
            case 1: titleColor = "#FFB347"; bubbleBg = "#1AFFB347"; break;
            case 2: titleColor = "#FF6B6B"; bubbleBg = "#1AFF6B6B"; break;
            case 3: titleColor = "#00D2A0"; bubbleBg = "#1A00D2A0"; break;
            default: titleColor = "#9B94FF"; bubbleBg = "#1A6C63FF";
        }
        
        holder.tvTitle.setTextColor(Color.parseColor(titleColor));
        holder.emojiBubble.setCardBackgroundColor(Color.parseColor(bubbleBg));

        // Relative Time
        holder.tvTime.setText(getRelativeTime(notification.getTimestamp()));

        // Expiry Tag
        if (notification.getExpiresAt() == null) {
            holder.tvExpiry.setVisibility(View.GONE);
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date expiryDate = sdf.parse(notification.getExpiresAt());
                long diff = expiryDate.getTime() - System.currentTimeMillis();
                long daysLeft = TimeUnit.MILLISECONDS.toDays(diff);
                
                if (daysLeft > 0) {
                    holder.tvExpiry.setText("Expires in " + daysLeft + "d");
                    holder.tvExpiry.setTextColor(Color.parseColor("#BDBDBD"));
                    holder.tvExpiry.setVisibility(View.VISIBLE);
                } else if (daysLeft == 0 && diff > 0) {
                    holder.tvExpiry.setText("Expiring soon");
                    holder.tvExpiry.setTextColor(Color.parseColor("#FF6B6B"));
                    holder.tvExpiry.setVisibility(View.VISIBLE);
                } else {
                    holder.tvExpiry.setVisibility(View.GONE);
                }
            } catch (ParseException e) {
                holder.tvExpiry.setVisibility(View.GONE);
            }
        }

        holder.itemView.setAlpha(notification.isRead() ? 0.75f : 1.0f);

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(notification.getId(), holder.getAdapterPosition());
            }
        });
    }

    private String getRelativeTime(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(timestamp);
            long diff = System.currentTimeMillis() - date.getTime();
            
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (minutes < 1) return "Just now";
            if (minutes < 60) return minutes + "m ago";
            if (hours < 24) return hours + "h ago";
            if (days < 7) return days + "d ago";
            
            return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(date);
        } catch (Exception e) {
            return timestamp;
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CardView emojiBubble;
        TextView tvEmoji, tvTitle, tvMessage, tvTime, tvExpiry;
        ImageButton btnDelete;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            emojiBubble = itemView.findViewById(R.id.emojiBubble);
            tvEmoji = itemView.findViewById(R.id.tvNotificationEmoji);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            tvExpiry = itemView.findViewById(R.id.tvExpiryTag);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotification);
        }
    }
}
