package de.jwi.droidsensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.method.TransformationMethod;
import android.widget.EditText;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MQTTPreferenceFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private WifiManager wifiManager;
    private LocationManager locationManager;

    public void setWifiManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // show the current value in the settings screen
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            updatePreferenceSummary(getPreferenceScreen().getPreference(i));
        }

        findPreference("pref_app_info").setSummary(BuildConfig.APPLICATION_ID + " " + BuildConfig.VERSION_NAME);

        Preference sethomewifi = findPreference("sethomewifi");

        Preference homewifi = findPreference("homewifi");


     //   WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);

        if (sethomewifi != null) {
            sethomewifi.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {

                   WifiInfo currentWifi = wifiManager.getConnectionInfo();
                   String ssid = currentWifi.getSSID();

                    homewifi.setSummary(ssid);

                    SharedPreferences.Editor editor = homewifi.getSharedPreferences().edit();
                    editor.putString("homewifi", ssid);
                    editor.apply();

                    return true;
                }
            });
        }

        Preference setgeofenceLocation = findPreference("setgeofenceLocation");
        Preference geofenceLocation = findPreference("geofenceLocation");

        if (setgeofenceLocation != null) {
            setgeofenceLocation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {

                    Location gpsLocation = null;
                    Location netLocation = null;
                    Location location = null;

                    List<Location> ll = new ArrayList<>();

                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        LocationListener l = new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                ll.add(location);
                            }
                        };

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, l);

                        gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        locationManager.removeUpdates(l);
                    }

                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                        LocationListener l = new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                ll.add(location);
                            }
                        };

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, l);

                        locationManager.removeUpdates(l);


                        netLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }


                    if (gpsLocation != null && netLocation != null)
                    {
                        location = gpsLocation.getAccuracy() > netLocation.getAccuracy()
                                ? gpsLocation : netLocation;
                    }
                    else
                    {
                        location = gpsLocation != null ? gpsLocation : netLocation;
                    }

                    if (location != null) {
                        String ls = String.format("%s,%s", Location.convert(location.getLatitude(), Location.FORMAT_SECONDS),
                                Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));

                        geofenceLocation.setSummary(ls);

                        SharedPreferences.Editor editor = homewifi.getSharedPreferences().edit();
                        editor.putString("geofenceLocation", ls);
                        editor.apply();
                    }
                    return true;
                }
            });
        }


    }

    private void updatePreferenceSummary(Preference p) {
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;

            EditText editText = editTextPref.getEditText();

            CharSequence textShown = editTextPref.getText();

            if (textShown != null) {
                TransformationMethod transformationMethod = editText.getTransformationMethod();

                if (transformationMethod != null) {
                    textShown = transformationMethod.getTransformation(textShown, editText);

                    p.setSummary(textShown);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        updatePreferenceSummary(findPreference(key));
    }

}

