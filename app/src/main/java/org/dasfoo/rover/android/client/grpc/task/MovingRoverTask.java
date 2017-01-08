package org.dasfoo.rover.android.client.grpc.task;

import org.dasfoo.rover.server.nano.RoverServiceGrpc;
import org.dasfoo.rover.server.nano.RoverWheelRequest;

import io.grpc.StatusRuntimeException;

/**
 * Created by Katarina Sheremet on 5/18/16 11:10 AM.
 */
public class MovingRoverTask extends AbstractGrpcTaskExecutor {
    private static final int FORWARD_MOVE_LEFT = 30;

    private static final int FORWARD_MOVE_RIGHT = 30;

    /**
     * Send a move request to the rover and wait for result.
     *
     * @param stub gRPC
     * @return response as text
     */
    @Override
    public String execute(final RoverServiceGrpc.RoverServiceBlockingStub stub) {
        try {
            // Not implemented yet. It moves forward.
            // TODO(ksheremet): Implement movement
            RoverWheelRequest roverWheelRequest = new RoverWheelRequest();
            roverWheelRequest.left = FORWARD_MOVE_LEFT;
            roverWheelRequest.right = FORWARD_MOVE_RIGHT;
            stub.moveRover(roverWheelRequest);
            return "Ok"; // TODO(ksheremet): Check errors and status, remove hardcode
        } catch (StatusRuntimeException e) {
            // Not implemented error messages
            switch (e.getStatus().getCode()) {
                case UNKNOWN:
                    return "Unknown error. Try later";
                default:
                    return e.getMessage();
            }
        }
    }
}
