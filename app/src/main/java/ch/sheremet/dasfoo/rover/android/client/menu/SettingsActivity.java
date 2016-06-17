package ch.sheremet.dasfoo.rover.android.client.menu;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Katarina Sheremet on 6/15/16 12:44 PM.
 *
 * Class is created for opening Settings from menu
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected  final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    /**
     * Subclasses should override this method and verify that the given fragment is a valid type
     * to be attached to this activity. The default implementation returns <code>true</code> for
     * apps built for <code>android:targetSdkVersion</code> older than Kitkat.
     * {For later versions, it will throw an exception.
     * This method is needed for:
     * https://securityintelligence.com/new-vulnerability-android-framework-fragment-injection/
     *
     * @param fragmentName the class name of the Fragment about to be attached to this activity.
     * @return true if the fragment class name is valid for this Activity and false otherwise.
     */
    @Override
    protected final boolean isValidFragment(final String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
    }
}
