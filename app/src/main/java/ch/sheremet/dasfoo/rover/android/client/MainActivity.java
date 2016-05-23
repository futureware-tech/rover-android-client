package ch.sheremet.dasfoo.rover.android.client;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import ch.sheremet.dasfoo.rover.android.client.grpc.task.AbstractGrpcTaskExecutor;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.EncodersReadingTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.GettingBoardInfoTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.IOnGrpcTaskCompleted;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.MovingRoverTask;
import dasfoo.grpc.roverserver.nano.RoverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class MainActivity extends AppCompatActivity implements IOnGrpcTaskCompleted {

    private Button mMoveForwardButton;
    private Button mInfoButton;
    private Button mReadEncodersButton;
    private EditText mHostEdit;
    private EditText mPortEdit;
    private TextView mResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMoveForwardButton = (Button) findViewById(R.id.move_forward_button);
        mMoveForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveForward();
            }
        });
        mInfoButton = (Button) findViewById(R.id.info_button);
        mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInfo();
            }
        });
        mReadEncodersButton = (Button) findViewById(R.id.read_encoders_button);
        mReadEncodersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readEncoders();
            }
        });
        mHostEdit = (EditText) findViewById(R.id.host_edit_text);
        mPortEdit = (EditText) findViewById(R.id.port_edit_text);
        mResultText = (TextView) findViewById(R.id.grpc_response_text);
    }

    private void moveForward() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mHostEdit.getWindowToken(), 0);
        AbstractGrpcTaskExecutor movingRoverTask = new MovingRoverTask();
        executeGrpcTask(movingRoverTask);
    }

    private void getInfo() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mHostEdit.getWindowToken(), 0);
        AbstractGrpcTaskExecutor getBoardInfoTask = new GettingBoardInfoTask();
        executeGrpcTask(getBoardInfoTask);
    }

    private void readEncoders() {
        AbstractGrpcTaskExecutor encodersReadingTask = new EncodersReadingTask();
        executeGrpcTask(encodersReadingTask);
    }

    private void executeGrpcTask(final AbstractGrpcTaskExecutor task) {
        String host = mHostEdit.getText().toString();
        String port = mPortEdit.getText().toString();
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(port)) {
            Toast.makeText(this, "You did not enter a host or a port", Toast.LENGTH_SHORT).show();
            return;
        }
        enableButtons(false);
        GrpcTask grpcTask = new GrpcTask(this, host, Integer.valueOf(port));
        grpcTask.execute(task);
    }

    private void enableButtons(boolean isEnabled) {
        mMoveForwardButton.setEnabled(isEnabled);
        mInfoButton.setEnabled(isEnabled);
        mReadEncodersButton.setEnabled(isEnabled);
    }

    @Override
    public void onGrpcTaskCompleted() {
        enableButtons(true);
    }

    public class GrpcTask extends AsyncTask<AbstractGrpcTaskExecutor, Void, String> {
        private final String mHost;
        private final int mPort;
        private ManagedChannel mChannel;
        private RoverServiceGrpc.RoverServiceBlockingStub mStub;
        private IOnGrpcTaskCompleted mListener;

        public GrpcTask(IOnGrpcTaskCompleted mListener, String mHost, int mPort) {
            this.mHost = mHost;
            this.mPort = mPort;
            this.mListener = mListener;
        }

        @Override
        protected void onPreExecute() {
            mChannel = ManagedChannelBuilder.forAddress(mHost, mPort)
                    .usePlaintext(true)
                    .build();
            mStub = RoverServiceGrpc.newBlockingStub(mChannel);
        }

        @Override
        protected String doInBackground(final AbstractGrpcTaskExecutor... params) {
            return params[0].execute(mStub);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) mResultText.setText(R.string.getting_null_result_text);
                mResultText.setText(result);
                mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
                mListener.onGrpcTaskCompleted();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
