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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import ch.sheremet.dasfoo.rover.android.client.grpc.task.AbstractGrpcTaskExecutor;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.EncodersReadingTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.GettingBoardInfoTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.GrpcConnection;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.MovingRoverTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final String EMPTY_PORT_HOST = "Port or Host are empty";

    private Button mMoveForwardButton;
    private Button mInfoButton;
    private Button mReadEncodersButton;
    private EditText mHostEdit;
    private EditText mPortEdit;
    private TextView mResultText;

    public final int getPort() {
        String port = mPortEdit.getText().toString();
        if (TextUtils.isEmpty(port)) {
            Toast.makeText(this, "You did not enter a port", Toast.LENGTH_SHORT).show();
            return -1;
        }
        return Integer.parseInt(port);
    }

    public final String getHost() {
        String host = mHostEdit.getText().toString();
        if (TextUtils.isEmpty(host)) {
            Toast.makeText(this, "You did not enter a host", Toast.LENGTH_SHORT).show();
            return null;
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

    public class GrpcTask extends AsyncTask<AbstractGrpcTaskExecutor, Void, String> {
        private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

        private static final String PROVIDER_NOT_INSTALLED =
                "The security provider installation failed, "
                        + "encrypted communication is not available: %s";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enableButtons(Boolean.FALSE);
        }

        @Override
        protected String doInBackground(final AbstractGrpcTaskExecutor... params) {
            checkProviderInstaller();
            if (notEmptyHostAndPort(getHost(), getPort())) {
                if (mGrpcConnection == null
                        || !getHost().equals(mGrpcConnection.getHost())
                        || getPort() != mGrpcConnection.getPort()) {
                    mGrpcConnection = new GrpcConnection(getHost(), getPort());
                }
                return params[0].execute(mGrpcConnection.getStub());
            }
            return EMPTY_PORT_HOST;
        }

        @Override
        protected void onPostExecute(final String result) {
            mResultText.setText(result);
            enableButtons(Boolean.TRUE);
        }

        private boolean notEmptyHostAndPort(final String host, final int port) {
            return getHost() != null && getPort() != -1;
        }

        private void checkProviderInstaller() {
            // Android relies on a security Provider to provide secure network communications.
            // It verifies that the security provider is up-to-date.
            try {
                ProviderInstaller.installIfNeeded(MainActivity.this);
            } catch (GooglePlayServicesRepairableException e) {
                GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
                googleAPI.getErrorDialog(MainActivity.this, e.getConnectionStatusCode(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } catch (GooglePlayServicesNotAvailableException e) {
                onProviderInstallerNotAvailable(e);
            }
        }

        private void onProviderInstallerNotAvailable(final Exception e) {
            new AlertDialog.Builder(MainActivity.this).
                    setMessage(String.format(PROVIDER_NOT_INSTALLED, e.getMessage())).
                    setCancelable(false).
                    setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }).
                    create().
                    show();
        }
    }
}
