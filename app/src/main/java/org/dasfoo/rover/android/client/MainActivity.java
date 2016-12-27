package org.dasfoo.rover.android.client;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.dasfoo.rover.android.client.auth.PasswordManager;
import org.dasfoo.rover.android.client.grpc.task.AbstractGrpcTaskExecutor;
import org.dasfoo.rover.android.client.grpc.task.EncodersReadingTask;
import org.dasfoo.rover.android.client.grpc.task.GettingBoardInfoTask;
import org.dasfoo.rover.android.client.grpc.task.GrpcConnection;
import org.dasfoo.rover.android.client.grpc.task.MovingRoverTask;
import org.dasfoo.rover.android.client.menu.MenuFragment;
import org.dasfoo.rover.android.client.menu.SharedPreferencesHandler;
import org.dasfoo.rover.android.client.util.LogUtil;
import org.dasfoo.rover.android.client.util.ResultCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/** Main Activity of the application.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Class information for logging.
     */
    private static final String TAG = LogUtil.tagFor(MainActivity.class);

    /**
     * ButterKnife.apply() callback to enable / disable views (useful for lists).
     */
    private static final ButterKnife.Setter<View, Boolean> ENABLED =
            new ButterKnife.Setter<View, Boolean>() {
                @Override
                public void set(@NonNull final View view, final Boolean value, final int index) {
                    view.setEnabled(value);
                }
            };

    /**
     * All buttons on this activity.
     */
    @BindViews({R.id.move_forward_button, R.id.info_button, R.id.read_encoders_button})
    protected List<Button> mButtons;
    // TODO(dotdoom): try using the same for OnClick below

    /**
     * Moves rover forward.
     */
    @BindView(R.id.move_forward_button)
    protected Button mMoveForwardButton;

    /**
     * Gets information about battery, humidity, temperature and light.
     */
    @BindView(R.id.info_button)
    protected Button mInfoButton;

    /**
     * Gets the number of turns the rights and left wheels.
     */
    @BindView(R.id.read_encoders_button)
    protected Button mReadEncodersButton;

    /**
     * Result text view.
     */
    @BindView(R.id.grpc_response_text)
    protected TextView mResultText;

    private ResultCallback mActivityResultCallback;

    private PasswordManager mPasswordManager;

    private GrpcConnection mGrpcConnection;

    /**
     * Get PasswordManager associated with this activity.
     * @return password manager
     */
    public PasswordManager getPasswordManager() {
        // TODO(dotdoom): remove this
        return mPasswordManager;
    }

    /**
     * Activity's click handler.
     *
     * @param v view that has been clicked
     */
    @OnClick({R.id.move_forward_button, R.id.info_button, R.id.read_encoders_button})
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.move_forward_button:
                executeGrpcTask(new MovingRoverTask());
                break;
            case R.id.info_button:
                executeGrpcTask(new GettingBoardInfoTask());
                break;
            case R.id.read_encoders_button:
                executeGrpcTask(new EncodersReadingTask());
                break;
            default:
                Log.v(TAG, "Button is not implemented yet.");
                break;
        }
    }

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
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mActivityResultCallback = new ResultCallback(this);
        mPasswordManager = new PasswordManager(mActivityResultCallback);

        // Add menu fragment
        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final MenuFragment menuFragment = new MenuFragment();
        fragmentTransaction.add(menuFragment, "menu");
        fragmentTransaction.commit();
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        if (mGrpcConnection != null) {
            mGrpcConnection.shutDownConnection();
            mGrpcConnection = null;
        }
    }

    private void executeGrpcTask(final AbstractGrpcTaskExecutor task) {
        mPasswordManager.getPassword(new PasswordManager.PasswordAvailableListener() {
            @Override
            public void handle(final String password) {
                new GrpcTask(password).execute(task);
            }
        });
    }

    private class GrpcTask extends AsyncTask<AbstractGrpcTaskExecutor, Void, String> {
        private final String mPassword;

        GrpcTask(final String password) {
            super();
            mPassword = password;
        }

        @Override
        protected final void onPreExecute() {
            super.onPreExecute();
            ButterKnife.apply(mButtons, ENABLED, false);
        }

        @Override
        protected final String doInBackground(final AbstractGrpcTaskExecutor... params) {
            try {
                final SharedPreferencesHandler sharedPreferences =
                        new SharedPreferencesHandler(MainActivity.this);
                final String host = sharedPreferences.getHost();
                final int port = sharedPreferences.getPort();
                // TODO(ksheremet): implement onSharedPreferencesChanged
                // TODO(dotdoom): forgetPassword() when access is denied by the server
                if (mGrpcConnection == null ||
                        !host.equals(mGrpcConnection.getHost()) ||
                        !mPassword.equals(mGrpcConnection.getPassword()) ||
                        port != mGrpcConnection.getPort()) {
                    mGrpcConnection = new GrpcConnection(host, port, mPassword);
                }
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
            return params[0].execute(mGrpcConnection.getStub());
        }

        @Override
        protected final void onPostExecute(final String result) {
            mResultText.setText(result);
            ButterKnife.apply(mButtons, ENABLED, true);
        }
    }
}
