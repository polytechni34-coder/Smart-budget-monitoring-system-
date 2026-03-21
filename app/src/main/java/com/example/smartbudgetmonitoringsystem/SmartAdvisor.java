package com.example.smartbudgetmonitoringsystem;

import java.util.Calendar;

public class SmartAdvisor {

    public static class Advice {
        public String emoji;
        public String title;
        public String message;
        public int colorType; // 0=info/purple, 1=warning/orange, 2=danger/red, 3=good/green

        public Advice(String emoji, String title, String message, int colorType) {
            this.emoji = emoji;
            this.title = title;
            this.message = message;
            this.colorType = colorType;
        }
    }

    public static Advice getAdvice(double spent, double budget, boolean isWeekly) {
        if (budget <= 0) {
            return new Advice("💡", "Set a Budget", "You haven't set a budget yet! Setting one helps you track and control your spending smartly.", 0);
        }

        double pct = (spent / budget) * 100;
        int pctInt = (int) pct;
        Calendar cal = Calendar.getInstance();
        
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        boolean isMonthEnd = (dayOfMonth > maxDay - 3);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

        if (isWeekly && isWeekend) {
            if (pct <= 50) {
                return new Advice("🌟", "Week Well Done!", "You spent less than I thought this week! You've used only " + pctInt + "% of your weekly budget. You're doing great — keep it up! 🎉", 3);
            } else if (pct <= 80) {
                return new Advice("👍", "Good Week!", "You're spending wisely this week at " + pctInt + "% of your budget. A little more mindfulness and you'll finish perfectly! 😊", 3);
            } else if (pct <= 100) {
                return new Advice("⚠️", "Week Almost Over", "You've used " + pctInt + "% of this week's budget. Try to coast through the weekend without big purchases! 🙏", 1);
            } else {
                return new Advice("😬", "Over Budget This Week", "Oops! You went over your weekly limit. No worries — next week is a fresh start. Let's plan better! 💪", 2);
            }
        }

        if (!isWeekly && isMonthEnd) {
            if (pct <= 60) {
                return new Advice("🏆", "Amazing Month!", "Wow, you spent way less than expected! Only " + pctInt + "% of your monthly budget used. You're a saving champion! 🌟", 3);
            } else if (pct <= 85) {
                return new Advice("😊", "Great Month!", "Month's almost done and you've spent " + pctInt + "% of your budget. You're doing really well — you should be proud! ✨", 3);
            } else if (pct <= 100) {
                return new Advice("🤞", "Month End Alert", "Just a few days left and you've used " + pctInt + "% of your budget. Hold on tight, you're almost there! 😤", 1);
            } else {
                return new Advice("📉", "Over Budget", "You crossed your monthly budget this month. Don't worry — let's review and set a better plan for next month! 💡", 2);
            }
        }

        // Mid-period rules
        if (pct >= 100) {
            return new Advice("🚨", "Budget Exhausted!", "Your expenses have hit 100%! Every rupee from here is extra. Time to pause and think before spending! 🛑", 2);
        } else if (pct >= 90) {
            return new Advice("😰", "Almost Empty!", "Only " + (100 - pctInt) + "% of your budget left. You might end up empty-handed if you're not careful now! 😚", 2);
        } else if (pct >= 75) {
            return new Advice("🔥", "Spending Too High!", "Whoa! You've already used " + pctInt + "% of your budget. Your expenses are going too high — be careful! 🧐", 2);
        } else if (pct >= 50) {
            return new Advice("👀", "Halfway There", "You've spent half your budget. Still in control — just keep an eye on it! 😌", 1);
        } else if (pct >= 25) {
            return new Advice("✅", "On Track", "You're spending wisely! " + pctInt + "% used so far. Stay consistent and you'll finish strong! 💚", 3);
        } else if (pct > 0) {
            return new Advice("😄", "Great Start!", "Only " + pctInt + "% spent so far. Off to an excellent start — keep tracking! 🌱", 3);
        }

        return null;
    }

    public static Advice getSummaryAdvice(double spent, double budget, boolean isWeekly) {
        if (budget <= 0) return null;
        double pct = (spent / budget) * 100;
        int pctInt = (int) pct;

        if (pct <= 50) {
            return new Advice("🌟", "Period Summary — Excellent!", "You finished the period spending only " + pctInt + "% of your budget. That's incredible discipline! You'd make a great CFO 😄", 3);
        } else if (pct <= 80) {
            return new Advice("👍", "Period Summary — Well Done!", "You spent " + pctInt + "% of your budget. That's healthy and balanced spending. Keep this up! 🎉", 3);
        } else if (pct <= 100) {
            return new Advice("😅", "Period Summary — Close Call!", "You used " + pctInt + "% of your budget. Pretty close to the limit — consider setting a slightly higher buffer next time.", 1);
        } else {
            int exceededBy = pctInt - 100;
            return new Advice("📊", "Period Summary — Over Budget", "You exceeded your budget by " + exceededBy + "%. Let's review your top spending categories and plan better! 💪", 2);
        }
    }
}
