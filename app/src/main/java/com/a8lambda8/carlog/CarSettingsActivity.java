package com.a8lambda8.carlog;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

import static com.a8lambda8.carlog.myUtils.SP;
import static com.a8lambda8.carlog.myUtils.currentCarRef;

public class CarSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        FloatingActionButton fab = findViewById(R.id.settingsFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> settingsData = new HashMap<>();
                settingsData.put("name", SP.getString("name",null));
                settingsData.put("type", SP.getString("type",null));
                settingsData.put("speedEnabled", SP.getBoolean("speedEnabled",true));
                settingsData.put("drainEnabled", SP.getBoolean("drainEnabled",true));

                currentCarRef.update(settingsData);
                Snackbar.make(view, "Changes saved", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}