package de.jwi.droidsensor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service {

    private static final String TAG = "ForegroundService";

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private BroadcastReceiver br;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate:");

        createNotificationChannel();

        br = new MyBroadcastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);

        filter.addAction("de.jwi.droidsensor.ServiceEvent");

        this.registerReceiver(br, filter);
    }

    private void createNotificationChannel() {

        Log.d(TAG, "createNotificationChannel:" + Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Log.d(TAG, "createNotificationChannel enter");

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "System Sensor",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if (wakeLock != null)
        {
            Log.d(TAG, "release Power WakeLock");
            wakeLock.release();
        }
        if (wifiLock != null)
        {
            Log.d(TAG, "release Wifi WakeLock");
            wifiLock.release();
        }
        this.unregisterReceiver(br);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Droid Sensor" +  BuildConfig.VERSION_NAME)
                .setContentText(input)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isWakeLock = prefs.getBoolean("wakeLock", false);


        if (isWakeLock)
        {
            Log.d(TAG, "Acquiring Power WakeLock");

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DroidSensor:service-wakelock");
            this.wakeLock.acquire();

            Log.d(TAG, "Acquiring Wifi WakeLock");

            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "DroidSensor:wifi-wakelock");
            wifiLock.acquire();
        }


        //stopSelf();

        return START_REDELIVER_INTENT;
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}