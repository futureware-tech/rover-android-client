package org.dasfoo.rover.android.client.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import org.dasfoo.rover.android.client.R;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for automatically managing requestCode values and activity/permission result callbacks.
 * onRequestPermissionsResult() and onActivityResult() of this instance need to be called from the
 * corresponding callbacks of the Activity instance.
 */
public class ResultCallback {

    private final CallbackStore<AbstractActivityResultListener> mActivityListeners =
            new CallbackStore<>(9000);

    private final CallbackStore<RequestPermissionListener> mRequestPermissionListeners =
            new CallbackStore<>(13000);

    private final Activity mActivity;

    /**
     * Create a new instance of ResultCallback (normally one per activity).
     *
     * @param activity activity this callback belongs to
     */
    public ResultCallback(final Activity activity) {
        mActivity = activity;
    }

    /**
     * Allocates a new requestCode, puts handle() as a listener for it, and starts a new activity.
     *
     * @param intent parameter for startActivityForResult, or AbstractActivityResultListener.start()
     * @param cb     executed in the UI thread
     */
    public void startActivityWithResultHandler(@Nullable final Intent intent,
                                               final AbstractActivityResultListener cb) {
        int requestCode = mActivityListeners.put(cb);
        if (intent == null) {
            cb.start(requestCode);
        } else {
            mActivity.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Allocates a new requestCode, puts handle() as a listener for it, and starts a new intent.
     *
     * @param cls activity class for the intent
     * @param cb  executed in the UI thread
     */
    public void startActivityWithResultHandler(final Class<?> cls,
                                               final AbstractActivityResultListener cb) {
        startActivityWithResultHandler(new Intent(mActivity, cls), cb);
    }

    /**
     * Allocates a new requestCode, puts handle() as a listener for it, and calls callbacks.start().
     *
     * @param permission permission name to request (e.g. a constant from Manifest.permission)
     * @param rationale  rationale to show to the user if necessary
     * @param cb         executed in the UI thread
     */
    public void startPermissionRequestWithResultHandler(
            final String permission,
            @Nullable final String rationale,
            final RequestPermissionListener cb) {
        if (ContextCompat.checkSelfPermission(mActivity, permission) ==
                PackageManager.PERMISSION_GRANTED) {
            cb.handle(PackageManager.PERMISSION_GRANTED);
            return;
        }

        if (rationale != null && ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                permission)) {
            new AlertDialog.Builder(mActivity)
                    .setMessage(rationale)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(final DialogInterface dialog) {
                            cb.handle(PackageManager.PERMISSION_DENIED);
                        }
                    })
                    .setPositiveButton(R.string.grant_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    startPermissionRequestWithResultHandler(permission, null, cb);
                                }
                            })
                    .show();
            return;
        }

        int requestCode = mRequestPermissionListeners.put(cb);
        ActivityCompat.requestPermissions(mActivity, new String[]{permission}, requestCode);
    }

    /**
     * Chain handler to call after super() in Activity.onRequestPermissionsResult().
     *
     * @param requestCode  pass verbatim from Activity.onRequestPermissionsResult()
     * @param permissions  pass verbatim from Activity.onRequestPermissionsResult()
     * @param grantResults pass verbatim from Activity.onRequestPermissionsResult()
     * @return true if requestCode was found in the registered callbacks
     */
    public boolean onRequestPermissionsResult(final int requestCode,
                                              final String[] permissions,
                                              final int[] grantResults) {
        RequestPermissionListener cb = mRequestPermissionListeners.remove(requestCode);
        if (cb != null) {
            if (grantResults.length > 0) {
                cb.handle(grantResults[0]);
            } else {
                cb.handle(PackageManager.PERMISSION_DENIED);
            }
            return true;
        }
        return false;
    }

    /**
     * Chain handler to call after super() in Activity.onActivityResult().
     *
     * @param requestCode pass verbatim from Activity.onActivityResult()
     * @param resultCode  pass verbatim from Activity.onActivityResult()
     * @param data        pass verbatim from Activity.onActivityResult()
     * @return true if requestCode was found in the registered callbacks
     */
    public boolean onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        AbstractActivityResultListener cb = mActivityListeners.remove(requestCode);
        if (cb != null) {
            cb.handle(resultCode, data);
            return true;
        }
        return false;
    }

    /**
     * Callbacks to start a request for permissions and handle the result.
     */
    public interface RequestPermissionListener {
        /**
         * Handles the result of permission request.
         *
         * @param grantResult granted/denied outcome of the request
         */
        void handle(int grantResult);
    }

    /**
     * Callbacks to start an activity and handle the result.
     */
    public abstract static class AbstractActivityResultListener {
        /**
         * If intent was not provided, this callback will be used to start a new activity.
         *
         * @param requestCode generated request code that should be used as a start activity param
         */
        public void start(final int requestCode) {
            throw new UnsupportedOperationException();
        }

        /**
         * Handles the return from the activity previously launched in start().
         *
         * @param resultCode activity result code (see onActivityResult)
         * @param data       activity response data (see onActivityResult)
         */
        public abstract void handle(int resultCode, Intent data);
    }

    private class CallbackStore<T> {
        private final Map<Integer, T> mCallbacks = new ConcurrentHashMap<>();

        private int mBase;

        /**
         * Create a new instance with keys starting at specific value.
         *
         * @param base minimum value for the request codes (only values above will be used)
         */
        CallbackStore(final int base) {
            mBase = base;
        }

        /**
         * Add a new callback.
         *
         * @param callback callback to add
         * @return requestCode which can then be used in {@link this.remove}
         */
        private synchronized int put(final T callback) {
            mBase++;
            mCallbacks.put(mBase, callback);
            return mBase;
        }

        /**
         * Remove and return a callback.
         *
         * @param id requestCode returned by {@link this.put}
         * @return callback stored by {@link this.put}
         */
        private T remove(final int id) {
            return mCallbacks.remove(id);
        }
    }
}
