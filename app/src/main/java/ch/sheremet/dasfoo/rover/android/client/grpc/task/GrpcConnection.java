package ch.sheremet.dasfoo.rover.android.client.grpc.task;

import java.util.concurrent.TimeUnit;

import dasfoo.grpc.roverserver.nano.RoverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


/**
 * Created by Katarina Sheremet on 5/24/16 5:09 PM.
 */
public class GrpcConnection {
    private ManagedChannel mChannel;
    private RoverServiceGrpc.RoverServiceBlockingStub mStub;
    private String mHost;
    private int mPort;

    public GrpcConnection(String host, int port) {
        this.mHost = host;
        this.mPort = port;
        establishConnection();
    }

    public int getPort() {
        return mPort;
    }

    public String getHost() {
        return mHost;
    }

    private void establishConnection() {
        mChannel = ManagedChannelBuilder.forAddress(mHost, mPort).build();
        mStub = RoverServiceGrpc.newBlockingStub(mChannel);
    }

    public RoverServiceGrpc.RoverServiceBlockingStub getStub() {
        return mStub;
    }

    public boolean shutDownConnection() {
        try {
            mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            return Boolean.TRUE;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
    }
}
