package com.example.smartbudgetmonitoringsystem;

public class Notification {
    private int id;
    private String title;
    private String message;
    private String timestamp;
    private boolean isRead;
    private int type;
    private int colorType;
    private String emoji;
    private String expiresAt;

    public Notification(int id, String title, String message, String timestamp, boolean isRead,
                        int type, int colorType, String emoji, String expiresAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.type = type;
        this.colorType = colorType;
        this.emoji = emoji;
        this.expiresAt = expiresAt;
    }

    public Notification(int id, String title, String message, String timestamp, boolean isRead) {
        this(id, title, message, timestamp, isRead, 0, 0, "🔔", null);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public int getType() { return type; }
    public int getColorType() { return colorType; }
    public String getEmoji() { return emoji != null ? emoji : "🔔"; }
    public String getExpiresAt() { return expiresAt; }
}
