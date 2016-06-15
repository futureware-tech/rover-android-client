package ch.sheremet.dasfoo.rover.android.client.menu;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import ch.sheremet.dasfoo.rover.android.client.R;

/**
 * Created by Katarina Sheremet on 6/15/16 10:17 AM.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load the preferences from XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
