package com.itmoldova.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.itmoldova.AppSettings;
import com.itmoldova.R;
import com.itmoldova.sync.SyncScheduler;

/**
 * Main UI for the settings screen.
 * <p>
 * author vgrec-home on 29.11.16.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private SyncScheduler scheduler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        scheduler = new SyncScheduler(getActivity());

        ListPreference notificationsList = (ListPreference) findPreference(getString(R.string.key_notifications));
        notificationsList.setOnPreferenceChangeListener(this);
        setPreferenceSummary(notificationsList, AppSettings.getInstance(getActivity().getApplicationContext()).getNotificationEntry());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        setPreferenceSummary(preference, newValue);
        if (getString(R.string.key_notifications).equals(preference.getKey())) {
            setSyncInterval(newValue);
        }
        return true;
    }

    private void setSyncInterval(Object newValue) {
        long interval = Long.valueOf(newValue.toString());
        AppSettings.getInstance(getActivity()).setSyncInterval(interval);
        if (interval != AppSettings.SYNC_INTERVAL_NEVER) {
            scheduler.scheduleRepeatingSync(interval);
        } else {
            scheduler.cancel();
        }
    }

    private void setPreferenceSummary(Preference preference, Object newValue) {
        String summary = newValue.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(summary);
            CharSequence[] entries = listPreference.getEntries();
            if (prefIndex >= 0) {
                preference.setSummary(entries[prefIndex]);
            }
        } else {
            preference.setSummary(summary);
        }
    }
}
