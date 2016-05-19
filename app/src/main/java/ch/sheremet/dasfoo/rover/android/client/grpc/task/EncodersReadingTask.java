package ch.sheremet.dasfoo.rover.android.client.grpc.task;

import dasfoo.grpc.roverserver.nano.ReadEncodersRequest;
import dasfoo.grpc.roverserver.nano.ReadEncodersResponse;
import io.grpc.StatusRuntimeException;

/**
 * Created by Katarina Sheremet on 5/17/16 9:32 PM.
 */
public class EncodersReadingTask extends GrpcTask {

    public EncodersReadingTask(OnTaskCompleted listener) {
        super(listener);
    }

    @Override
    protected String doInBackground(String... params) {
       establishConnection(params[0], params[1]);
        try {
            ReadEncodersRequest readEncodersRequest = new ReadEncodersRequest();
            ReadEncodersResponse readEncodersResponse = stub.readEncoders(readEncodersRequest);
            String answer = "Encoders\n";
            answer += "Front left: " + readEncodersResponse.leftFront + "\n";
            answer += "Back left: " + readEncodersResponse.leftBack + "\n";
            answer += "Front right: " + readEncodersResponse.rightFront + "\n";
            answer += "Back right: " + readEncodersResponse.rightBack + "\n";
            return answer;
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
    }

}
