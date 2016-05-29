package ch.sheremet.dasfoo.rover.android.client.grpc.task;

import dasfoo.grpc.roverserver.nano.RoverServiceGrpc;
import dasfoo.grpc.roverserver.nano.RoverWheelRequest;
import io.grpc.StatusRuntimeException;

/**
 * Created by Katarina Sheremet on 5/18/16 11:10 AM.
 */
public class MovingRoverTask extends AbstractGrpcTaskExecutor {

    @Override
    public String execute(final RoverServiceGrpc.RoverServiceBlockingStub stub) {
        try {
            // Not implemented yet. It moves forward.
            // Todo: Implement movement
            RoverWheelRequest roverWheelRequest = new RoverWheelRequest();
            roverWheelRequest.left = 30;
            roverWheelRequest.right = 30;
            stub.moveRover(roverWheelRequest);
            return "Ok"; //Todo: Check errors and status, remove hardcode
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
