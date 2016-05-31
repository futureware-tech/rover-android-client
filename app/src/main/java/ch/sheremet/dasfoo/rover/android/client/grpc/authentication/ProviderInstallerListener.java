package ch.sheremet.dasfoo.rover.android.client.grpc.authentication;

import android.content.Intent;

import com.google.android.gms.security.ProviderInstaller;

/**
 * Created by Katarina Sheremet on 5/31/16 3:22 PM.
 */
public class ProviderInstallerListener implements ProviderInstaller.ProviderInstallListener {
    /**
     * This method is only called if the provider is successfully updated
     * (or is already up-to-date).
     */
    @Override
    public void onProviderInstalled() {
        // It is not implemented yet
    }

    /**
     * This method is called if updating fails; the error code indicates
     * whether the error is recoverable.
     */
    @Override
    public void onProviderInstallFailed(final int i, final Intent intent) {
        // It is not implemented yet. It should show a dialog prompting the user to
        // install/update/enable Google Play services.
    }
}
