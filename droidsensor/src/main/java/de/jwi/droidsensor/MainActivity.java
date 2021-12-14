package de.jwi.droidsensor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Switch serviceState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceState = findViewById(R.id.switchService);

        serviceState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = serviceState.isChecked();

                Intent intent = new Intent(MyBroadcastReceiver.SERVICE_ACTION);

                if (b) {
                    startService();
                    intent.putExtra(MyBroadcastReceiver.EXTRA_CMD, MyBroadcastReceiver.EXTRA_START);
                    sendBroadcast(intent);
                    saveState(true);
                }
                else
                {
                    intent.putExtra(MyBroadcastReceiver.EXTRA_CMD, MyBroadcastReceiver.EXTRA_STOP);
                    sendBroadcast(intent);
                    stopService();
                    saveState(false);
                }
            }
        });

        boolean b = readState();
        serviceState.setChecked(b);

        if (b)
        {
            startService();
        }
        else
        {
            stopService();
        }

    }

    private boolean readState()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean b = preferences.getBoolean("servicestate", false);
        return b;
    }

    private void saveState(boolean b)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("servicestate", b);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_thingy:
                Toast.makeText(this, "Options!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, MQTTPreferencesActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Droid Sensor MQTT Service");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }



}