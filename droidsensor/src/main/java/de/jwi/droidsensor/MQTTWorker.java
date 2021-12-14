package de.jwi.droidsensor;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTWorker extends Worker {

    private static final String TAG = MQTTWorker.class.getSimpleName();

    public static final String KEY_TOPIC = "topic";
    public static final String KEY_PAYLOAD = "payload";

    public MQTTWorker(Context appContext, WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public Result doWork() {

        String url = getInputData().getString("mqtturl");

        String topic = getInputData().getString(KEY_TOPIC);

        String payload = getInputData().getString(KEY_PAYLOAD);

        Context context = getApplicationContext();

        boolean auth = getInputData().getBoolean("auth", false);

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        if (auth) {
            String username = getInputData().getString("username");
            String password = getInputData().getString("password");
            connectOptions.setUserName(username);
            connectOptions.setPassword(password.toCharArray());
        }

        MqttClient client = null;
        try {
            client = new MqttClient(url, "BroadcastReceiver", new MemoryPersistence());

            client.connect(connectOptions);

            MqttMessage mqttMessage = new MqttMessage(payload.getBytes());

            client.publish(topic, mqttMessage);

            client.disconnect();
        } catch (MqttException e) {

            Log.e(TAG, e.getMessage(), e);

            String m = e.getClass().getName() + " : " + e.getMessage();

            return Result.failure();
        }

        return Result.success();
    }
}
