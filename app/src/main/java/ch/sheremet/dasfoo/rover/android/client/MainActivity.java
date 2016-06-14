package ch.sheremet.dasfoo.rover.android.client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;

import java.util.MissingFormatArgumentException;

import ch.sheremet.dasfoo.rover.android.client.grpc.task.AbstractGrpcTaskExecutor;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.EncodersReadingTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.GettingBoardInfoTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.GrpcConnection;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.MovingRoverTask;

public class MainActivity extends AppCompatActivity
        implements ProviderInstaller.ProviderInstallListener {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String PROVIDER_NOT_INSTALLED =
            "The security provider installation failed, "
                    + "encrypted communication is not available: %s";
    private static final String TAG = MainActivity.class.getName();

    /**
     * Moves rover forward.
     */
    private Button mMoveForwardButton;

    /**
     * Gets information about battery, humidity, temperature and light.
     */
    private Button mInfoButton;

    /**
     * Gets the number of turns the rights and left wheels.
     */
    private Button mReadEncodersButton;

    /**
     * Host edit text.
     */
    private EditText mHostEdit;

    /**
     * Port edit text.
     */
    private EditText mPortEdit;

    /**
     * Result text view.
     */
    private TextView mResultText;

    /**
     * Gets Port from mPortEdit. If view is empty, it returns error.
     * @return Port for connection to the server
     * @throws MissingFormatArgumentException Port is empty
     */
    public final int getPort() throws MissingFormatArgumentException {
        String port = mPortEdit.getText().toString();
        if (TextUtils.isEmpty(port)) {
            throw new MissingFormatArgumentException("You did not enter a port");
        }
        return Integer.parseInt(port);
    }

    /**
     * Gets Host from mHostEdit. If view is empty, it returns error.
     * @return Host for connection to the server
     * @throws MissingFormatArgumentException Host is empty
     */
    public final String getHost() throws MissingFormatArgumentException {
        String host = mHostEdit.getText().toString();
        if (TextUtils.isEmpty(host)) {
            throw new MissingFormatArgumentException("You did not enter a host");
        }
        return host;
    }

    private GrpcConnection mGrpcConnection;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            hideKeyboard();
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
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMoveForwardButton = (Button) findViewById(R.id.move_forward_button);
        mMoveForwardButton.setOnClickListener(onClickListener);
        mInfoButton = (Button) findViewById(R.id.info_button);
        mInfoButton.setOnClickListener(onClickListener);
        mReadEncodersButton = (Button) findViewById(R.id.read_encoders_button);
        mReadEncodersButton.setOnClickListener(onClickListener);
        mHostEdit = (EditText) findViewById(R.id.host_edit_text);
        mPortEdit = (EditText) findViewById(R.id.port_edit_text);
        mResultText = (TextView) findViewById(R.id.grpc_response_text);

        // Android relies on a security Provider to provide secure network communications.
        // It verifies that the security provider is up-to-date.
        ProviderInstaller.installIfNeededAsync(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGrpcConnection != null) {
            mGrpcConnection.shutDownConnection();
            mGrpcConnection = null;
        }
    }

    private void hideKeyboard() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mHostEdit.getWindowToken(), 0);
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mPortEdit.getWindowToken(), 0);
    }

    private void executeGrpcTask(final AbstractGrpcTaskExecutor task) {
        new GrpcTask().execute(task);
    }

    private void enableButtons(final boolean isEnabled) {
        mMoveForwardButton.setEnabled(isEnabled);
        mInfoButton.setEnabled(isEnabled);
        mReadEncodersButton.setEnabled(isEnabled);
    }

    @Override
    public void onProviderInstalled() {
        Toast.makeText(this, "The security provider installed.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderInstallFailed(final int errorCode, final Intent intent) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        if (googleAPI.isUserResolvableError(errorCode)) {
            googleAPI.getErrorDialog(MainActivity.this, errorCode,
                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
        } else {
            onProviderInstallerNotAvailable(errorCode);
        }
    }

    /**
     * Executes when provider installation was failed. And it is not possible to install it.
     * Shows the dialog to a user and returns to the main screen.
     * @param errorCode code of exception
     */
    private void onProviderInstallerNotAvailable(final int errorCode) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(String.format(PROVIDER_NOT_INSTALLED, errorCode))
                .setCancelable(false)
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .create()
                .show();
    }

    public class GrpcTask extends AsyncTask<AbstractGrpcTaskExecutor, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enableButtons(Boolean.FALSE);
        }

        @Override
        protected String doInBackground(final AbstractGrpcTaskExecutor... params) {
            try {
                if (mGrpcConnection == null
                        || !getHost().equals(mGrpcConnection.getHost())
                        || getPort() != mGrpcConnection.getPort()) {
                    mGrpcConnection = new GrpcConnection(getHost(), getPort());
                }
            } catch (MissingFormatArgumentException e) {
                return e.getMessage();
            }
            return params[0].execute(mGrpcConnection.getStub());
        }

        @Override
        protected void onPostExecute(final String result) {
            mResultText.setText(result);
            enableButtons(Boolean.TRUE);
        }
    }
}
