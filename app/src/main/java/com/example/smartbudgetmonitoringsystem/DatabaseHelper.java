package com.example.smartbudgetmonitoringsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "budget.db";
    private static final int DB_VERSION = 8;

    private static final String TABLE_EXPENSE = "expenses";
    private static final String TABLE_BUDGET = "budgets";
    private static final String TABLE_USER = "users";
    private static final String TABLE_NOTIFICATION = "notifications";
    private static final String TABLE_PEERS = "peers";
    private static final String TABLE_PEER_REQUESTS = "peer_requests";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createExpenseTable = "CREATE TABLE " + TABLE_EXPENSE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "name TEXT," +
                "amount REAL," +
                "category TEXT," +
                "date TEXT" +
                ")";
        db.execSQL(createExpenseTable);

        String createBudgetTable = "CREATE TABLE " + TABLE_BUDGET + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "monthly_budget REAL," +
                "month TEXT" +
                ")";
        db.execSQL(createBudgetTable);

        String createUserTable = "CREATE TABLE " + TABLE_USER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "email TEXT UNIQUE," +
                "password TEXT" +
                ")";
        db.execSQL(createUserTable);

        String createNotificationTable = "CREATE TABLE " + TABLE_NOTIFICATION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "title TEXT," +
                "message TEXT," +
                "timestamp TEXT," +
                "is_read INTEGER DEFAULT 0," +
                "type INTEGER DEFAULT 0," +
                "color_type INTEGER DEFAULT 0," +
                "emoji TEXT DEFAULT '🔔'," +
                "expires_at TEXT" +
                ")";
        db.execSQL(createNotificationTable);

        String createPeersTable = "CREATE TABLE " + TABLE_PEERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "peer_user_id INTEGER," +
                "status TEXT DEFAULT 'active'," +
                "connected_at TEXT" +
                ")";
        db.execSQL(createPeersTable);

        String createPeerRequestsTable = "CREATE TABLE " + TABLE_PEER_REQUESTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "from_user_id INTEGER," +
                "to_user_id INTEGER," +
                "invite_code TEXT," +
                "status TEXT DEFAULT 'pending'," +
                "created_at TEXT" +
                ")";
        db.execSQL(createPeerRequestsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            String createNotificationTable = "CREATE TABLE " + TABLE_NOTIFICATION + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "title TEXT," +
                    "message TEXT," +
                    "timestamp TEXT," +
                    "is_read INTEGER DEFAULT 0" +
                    ")";
            db.execSQL(createNotificationTable);
        }
        if (oldVersion < 7) {
            try { db.execSQL("ALTER TABLE " + TABLE_NOTIFICATION + " ADD COLUMN type INTEGER DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + TABLE_NOTIFICATION + " ADD COLUMN color_type INTEGER DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + TABLE_NOTIFICATION + " ADD COLUMN emoji TEXT DEFAULT '🔔'"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + TABLE_NOTIFICATION + " ADD COLUMN expires_at TEXT"); } catch (Exception e) {}
        }
        if (oldVersion < 8) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PEERS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "peer_user_id INTEGER," +
                    "status TEXT DEFAULT 'active'," +
                    "connected_at TEXT" +
                    ")");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PEER_REQUESTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "from_user_id INTEGER," +
                    "to_user_id INTEGER," +
                    "invite_code TEXT," +
                    "status TEXT DEFAULT 'pending'," +
                    "created_at TEXT" +
                    ")");
        }
    }

    // Helper Functions
    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    private String daysFromNow(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(cal.getTime());
    }

    // User Functions
    public long registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("email", email);
        cv.put("password", password);
        return db.insert(TABLE_USER, null, cv);
    }

    public int loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_USER + " WHERE email=? AND password=?", new String[]{email, password});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE id=?", new String[]{String.valueOf(userId)});
    }

    public boolean updateUserName(int userId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", newName);
        int result = db.update(TABLE_USER, cv, "id=?", new String[]{String.valueOf(userId)});
        return result > 0;
    }

    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        int result = db.update(TABLE_USER, cv, "id=?", new String[]{String.valueOf(userId)});
        return result > 0;
    }

    public boolean checkCurrentPassword(int userId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE id=? AND password=?", new String[]{String.valueOf(userId), password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Budget Functions
    public boolean addBudget(int userId, double amount, String month) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("monthly_budget", amount);
        cv.put("month", month);
        long result = db.insert(TABLE_BUDGET, null, cv);
        return result != -1;
    }

    public boolean updateBudget(int id, int userId, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("monthly_budget", amount);
        int result = db.update(TABLE_BUDGET, values, "id=? AND user_id=?", new String[]{String.valueOf(id), String.valueOf(userId)});
        return result > 0;
    }

    public boolean deleteBudget(int id, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_BUDGET, "id=? AND user_id=?", new String[]{String.valueOf(id), String.valueOf(userId)});
        return result > 0;
    }

    public Cursor getBudgetByMonth(int userId, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BUDGET + " WHERE month = ? AND user_id = ?", new String[]{month, String.valueOf(userId)});
    }

    public Cursor getCurrentMonthBudget(int userId) {
        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());
        return getBudgetByMonth(userId, month);
    }

    // Expense Functions
    public boolean addExpense(int userId, String name, double amount, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("name", name);
        cv.put("amount", amount);
        cv.put("category", category);
        cv.put("date", date);
        long result = db.insert(TABLE_EXPENSE, null, cv);
        return result != -1;
    }

    public boolean updateExpense(int id, int userId, String name, double amount, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);

        int result = db.update(TABLE_EXPENSE, values, "id=? AND user_id=?", new String[]{String.valueOf(id), String.valueOf(userId)});
        return result > 0;
    }

    public boolean deleteExpense(int id, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EXPENSE, "id=? AND user_id=?", new String[]{String.valueOf(id), String.valueOf(userId)});
        return result > 0;
    }

    public int getExpenseCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EXPENSE + " WHERE user_id=?", new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<Expense> getAllExpenses(int userId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSE + " WHERE user_id=? ORDER BY date DESC", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                Expense e = new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date"))
                );
                expenses.add(e);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    public List<Expense> getExpensesByCategory(int userId, String category) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSE + " WHERE user_id=? AND category LIKE ? ORDER BY date DESC", 
                new String[]{String.valueOf(userId), "%" + category + "%"});
        if (cursor.moveToFirst()) {
            do {
                Expense e = new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date"))
                );
                expenses.add(e);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    public Cursor getExpenseById(int id, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EXPENSE + " WHERE id=? AND user_id=?",
                new String[]{String.valueOf(id), String.valueOf(userId)});
    }

    public double getCurrentMonthTotalExpenses(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_EXPENSE + " WHERE date LIKE ? AND user_id=?", new String[]{month + "%", String.valueOf(userId)});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public double getWeeklyTotalExpenses(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String weekStart = sdf.format(cal.getTime());
        
        cal.add(Calendar.DAY_OF_WEEK, 6);
        String weekEnd = sdf.format(cal.getTime());

        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_EXPENSE + " WHERE user_id=? AND date BETWEEN ? AND ?", 
                new String[]{String.valueOf(userId), weekStart, weekEnd});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public List<Expense> getExpensesLastWeek(int userId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String weekStart = sdf.format(cal.getTime());

        cal.add(Calendar.DAY_OF_WEEK, 6);
        String weekEnd = sdf.format(cal.getTime());

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSE + " WHERE user_id=? AND date BETWEEN ? AND ? ORDER BY date DESC",
                new String[]{String.valueOf(userId), weekStart, weekEnd});

        if (cursor.moveToFirst()) {
            do {
                Expense e = new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date"))
                );
                expenses.add(e);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    public List<Expense> getExpensesCurrentMonth(int userId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSE + " WHERE user_id=? AND date LIKE ? ORDER BY date DESC",
                new String[]{String.valueOf(userId), month + "%"});

        if (cursor.moveToFirst()) {
            do {
                Expense e = new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date"))
                );
                expenses.add(e);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    // Notification Functions
    public void addNotification(int userId, String title, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("title", title);
        cv.put("message", message);
        cv.put("timestamp", now());
        db.insert(TABLE_NOTIFICATION, null, cv);
    }

    public void addAdvisorNotification(int userId, String title, String message, String emoji, int type, int colorType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("title", title);
        cv.put("message", message);
        cv.put("emoji", emoji);
        cv.put("type", type);
        cv.put("color_type", colorType);
        cv.put("timestamp", now());
        cv.put("expires_at", daysFromNow(30));
        db.insert(TABLE_NOTIFICATION, null, cv);
    }

    public boolean advisorNotificationExistsToday(int userId, String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NOTIFICATION + " WHERE user_id=? AND title=? AND timestamp LIKE ?", 
                new String[]{String.valueOf(userId), title, today + "%"});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    public void purgeExpiredNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTIFICATION, "expires_at IS NOT NULL AND expires_at < ?", new String[]{now()});
    }

    public Cursor getNotifications(int userId) {
        purgeExpiredNotifications();
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATION + " WHERE user_id=? ORDER BY timestamp DESC", new String[]{String.valueOf(userId)});
    }

    public void markNotificationsAsRead(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_read", 1);
        db.update(TABLE_NOTIFICATION, cv, "user_id=?", new String[]{String.valueOf(userId)});
    }

    public int getUnreadNotificationCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NOTIFICATION + " WHERE user_id=? AND is_read=0", new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public boolean deleteNotification(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NOTIFICATION, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // Peer Functions
    public String generateInviteCode(int userId) {
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase() + (userId % 10);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("from_user_id", userId);
        cv.put("invite_code", code);
        cv.put("status", "pending");
        cv.put("created_at", now());
        db.insert(TABLE_PEER_REQUESTS, null, cv);
        return code;
    }

    public int getUserIdByInviteCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT from_user_id FROM " + TABLE_PEER_REQUESTS + " WHERE invite_code=? AND status='pending'", new String[]{code});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    public boolean connectPeers(int userId, String inviteCode) {
        int peerUserId = getUserIdByInviteCode(inviteCode);
        if (peerUserId == -1 || peerUserId == userId) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues cv1 = new ContentValues();
        cv1.put("user_id", userId);
        cv1.put("peer_user_id", peerUserId);
        cv1.put("connected_at", now());
        db.insert(TABLE_PEERS, null, cv1);

        ContentValues cv2 = new ContentValues();
        cv2.put("user_id", peerUserId);
        cv2.put("peer_user_id", userId);
        cv2.put("connected_at", now());
        db.insert(TABLE_PEERS, null, cv2);

        ContentValues updateCv = new ContentValues();
        updateCv.put("status", "accepted");
        db.update(TABLE_PEER_REQUESTS, updateCv, "invite_code=?", new String[]{inviteCode});

        return true;
    }

    public List<Integer> getPeerIds(int userId) {
        List<Integer> peerIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT peer_user_id FROM " + TABLE_PEERS + " WHERE user_id=? AND status='active'", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                peerIds.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return peerIds;
    }

    public void removePeer(int userId, int peerUserId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PEERS, "(user_id=? AND peer_user_id=?) OR (user_id=? AND peer_user_id=?)", 
                new String[]{String.valueOf(userId), String.valueOf(peerUserId), String.valueOf(peerUserId), String.valueOf(userId)});
    }

    public String getPeerName(int peerUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM " + TABLE_USER + " WHERE id=?", new String[]{String.valueOf(peerUserId)});
        String name = "Unknown";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public Map<String, Double> getCategoryTotalsForUser(int userId, boolean isMonthly) {
        Map<String, Double> totals = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        String[] args;

        if (isMonthly) {
            String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());
            query = "SELECT category, amount FROM " + TABLE_EXPENSE + " WHERE user_id=? AND date LIKE ?";
            args = new String[]{String.valueOf(userId), month + "%"};
        } else {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -7);
            String lastWeek = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            query = "SELECT category, amount FROM " + TABLE_EXPENSE + " WHERE user_id=? AND date >= ?";
            args = new String[]{String.valueOf(userId), lastWeek};
        }

        Cursor cursor = db.rawQuery(query, args);
        if (cursor.moveToFirst()) {
            do {
                String cat = cursor.getString(0);
                double amt = cursor.getDouble(1);
                totals.put(cat, totals.getOrDefault(cat, 0.0) + amt);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return totals;
    }

    public double getTotalSpentForUser(int userId, boolean isMonthly) {
        if (isMonthly) return getCurrentMonthTotalExpenses(userId);
        else return getWeeklyTotalExpenses(userId);
    }

    public double getBudgetForUser(int userId) {
        Cursor cursor = getCurrentMonthBudget(userId);
        double budget = 0;
        if (cursor.moveToFirst()) {
            budget = cursor.getDouble(cursor.getColumnIndexOrThrow("monthly_budget"));
        }
        cursor.close();
        return budget;
    }
}
