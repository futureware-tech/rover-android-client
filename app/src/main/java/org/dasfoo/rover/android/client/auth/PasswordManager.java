package org.dasfoo.rover.android.client.auth;

import android.app.Activity;
import android.content.Intent;

import org.dasfoo.rover.android.client.util.ResultCallback;

/**
 * Caching single credential manager.
 */
public class PasswordManager {
    private final ResultCallback mResultCallback;

    private String mPassword;

    /**
     * Constructor for the PasswordManager. Does not try to fetch the password.
     * @param resultCallback used to launch new activities
     */
    public PasswordManager(final ResultCallback resultCallback) {
        mResultCallback = resultCallback;
    }

    /**
     * Get password and start the listener.
     * @param listener callback when the password is available. Executed in the main thread
     */
    public void getPassword(final PasswordAvailableListener listener) {
        if (mPassword != null) {
            listener.handle(mPassword);
            return;
        }
        mResultCallback.startActivityWithResultHandler(GetRoverPasswordActivity.class,
                new ResultCallback.AbstractActivityResultListener() {
                    @Override
                    public void handle(final int resultCode, final Intent data) {
                        if (resultCode == Activity.RESULT_OK) {
                            listener.handle(data.getData().getAuthority());
                        }
                    }
                });
    }

    /**
     * Remove password from the cache.
     */
    public void forgetPassword() {
        mPassword = null;
    }

    /**
     * An interface to listen for the password to be available.
     */
    public interface PasswordAvailableListener {
        /**
         * Called when a password is successfully fetched.
         * @param password a password that was fetched
         */
        void handle(String password);
    }
}
