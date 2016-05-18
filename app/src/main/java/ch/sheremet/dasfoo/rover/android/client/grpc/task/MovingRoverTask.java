package ch.sheremet.dasfoo.rover.android.client.grpc.task;

import dasfoo.grpc.roverserver.nano.RoverWheelRequest;
import dasfoo.grpc.roverserver.nano.RoverWheelResponse;
import io.grpc.StatusRuntimeException;

/**
 * Created by Katarina Sheremet on 5/18/16 11:10 AM.
 */
public class MovingRoverTask extends GrpcTask {

    public MovingRoverTask(OnTaskCompleted listener) {
        super(listener);
    }

    @Override
    protected String doInBackground(String... params) {
        establishConnection(params[0], params[1]);
        try {
            // Not implemented yet. It moves forward.
            // Todo: implement movement
            RoverWheelRequest message = new RoverWheelRequest();
            message.left = 30;
            message.right = 30;
            RoverWheelResponse reply = stub.moveRover(message);
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
