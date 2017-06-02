package org.dasfoo.rover.android.client.grpc.task;

import org.dasfoo.rover.server.RoverServiceGrpc;
import org.dasfoo.rover.server.ReadEncodersRequest;
import org.dasfoo.rover.server.ReadEncodersResponse;

import io.grpc.StatusRuntimeException;

/**
 * Created by Katarina Sheremet on 5/17/16 9:32 PM.
 */
public class EncodersReadingTask extends AbstractGrpcTaskExecutor {

    /**
     * Send an encoder read request to the rover and wait for result.
     *
     * @param stub gRPC
     * @return response as text
     */
    @Override
    public String execute(final RoverServiceGrpc.RoverServiceBlockingStub stub) {
        try {
            final ReadEncodersRequest readEncodersRequest =
                    ReadEncodersRequest.getDefaultInstance();
            ReadEncodersResponse readEncodersResponse = stub.readEncoders(readEncodersRequest);
            return String.format(
                    "Encoders\n" +
                            "Front left: %d\n" +
                            "Back left: %d\n" +
                            "Front right: %d\n" +
                            "Back right: %d\n",
                    readEncodersResponse.getLeftFront(),
                    readEncodersResponse.getLeftBack(),
                    readEncodersResponse.getRightFront(),
                    readEncodersResponse.getRightBack()
            );
        } catch (StatusRuntimeException e) {
            switch (e.getStatus().getCode()) {
                case UNKNOWN:
                    return "Unknown error. Try later";
                default:
                    return e.getMessage();
            }
        }
    }
}
