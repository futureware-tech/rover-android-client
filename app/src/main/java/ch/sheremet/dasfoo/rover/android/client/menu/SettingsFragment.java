package ch.sheremet.dasfoo.rover.android.client.menu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.BaseAdapter;

import ch.sheremet.dasfoo.rover.android.client.R;

/**
 * Created by Katarina Sheremet on 6/15/16 10:17 AM.
 * Class used to load preferences from XML,
 * manage changes on settings window
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load the preferences from XML resource
        addPreferencesFromResource(R.xml.preferences);
        updatePreference();
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to Activity.onResume of the containing
     * Activity's lifecycle.
     */
    @Override
    public final void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        updatePreference();
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to Activity.onPause of the containing
     * Activity's lifecycle.
     */
    @Override
    public final void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public final void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
                                          final String key) {
        Preference pref = findPreference(key);
        pref.setSummary(sharedPreferences.getString(key, ""));
        //This is necessary to reflect change after coming back from sub-pref screen
        ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }

    /**
     * Updates and shows summary for every editText.
     */
    private void updatePreference() {
        for (Settings settings : Settings.values()) {
            Preference preference = findPreference(settings.toString());
            if (preference instanceof EditTextPreference) {
                EditTextPreference editTextPreference = (EditTextPreference) preference;
                editTextPreference.setSummary(editTextPreference.getText());
            }
        }
    }
}
