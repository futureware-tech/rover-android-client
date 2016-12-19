package org.dasfoo.rover.android.client.util;

import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for automatically managing requestCode values and activity/permission result callbacks.
 * onRequestPermissionsResult() and onActivityResult() of this instance need to be called from the
 * corresponding callbacks of the Activity instance.
 */
public class ResultCallback {

    // TODO(dotdoom): make it thread-safe
    // TODO(dotdoom): delete callbacks once used (by e.g. assigning null)

    private static final int ACTIVITY_REQUEST_BASE = 9000;

    private static final int REQUEST_PERMISSION_REQUEST_BASE = 13000;

    private final List<ActivityCallbacks> mActivityCallbacks = new ArrayList<>();

    private final List<RequestPermissionCallbacks> mRequestPermissionCallbacks = new ArrayList<>();

    /**
     * Allocates a new requestCode, puts handle() as a listener for it, and calls callbacks.start().
     * @param cb executed in the UI thread
     */
    public void startActivityWithResultHandler(final ActivityCallbacks cb) {
        int requestCode = mActivityCallbacks.size() + ACTIVITY_REQUEST_BASE;
        mActivityCallbacks.add(cb);
        cb.start(requestCode);
    }

    /**
     * Allocates a new requestCode, puts handle() as a listener for it, and calls callbacks.start().
     * @param cb executed in the UI thread
     */
    public void startPermissionRequestWithResultHandler(final RequestPermissionCallbacks cb) {
        int requestCode = mRequestPermissionCallbacks.size() + REQUEST_PERMISSION_REQUEST_BASE;
        mRequestPermissionCallbacks.add(cb);
        cb.start(requestCode);
    }

    /**
     * Chain handler to call after super() in Activity.onRequestPermissionsResult().
     * @param requestCode pass verbatim from Activity.onRequestPermissionsResult()
     * @param permissions pass verbatim from Activity.onRequestPermissionsResult()
     * @param grantResults pass verbatim from Activity.onRequestPermissionsResult()
     * @return true if requestCode was found in the registered callbacks
     */
    public boolean onRequestPermissionsResult(final int requestCode,
                                              final String[] permissions,
                                              final int[] grantResults) {
        if (requestCode >= REQUEST_PERMISSION_REQUEST_BASE) {
            int handlerId = requestCode - REQUEST_PERMISSION_REQUEST_BASE;
            if (handlerId < mRequestPermissionCallbacks.size()) {
                mRequestPermissionCallbacks.get(handlerId).handle(permissions, grantResults);
                return true;
            }
        }
        return false;
    }

    /**
     * Chain handler to call after super() in Activity.onActivityResult().
     * @param requestCode pass verbatim from Activity.onActivityResult()
     * @param resultCode pass verbatim from Activity.onActivityResult()
     * @param data pass verbatim from Activity.onActivityResult()
     * @return true if requestCode was found in the registered callbacks
     */
    public boolean onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode >= ACTIVITY_REQUEST_BASE) {
            int handlerId = requestCode - ACTIVITY_REQUEST_BASE;
            if (handlerId < mActivityCallbacks.size()) {
                mActivityCallbacks.get(handlerId).handle(resultCode, data);
                return true;
            }
        }
        return false;
    }

    /**
     * Callbacks to start an activity and handle the result.
     */
    public interface ActivityCallbacks {
        /**
         * Starts the activity (normally by calling startActivityForResult).
         * @param requestCode a unique value to use as an activity request code
         */
        void start(int requestCode);

        /**
         * Handles the return from the activity previously launched in start().
         * @param resultCode activity result code (see onActivityResult)
         * @param data activity response data (see onActivityResult)
         */
        void handle(int resultCode, Intent data);
    }

    /**
     * Callbacks to start a request for permissions and handle the result.
     */
    public interface RequestPermissionCallbacks {
        /**
         * Initiates a request for permissions (usually via ActivityCompat.requestPermissions).
         * @param requestCode a unique value to use as a permission request code
         */
        void start(int requestCode);

        /**
         * Handles the result of permissions request.
         * @param permissions permissions requested (see onRequestPermissionsResult)
         * @param grantResults respective granted/denied results (see onRequestPermissionsResult)
         */
        void handle(String[] permissions, int[] grantResults);
    }
}
