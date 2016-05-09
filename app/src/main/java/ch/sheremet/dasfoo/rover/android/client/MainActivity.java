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

import java.util.concurrent.TimeUnit;

import dasfoo.grpc.roverserver.nano.BoardInfoRequest;
import dasfoo.grpc.roverserver.nano.BoardInfoResponse;
import dasfoo.grpc.roverserver.nano.RoverServiceGrpc;
import dasfoo.grpc.roverserver.nano.RoverWheelRequest;
import dasfoo.grpc.roverserver.nano.RoverWheelResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class MainActivity extends AppCompatActivity {

    private Button mSendButton;
    private EditText mHostEdit;
    private EditText mPortEdit;
    private TextView mResultText;
    private Button mInfoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSendButton = (Button) findViewById(R.id.send_button);
        mInfoButton = (Button) findViewById(R.id.info_button);
        mHostEdit = (EditText) findViewById(R.id.host_edit_text);
        mPortEdit = (EditText) findViewById(R.id.port_edit_text);
        mResultText = (TextView) findViewById(R.id.grpc_response_text);
    }

    public void sendMessage(View view) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mHostEdit.getWindowToken(), 0);
        mSendButton.setEnabled(false);
        new GrpcTask().execute("moveCommand");
    }

    public void getInfo(View view) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mHostEdit.getWindowToken(), 0);
        mSendButton.setEnabled(false);
        new GrpcTask().execute("getInfoCommand");
    }

    private class GrpcTask extends AsyncTask<String, Void, String> {
        private String mHost;
        private String mMessage;
        private int mPort;
        private ManagedChannel mChannel;

        @Override
        protected void onPreExecute() {
            mHost = mHostEdit.getText().toString();
            String portStr = mPortEdit.getText().toString();
            mPort = TextUtils.isEmpty(portStr) ? 0 : Integer.valueOf(portStr);
            mResultText.setText("");
        }

        private String moveForward(ManagedChannel channel) {
            RoverServiceGrpc.RoverServiceBlockingStub stub = RoverServiceGrpc.newBlockingStub(channel);
            RoverWheelRequest message = new RoverWheelRequest();
            message.left = 30;
            message.right = 30;
            RoverWheelResponse reply = stub.moveRover(message);
            return reply.message;
        }

        private  String getServerInfo(ManagedChannel channel) {
            RoverServiceGrpc.RoverServiceBlockingStub stub = RoverServiceGrpc.newBlockingStub(channel);
            BoardInfoRequest message = new BoardInfoRequest();
            BoardInfoResponse reply = stub.getBoardInfo(message);
            // Create answer
            String answer = "Battery:" + reply.battery + "\n";
            answer = answer + "Light:" + reply.light + "\n";
            answer = answer + "Temperature:" + reply.temperature + "\n";
            answer = answer + "Humidity:" + reply.humidity;

            return answer;
        }

        @Override
        protected String doInBackground(String... command) {
            try {
                mChannel = ManagedChannelBuilder.forAddress(mHost, mPort)
                        .usePlaintext(true)
                        .build();
                if (command[0].equals("moveCommand")) return moveForward(mChannel);
                if (command[0].equals("getInfoCommand")) return  getServerInfo(mChannel);
            } catch (Exception e) {
                return "Failed... : " + e.getMessage();
            }
            return "Error in command name";
        }



        @Override
        protected void onPostExecute(String result) {
            try {
                mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            mResultText.setText(result);
            mSendButton.setEnabled(true);
        }
    }
}
