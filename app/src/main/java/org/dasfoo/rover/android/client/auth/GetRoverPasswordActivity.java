package org.dasfoo.rover.android.client.auth;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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
// TODO(dotdoom): move secureproviderinstaller here
public class GetRoverPasswordActivity extends AppCompatActivity {

    private static final String TAG = LogUtil.tagFor(GetRoverPasswordActivity.class);

    private GoogleAccountCredential mAccountCredential;
    private Storage mStorage;

    private final ResultCallback mActivityResultCallback = new ResultCallback();

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
        askAccountPermission();
    }

    private void askAccountPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) ==
                PackageManager.PERMISSION_GRANTED) {
            selectAccount();
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.GET_ACCOUNTS)) {
            new AlertDialog.Builder(this)
                    .setMessage("Access is required to select an account.")
                    .setCancelable(false)
                    .setPositiveButton(R.string.grant_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    askAccountPermission();
                                }
                            })
                    .create()
                    .show();
            return;
        }

        mActivityResultCallback.startPermissionRequestWithResultHandler(
                new ResultCallback.RequestPermissionCallbacks() {
                    @Override
                    public void start(final int requestCode) {
                        ActivityCompat.requestPermissions(
                                GetRoverPasswordActivity.this,
                                new String[]{Manifest.permission.GET_ACCOUNTS},
                                requestCode);
                    }

                    @Override
                    public void handle(final String[] permissions, final int[] grantResults) {
                        if (grantResults.length == 1 &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            selectAccount();
                        }
                    }
                }
        );
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
                new ResultCallback.ActivityCallbacks() {
                    @Override
                    public void start(final int requestCode) {
                        GetRoverPasswordActivity.this.startActivityForResult(
                                mAccountCredential.newChooseAccountIntent(),
                                requestCode);
                    }

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
                    // TODO(dotdoom): get email, not account name
                    mAccountCredential.getSelectedAccountName()
            );
        } catch (IOException e) {
            // TODO(dotdoom): handle properly
            Log.e(TAG, "Can't get a Storage object", e);
            return;
        }
        getObject.getMediaHttpDownloader().setDirectDownloadEnabled(true);
        new AsyncTask<Storage.Objects.Get, Void, String>() {
            @Override
            protected String doInBackground(final Storage.Objects.Get... params) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                try {
                    params[0].executeMediaAndDownloadTo(b);
                    return b.toString();
                } catch (final UserRecoverableAuthIOException e) {
                    mActivityResultCallback.startActivityWithResultHandler(
                            new ResultCallback.ActivityCallbacks() {
                                @Override
                                public void start(final int requestCode) {
                                    GetRoverPasswordActivity.this.startActivityForResult(
                                            e.getIntent(), requestCode);
                                }

                                @Override
                                public void handle(final int resultCode, final Intent data) {
                                    if (resultCode == Activity.RESULT_OK) {
                                        downloadPassword();
                                    }
                                }
                            }
                    );
                } catch (IOException e) {
                    // TODO(dotdoom): handle this and other exceptions properly
                    Log.e(TAG, "Can't download from storage", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(final String password) {
                setResult(Activity.RESULT_OK, new Intent(
                        getClass().getCanonicalName(),
                        new Uri.Builder()
                                // TODO(dotdoom): not name, but Email
                                .authority(mAccountCredential.getSelectedAccountName() + ":" +
                                        password)
                                .build()));
                finish();
            }
        }.execute(getObject);
    }
}
