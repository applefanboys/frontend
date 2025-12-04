package com.example.stocksapp.ui.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.stocksapp.R;

public class NewsAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String slot = intent.getStringExtra("slot");
        String title = "뉴스 볼 시간이에요";
        String text = "설정한 " + (slot != null ? slot.toUpperCase() : "") + " 시간입니다. 오늘의 뉴스를 확인하세요.";

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        String channelId = MainActivity.CHANNEL_ID;

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .build();

        int id = "pm".equals(slot) ? 2 : 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.notify(id, notification);
        } else {
            manager.notify(id, notification);
        }
    }
}
