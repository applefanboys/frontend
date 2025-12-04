package com.example.stocksapp.ui.main;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.stocksapp.R;
import com.example.stocksapp.ui.main.home.HomeFragment;
import com.example.stocksapp.ui.main.mypage.MyPageFragment;
import com.example.stocksapp.ui.main.topics.TopicsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    public static final String PREFS_NAME = "settings";
    public static final String CHANNEL_ID = "news_time_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment;
            if (id == R.id.navigation_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.navigation_topics) {
                fragment = new TopicsFragment();
            } else {
                fragment = new MyPageFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_nav_host, fragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.navigation_home);
        }

        showNewsTimeDialogIfNeeded();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "뉴스 알림",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNewsTimeDialogIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean alreadySet = prefs.getBoolean("news_time_set", false);
        if (!alreadySet) {
            showNewsTimeDialog();
        } else {
            String am = prefs.getString("time_am", null);
            String pm = prefs.getString("time_pm", null);
            if (am != null && pm != null) {
                scheduleNewsAlarms(am, pm);
            }
        }
    }

    public void openNewsTimeDialogFromMyPage() {
        showNewsTimeDialog();
    }

    private void showNewsTimeDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_news_time, null);

        LinearLayout layoutRoutine1 = dialogView.findViewById(R.id.layoutRoutine1);
        LinearLayout layoutRoutine2 = dialogView.findViewById(R.id.layoutRoutine2);
        LinearLayout layoutRoutine3 = dialogView.findViewById(R.id.layoutRoutine3);
        ImageView ivCheck1 = dialogView.findViewById(R.id.ivCheck1);
        ImageView ivCheck2 = dialogView.findViewById(R.id.ivCheck2);
        ImageView ivCheck3 = dialogView.findViewById(R.id.ivCheck3);
        TextView tvTimeAm = dialogView.findViewById(R.id.tvTimeAm);
        TextView tvTimePm = dialogView.findViewById(R.id.tvTimePm);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        final int[] selected = {-1};
        final String[] timeAm = {"08:00"};
        final String[] timePm = {"18:30"};

        tvTimeAm.setText("아침 시간: " + timeAm[0]);
        tvTimePm.setText("저녁 시간: " + timePm[0]);

        View.OnClickListener routineClickListener = v -> {
            if (v.getId() == R.id.layoutRoutine1) {
                selected[0] = 1;
                timeAm[0] = "08:00";
                timePm[0] = "18:30";
            } else if (v.getId() == R.id.layoutRoutine2) {
                selected[0] = 2;
                timeAm[0] = "12:30";
                timePm[0] = "19:30";
            } else if (v.getId() == R.id.layoutRoutine3) {
                selected[0] = 3;
                timeAm[0] = "08:00";
                timePm[0] = "22:00";
            }
            updateCheckIcons(selected[0], ivCheck1, ivCheck2, ivCheck3);
            tvTimeAm.setText("아침 시간: " + timeAm[0]);
            tvTimePm.setText("저녁 시간: " + timePm[0]);
        };

        layoutRoutine1.setOnClickListener(routineClickListener);
        layoutRoutine2.setOnClickListener(routineClickListener);
        layoutRoutine3.setOnClickListener(routineClickListener);

        tvTimeAm.setOnClickListener(v -> {
            String[] parts = timeAm[0].split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            TimePickerDialog picker = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
                timeAm[0] = String.format("%02d:%02d", hourOfDay, minute1);
                tvTimeAm.setText("아침 시간: " + timeAm[0]);
            }, hour, minute, true);
            picker.show();
        });

        tvTimePm.setOnClickListener(v -> {
            String[] parts = timePm[0].split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            TimePickerDialog picker = new TimePickerDialog(this, (view, hourOfDay, minute12) -> {
                timePm[0] = String.format("%02d:%02d", hourOfDay, minute12);
                tvTimePm.setText("저녁 시간: " + timePm[0]);
            }, hour, minute, true);
            picker.show();
        });

        btnConfirm.setOnClickListener(v -> {
            if (selected[0] == -1) {
                Toast.makeText(this, "루틴을 하나 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveNewsTimeRoutine(selected[0], timeAm[0], timePm[0]);
            scheduleNewsAlarms(timeAm[0], timePm[0]);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateCheckIcons(int selected,
                                  ImageView ivCheck1,
                                  ImageView ivCheck2,
                                  ImageView ivCheck3) {
        ivCheck1.setImageResource(selected == 1 ? R.drawable.ic_check_on : R.drawable.ic_check_off);
        ivCheck2.setImageResource(selected == 2 ? R.drawable.ic_check_on : R.drawable.ic_check_off);
        ivCheck3.setImageResource(selected == 3 ? R.drawable.ic_check_on : R.drawable.ic_check_off);
    }

    private void saveNewsTimeRoutine(int routine, String timeAm, String timePm) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("news_time_set", true);
        editor.putInt("news_routine", routine);
        editor.putString("time_am", timeAm);
        editor.putString("time_pm", timePm);
        editor.apply();
    }

    private void scheduleNewsAlarms(String timeAm, String timePm) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (manager == null) return;

        PendingIntent amIntent = PendingIntent.getBroadcast(
                this,
                1001,
                new Intent(this, NewsAlarmReceiver.class).putExtra("slot", "am"),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        PendingIntent pmIntent = PendingIntent.getBroadcast(
                this,
                1002,
                new Intent(this, NewsAlarmReceiver.class).putExtra("slot", "pm"),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        manager.cancel(amIntent);
        manager.cancel(pmIntent);

        Calendar calAm = createCalendarFromTime(timeAm);
        Calendar calPm = createCalendarFromTime(timePm);
        Calendar now = Calendar.getInstance();

        if (calAm.before(now)) calAm.add(Calendar.DATE, 1);
        if (calPm.before(now)) calPm.add(Calendar.DATE, 1);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, calAm.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, amIntent);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calPm.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pmIntent);
    }

    private Calendar createCalendarFromTime(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
}
