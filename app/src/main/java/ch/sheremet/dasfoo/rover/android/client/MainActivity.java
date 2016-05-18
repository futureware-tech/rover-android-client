package ch.sheremet.dasfoo.rover.android.client;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ch.sheremet.dasfoo.rover.android.client.grpc.task.EncodersReadingTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.GettingBoardInfoTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.GrpcTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.MovingRoverTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.OnTaskCompleted;

public class MainActivity extends AppCompatActivity implements OnTaskCompleted{

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

    public void moveForward(View view) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mHostEdit.getWindowToken(), 0);
        mSendButton.setEnabled(false);
        mInfoButton.setEnabled(false);
        GrpcTask movingRoverTask = new MovingRoverTask(this);
        //Todo: move the same code to private method
        movingRoverTask.execute(mHostEdit.getText().toString(), mPortEdit.getText().toString());
    }

    public void getInfo(View view) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mHostEdit.getWindowToken(), 0);
        mSendButton.setEnabled(false);
        mInfoButton.setEnabled(false);
        GrpcTask getBoardInfoTask = new GettingBoardInfoTask(this);
        //Todo: move the same code to private method
        getBoardInfoTask.execute(mHostEdit.getText().toString(), mPortEdit.getText().toString());
    }

    public void readEncoders(View view) {
        mSendButton.setEnabled(false);
        mInfoButton.setEnabled(false);
        GrpcTask encodersReadingTask = new EncodersReadingTask(this);
        encodersReadingTask.execute(mHostEdit.getText().toString(), mPortEdit.getText().toString());//host and port
    }

    @Override
    public void onTaskCompleted(String result) {
        if (result==null) mResultText.setText(R.string.getting_null_result_text);
        mResultText.setText(result);
        mSendButton.setEnabled(true);
        mInfoButton.setEnabled(true);
    }

    /**  public class GrpcTask extends AsyncTask<String, Void, String> {
        private String mHost;
        private int mPort;
        private ManagedChannel mChannel;

        @Override
        protected void onPreExecute() {
            mHost = mHostEdit.getText().toString();
            String portStr = mPortEdit.getText().toString();
            mPort = TextUtils.isEmpty(portStr) ? 0 : Integer.valueOf(portStr);
            mResultText.setText("");
        }

        public String readEncoders(ManagedChannel channel) {
            RoverServiceGrpc.RoverServiceBlockingStub stub = RoverServiceGrpc.newBlockingStub(channel);
            ReadEncodersRequest readEncodersRequest = new ReadEncodersRequest();
            ReadEncodersResponse readEncodersResponse = stub.readEncoders(readEncodersRequest);
            String answer = "Encoders\n";
            answer = answer + "Front left: " + readEncodersResponse.leftFront + "\n";
            answer = answer + "Back left: " + readEncodersResponse.leftBack + "\n";
            answer = answer + "Front right: " + readEncodersResponse.rightFront + "\n";
            answer = answer + "Back right: " + readEncodersResponse.rightBack + "\n";
            return answer;
        }

        private String moveForward(ManagedChannel channel) {
            RoverServiceGrpc.RoverServiceBlockingStub stub = RoverServiceGrpc.newBlockingStub(channel);
            RoverWheelRequest message = new RoverWheelRequest();
            message.left = 30;
            message.right = 30;
            RoverWheelResponse reply = stub.moveRover(message);
            return "Ok"; //TODO: check errors and status, remove hardcode
        }

        private String getServerInfo(ManagedChannel channel) {
            RoverServiceGrpc.RoverServiceBlockingStub stub = RoverServiceGrpc.newBlockingStub(channel);
            // Get battery percentage
            BatteryPercentageRequest batteryReq = new BatteryPercentageRequest();
            BatteryPercentageResponse batteryRes = stub.getBatteryPercentage(batteryReq);
            // Get light
            AmbientLightRequest lightReq = new AmbientLightRequest();
            AmbientLightResponse lightRes = stub.getAmbientLight(lightReq);
            TemperatureAndHumidityRequest tAndHReq = new TemperatureAndHumidityRequest();
            TemperatureAndHumidityResponse tAndHRes = stub.getTemperatureAndHumidity(tAndHReq);
            // Create answer
            String answer = "Battery:" + batteryRes.battery + "\n";
            answer = answer + "Light:" + lightRes.light + "\n";
            answer = answer + "Temperature:" + tAndHRes.temperature + "\n";
            answer = answer + "Humidity:" + tAndHRes.humidity;
            return answer;
        }

        @Override
        protected String doInBackground(String... command) {
            try {
                mChannel = ManagedChannelBuilder.forAddress(mHost, mPort)
                        .usePlaintext(true)
                        .build();
                if (command[0].equals("moveCommand"))
                    return moveForward(mChannel);//Todo: change to switch
                if (command[0].equals("getInfoCommand"))
                    return getServerInfo(mChannel);// Todo:command
                if (command[0].equals("readEncoders")) return readEncoders(mChannel);
            } catch (StatusRuntimeException e) {
                switch (e.getStatus().getCode()) {
                    case UNKNOWN:
                        return "Unknown error. Try later";
                    default:
                        return e.getMessage();
                }
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
            mInfoButton.setEnabled(true);
        }
    } */
}
