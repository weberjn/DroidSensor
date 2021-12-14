package de.jwi.droidsensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.Map;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadcastReceiver";

    static final String SERVICE_ACTION = "de.jwi.droidsensor.ServiceEvent";
    static final String EXTRA_CMD = "cmd";
    static final String EXTRA_START = "start";
    static final String EXTRA_STOP  = "stop";

//    String url = "tcp://nico";

    private Integer lastBatteryLevel = null;

    private String ssid;

    private NetworkInfo.DetailedState networkInfoDetailedState;



    @Override
    public void onReceive(Context context, Intent intent) {
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        String log = sb.toString();
        Log.d(TAG, log);
        //Toast.makeText(context, log, Toast.LENGTH_LONG).show();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String topicprefix = prefs.getString("topicprefix", "");
        int nthbatterylevel = Integer.parseInt(prefs.getString("nthbatterylevel", "1"));

        boolean requirehomewifi = prefs.getBoolean("requirehomewifi", false);
        String homewifi = prefs.getString("homewifi", null);

        WorkManager workManager = WorkManager.getInstance(context);

        String topic = null;
        String payload = null;

        boolean skip = false;

        Bundle extras = intent.getExtras();

        switch (intent.getAction()) {

            case SERVICE_ACTION:
                String cmd = (String) extras.get(EXTRA_CMD);
                topic = "cmd";
                payload = cmd;
                break;

            case Intent.ACTION_BATTERY_LOW:
                topic = "battery/state";
                payload = "low";
                break;
            case Intent.ACTION_BATTERY_OKAY:
                topic = "battery/state";
                payload = "okay";
                break;
            case Intent.ACTION_BATTERY_CHANGED:
                Integer level = (Integer) extras.get(BatteryManager.EXTRA_LEVEL);

                if (level.equals(lastBatteryLevel) || level % nthbatterylevel != 0)
                {
                    skip = true;
                }

                lastBatteryLevel = level;
                Integer scale = (Integer) extras.get(BatteryManager.EXTRA_SCALE);
                topic = "battery/level";
                payload = String.format("%d/%d", level, scale);
                break;
            case Intent.ACTION_POWER_CONNECTED:
                topic = "power";
                payload = "connected";
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                topic = "power";
                payload = "disconnected";
                break;
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                topic = "wifi";
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State networkInfoState = networkInfo.getState();
                payload = networkInfoState.toString();

                networkInfoDetailedState = networkInfo.getDetailedState();

                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo currentWifi = wifiManager.getConnectionInfo();
                ssid = currentWifi.getSSID();
                Log.d(TAG, "currentWifi: " + ssid);
                Log.d(TAG, "homeWifi: " + homewifi);

                break;
        }


        if (requirehomewifi) {

            if (homewifi == null)
            {
                return;
            }

            if (!homewifi.equals(ssid))
            {
                return;
            }

            if(!networkInfoDetailedState.equals(NetworkInfo.DetailedState.CONNECTED)) {
                return;
            }
        }


        if (!skip)
        {
            topic = String.format("%s/%s", topicprefix, topic);

            sendMQTT(context, topic, payload);
        }

    }

    private void sendMQTT(Context context, String topic, String payload)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        WorkRequest mqttWork =
                new OneTimeWorkRequest.Builder(MQTTWorker.class)
                        .setInputData(
                                new Data.Builder()
                                        .putAll((Map<String, Object>) prefs.getAll())
                                        .putString(MQTTWorker.KEY_TOPIC, topic)
                                        .putString(MQTTWorker.KEY_PAYLOAD, payload)
                                        .build()
                        )
                        .build();

        WorkManager.getInstance(context).enqueue(mqttWork);

    }
}
