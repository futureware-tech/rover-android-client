package ch.sheremet.dasfoo.rover.android.client.grpc.task;

import dasfoo.grpc.roverserver.nano.RoverWheelRequest;
import dasfoo.grpc.roverserver.nano.RoverWheelResponse;
import io.grpc.StatusRuntimeException;

/**
 * Created by Katarina Sheremet on 5/18/16 11:10 AM.
 */
public class MovingRoverTask extends GrpcTask {

    public MovingRoverTask(final OnTaskCompleted listener) {
        super(listener);
    }

    @Override
    protected final String doInBackground(final String... params) {
        super.doInBackground(params[0], params[1]);
        try {
            // Not implemented yet. It moves forward.
            // Todo: Implement movement
            RoverWheelRequest roverWheelRequest = new RoverWheelRequest();
            roverWheelRequest.left = 30;
            roverWheelRequest.right = 30;
            RoverWheelResponse reply = getStub().moveRover(roverWheelRequest);
            return "Ok"; //TODO: check errors and status, remove hardcode
        } catch (StatusRuntimeException e) {
            // Not implemented error messages
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
