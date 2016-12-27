package org.dasfoo.rover.android.client.auth;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;

import org.dasfoo.rover.android.client.R;
import org.dasfoo.rover.android.client.menu.SharedPreferencesHandler;
import org.dasfoo.rover.android.client.util.LogUtil;
import org.dasfoo.rover.android.client.util.ResultCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import butterknife.ButterKnife;

/**
 * Empty activity for fetching credentials, asking user permission on the way as needed.
 */
public class GetRoverPasswordActivity
        extends AppCompatActivity
        implements ProviderInstaller.ProviderInstallListener {

    private static final String TAG = LogUtil.tagFor(GetRoverPasswordActivity.class);

    private GoogleAccountCredential mAccountCredential;
    private Storage mStorage;

    private ResultCallback mActivityResultCallback;

    /** {@inheritDoc} */
    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mActivityResultCallback.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mActivityResultCallback.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_rover_password);
        ButterKnife.bind(this);

        mActivityResultCallback = new ResultCallback(this);

        mAccountCredential = GoogleAccountCredential.usingOAuth2(
                this,
                Collections.singleton(StorageScopes.DEVSTORAGE_READ_ONLY));
        mStorage = new Storage.Builder(
                AndroidHttp.newCompatibleTransport(),
                new JacksonFactory(),
                mAccountCredential
        ).setApplicationName(
                // TODO(dotdoom): use real values or try to get rid of it
                "SomeApplication/1.0"
        ).build();

        // Android relies on a security Provider to provide secure network communications.
        // It verifies that the security provider is up-to-date.
        ProviderInstaller.installIfNeededAsync(this, this);
    }

    /**
     * Callback for successful installation of the security provider.
     */
    @Override
    public void onProviderInstalled() {
        mActivityResultCallback.startPermissionRequestWithResultHandler(
                Manifest.permission.GET_ACCOUNTS,
                getString(R.string.get_accounts_permission_rationale),
                new ResultCallback.RequestPermissionListener() {
                    @Override
                    public void handle(final int grantResult) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            selectAccount();
                        } else {
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        }
                    }
                }
        );
    }

    /**
     * Callback for the failure to install a security provider.
     * @param errorCode error code
     * @param recoveryIntent an intent that can be used to recover from the failure
     */
    @Override
    public void onProviderInstallFailed(final int errorCode, final Intent recoveryIntent) {
        final GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        if (googleAPI.isUserResolvableError(errorCode)) {
            mActivityResultCallback.startActivityWithResultHandler(
                    (Intent) null,
                    new ResultCallback.AbstractActivityResultListener() {
                        @Override
                        public void start(final int requestCode) {
                            googleAPI.getErrorDialog(GetRoverPasswordActivity.this, errorCode,
                                    requestCode).show();
                        }

                        @Override
                        public void handle(final int resultCode, final Intent data) {
                            ProviderInstaller.installIfNeededAsync(
                                    GetRoverPasswordActivity.this, GetRoverPasswordActivity.this);
                        }
                    });
        } else {
            buildCancelableAlertDialog()
                    // TODO(dotdoom): get descriptive error message from the code
                    .setMessage(getString(R.string.security_provider_failed_message, errorCode))
                    .create()
                    .show();
        }
    }

    private void selectAccount() {
        final SharedPreferencesHandler sharedPreferences = new SharedPreferencesHandler(this);
        try {
            String accountName = sharedPreferences.getAccountName();
            mAccountCredential.setSelectedAccountName(accountName);
            if (mAccountCredential.getSelectedAccountName() != null) {
                downloadPassword();
                return;
            }
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Can't get pre-saved account name: " + e.getMessage());
        }
        // The account we have in Preferences may no longer exist. Request a new one.
        mActivityResultCallback.startActivityWithResultHandler(
                mAccountCredential.newChooseAccountIntent(),
                new ResultCallback.AbstractActivityResultListener() {
                    @Override
                    public void handle(final int resultCode, final Intent data) {
                        if (resultCode == Activity.RESULT_OK &&
                                data != null && data.getExtras() != null) {
                            String accountName = data.getExtras().getString(
                                    AccountManager.KEY_ACCOUNT_NAME);
                            if (accountName != null) {
                                sharedPreferences.setAccountName(accountName);
                                mAccountCredential.setSelectedAccountName(accountName);
                                downloadPassword();
                            }
                        } else {
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        }
                    }
                });
    }

    private void downloadPassword() {
        Storage.Objects.Get getObject;
        try {
            getObject = mStorage.objects().get(
                    // TODO(dotdoom): use preferences to get this value
                    "rover-authentication",
                    // E-mail is the same as account name for GoogleCredential.
                    mAccountCredential.getSelectedAccountName()
            );
        } catch (IOException e) {
            buildCancelableAlertDialog()
                    .setMessage(e.getMessage())
                    .setPositiveButton(R.string.retry_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog,
                                                    final int which) {
                                    downloadPassword();
                                }
                            })
                    .create()
                    .show();
            return;
        }
        getObject.getMediaHttpDownloader().setDirectDownloadEnabled(true);
        new DownloadPasswordTask().execute(getObject);
    }

    private AlertDialog.Builder buildCancelableAlertDialog() {
        return new AlertDialog.Builder(this)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                });
    }

    private class DownloadPasswordTask extends AsyncTask<Storage.Objects.Get, Void, String> {
        private IOException mIOError;

        @Override
        protected String doInBackground(final Storage.Objects.Get... params) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try {
                params[0].executeMediaAndDownloadTo(b);
                return b.toString();
            } catch (IOException e) {
                mIOError = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String password) {
            if (password != null) {
                setResult(Activity.RESULT_OK, new Intent(
                        getClass().getCanonicalName(),
                        new Uri.Builder()
                                .authority(
                                        mAccountCredential.getSelectedAccountName() + ":" +
                                                password)
                                .build()));
                finish();
                return;
            }

            if (mIOError instanceof UserRecoverableAuthIOException) {
                mActivityResultCallback.startActivityWithResultHandler(
                        ((UserRecoverableAuthIOException) mIOError).getIntent(),
                        new ResultCallback.AbstractActivityResultListener() {
                            @Override
                            public void handle(final int resultCode, final Intent data) {
                                if (resultCode == Activity.RESULT_OK) {
                                    downloadPassword();
                                } else {
                                    setResult(Activity.RESULT_CANCELED);
                                    finish();
                                }
                            }
                        }
                );
                return;
            }

            buildCancelableAlertDialog()
                    .setMessage(mIOError.getMessage())
                    .setPositiveButton(R.string.retry_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog,
                                                    final int which) {
                                    downloadPassword();
                                }
                            })
                    .create()
                    .show();
        }
    }
}
