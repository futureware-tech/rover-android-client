package org.dasfoo.rover.android.client.grpc.task;

import dasfoo.grpc.roverserver.nano.RoverServiceGrpc;

/**
 * Created by Katarina Sheremet on 5/23/16 11:12 AM.
 */
public abstract class AbstractGrpcTaskExecutor {
    /**
     * Executes given gRPC.
     *
     * @param stub gRPC client stub
     * @return response from gRPC method call
     */
    public abstract String execute(RoverServiceGrpc.RoverServiceBlockingStub stub);
}
