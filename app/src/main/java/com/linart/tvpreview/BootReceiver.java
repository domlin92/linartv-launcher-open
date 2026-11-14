package com.linart.tvpreview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.SystemClock;

/**
 * When LinarTV is the default home screen: open it after boot (some TVs first start their
 * own TV source) and keep WakeService running so it returns after standby.
 */
public final class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent received) {
        String action = received.getAction();
        if (!isDefaultHome(context)) return;
        // An app update stops the wake monitor; restart it without opening the dashboard.
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) { startWakeMonitor(context); return; }
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)) return;
        startWakeMonitor(context);
        PendingIntent launch = PendingIntent.getActivity(context, 100, homeIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) alarms.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 12000, launch);
    }

    /** True when the system resolves the HOME intent to LinarTV (it is the chosen launcher). */
    static boolean isDefaultHome(Context context) {
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo info = context.getPackageManager().resolveActivity(home, 0);
        return info != null && info.activityInfo != null
                && context.getPackageName().equals(info.activityInfo.packageName);
    }

    /** Resolve through HOME so boot and wake reuse the launcher's home task. */
    static Intent homeIntent(Context context) {
        return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .setPackage(context.getPackageName())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    /** Starts WakeService, which brings the dashboard back whenever the screen wakes from standby. */
    static void startWakeMonitor(Context context) {
        Intent monitor = new Intent(context, WakeService.class);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(monitor);
        else context.startService(monitor);
    }
}
