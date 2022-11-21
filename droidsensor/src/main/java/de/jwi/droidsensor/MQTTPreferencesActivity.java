package de.jwi.droidsensor;

import android.content.Context;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class MQTTPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        MQTTPreferenceFragment mqttPreferenceFragment = new MQTTPreferenceFragment();
        mqttPreferenceFragment.setWifiManager(wifiManager);

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mqttPreferenceFragment.setLocationManager(locationManager);

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, mqttPreferenceFragment).commit();
    }


}
