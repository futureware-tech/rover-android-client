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
        super.doInBackground(params[0], params[1]);
        try {
            ReadEncodersRequest readEncodersRequest = new ReadEncodersRequest();
            ReadEncodersResponse readEncodersResponse = stub.readEncoders(readEncodersRequest);
            StringBuilder answer = new StringBuilder("Encoders\n");
            answer.append("Front left: ").append(readEncodersResponse.leftFront).append("\n");
            answer.append("Back left: ").append(readEncodersResponse.leftBack).append("\n");
            answer.append("Front right: ").append(readEncodersResponse.rightFront).append("\n");
            answer.append("Back right: ").append(readEncodersResponse.rightBack).append("\n");
            return answer.toString();
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
