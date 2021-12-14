package de.jwi.droidsensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.method.TransformationMethod;
import android.widget.EditText;

import java.util.Date;


public class MQTTPreferenceFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private WifiManager wifiManager;

    public void setWifiManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
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

