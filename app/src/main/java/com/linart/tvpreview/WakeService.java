package com.linart.tvpreview;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/** Brings LinarTV back when the screen wakes from standby, while it is the default home screen. */
public final class WakeService extends Service {
    private static final String CHANNEL = "linartv_wake";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final BroadcastReceiver screenOn = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (!Intent.ACTION_SCREEN_ON.equals(intent.getAction())) return;
            handler.postDelayed(() -> {
                if (!BootReceiver.isDefaultHome(WakeService.this)) { stopSelf(); return; }
                startActivity(BootReceiver.homeIntent(WakeService.this));
            }, 3500);
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager notifications = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notifications != null) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "LinarTV",
                    NotificationManager.IMPORTANCE_MIN);
            channel.setShowBadge(false);
            notifications.createNotificationChannel(channel);
        }
        Notification notification = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle("LinarTV")
                .setContentText("LinarTV · home screen")
                .setOngoing(true)
                .build();
        startForeground(7, notification);
        registerReceiver(screenOn, new IntentFilter(Intent.ACTION_SCREEN_ON));
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onDestroy() {
        unregisterReceiver(screenOn);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
